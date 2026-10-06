package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RestlessFortress.class})
class RestlessFortressTest extends BaseCardTest {

    @Test
    @DisplayName("Restless Fortress enters tapped and produces white or black mana")
    void entersTappedAndProducesMana() {
        harness.setHand(player1, List.of(new RestlessFortress()));
        harness.playLand(player1, 0);

        Permanent fortress = findPermanent(player1, "Restless Fortress");
        assertThat(fortress.isTapped()).isTrue();

        fortress.untap();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "WHITE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
    }

    @Test
    @DisplayName("Restless Fortress becomes a 1/4 white and black Nightmare and stays a land")
    void animatesIntoNightmare() {
        Permanent fortress = addReadyFortress(player1);
        addAnimationMana(player1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, fortress)).isTrue();
        assertThat(gqs.isLand(gd, fortress)).isTrue();
        assertThat(gqs.getEffectivePower(gd, fortress)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, fortress)).isEqualTo(4);
        assertThat(gqs.getEffectiveColors(gd, fortress))
                .containsExactlyInAnyOrder(CardColor.WHITE, CardColor.BLACK);
        assertThat(gqs.effectiveCreatureSubtypes(gd, fortress)).contains(CardSubtype.NIGHTMARE);
    }

    @Test
    @DisplayName("When Restless Fortress attacks, defending player loses 2 life and its controller gains 2 life")
    void attackingDrainsDefendingPlayer() {
        addReadyFortress(player1);
        addAnimationMana(player1);
        harness.setLife(player1, 10);
        harness.setLife(player2, 10);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(12);
        assertThat(gd.getLife(player2.getId())).isEqualTo(7);
    }

    @Test
    @DisplayName("Restless Fortress can produce black mana")
    void producesBlackMana() {
        Permanent fortress = addReadyFortress(player1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "BLACK");

        assertThat(fortress.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
    }

    @Test
    @DisplayName("A tapped Restless Fortress can animate without untapping")
    void animatesWhileTapped() {
        Permanent fortress = addReadyFortress(player1);
        fortress.tap();
        addAnimationMana(player1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, fortress)).isTrue();
        assertThat(fortress.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Animation lasts through the end step and expires before the next turn")
    void animationExpiresAtEndOfTurn() {
        Permanent fortress = addReadyFortress(player1);
        addAnimationMana(player1);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        assertThat(gqs.isCreature(gd, fortress)).isTrue();
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.isCreature(gd, fortress)).isFalse();
        assertThat(gqs.isLand(gd, fortress)).isTrue();
        assertThat(gqs.getEffectiveColors(gd, fortress)).isEmpty();
        assertThat(gqs.effectiveCreatureSubtypes(gd, fortress)).doesNotContain(CardSubtype.NIGHTMARE);
    }

    @Test
    @DisplayName("The attack trigger drains life even after Restless Fortress leaves the battlefield")
    void attackTriggerResolvesWithoutSource() {
        Permanent fortress = addReadyFortress(player1);
        addAnimationMana(player1);
        harness.setLife(player1, 10);
        harness.setLife(player2, 10);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(0)));
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.getLife(player1.getId())).isEqualTo(10);
        assertThat(gd.getLife(player2.getId())).isEqualTo(10);
        gd.playerBattlefields.get(player1.getId()).remove(fortress);
        gd.playerGraveyards.get(player1.getId()).add(fortress.getCard());
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(12);
        assertThat(gd.getLife(player2.getId())).isEqualTo(8);
    }

    private void addAnimationMana(Player player) {
        harness.addMana(player, ManaColor.COLORLESS, 2);
        harness.addMana(player, ManaColor.WHITE, 1);
        harness.addMana(player, ManaColor.BLACK, 1);
    }

    private Permanent addReadyFortress(Player player) {
        return addCreatureReady(player, new RestlessFortress());
    }
}
