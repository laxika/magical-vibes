package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.a.AesthirGlider;
import com.github.laxika.magicalvibes.cards.s.ShieldSphere;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WhirlingCatapult.class, AesthirGlider.class, WildAesthir.class, ShieldSphere.class})
class WhirlingCatapultTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 1 damage to each creature with flying and each player, exiling two cards")
    void damagesFliersAndPlayers() {
        harness.addToBattlefield(player1, new WhirlingCatapult());
        harness.addToBattlefield(player1, new WildAesthir());
        harness.addToBattlefield(player2, new WildAesthir());
        var shieldSphere = harness.addToBattlefieldAndReturn(player2, new ShieldSphere());
        var aesthirGlider = harness.addToBattlefieldAndReturn(player2, new AesthirGlider());
        GameData gd = harness.getGameData();

        int deckBefore = gd.playerDecks.get(player1.getId()).size();
        int exileBefore = gd.exiledCards.size();
        int life1 = gd.playerLifeTotals.get(player1.getId());
        int life2 = gd.playerLifeTotals.get(player2.getId());

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore - 2);
        assertThat(gd.exiledCards).hasSize(exileBefore + 2);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(life1 - 1);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(life2 - 1);

        // Both 1/1 fliers die; the non-flying creature is untouched.
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard() instanceof WildAesthir);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard() instanceof WildAesthir)
                .anyMatch(p -> p.getCard() instanceof ShieldSphere);
        assertThat(aesthirGlider.getMarkedDamage()).isEqualTo(1);
        assertThat(shieldSphere.getMarkedDamage()).isZero();
    }

    @Test
    void paysExileCostBeforeResolution() {
        harness.addToBattlefield(player1, new WhirlingCatapult());
        int deckBefore = gd.playerDecks.get(player1.getId()).size();
        int exileBefore = gd.exiledCards.size();
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore - 2);
        assertThat(gd.exiledCards).hasSize(exileBefore + 2);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);

        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Cannot activate with fewer than two cards in library")
    void cannotActivateWithShortLibrary() {
        harness.addToBattlefield(player1, new WhirlingCatapult());
        GameData gd = harness.getGameData();
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate without paying {2}")
    void cannotActivateWithoutMana() {
        harness.addToBattlefield(player1, new WhirlingCatapult());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotPayExileCostWithOnlyOneCard() {
        harness.addToBattlefield(player1, new WhirlingCatapult());
        var remainingCard = new ShieldSphere();
        harness.setLibrary(player1, List.of(remainingCard));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        int exileBefore = gd.exiledCards.size();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remainingCard);
        assertThat(gd.exiledCards).hasSize(exileBefore);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canActivateRepeatedlyWhileTapped() {
        var catapult = harness.addToBattlefieldAndReturn(player1, new WhirlingCatapult());
        catapult.tap();
        var first = new ShieldSphere();
        var second = new WildAesthir();
        var third = new AesthirGlider();
        var fourth = new WhirlingCatapult();
        harness.setLibrary(player1, List.of(first, second, third, fourth));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        int exileBefore = gd.exiledCards.size();

        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(third, fourth);
        assertThat(gd.exiledCards).hasSize(exileBefore + 2)
                .extracting(entry -> entry.card()).contains(first, second);
        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.exiledCards).hasSize(exileBefore + 4)
                .extracting(entry -> entry.card()).contains(first, second, third, fourth);

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 18);
        assertThat(catapult.isTapped()).isTrue();
    }

    @Test
    void abilityResolvesAfterSourceLeavesBattlefield() {
        harness.addToBattlefield(player1, new WhirlingCatapult());
        harness.addToBattlefield(player2, new WildAesthir());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, null);

        gd.playerBattlefields.get(player1.getId()).clear();
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
        harness.assertLife(player2, 19);
        harness.assertInGraveyard(player2, "Wild Aesthir");
    }
}
