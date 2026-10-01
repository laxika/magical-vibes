package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EbonyFly.class, GrizzlyBears.class})
class EbonyFlyTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield tapped")
    void entersTapped() {
        harness.setHand(player1, List.of(new EbonyFly()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Ebony Fly").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping adds one colorless mana")
    void tapsForColorlessMana() {
        Permanent fly = addReadyFly();

        harness.activateAbility(player1, battlefieldIndex(fly), 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("The d6 animation is optional and uses the rolled result as base power and toughness")
    void acceptsRandomAnimation() {
        Permanent fly = addReadyFly();
        activateAnimation(fly);

        assertThat(gqs.isCreature(gd, fly)).isTrue();
        assertThat(gqs.isArtifact(gd, fly)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, fly, CardSubtype.INSECT)).isTrue();
        assertThat(gqs.hasKeyword(gd, fly, Keyword.FLYING)).isTrue();
        assertThat(gqs.getEffectivePower(gd, fly))
                .isBetween(1, 6)
                .isEqualTo(gqs.getEffectiveToughness(gd, fly));
    }

    @Test
    @DisplayName("Declining the d6 animation leaves the artifact unchanged")
    void declinesRandomAnimation() {
        Permanent fly = addReadyFly();
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateAbility(player1, battlefieldIndex(fly), 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gqs.isCreature(gd, fly)).isFalse();
        assertThat(gqs.isArtifact(gd, fly)).isTrue();
    }

    @Test
    @DisplayName("Attacking grants flying to another attacking creature")
    void grantsFlyingToAnotherAttackingCreature() {
        Permanent fly = addReadyFly();
        activateAnimation(fly);
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(battlefieldIndex(fly), battlefieldIndex(attacker)));
        harness.handlePermanentChosen(player1, attacker.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, attacker, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("The attack trigger cannot target Ebony Fly itself")
    void cannotTargetItself() {
        Permanent fly = addReadyFly();
        activateAnimation(fly);
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(battlefieldIndex(fly), battlefieldIndex(attacker)));

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, fly.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The d6 animation ends at end of turn")
    void animationEndsAtEndOfTurn() {
        Permanent fly = addReadyFly();
        activateAnimation(fly);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, fly)).isFalse();
        assertThat(gqs.hasEffectiveSubtype(gd, fly, CardSubtype.INSECT)).isFalse();
    }

    private Permanent addReadyFly() {
        return addCreatureReady(player1, new EbonyFly());
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }

    private void activateAnimation(Permanent fly) {
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateAbility(player1, battlefieldIndex(fly), 1, null, null);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
    }
}
