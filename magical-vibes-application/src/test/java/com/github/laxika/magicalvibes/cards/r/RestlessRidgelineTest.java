package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RestlessRidgeline.class, GrizzlyBears.class})
class RestlessRidgelineTest extends BaseCardTest {

    @Test
    @DisplayName("Restless Ridgeline enters tapped and produces red or green mana")
    void entersTappedAndProducesMana() {
        harness.setHand(player1, List.of(new RestlessRidgeline()));
        harness.playLand(player1, 0);

        Permanent ridgeline = findPermanent(player1, "Restless Ridgeline");
        assertThat(ridgeline.isTapped()).isTrue();

        ridgeline.untap();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "RED");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    @DisplayName("Restless Ridgeline becomes a 3/4 red and green Dinosaur and stays a land")
    void animatesIntoDinosaur() {
        Permanent ridgeline = addReadyRidgeline(player1);
        addAnimationMana(player1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, ridgeline)).isTrue();
        assertThat(gqs.isLand(gd, ridgeline)).isTrue();
        assertThat(gqs.getEffectivePower(gd, ridgeline)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ridgeline)).isEqualTo(4);
        assertThat(gqs.getEffectiveColors(gd, ridgeline))
                .containsExactlyInAnyOrder(CardColor.RED, CardColor.GREEN);
        assertThat(gqs.effectiveCreatureSubtypes(gd, ridgeline)).contains(CardSubtype.DINOSAUR);
    }

    @Test
    @DisplayName("When Restless Ridgeline attacks, another attacking creature gets +2/+0 and untaps")
    void attackBoostsAndUntapsAnotherAttacker() {
        Permanent ridgeline = addReadyRidgeline(player1);
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        addAnimationMana(player1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        declareAttackers(player1, List.of(0, 1));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(bears.getId());
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(bears.isTapped()).isFalse();
        assertThat(ridgeline.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Restless Ridgeline's animation and attack boost end at end of turn")
    void effectsEndAtEndOfTurn() {
        Permanent ridgeline = addReadyRidgeline(player1);
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        addAnimationMana(player1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        declareAttackers(player1, List.of(0, 1));
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, ridgeline)).isFalse();
        assertThat(gqs.isLand(gd, ridgeline)).isTrue();
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    private void addAnimationMana(Player player) {
        harness.addMana(player, ManaColor.COLORLESS, 2);
        harness.addMana(player, ManaColor.RED, 1);
        harness.addMana(player, ManaColor.GREEN, 1);
    }

    private Permanent addReadyRidgeline(Player player) {
        Permanent permanent = new Permanent(new RestlessRidgeline());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
