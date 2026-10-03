package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.v.VedalkenOrrery;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AtraxiWarden.class, AdiposeOffspring.class, VedalkenOrrery.class})
class AtraxiWardenTest extends BaseCardTest {

    @Test
    @DisplayName("Suspend exiles Atraxi Warden with five time counters")
    void suspendExilesWithFiveTimeCounters() {
        AtraxiWarden card = new AtraxiWarden();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateHandAbility(player1, 0, null);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 5);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("ETB exiles up to one target tapped creature")
    void exilesTappedCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AdiposeOffspring());
        target.tap();

        castAtraxiWarden(List.of(target.getId()));

        harness.assertNotOnBattlefield(player2, "Adipose Offspring");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Adipose Offspring");
    }

    @Test
    @DisplayName("ETB may choose no target")
    void mayChooseNoTarget() {
        castAtraxiWarden(List.of());

        harness.assertOnBattlefield(player1, "Atraxi Warden");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot target an untapped creature")
    void cannotTargetUntappedCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AdiposeOffspring());

        harness.setHand(player1, List.of(new AtraxiWarden()));
        addMana();

        assertThatThrownBy(() -> harness.castCreature(player1, 0, List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("tapped creature");
    }

    @Test
    void mayLeaveAnAvailableTappedCreatureUnexiled() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AdiposeOffspring());
        target.tap();

        castAtraxiWarden(List.of());

        harness.assertOnBattlefield(player2, "Adipose Offspring");
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    void mayExileItsControllersTappedCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new AdiposeOffspring());
        target.tap();

        castAtraxiWarden(List.of(target.getId()));

        harness.assertNotOnBattlefield(player1, "Adipose Offspring");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(target.getCard());
    }

    @Test
    void untappingTheTargetBeforeResolutionPreventsExile() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AdiposeOffspring());
        target.tap();
        harness.setHand(player1, List.of(new AtraxiWarden()));
        addMana();
        harness.castCreature(player1, 0, List.of(target.getId()));
        harness.passBothPriorities();

        target.untap();
        resolveAllTriggers();

        harness.assertOnBattlefield(player2, "Adipose Offspring");
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    void onlyTheOwnersUpkeepRemovesTimeCounters() {
        AtraxiWarden card = suspendCard();

        advanceToUpkeep(player2);
        resolveAllTriggers();
        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 5);

        advanceToUpkeep(player1);
        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 5);
        resolveAllTriggers();
        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 4);
    }

    @Test
    void lastCounterCreatesASeparateRespondableCastTrigger() {
        AtraxiWarden card = suspendCard();
        for (int i = 0; i < 4; i++) {
            advanceToUpkeep(player1);
            resolveAllTriggers();
        }

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.exiledCardTimeCounters).doesNotContainKey(card.getId());
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
    }

    @Test
    void decliningTheFreeCastLeavesTheCardExiledWithoutCounters() {
        AtraxiWarden card = suspendCard();
        for (int i = 0; i < 5; i++) {
            advanceToUpkeep(player1);
            resolveAllTriggers();
        }

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
        assertThat(gd.exiledCardTimeCounters).doesNotContainKey(card.getId());
        advanceToUpkeep(player1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canCastFromSuspendWithoutAnyTappedCreature() {
        AtraxiWarden card = suspendCard();
        for (int i = 0; i < 5; i++) {
            advanceToUpkeep(player1);
            resolveAllTriggers();
        }

        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        Permanent warden = findPermanent(player1, "Atraxi Warden");
        assertThat(gqs.hasKeyword(gd, warden, Keyword.HASTE)).isTrue();
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(card);
    }

    @Test
    void theFreeCastFromSuspendGrantsHasteAndResolvesTheEnterAbility() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AdiposeOffspring());
        target.tap();
        AtraxiWarden card = suspendCard();
        for (int i = 0; i < 5; i++) {
            advanceToUpkeep(player1);
            resolveAllTriggers();
        }

        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, target.getId());
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, findPermanent(player1, "Atraxi Warden"), Keyword.HASTE)).isTrue();
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(card);
        harness.assertNotOnBattlefield(player2, "Adipose Offspring");
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(target.getCard());
    }

    @Test
    void canSuspendOnOpponentsTurnWhenSpellsCanBeCastWithFlash() {
        harness.addToBattlefield(player1, new VedalkenOrrery());
        AtraxiWarden card = new AtraxiWarden();
        harness.setHand(player1, List.of(card));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);
        harness.ensurePriority(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateHandAbility(player1, 0, null);

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 5);
        assertThat(gd.stack).isEmpty();
    }

    private AtraxiWarden suspendCard() {
        AtraxiWarden card = new AtraxiWarden();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateHandAbility(player1, 0, null);
        return card;
    }

    private void castAtraxiWarden(List<java.util.UUID> targetIds) {
        harness.setHand(player1, List.of(new AtraxiWarden()));
        addMana();
        harness.castCreature(player1, 0, targetIds);
        resolveAllTriggers();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }
}
