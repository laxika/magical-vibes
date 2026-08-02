package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.p.ProdigalSorcerer;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HumilityTest extends BaseCardTest {

    @Test
    @DisplayName("Creatures both players control become base 1/1 and lose their keywords")
    void allCreaturesBecomeVanillaOneOnes() {
        Permanent ownElemental = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        Permanent enemyElemental = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        resolveHumility();

        assertThat(gqs.getEffectivePower(gd, ownElemental)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, ownElemental)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, ownElemental, Keyword.FLYING)).isFalse();
        assertThat(gqs.getEffectivePower(gd, enemyElemental)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, enemyElemental)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, enemyElemental, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Creatures entering after Humility resolved are also neutered")
    void laterCreaturesAreAlsoNeutered() {
        resolveHumility();
        Permanent elemental = harness.addToBattlefieldAndReturn(player2, new AirElemental());

        assertThat(gqs.getEffectivePower(gd, elemental)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, elemental, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("+1/+1 counters still apply on top of the 1/1 base (layer 7d after 7b)")
    void countersApplyOnTopOfBase() {
        Permanent elemental = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        elemental.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        resolveHumility();

        assertThat(gqs.getEffectivePower(gd, elemental)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, elemental)).isEqualTo(3);
    }

    @Test
    @DisplayName("An activated ability of a creature can no longer be activated")
    void creatureActivatedAbilityIsStripped() {
        harness.addToBattlefield(player1, new ProdigalSorcerer());
        resolveHumility();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Creatures return to normal once Humility leaves the battlefield")
    void effectEndsWhenHumilityLeaves() {
        Permanent elemental = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        Permanent humility = resolveHumility();

        assertThat(gqs.getEffectivePower(gd, elemental)).isEqualTo(1);

        gd.playerBattlefields.get(player1.getId()).remove(humility);

        assertThat(gqs.getEffectivePower(gd, elemental)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, elemental)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, elemental, Keyword.FLYING)).isTrue();
    }

    /** Casts and resolves Humility for player1, returning the resulting battlefield permanent. */
    private Permanent resolveHumility() {
        harness.setHand(player1, List.of(new Humility()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Humility"))
                .findFirst()
                .orElseThrow();
    }
}
