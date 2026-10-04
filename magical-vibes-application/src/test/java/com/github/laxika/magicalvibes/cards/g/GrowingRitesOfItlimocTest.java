package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.cards.j.JungleDelver;
import com.github.laxika.magicalvibes.cards.o.Opt;
import com.github.laxika.magicalvibes.cards.r.RaptorCompanion;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GrowingRitesOfItlimoc.class, JungleDelver.class, Opt.class, RaptorCompanion.class})
class GrowingRitesOfItlimocTest extends BaseCardTest {

    @Test
    @DisplayName("ETB offers creature cards among top 4 for selection")
    void etbOffersCreatureCards() {
        setupTopCards(List.of(
                new JungleDelver(),
                new Opt(),
                new RaptorCompanion(),
                new Opt()
        ));
        harness.setHand(player1, List.of(new GrowingRitesOfItlimoc()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities(); // resolve enchantment, triggers ETB

        // ETB is pushed onto the stack as a triggered ability
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().playerId()).isEqualTo(player1.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().canFailToFind()).isTrue();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards()).hasSize(2);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards().stream().map(Card::getName))
                .containsExactlyInAnyOrder("Jungle Delver", "Raptor Companion");
    }

    @Test
    @DisplayName("ETB allows choosing a creature to put in hand")
    void etbChooseCreatureToHand() {
        Card bear = new JungleDelver();
        Card bolt = new Opt();
        Card elf = new RaptorCompanion();
        Card enchant = new Opt();
        setupTopCards(List.of(bear, bolt, elf, enchant));
        harness.setHand(player1, List.of(new GrowingRitesOfItlimoc()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities(); // resolve enchantment
        harness.passBothPriorities(); // resolve ETB

        // Choose the first creature (Jungle Delver)
        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Jungle Delver");
        // Remaining 3 cards should be offered for reorder
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class).cards()).hasSize(3);
    }

    @Test
    @DisplayName("ETB allows declining to choose a creature")
    void etbDeclineCreature() {
        setupTopCards(List.of(
                new JungleDelver(),
                new Opt(),
                new RaptorCompanion(),
                new Opt()
        ));
        harness.setHand(player1, List.of(new GrowingRitesOfItlimoc()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities(); // resolve enchantment
        harness.passBothPriorities(); // resolve ETB

        int handBefore = gd.playerHands.get(player1.getId()).size();

        // Decline to choose (-1 = fail to find)
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        // All 4 cards should be offered for reorder
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class).cards()).hasSize(4);
    }

    @Test
    @DisplayName("ETB with no creatures among top 4 skips to reorder")
    void etbNoCreaturesSkipsToReorder() {
        setupTopCards(List.of(
                new Opt(),
                new Opt(),
                new Opt(),
                new Opt()
        ));
        harness.setHand(player1, List.of(new GrowingRitesOfItlimoc()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities(); // resolve enchantment
        harness.passBothPriorities(); // resolve ETB

        // No creatures → directly go to reorder
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class).cards()).hasSize(4);
    }

    @Test
    @DisplayName("Transforms at end step with exactly 4 creatures")
    void transformsWithFourCreatures() {
        Permanent enchantment = addEnchantmentReady(player1);
        addCreatureReady(player1);
        addCreatureReady(player1);
        addCreatureReady(player1);
        addCreatureReady(player1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to end step, trigger goes on stack
        harness.passBothPriorities(); // resolve transform trigger

        assertThat(enchantment.isTransformed()).isTrue();
        assertThat(enchantment.getCard().getName()).isEqualTo("Itlimoc, Cradle of the Sun");
    }

    @Test
    @DisplayName("Transforms at end step with more than 4 creatures")
    void transformsWithFiveCreatures() {
        Permanent enchantment = addEnchantmentReady(player1);
        addCreatureReady(player1);
        addCreatureReady(player1);
        addCreatureReady(player1);
        addCreatureReady(player1);
        addCreatureReady(player1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to end step
        harness.passBothPriorities(); // resolve transform trigger

        assertThat(enchantment.isTransformed()).isTrue();
    }

    @Test
    @DisplayName("Does not transform at end step with only 3 creatures")
    void doesNotTransformWithThreeCreatures() {
        Permanent enchantment = addEnchantmentReady(player1);
        addCreatureReady(player1);
        addCreatureReady(player1);
        addCreatureReady(player1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to end step — no trigger

        assertThat(enchantment.isTransformed()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Does not transform at end step with zero creatures")
    void doesNotTransformWithZeroCreatures() {
        Permanent enchantment = addEnchantmentReady(player1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(enchantment.isTransformed()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Does not trigger on opponent's end step")
    void doesNotTriggerOnOpponentEndStep() {
        Permanent enchantment = addEnchantmentReady(player1);
        addCreatureReady(player1);
        addCreatureReady(player1);
        addCreatureReady(player1);
        addCreatureReady(player1);

        // It's player2's turn, not player1's
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(enchantment.isTransformed()).isFalse();
    }

    @Test
    @DisplayName("Itlimoc basic tap adds one green mana")
    void itlimocBasicTapAddsGreen() {
        Permanent itlimoc = addTransformedItlimoc(player1);

        int itlimocIdx = indexOf(player1, itlimoc);
        harness.activateAbility(player1, itlimocIdx, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Itlimoc per-creature tap adds green for each creature")
    void itlimocPerCreatureTapAddsGreenPerCreature() {
        Permanent itlimoc = addTransformedItlimoc(player1);
        addCreatureReady(player1);
        addCreatureReady(player1);
        addCreatureReady(player1);

        int itlimocIdx = indexOf(player1, itlimoc);
        harness.activateAbility(player1, itlimocIdx, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(3);
    }

    @Test
    @DisplayName("Itlimoc per-creature tap with zero creatures adds zero mana")
    void itlimocPerCreatureTapWithZeroCreatures() {
        Permanent itlimoc = addTransformedItlimoc(player1);

        int greenBefore = gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN);
        int itlimocIdx = indexOf(player1, itlimoc);
        harness.activateAbility(player1, itlimocIdx, 1, null, null);
        assertThat(itlimoc.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(greenBefore);
    }

    @Test
    void doesNotTransformWhenCreatureCountDropsBeforeResolution() {
        Permanent enchantment = addEnchantmentReady(player1);
        for (int i = 0; i < 3; i++) {
            addCreatureReady(player1);
        }
        Permanent fourth = addCreatureReady(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);

        gd.playerBattlefields.get(player1.getId()).remove(fourth);
        harness.setGraveyard(player1, List.of(fourth.getCard()));
        harness.passBothPriorities();

        assertThat(enchantment.isTransformed()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void opponentCreaturesDoNotMeetTransformThreshold() {
        Permanent enchantment = addEnchantmentReady(player1);
        for (int i = 0; i < 3; i++) {
            addCreatureReady(player1);
        }
        addCreatureReady(player2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(enchantment.isTransformed()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void transformedLandCanProduceManaImmediatelyAndIgnoresOpponentCreatures() {
        Permanent enchantment = addEnchantmentReady(player1);
        for (int i = 0; i < 4; i++) {
            addCreatureReady(player1);
        }
        addCreatureReady(player2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.activateAbility(player1, indexOf(player1, enchantment), 1, null, null);

        assertThat(enchantment.isTransformed()).isTrue();
        assertThat(enchantment.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(4);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void selectedCreatureGoesToHandAndRestGoToBottomInChosenOrder() {
        Card creature = new JungleDelver();
        Card first = new Opt();
        Card second = new Opt();
        Card third = new Opt();
        Card untouched = new RaptorCompanion();
        setupTopCards(List.of(creature, first, second, third, untouched));
        harness.setHand(player1, List.of(new GrowingRitesOfItlimoc()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.handleCardChosen(player1, 0);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(2, 0, 1)));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(creature);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(untouched, third, first, second);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    void singleCreatureLibraryStillAllowsDeclining() {
        Card creature = new JungleDelver();
        setupTopCards(List.of(creature));
        harness.setHand(player1, List.of(new GrowingRitesOfItlimoc()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(creature);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void emptyLibraryDoesNotPreventEnchantmentResolving() {
        setupTopCards(List.of());
        harness.setHand(player1, List.of(new GrowingRitesOfItlimoc()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Growing Rites of Itlimoc");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void decliningCreaturePutsAllViewedCardsOnBottomInChosenOrder() {
        Card creature = new JungleDelver();
        Card first = new Opt();
        Card second = new Opt();
        Card third = new Opt();
        Card untouched = new RaptorCompanion();
        setupTopCards(List.of(creature, first, second, third, untouched));
        harness.setHand(player1, List.of(new GrowingRitesOfItlimoc()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.handleCardChosen(player1, -1);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(3, 2, 1, 0)));

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(untouched, third, second, first, creature);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    void shortLibraryAllowsChoosingCreatureAndReturnsRemainingCard() {
        Card creature = new JungleDelver();
        Card other = new Opt();
        setupTopCards(List.of(other, creature));
        harness.setHand(player1, List.of(new GrowingRitesOfItlimoc()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(creature);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(other);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private Permanent addEnchantmentReady(Player player) {
        return harness.addToBattlefieldAndReturn(player, new GrowingRitesOfItlimoc());
    }

    private Permanent addTransformedItlimoc(Player player) {
        Permanent perm = addEnchantmentReady(player);
        perm.setCard(perm.getOriginalCard().getBackFaceCard());
        perm.setTransformed(true);
        return perm;
    }

    private Permanent addCreatureReady(Player player) {
        return addCreatureReady(player, new JungleDelver());
    }

    private void setupTopCards(List<Card> cards) {
        harness.setLibrary(player1, cards);
    }

    private int indexOf(Player player, Permanent perm) {
        return gd.playerBattlefields.get(player.getId()).indexOf(perm);
    }
}
