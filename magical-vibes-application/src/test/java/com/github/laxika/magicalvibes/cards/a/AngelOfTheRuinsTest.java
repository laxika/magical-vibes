package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.SolRing;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AngelOfTheRuins.class, GloriousAnthem.class, Plains.class, SolRing.class})
class AngelOfTheRuinsTest extends BaseCardTest {

    @Test
    @DisplayName("Enters and exiles up to two target artifacts and/or enchantments")
    void exilesTwoTargets() {
        Permanent ring = harness.addToBattlefieldAndReturn(player2, new SolRing());
        Permanent anthem = harness.addToBattlefieldAndReturn(player2, new GloriousAnthem());

        castAngel(List.of(ring.getId(), anthem.getId()));

        harness.assertOnBattlefield(player1, "Angel of the Ruins");
        harness.assertNotOnBattlefield(player2, "Sol Ring");
        harness.assertNotOnBattlefield(player2, "Glorious Anthem");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(card -> card.getName())
                .containsExactlyInAnyOrder("Sol Ring", "Glorious Anthem");
    }

    @Test
    @DisplayName("Can enter with no ETB targets")
    void canChooseNoTargets() {
        castAngel(List.of());

        harness.assertOnBattlefield(player1, "Angel of the Ruins");
    }

    @Test
    @DisplayName("Cannot target a non-artifact, non-enchantment permanent")
    void cannotTargetPlains() {
        Permanent plains = harness.addToBattlefieldAndReturn(player2, new Plains());
        harness.setHand(player1, List.of(new AngelOfTheRuins()));
        addAngelMana();

        assertThatThrownBy(() -> harness.castCreature(player1, 0, List.of(plains.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Plainscycling searches a Plains into hand")
    void plainscyclingSearchesPlains() {
        harness.setHand(player1, List.of(new AngelOfTheRuins()));
        harness.setLibrary(player1, List.of(new Plains()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Angel of the Ruins");
        harness.assertInHand(player1, "Plains");
    }

    @Test
    void canExileOneOfItsControllersArtifacts() {
        Permanent ring = harness.addToBattlefieldAndReturn(player1, new SolRing());

        castAngel(List.of(ring.getId()));

        harness.assertNotOnBattlefield(player1, "Sol Ring");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(card -> card.getName()).containsExactly("Sol Ring");
        harness.assertOnBattlefield(player1, "Angel of the Ruins");
    }

    @Test
    void canExileTwoArtifacts() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new SolRing());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new SolRing());

        castAngel(List.of(first.getId(), second.getId()));

        harness.assertNotOnBattlefield(player2, "Sol Ring");
        assertThat(gd.getPlayerExiledCards(player2.getId())).hasSize(2);
    }

    @Test
    void canDeclineTargetsEvenWhenArtifactsAreAvailable() {
        harness.addToBattlefield(player2, new SolRing());

        castAngel(List.of());

        harness.assertOnBattlefield(player1, "Angel of the Ruins");
        harness.assertOnBattlefield(player2, "Sol Ring");
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    void plainscyclingDiscardsAsACostBeforeResolution() {
        harness.setHand(player1, List.of(new AngelOfTheRuins()));
        harness.setLibrary(player1, List.of(new Plains()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);

        harness.assertNotInHand(player1, "Angel of the Ruins");
        harness.assertInGraveyard(player1, "Angel of the Ruins");
        harness.assertNotInHand(player1, "Plains");
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void plainscyclingCanFailToFindEvenWhenPlainsIsAvailable() {
        harness.setHand(player1, List.of(new AngelOfTheRuins()));
        harness.setLibrary(player1, List.of(new Plains()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        harness.assertInGraveyard(player1, "Angel of the Ruins");
        harness.assertNotInHand(player1, "Plains");
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(card -> card.getName()).containsExactly("Plains");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void plainscyclingCannotBeActivatedWithOnlyOneMana() {
        harness.setHand(player1, List.of(new AngelOfTheRuins()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Angel of the Ruins");
        harness.assertNotInGraveyard(player1, "Angel of the Ruins");
        assertThat(gd.stack).isEmpty();
    }

    private void castAngel(List<java.util.UUID> targetIds) {
        harness.setHand(player1, List.of(new AngelOfTheRuins()));
        addAngelMana();

        harness.castCreature(player1, 0, targetIds);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void addAngelMana() {
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
    }
}
