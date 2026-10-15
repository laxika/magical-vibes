package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.m.MudbuttonClanger;
import com.github.laxika.magicalvibes.cards.m.MothdustChangeling;
import com.github.laxika.magicalvibes.model.CounterType;
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

@CardUsed({SunflareShaman.class, Spitebellows.class, MudbuttonClanger.class, MothdustChangeling.class})
class SunflareShamanTest extends BaseCardTest {

    @Test
    @DisplayName("Deals X to any target and X to itself, X = Elemental cards in your graveyard")
    void dealsElementalCountToTargetAndSelf() {
        addReadyShaman(player1);
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

        UUID clangerId = harness.addToBattlefieldAndReturn(player2, new MudbuttonClanger()).getId();

        harness.activateAbility(player1, 0, null, clangerId);
        harness.passBothPriorities();

        // X = 2 kills the 1/1 Mudbutton Clanger.
        harness.assertNotOnBattlefield(player2, "Mudbutton Clanger");
        harness.assertInGraveyard(player2, "Mudbutton Clanger");
    }

    @Test
    void changelingInGraveyardCountsAsElemental() {
        addReadyShaman(player1);
        harness.setGraveyard(player1, List.of(new MothdustChangeling()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
        harness.assertInGraveyard(player1, "Sunflare Shaman");
    }

    @Test
    void targetingItselfDealsBothAmounts() {
        Permanent shaman = addReadyShaman(player1);
        shaman.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        harness.setGraveyard(player1, List.of(new Spitebellows()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, shaman.getId());
        harness.passBothPriorities();

        assertThat(shaman.getMarkedDamage()).isEqualTo(2);
        harness.assertOnBattlefield(player1, "Sunflare Shaman");
    }

    @Test
    void cannotActivateWithSummoningSickness() {
        harness.addToBattlefield(player1, new SunflareShaman());
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
    }

    @Test
    void cannotActivateWithoutRedMana() {
        Permanent shaman = addReadyShaman(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
        assertThat(shaman.isTapped()).isFalse();
    }

    @Test
    void illegalTargetPreventsSelfDamageToo() {
        Permanent shaman = addReadyShaman(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new MudbuttonClanger());
        harness.setGraveyard(player1, List.of(new Spitebellows()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        harness.setGraveyard(player2, List.of(target.getCard()));
        harness.passBothPriorities();

        assertThat(shaman.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player1, "Sunflare Shaman");
    }

    @Test
    void sourceInGraveyardStillDealsDamageAndCountsItself() {
        Permanent shaman = addReadyShaman(player1);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, player2.getId());
        gd.playerBattlefields.get(player1.getId()).remove(shaman);
        harness.setGraveyard(player1, List.of(shaman.getCard()));
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
        harness.assertInGraveyard(player1, "Sunflare Shaman");
    }

    private Permanent addReadyShaman(Player player) {
        return addCreatureReady(player, new SunflareShaman());
    }
}
