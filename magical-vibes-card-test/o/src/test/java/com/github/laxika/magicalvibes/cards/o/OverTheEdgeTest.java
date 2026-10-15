package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.MineshaftSpider;
import com.github.laxika.magicalvibes.cards.c.CompassGnome;
import com.github.laxika.magicalvibes.cards.p.Petrify;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OverTheEdge.class, Forest.class, MineshaftSpider.class, CompassGnome.class, Petrify.class})
class OverTheEdgeTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a target artifact")
    void destroysTargetArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new CompassGnome());
        castAndResolve(0, artifact.getId());

        harness.assertNotOnBattlefield(player2, "Compass Gnome");
        harness.assertInGraveyard(player2, "Compass Gnome");
    }

    @Test
    @DisplayName("Target creature you control explores twice")
    void targetCreatureExploresTwice() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new MineshaftSpider());
        Card firstLand = new Forest();
        Card secondLand = new Forest();
        harness.setLibrary(player1, List.of(firstLand, secondLand));
        harness.setHand(player1, List.of(new OverTheEdge()));
        addMana();

        harness.castAndResolveSorcery(player1, 0, 1, creature.getId());

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getId)
                .contains(firstLand.getId(), secondLand.getId());
    }

    @Test
    @DisplayName("Destroy mode rejects a creature target")
    void destroyModeRejectsCreatureTarget() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new MineshaftSpider());
        harness.setHand(player1, List.of(new OverTheEdge()));
        addMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Explore mode rejects a creature controlled by an opponent")
    void exploreModeRejectsOpponentCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new MineshaftSpider());
        harness.setHand(player1, List.of(new OverTheEdge()));
        addMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 1, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void destroysTargetEnchantment() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new MineshaftSpider());
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new Petrify());
        aura.setAttachedTo(creature.getId());

        castAndResolve(0, aura.getId());

        harness.assertInGraveyard(player2, "Petrify");
        harness.assertOnBattlefield(player2, "Mineshaft Spider");
    }

    @Test
    void keepingNonlandExploresSameCardAgain() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new MineshaftSpider());
        Card nonland = new CompassGnome();
        harness.setLibrary(player1, List.of(nonland));

        castAndResolve(1, creature.getId());
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.handleMayAbilityChosen(player1, false);
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nonland);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Over the Edge");
    }

    @Test
    void puttingNonlandInGraveyardExploresNextCard() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new MineshaftSpider());
        Card nonland = new CompassGnome();
        Card land = new Forest();
        harness.setLibrary(player1, List.of(nonland, land));

        castAndResolve(1, creature.getId());
        harness.handleMayAbilityChosen(player1, true);

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(nonland);
        assertThat(gd.playerHands.get(player1.getId())).contains(land);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void bothNonlandsCanBePutInGraveyard() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new MineshaftSpider());
        Card first = new CompassGnome();
        Card second = new OverTheEdge();
        harness.setLibrary(player1, List.of(first, second));

        castAndResolve(1, creature.getId());
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(first, second);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void lastLandIsFollowedByEmptyLibraryExplore() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new MineshaftSpider());
        Card land = new Forest();
        harness.setLibrary(player1, List.of(land));

        castAndResolve(1, creature.getId());

        assertThat(gd.playerHands.get(player1.getId())).contains(land);
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void emptyLibraryStillAddsTwoCounters() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new MineshaftSpider());
        harness.setLibrary(player1, List.of());

        castAndResolve(1, creature.getId());

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void removedTargetDoesNotExplore() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new MineshaftSpider());
        Card land = new Forest();
        harness.setLibrary(player1, List.of(land));
        harness.setHand(player1, List.of(new OverTheEdge()));
        addMana();
        harness.castSorcery(player1, 0, 1, creature.getId());
        gd.playerBattlefields.get(player1.getId()).remove(creature);

        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(land);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(land);
        harness.assertInGraveyard(player1, "Over the Edge");
    }

    private void castAndResolve(int mode, java.util.UUID targetId) {
        harness.setHand(player1, List.of(new OverTheEdge()));
        addMana();
        harness.castAndResolveSorcery(player1, 0, mode, targetId);
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
