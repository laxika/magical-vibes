package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.d.DuneBeetle;
import com.github.laxika.magicalvibes.cards.w.WindsOfRebuke;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NissaStewardOfElements.class, Forest.class, DuneBeetle.class, WindsOfRebuke.class})
class NissaStewardOfElementsTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield with loyalty counters equal to X paid")
    void entersWithXLoyalty() {
        harness.setHand(player1, List.of(new NissaStewardOfElements()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castPlaneswalker(player1, 0, 3); // {X}{G}{U} with X=3
        harness.passBothPriorities();

        Permanent nissa = findPermanent(player1, "Nissa, Steward of Elements");
        assertThat(nissa.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
    }

    @Test
    @DisplayName("+2 raises loyalty by two and scries 2")
    void plusTwoScries() {
        Permanent nissa = addReadyNissa(player1, 3);
        harness.setLibrary(player1, List.of(new Forest(), new DuneBeetle(), new Forest()));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(nissa.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(2);

        // Keep both on top to finish the interaction.
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0, 1), List.of()));
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("0 may put a land from the top of library onto the battlefield")
    void zeroPutsLand() {
        addReadyNissa(player1, 3);
        harness.setLibrary(player1, List.of(new Forest(), new DuneBeetle()));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Forest");
        assertThat(gd.playerDecks.get(player1.getId())).noneMatch(c -> c.getName().equals("Forest"));
    }

    @Test
    @DisplayName("0 may put a creature with mana value <= loyalty onto the battlefield")
    void zeroPutsLowCostCreature() {
        addReadyNissa(player1, 3); // Dune Beetle MV 2 <= 3
        harness.setLibrary(player1, List.of(new DuneBeetle(), new Forest()));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Dune Beetle");
    }

    @Test
    @DisplayName("0 offers no choice when the top creature's mana value exceeds loyalty")
    void zeroCreatureTooExpensive() {
        addReadyNissa(player1, 1); // Dune Beetle MV 2 > 1
        harness.setLibrary(player1, List.of(new DuneBeetle(), new Forest()));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId()).get(0).getName()).isEqualTo("Dune Beetle");
        harness.assertNotOnBattlefield(player1, "Dune Beetle");
    }

    @Test
    @DisplayName("0 leaves the card on top when declined")
    void zeroDeclined() {
        addReadyNissa(player1, 3);
        harness.setLibrary(player1, List.of(new Forest(), new DuneBeetle()));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId()).get(0).getName()).isEqualTo("Forest");
        harness.assertNotOnBattlefield(player1, "Forest");
    }

    @Test
    @DisplayName("-6 untaps and animates two target lands into 5/5 fliers with haste that are still lands")
    void minusSixAnimatesLands() {
        Permanent nissa = addReadyNissa(player1, 7);
        Permanent forest1 = addForest(player1);
        Permanent forest2 = addForest(player1);
        forest1.tap();
        forest2.tap();

        harness.activateAbilityWithMultiTargets(player1, 0, 2, List.of(forest1.getId(), forest2.getId()));
        harness.passBothPriorities();

        assertThat(nissa.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        for (Permanent land : List.of(forest1, forest2)) {
            assertThat(land.isTapped()).isFalse();
            assertThat(gqs.isCreature(gd, land)).isTrue();
            assertThat(gqs.hasEffectiveSubtype(gd, land, CardSubtype.ELEMENTAL)).isTrue();
            assertThat(gqs.getEffectivePower(gd, land)).isEqualTo(5);
            assertThat(gqs.getEffectiveToughness(gd, land)).isEqualTo(5);
            assertThat(gqs.hasKeyword(gd, land, Keyword.FLYING)).isTrue();
            assertThat(gqs.hasKeyword(gd, land, Keyword.HASTE)).isTrue();
            assertThat(land.getCard().hasType(CardType.LAND)).isTrue();
        }
    }

    @Test
    @DisplayName("-6 animation wears off at end of turn")
    void minusSixWearsOff() {
        addReadyNissa(player1, 7);
        Permanent forest = addForest(player1);

        harness.activateAbilityWithMultiTargets(player1, 0, 2, List.of(forest.getId()));
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, forest)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, forest)).isFalse();
        assertThat(forest.getCard().hasType(CardType.LAND)).isTrue();
    }

    @Test
    @DisplayName("-6 cannot target a land you don't control")
    void minusSixCannotTargetOpponentLand() {
        addReadyNissa(player1, 7);
        Permanent oppForest = addForest(player2);

        assertThatThrownBy(() ->
                harness.activateAbilityWithMultiTargets(player1, 0, 2, List.of(oppForest.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void plusTwoCanPutOneCardOnBottom() {
        addReadyNissa(player1, 3);
        Card first = new Forest();
        Card second = new DuneBeetle();
        Card third = new WindsOfRebuke();
        harness.setLibrary(player1, List.of(first, second, third));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(1), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second, third, first);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void zeroUsesLastKnownLoyaltyAfterNissaIsReturnedToHand() {
        Permanent nissa = addReadyNissa(player1, 2);
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new DuneBeetle(), new Forest()));
        harness.setHand(player2, List.of(new WindsOfRebuke()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, nissa.getId());
        harness.passBothPriorities();
        harness.assertInHand(player1, "Nissa, Steward of Elements");
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handleMayAbilityChosen(player1, true);
        harness.assertOnBattlefield(player1, "Dune Beetle");
    }

    @Test
    void zeroPutsCreatureWithManaValueEqualToLoyalty() {
        addReadyNissa(player1, 2);
        harness.setLibrary(player1, List.of(new DuneBeetle(), new Forest()));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Dune Beetle");
    }

    @Test
    void zeroDoesNotPutNonlandNoncreatureOntoBattlefield() {
        addReadyNissa(player1, 7);
        Card topCard = new WindsOfRebuke();
        harness.setLibrary(player1, List.of(topCard, new Forest()));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(topCard);
        harness.assertNotOnBattlefield(player1, "Winds of Rebuke");
    }

    @Test
    void zeroDoesNothingWithEmptyLibrary() {
        addReadyNissa(player1, 3);
        harness.setLibrary(player1, List.of());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void castingWithZeroXLeavesNissaInGraveyard() {
        harness.setHand(player1, List.of(new NissaStewardOfElements()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castPlaneswalker(player1, 0, 0);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Nissa, Steward of Elements");
        harness.assertInGraveyard(player1, "Nissa, Steward of Elements");
    }

    @Test
    void minusSixCanTargetNoLandsAndResolveAfterNissaDies() {
        addReadyNissa(player1, 6);

        harness.activateAbilityWithMultiTargets(player1, 0, 2, List.of());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Nissa, Steward of Elements");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void minusSixPreservesCountersOnAnimatedLand() {
        addReadyNissa(player1, 6);
        Permanent forest = addForest(player1);
        forest.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        forest.tap();

        harness.activateAbilityWithMultiTargets(player1, 0, 2, List.of(forest.getId()));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Nissa, Steward of Elements");
        assertThat(forest.isTapped()).isFalse();
        assertThat(gqs.getEffectivePower(gd, forest)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, forest)).isEqualTo(7);
        assertThat(gqs.hasKeyword(gd, forest, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, forest, Keyword.HASTE)).isTrue();
    }

    private Permanent addReadyNissa(Player player, int loyalty) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new NissaStewardOfElements());
        perm.setCounterCount(CounterType.LOYALTY, loyalty);
        perm.setSummoningSick(false);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }

    private Permanent addForest(Player player) {
        return harness.addToBattlefieldAndReturn(player, new Forest());
    }

}
