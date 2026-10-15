package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.t.ThrivingTurtle;
import com.github.laxika.magicalvibes.cards.p.PropheticPrism;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({InventorsFair.class, PropheticPrism.class, Forest.class, ThrivingTurtle.class})
class InventorsFairTest extends BaseCardTest {

    @Test
    void tapsForColorlessMana() {
        Permanent fair = addCreatureReady(player1, new InventorsFair());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(fair.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    void gainsLifeAtUpkeepWithThreeArtifacts() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new InventorsFair());
        harness.addToBattlefield(player1, new PropheticPrism());
        harness.addToBattlefield(player1, new PropheticPrism());
        harness.addToBattlefield(player1, new PropheticPrism());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertLife(player1, 21);
    }

    @Test
    void doesNotGainLifeAtUpkeepWithoutThreeArtifacts() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new InventorsFair());
        harness.addToBattlefield(player1, new PropheticPrism());
        harness.addToBattlefield(player1, new PropheticPrism());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
    }

    @Test
    void cannotActivateSearchWithoutMetalcraft() {
        addCreatureReady(player1, new InventorsFair());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("three or more artifacts");
    }

    @Test
    void searchesForAnArtifactAndPutsItIntoHand() {
        Permanent fair = addCreatureReady(player1, new InventorsFair());
        harness.addToBattlefield(player1, new PropheticPrism());
        harness.addToBattlefield(player1, new PropheticPrism());
        harness.addToBattlefield(player1, new PropheticPrism());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.setLibrary(player1, List.of(new PropheticPrism(), new Forest(), new ThrivingTurtle()));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(fair);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(fair.getCard());
        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards())
                .hasSize(1)
                .allMatch(card -> card.hasType(CardType.ARTIFACT));

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card.hasType(CardType.ARTIFACT));
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
    @Test
    void upkeepDoesNotTriggerWhenMetalcraftIsGainedAfterUpkeepBegins() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new InventorsFair());
        harness.addToBattlefield(player1, new PropheticPrism());
        harness.addToBattlefield(player1, new PropheticPrism());

        advanceToUpkeep(player1);

        assertThat(gd.stack).isEmpty();
        harness.addToBattlefield(player1, new PropheticPrism());
        harness.passBothPriorities();
        harness.assertLife(player1, 20);
    }

    @Test
    void upkeepRechecksMetalcraftOnResolution() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new InventorsFair());
        harness.addToBattlefield(player1, new PropheticPrism());
        harness.addToBattlefield(player1, new PropheticPrism());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new PropheticPrism());

        advanceToUpkeep(player1);

        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(artifact);
        gd.playerGraveyards.get(player1.getId()).add(artifact.getCard());
        harness.passBothPriorities();
        harness.assertLife(player1, 20);
    }

    @Test
    void doesNotGainLifeDuringOpponentsUpkeep() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new InventorsFair());
        harness.addToBattlefield(player1, new PropheticPrism());
        harness.addToBattlefield(player1, new PropheticPrism());
        harness.addToBattlefield(player1, new PropheticPrism());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
    }

    @Test
    void opponentsArtifactsDoNotEnableMetalcraft() {
        harness.setLife(player1, 20);
        addCreatureReady(player1, new InventorsFair());
        harness.addToBattlefield(player1, new PropheticPrism());
        harness.addToBattlefield(player1, new PropheticPrism());
        harness.addToBattlefield(player2, new PropheticPrism());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("three or more artifacts");
    }

    @Test
    void searchStillResolvesAfterLosingMetalcraft() {
        addCreatureReady(player1, new InventorsFair());
        harness.addToBattlefield(player1, new PropheticPrism());
        harness.addToBattlefield(player1, new PropheticPrism());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new PropheticPrism());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.setLibrary(player1, List.of(new PropheticPrism(), new Forest()));

        harness.activateAbility(player1, 0, 1, null, null);
        gd.playerBattlefields.get(player1.getId()).remove(artifact);
        gd.playerGraveyards.get(player1.getId()).add(artifact.getCard());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Prophetic Prism");
        harness.assertInGraveyard(player1, "Inventors' Fair");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void mayFailToFindEvenWhenAnArtifactIsInLibrary() {
        addCreatureReady(player1, new InventorsFair());
        harness.addToBattlefield(player1, new PropheticPrism());
        harness.addToBattlefield(player1, new PropheticPrism());
        harness.addToBattlefield(player1, new PropheticPrism());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.setLibrary(player1, List.of(new PropheticPrism(), new Forest()));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        harness.assertNotInHand(player1, "Prophetic Prism");
        harness.assertInGraveyard(player1, "Inventors' Fair");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void searchWithNoArtifactsInLibraryCompletesWithoutAChoice() {
        addCreatureReady(player1, new InventorsFair());
        harness.addToBattlefield(player1, new PropheticPrism());
        harness.addToBattlefield(player1, new PropheticPrism());
        harness.addToBattlefield(player1, new PropheticPrism());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.setLibrary(player1, List.of(new Forest(), new ThrivingTurtle()));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Inventors' Fair");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotSearchWhileTapped() {
        Permanent fair = addCreatureReady(player1, new InventorsFair());
        fair.tap();
        harness.addToBattlefield(player1, new PropheticPrism());
        harness.addToBattlefield(player1, new PropheticPrism());
        harness.addToBattlefield(player1, new PropheticPrism());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Inventors' Fair");
        harness.assertNotInGraveyard(player1, "Inventors' Fair");
    }

    @Test
    void cannotSearchWithOnlyThreeMana() {
        addCreatureReady(player1, new InventorsFair());
        harness.addToBattlefield(player1, new PropheticPrism());
        harness.addToBattlefield(player1, new PropheticPrism());
        harness.addToBattlefield(player1, new PropheticPrism());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Inventors' Fair");
        harness.assertNotInGraveyard(player1, "Inventors' Fair");
    }
}
