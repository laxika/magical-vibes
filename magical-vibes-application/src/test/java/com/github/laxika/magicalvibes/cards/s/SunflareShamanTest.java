package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.m.MudbuttonClanger;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SunflareShaman.class, Spitebellows.class, MudbuttonClanger.class})
class SunflareShamanTest extends BaseCardTest {

    @Test
    @DisplayName("Deals X to any target and X to itself, X = Elemental cards in your graveyard")
    void dealsElementalCountToTargetAndSelf() {
        Permanent shaman = addReadyShaman(player1);
        harness.setGraveyard(player1, List.of(new Spitebellows(), new Spitebellows()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        // X = 2: player2 takes 2, and the 2/1 shaman takes 2 and dies to its own damage.
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        harness.assertNotOnBattlefield(player1, "Sunflare Shaman");
        harness.assertInGraveyard(player1, "Sunflare Shaman");
    }

    @Test
    @DisplayName("Only Elemental cards in the graveyard count toward X")
    void nonElementalCardsDoNotCount() {
        addReadyShaman(player1);
        harness.setGraveyard(player1, List.of(new Spitebellows(), new MudbuttonClanger()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        // X = 1: only the Spitebellows counts. The 2/1 shaman takes 1 and dies to its own damage.
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        harness.assertNotOnBattlefield(player1, "Sunflare Shaman");
    }

    @Test
    @DisplayName("With no Elemental cards in graveyard X is 0 — no damage, source survives")
    void zeroElementalsDealsNoDamage() {
        Permanent shaman = addReadyShaman(player1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(shaman.getMarkedDamage()).isEqualTo(0);
        harness.assertOnBattlefield(player1, "Sunflare Shaman");
    }

    @Test
    @DisplayName("Elementals in an opponent's graveyard do not count toward X")
    void opponentElementalsDoNotCount() {
        Permanent shaman = addReadyShaman(player1);
        harness.setGraveyard(player2, List.of(new Spitebellows()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(shaman.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player1, "Sunflare Shaman");
    }

    @Test
    @DisplayName("Counts the Elemental cards in the graveyard when the ability resolves")
    void countsElementalsAtResolution() {
        addReadyShaman(player1);
        harness.setGraveyard(player1, List.of(new Spitebellows()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.setGraveyard(player1, List.of(new Spitebellows(), new Spitebellows()));
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        harness.assertInGraveyard(player1, "Sunflare Shaman");
    }

    @Test
    @DisplayName("Cannot activate again while the source is tapped")
    void cannotActivateAgainWhileTapped() {
        Permanent shaman = addReadyShaman(player1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(shaman.isTapped()).isTrue();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    @Test
    @DisplayName("Can target a creature — deals X damage to it")
    void dealsDamageToTargetCreature() {
        addReadyShaman(player1);
        harness.setGraveyard(player1, List.of(new Spitebellows(), new Spitebellows()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.addToBattlefield(player2, new MudbuttonClanger());
        UUID clangerId = harness.getPermanentId(player2, "Mudbutton Clanger");

        harness.activateAbility(player1, 0, null, clangerId);
        harness.passBothPriorities();

        // X = 2 kills the 1/1 Mudbutton Clanger.
        harness.assertNotOnBattlefield(player2, "Mudbutton Clanger");
        harness.assertInGraveyard(player2, "Mudbutton Clanger");
    }

    private Permanent addReadyShaman(Player player) {
        return addCreatureReady(player, new SunflareShaman());
    }
}
