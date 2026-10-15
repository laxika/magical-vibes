package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AuriokGlaivemaster;
import com.github.laxika.magicalvibes.cards.b.BlinkmothNexus;
import com.github.laxika.magicalvibes.cards.d.DarksteelCitadel;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ReapAndSow.class, BlinkmothNexus.class, AuriokGlaivemaster.class, DarksteelCitadel.class})
class ReapAndSowTest extends BaseCardTest {

    @Test
    @DisplayName("Destroy mode destroys target land")
    void destroyModeDestroysTargetLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new BlinkmothNexus());
        harness.setHand(player1, List.of(new ReapAndSow()));
        addMana(false);

        harness.castModalSorceryWithModes(player1, 0, 1, 2, new int[]{0}, List.of(land.getId()), null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Blinkmoth Nexus");
        harness.assertInGraveyard(player2, "Blinkmoth Nexus");
    }

    @Test
    @DisplayName("Search mode puts a land from the library onto the battlefield")
    void searchModePutsLandOntoBattlefield() {
        prepareCast(false, List.of(new BlinkmothNexus(), new AuriokGlaivemaster()));
        harness.castModalSorceryWithModes(player1, 0, 1, 2, new int[]{1}, List.of(), null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Blinkmoth Nexus");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst().getName()).isEqualTo("Auriok Glaivemaster");
    }

    @Test
    @DisplayName("Entwine destroys a land and searches for another land")
    void entwinedResolvesBothModes() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new BlinkmothNexus());
        prepareCast(true, List.of(new BlinkmothNexus()));

        harness.castModalSorceryWithModes(player1, 0, 1, 2, new int[]{0, 1}, List.of(land.getId()), null);
        harness.passBothPriorities();
        harness.assertInGraveyard(player2, "Blinkmoth Nexus");
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player2, "Blinkmoth Nexus");
        harness.assertOnBattlefield(player1, "Blinkmoth Nexus");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    @DisplayName("Destroy mode cannot target a creature")
    void destroyModeCannotTargetCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new AuriokGlaivemaster());
        harness.setHand(player1, List.of(new ReapAndSow()));
        addMana(false);

        assertThatThrownBy(() -> harness.castModalSorceryWithModes(
                player1, 0, 1, 2, new int[]{0}, List.of(creature.getId()), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a land");
    }

    @Test
    @DisplayName("Search mode cannot find a nonland card")
    void searchModeCannotFindNonlandCard() {
        prepareCast(false, List.of(new AuriokGlaivemaster()));
        harness.castModalSorceryWithModes(player1, 0, 1, 2, new int[]{1}, List.of(), null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Auriok Glaivemaster");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst().getName()).isEqualTo("Auriok Glaivemaster");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Entwine requires its additional mana cost")
    void entwineRequiresAdditionalMana() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new BlinkmothNexus());
        prepareCast(false, List.of(new BlinkmothNexus()));

        assertThatThrownBy(() -> harness.castModalSorceryWithModes(
                player1, 0, 1, 2, new int[]{0, 1}, List.of(land.getId()), null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Destroy mode does not destroy an indestructible land")
    void destroyModeDoesNotDestroyIndestructibleLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new DarksteelCitadel());
        harness.setHand(player1, List.of(new ReapAndSow()));
        addMana(false);

        harness.castModalSorceryWithModes(player1, 0, 1, 2, new int[]{0}, List.of(land.getId()), null);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Darksteel Citadel");
        harness.assertNotInGraveyard(player2, "Darksteel Citadel");
    }

    @Test
    @DisplayName("Search may fail to find even when a land is available")
    void searchMayFailToFindAvailableLand() {
        prepareCast(false, List.of(new BlinkmothNexus()));
        harness.castModalSorceryWithModes(player1, 0, 1, 2, new int[]{1}, List.of(), null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        harness.assertNotOnBattlefield(player1, "Blinkmoth Nexus");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Reap and Sow");
    }

    @Test
    @DisplayName("Search resolves with an empty library")
    void searchResolvesWithEmptyLibrary() {
        prepareCast(false, List.of());
        harness.castModalSorceryWithModes(player1, 0, 1, 2, new int[]{1}, List.of(), null);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Reap and Sow");
    }

    @Test
    @DisplayName("Entwine still searches when the targeted land is indestructible")
    void entwineSearchesDespiteIndestructibleTarget() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new DarksteelCitadel());
        prepareCast(true, List.of(new BlinkmothNexus()));

        harness.castModalSorceryWithModes(player1, 0, 1, 2, new int[]{0, 1}, List.of(land.getId()), null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player2, "Darksteel Citadel");
        harness.assertNotInGraveyard(player2, "Darksteel Citadel");
        harness.assertOnBattlefield(player1, "Blinkmoth Nexus");
        assertThat(findPermanent(player1, "Blinkmoth Nexus").isTapped()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Reap and Sow");
    }

    @Test
    @DisplayName("Entwine does not search when its only target leaves the battlefield")
    void entwineDoesNotSearchWithIllegalTarget() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new BlinkmothNexus());
        prepareCast(true, List.of(new DarksteelCitadel()));

        harness.castModalSorceryWithModes(player1, 0, 1, 2, new int[]{0, 1}, List.of(land.getId()), null);
        gd.playerBattlefields.get(player2.getId()).remove(land);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Darksteel Citadel");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Reap and Sow");
    }

    private void prepareCast(boolean entwined, List<Card> library) {
        harness.setHand(player1, List.of(new ReapAndSow()));
        harness.setLibrary(player1, library);
        addMana(entwined);
    }

    private void addMana(boolean entwined) {
        harness.addMana(player1, ManaColor.GREEN, entwined ? 2 : 1);
        harness.addMana(player1, ManaColor.COLORLESS, entwined ? 4 : 3);
    }

}
