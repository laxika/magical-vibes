package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FathomFleetFirebrand;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SunglassesOfUrzaTest extends BaseCardTest {

    @Test
    @DisplayName("A red spell can be cast paying its {R} with white mana")
    void castsRedSpellPayingRedWithWhite() {
        harness.addToBattlefield(player1, new SunglassesOfUrza());
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, bearsId);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(bearsId));
    }

    @Test
    @DisplayName("Only the white mana actually needed for the red pip is spent as red")
    void spendsOnlyTheWhiteNeededForTheRedPip() {
        harness.addToBattlefield(player1, new SunglassesOfUrza());
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castInstant(player1, 0, bearsId);

        ManaPool pool = gd.playerManaPools.get(player1.getId());
        assertThat(pool.get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(pool.get(ManaColor.RED)).isEqualTo(0);
    }

    @Test
    @DisplayName("A red spell is playable with only white mana while Sunglasses is in play")
    void redSpellPlayableWithOnlyWhiteWhenPresent() {
        harness.addToBattlefield(player1, new SunglassesOfUrza());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        Card shock = gd.playerHands.get(player1.getId()).getFirst();
        ManaPool pool = gd.playerManaPools.get(player1.getId());

        assertThat(harness.getGameBroadcastService()
                .isCardPlayable(gd, player1.getId(), shock, pool, 0)).isTrue();
    }

    @Test
    @DisplayName("Without Sunglasses, white mana cannot make a red spell playable")
    void redSpellNotPlayableWithOnlyWhiteWithoutSunglasses() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        Card shock = gd.playerHands.get(player1.getId()).getFirst();
        ManaPool pool = gd.playerManaPools.get(player1.getId());

        assertThat(harness.getGameBroadcastService()
                .isCardPlayable(gd, player1.getId(), shock, pool, 0)).isFalse();
    }

    @Test
    @DisplayName("A {1}{R} activated ability can be paid entirely with white mana")
    void activatesRedAbilityPayingWithWhite() {
        Permanent firebrand = addReadyFirebrand(player1);
        harness.addToBattlefield(player1, new SunglassesOfUrza());
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(firebrand.getEffectivePower()).isEqualTo(3);
    }

    @Test
    @DisplayName("Without Sunglasses, white mana cannot pay a {1}{R} ability's red pip")
    void cannotPayRedAbilityWithWhiteWithoutSunglasses() {
        addReadyFirebrand(player1);
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    private Permanent addReadyFirebrand(Player player) {
        Permanent perm = new Permanent(new FathomFleetFirebrand());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
