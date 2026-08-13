package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.ConsumeSpirit;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SulfuricVaporsTest extends BaseCardTest {

    @Test
    @DisplayName("A red spell deals one extra damage to a player regardless of its controller")
    void redSpellDealsExtraDamageToPlayer() {
        harness.addToBattlefield(player1, new SulfuricVapors());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.setLife(player1, 20);
        harness.forceActivePlayer(player2);

        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("A red spell deals one extra damage to a permanent")
    void redSpellDealsExtraDamageToPermanent() {
        harness.addToBattlefield(player1, new SulfuricVapors());
        Permanent target = addCreatureReady(player2, new SerraAngel());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);

        harness.castInstant(player2, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("A nonred spell is not boosted")
    void nonredSpellIsNotBoosted() {
        harness.addToBattlefield(player1, new SulfuricVapors());
        harness.setHand(player2, List.of(new ConsumeSpirit()));
        harness.addMana(player2, ManaColor.BLACK, 4);
        harness.setLife(player1, 20);
        harness.forceActivePlayer(player2);

        harness.castSorcery(player2, 0, 2, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("A red activated ability is not boosted")
    void redActivatedAbilityIsNotBoosted() {
        harness.addToBattlefield(player1, new SulfuricVapors());
        addCreatureReady(player2, new ProdigalPyromancer());
        harness.setLife(player1, 20);
        harness.forceActivePlayer(player2);

        harness.activateAbility(player2, 0, null, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
    }
}
