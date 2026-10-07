package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.CopperHostCrusher;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TidalTerror.class, CopperHostCrusher.class, Island.class})
class TidalTerrorTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking can tap two other creatures to make Tidal Terror unblockable")
    void attacksCanTapTwoOtherCreaturesToBecomeUnblockable() {
        Permanent terror = addCreatureReady(player1, new TidalTerror());
        Permanent firstCreature = addCreatureReady(player1, new CopperHostCrusher());
        Permanent secondCreature = addCreatureReady(player1, new CopperHostCrusher());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        harness.handleMultiplePermanentsChosen(player1, List.of(firstCreature.getId(), secondCreature.getId()));

        assertThat(firstCreature.isTapped()).isTrue();
        assertThat(secondCreature.isTapped()).isTrue();
        assertThat(terror.isCantBeBlocked()).isTrue();
    }

    @Test
    @DisplayName("Declining to tap creatures leaves Tidal Terror blockable")
    void decliningLeavesTidalTerrorBlockable() {
        Permanent terror = addCreatureReady(player1, new TidalTerror());
        Permanent firstCreature = addCreatureReady(player1, new CopperHostCrusher());
        Permanent secondCreature = addCreatureReady(player1, new CopperHostCrusher());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of());

        assertThat(firstCreature.isTapped()).isFalse();
        assertThat(secondCreature.isTapped()).isFalse();
        assertThat(terror.isCantBeBlocked()).isFalse();
    }

    @Test
    @DisplayName("The attack trigger requires exactly two creatures when accepted")
    void acceptedChoiceRequiresExactlyTwoCreatures() {
        Permanent terror = addCreatureReady(player1, new TidalTerror());
        Permanent firstCreature = addCreatureReady(player1, new CopperHostCrusher());
        Permanent secondCreature = addCreatureReady(player1, new CopperHostCrusher());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleMultiplePermanentsChosen(player1, List.of(firstCreature.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("exactly 2");
        harness.handleMultiplePermanentsChosen(player1, List.of(firstCreature.getId(), secondCreature.getId()));

        assertThat(terror.isCantBeBlocked()).isTrue();
    }

    @Test
    @DisplayName("The attack trigger does nothing without two other untapped creatures")
    void attackTriggerDoesNothingWithoutTwoOtherCreatures() {
        Permanent terror = addCreatureReady(player1, new TidalTerror());
        addCreatureReady(player1, new CopperHostCrusher());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(terror.isCantBeBlocked()).isFalse();
    }

    @Test
    void summoningSickCreaturesCanBeTappedForTheAttackTrigger() {
        Permanent terror = addCreatureReady(player1, new TidalTerror());
        Permanent firstCreature = addCreatureReady(player1, new CopperHostCrusher());
        Permanent secondCreature = addCreatureReady(player1, new CopperHostCrusher());
        firstCreature.setSummoningSick(true);
        secondCreature.setSummoningSick(true);

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(firstCreature.getId(), secondCreature.getId()));

        assertThat(firstCreature.isTapped()).isTrue();
        assertThat(secondCreature.isTapped()).isTrue();
        assertThat(terror.isCantBeBlocked()).isTrue();
    }

    @Test
    void tappedCreaturesAndOpponentsCreaturesCannotPayForTheAttackTrigger() {
        Permanent terror = addCreatureReady(player1, new TidalTerror());
        addCreatureReady(player1, new CopperHostCrusher());
        Permanent tappedCreature = addCreatureReady(player1, new CopperHostCrusher());
        tappedCreature.tap();
        Permanent opposingCreature = addCreatureReady(player2, new CopperHostCrusher());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(terror.isCantBeBlocked()).isFalse();
        assertThat(opposingCreature.isTapped()).isFalse();
    }

    @Test
    void islandcyclingCanFailToFindEvenWhenAnIslandIsAvailable() {
        TidalTerror terror = new TidalTerror();
        Island island = new Island();
        harness.setHand(player1, List.of(terror));
        harness.setLibrary(player1, List.of(island));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(terror);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(terror);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(island);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void islandcyclingWithNoIslandDoesNotDrawANonIslandCard() {
        TidalTerror terror = new TidalTerror();
        CopperHostCrusher nonIsland = new CopperHostCrusher();
        harness.setHand(player1, List.of(terror));
        harness.setLibrary(player1, List.of(nonIsland));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(terror);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nonIsland);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void islandcyclingRequiresTwoManaBeforeDiscarding() {
        TidalTerror terror = new TidalTerror();
        harness.setHand(player1, List.of(terror));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(terror);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(terror);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Islandcycling searches for an Island and discards Tidal Terror")
    void islandcyclingSearchesForIsland() {
        Card terror = new TidalTerror();
        Island island = new Island();
        harness.setHand(player1, List.of(terror));
        harness.setLibrary(player1, List.of(island, new CopperHostCrusher()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(terror);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .hasSize(1)
                .allMatch(card -> card instanceof Island);

        harness.handleCardChosen(player1, 0);
        assertThat(gd.playerHands.get(player1.getId())).contains(island);
    }
}
