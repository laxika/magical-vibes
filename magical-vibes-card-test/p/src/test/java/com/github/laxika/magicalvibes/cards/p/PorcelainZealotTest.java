package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.AnointWithAffliction;
import com.github.laxika.magicalvibes.cards.a.AspirantsAscent;
import com.github.laxika.magicalvibes.cards.c.CrawlingChorus;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PorcelainZealot.class, CrawlingChorus.class, PredationSteward.class,
        AnointWithAffliction.class, AspirantsAscent.class})
class PorcelainZealotTest extends BaseCardTest {

    @Test
    @DisplayName("Beginning of combat gives a non-toxic target +1/+1")
    void boostsNonToxicTarget() {
        addZealot();
        Permanent target = harness.addToBattlefieldAndReturn(player1, new PredationSteward());

        resolveBeginningOfCombat(player1, target);

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
    }

    @Test
    @DisplayName("Beginning of combat gives a toxic target +2/+2 instead")
    void boostsToxicTargetByTwoInsteadOfOne() {
        addZealot();
        Permanent target = harness.addToBattlefieldAndReturn(player1, new CrawlingChorus());

        resolveBeginningOfCombat(player1, target);

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
    }

    @Test
    @DisplayName("The trigger targets only creatures you control")
    void targetsOnlyCreaturesYouControl() {
        Permanent zealot = addZealot();
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new PredationSteward());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new PredationSteward());

        advanceToBeginningOfCombat(player1);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(zealot.getId(), ownCreature.getId())
                .doesNotContain(opponentCreature.getId());
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The beginning-of-combat boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        addZealot();
        Permanent target = harness.addToBattlefieldAndReturn(player1, new PredationSteward());

        resolveBeginningOfCombat(player1, target);
        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
    }

    @Test
    @DisplayName("Porcelain Zealot can target itself")
    void canBoostItself() {
        Permanent zealot = addZealot();

        resolveBeginningOfCombat(player1, zealot);

        assertThat(gqs.getEffectivePower(gd, zealot)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, zealot)).isEqualTo(4);
    }

    @Test
    @DisplayName("The ability does not trigger during an opponent's combat")
    void doesNotTriggerOnOpponentsTurn() {
        Permanent zealot = addZealot();

        advanceToBeginningOfCombat(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gqs.getEffectivePower(gd, zealot)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, zealot)).isEqualTo(3);
    }

    @Test
    @DisplayName("Toxic gained in response upgrades the boost at resolution")
    void checksToxicAtResolution() {
        Permanent zealot = addZealot();
        advanceToBeginningOfCombat(player1);
        harness.handlePermanentChosen(player1, zealot.getId());
        harness.setHand(player1, List.of(new AspirantsAscent()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0, zealot.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, zealot)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, zealot)).isEqualTo(8);
    }

    @Test
    @DisplayName("A target exiled in response makes the ability do nothing")
    void removedTargetDoesNotReceiveBoost() {
        Permanent zealot = addZealot();
        Permanent target = harness.addToBattlefieldAndReturn(player1, new CrawlingChorus());
        advanceToBeginningOfCombat(player1);
        harness.handlePermanentChosen(player1, target.getId());
        harness.setHand(player2, List.of(new AnointWithAffliction()));
        harness.addMana(player2, ManaColor.BLACK, 2);

        harness.castAndResolveInstant(player2, 0, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Crawling Chorus");
        assertThat(gd.exiledCards).anyMatch(entry -> entry.card().getId().equals(target.getCard().getId()));
        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, zealot)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, zealot)).isEqualTo(3);
    }

    @Test
    @DisplayName("The upgraded toxic boost also wears off at end of turn")
    void toxicBoostWearsOffAtEndOfTurn() {
        addZealot();
        Permanent target = harness.addToBattlefieldAndReturn(player1, new CrawlingChorus());

        resolveBeginningOfCombat(player1, target);
        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(1);
    }

    private Permanent addZealot() {
        return harness.addToBattlefieldAndReturn(player1, new PorcelainZealot());
    }

    private void resolveBeginningOfCombat(Player activePlayer, Permanent target) {
        advanceToBeginningOfCombat(activePlayer);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
    }

    private void advanceToBeginningOfCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(activePlayer, TurnStep.BEGINNING_OF_COMBAT);
    }
}
