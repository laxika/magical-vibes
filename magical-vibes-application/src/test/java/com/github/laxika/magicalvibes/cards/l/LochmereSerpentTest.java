package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LochmereSerpent.class, GrizzlyBears.class, Island.class, Swamp.class})
class LochmereSerpentTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing an Island makes Lochmere Serpent unblockable this turn")
    void islandAbilityMakesSerpentUnblockable() {
        Permanent serpent = addReadySerpent();
        harness.addToBattlefield(player1, new Island());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(serpent.isCantBeBlocked()).isTrue();
        harness.assertInGraveyard(player1, "Island");
    }

    @Test
    @DisplayName("The Island ability's unblockable effect wears off at cleanup")
    void islandAbilityWearsOffAtCleanup() {
        Permanent serpent = addReadySerpent();
        harness.addToBattlefield(player1, new Island());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        assertThat(serpent.isCantBeBlocked()).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(serpent.isCantBeBlocked()).isFalse();
    }

    @Test
    @DisplayName("Sacrificing a Swamp gains life and draws a card")
    void swampAbilityGainsLifeAndDraws() {
        addReadySerpent();
        harness.addToBattlefield(player1, new Swamp());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setLife(player1, 10);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(11);
        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Swamp");
    }

    @Test
    @DisplayName("The graveyard ability exiles five opposing cards and returns Lochmere Serpent")
    void graveyardAbilityExilesFiveCardsAndReturnsSerpent() {
        LochmereSerpent serpent = new LochmereSerpent();
        List<Card> targets = List.of(
                new GrizzlyBears(),
                new GrizzlyBears(),
                new GrizzlyBears(),
                new GrizzlyBears(),
                new GrizzlyBears()
        );
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(serpent));
        harness.setGraveyard(player2, new ArrayList<>(targets));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateGraveyardAbilityWithGraveyardTargets(
                player1, 0, 0, targets.stream().map(Card::getId).toList());
        harness.passBothPriorities();

        harness.assertInHand(player1, "Lochmere Serpent");
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).hasSize(5);
    }

    @Test
    @DisplayName("The graveyard ability requires five cards in one opponent graveyard")
    void graveyardAbilityRequiresFiveTargets() {
        LochmereSerpent serpent = new LochmereSerpent();
        Card target = new GrizzlyBears();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(serpent));
        harness.setGraveyard(player2, List.of(target));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateGraveyardAbilityWithGraveyardTargets(
                player1, 0, 0, List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("exactly 5");
    }

    private Permanent addReadySerpent() {
        Permanent serpent = harness.addToBattlefieldAndReturn(player1, new LochmereSerpent());
        serpent.setSummoningSick(false);
        return serpent;
    }

    @Test
    void canBeCastDuringOpponentsEndStep() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);
        harness.setHand(player1, List.of(new LochmereSerpent()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Lochmere Serpent");
    }

    @Test
    void tappedSummoningSickSerpentCanSacrificeTappedSwamp() {
        Permanent serpent = harness.addToBattlefieldAndReturn(player1, new LochmereSerpent());
        serpent.setTapped(true);
        serpent.setSummoningSick(true);
        Permanent swamp = harness.addToBattlefieldAndReturn(player1, new Swamp());
        swamp.setTapped(true);
        harness.setLibrary(player1, List.of(new Island()));
        harness.setLife(player1, 10);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.assertInGraveyard(player1, "Swamp");
        harness.assertLife(player1, 10);
        harness.assertNotInHand(player1, "Island");
        harness.passBothPriorities();

        harness.assertLife(player1, 11);
        harness.assertInHand(player1, "Island");
    }

    @Test
    void cannotSacrificeOpponentsIslandOrOwnSwampForIslandAbility() {
        addReadySerpent();
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player2, new Island());
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Swamp");
        harness.assertOnBattlefield(player2, "Island");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void graveyardAbilityReturnsSerpentWithOnlyOneLegalTargetRemaining() {
        List<Card> targets = prepareGraveyardAbility();
        activateGraveyardAbility(targets);
        harness.setGraveyard(player2, List.of(targets.getFirst()));
        harness.setExile(player2, targets.subList(1, 5));

        harness.passBothPriorities();

        harness.assertInHand(player1, "Lochmere Serpent");
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).hasSize(5);
    }

    @Test
    void graveyardAbilityDoesNotReturnSerpentWhenAllTargetsAreIllegal() {
        List<Card> targets = prepareGraveyardAbility();
        activateGraveyardAbility(targets);
        harness.setGraveyard(player2, List.of());
        harness.setExile(player2, targets);

        harness.passBothPriorities();

        harness.assertNotInHand(player1, "Lochmere Serpent");
        harness.assertInGraveyard(player1, "Lochmere Serpent");
    }

    @Test
    void graveyardAbilityCannotChooseOneCardFiveTimes() {
        List<Card> targets = prepareGraveyardAbility();
        harness.setGraveyard(player2, List.of(targets.getFirst()));

        assertThatThrownBy(() -> harness.activateGraveyardAbilityWithGraveyardTargets(
                player1, 0, 0, java.util.Collections.nCopies(5, targets.getFirst().getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void graveyardAbilityCannotBeActivatedOutsideMainPhase() {
        List<Card> targets = prepareGraveyardAbility();
        harness.forceStep(TurnStep.END_STEP);

        assertThatThrownBy(() -> activateGraveyardAbility(targets))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    @Test
    void graveyardAbilityCannotBeActivatedOnOpponentsTurn() {
        List<Card> targets = prepareGraveyardAbility();
        harness.forceActivePlayer(player2);

        assertThatThrownBy(() -> activateGraveyardAbility(targets))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    @Test
    void graveyardAbilityCannotBeActivatedWithNonemptyStack() {
        List<Card> targets = prepareGraveyardAbility();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        activateGraveyardAbility(targets);

        assertThatThrownBy(() -> activateGraveyardAbility(targets))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("stack is empty");
    }

    @Test
    void graveyardAbilityDoesNotExileTargetThatLeftAndReenteredGraveyard() {
        List<Card> targets = prepareGraveyardAbility();
        activateGraveyardAbility(targets);
        Card movedCard = targets.getFirst();
        harness.setGraveyard(player2, targets.subList(1, 5));
        harness.setHand(player2, List.of(movedCard));
        harness.setHand(player2, List.of());
        harness.setGraveyard(player2, targets);
        gd.markGraveyardEntry(movedCard);

        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(movedCard);
        assertThat(gd.getPlayerExiledCards(player2.getId())).hasSize(4);
        harness.assertInHand(player1, "Lochmere Serpent");
    }

    @Test
    void graveyardAbilityDoesNotReturnSourceThatLeftAndReenteredGraveyard() {
        List<Card> targets = prepareGraveyardAbility();
        Card serpent = gd.playerGraveyards.get(player1.getId()).getFirst();
        activateGraveyardAbility(targets);
        harness.setGraveyard(player1, List.of());
        harness.setHand(player1, List.of(serpent));
        harness.setHand(player1, List.of());
        harness.setGraveyard(player1, List.of(serpent));
        gd.markGraveyardEntry(serpent);

        harness.passBothPriorities();

        harness.assertNotInHand(player1, "Lochmere Serpent");
        harness.assertInGraveyard(player1, "Lochmere Serpent");
        assertThat(gd.getPlayerExiledCards(player2.getId())).hasSize(5);
    }

    private List<Card> prepareGraveyardAbility() {
        LochmereSerpent serpent = new LochmereSerpent();
        List<Card> targets = List.of(new Island(), new Island(), new Island(), new Swamp(), new Swamp());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(serpent));
        gd.markGraveyardEntry(serpent);
        harness.setGraveyard(player2, targets);
        targets.forEach(gd::markGraveyardEntry);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        return targets;
    }

    private void activateGraveyardAbility(List<Card> targets) {
        harness.activateGraveyardAbilityWithGraveyardTargets(
                player1, 0, 0, targets.stream().map(Card::getId).toList());
    }

}
