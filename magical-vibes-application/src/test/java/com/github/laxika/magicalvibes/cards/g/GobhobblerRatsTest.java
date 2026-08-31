package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GobhobblerRats.class, GrizzlyBears.class})
class GobhobblerRatsTest extends BaseCardTest {

    @Test
    void hellbentBoostsGobhobblerRats() {
        Permanent rats = addRatsReady();
        harness.setHand(player1, List.of());

        assertThat(gqs.getEffectivePower(gd, rats)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, rats)).isEqualTo(2);

        harness.setHand(player1, List.of(new GrizzlyBears()));

        assertThat(gqs.getEffectivePower(gd, rats)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, rats)).isEqualTo(2);
    }

    @Test
    void hellbentGrantsRegenerationAbility() {
        Permanent rats = addRatsReady();
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(rats.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    void regenerationAbilityIsUnavailableWithCardsInHand() {
        addRatsReady();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addRatsReady() {
        Permanent rats = new Permanent(new GobhobblerRats());
        rats.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(rats);
        return rats;
    }
}
