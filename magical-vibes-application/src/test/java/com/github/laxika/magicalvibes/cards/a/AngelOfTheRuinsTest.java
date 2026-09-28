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
