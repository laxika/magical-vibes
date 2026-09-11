package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.SpinedWurm;
import com.github.laxika.magicalvibes.cards.w.WhiptailWurm;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

@CardUsed({VampiricFeast.class, HillGiant.class, SpinedWurm.class, WhiptailWurm.class})
class VampiricFeastTest extends BaseCardTest {

    @Test
    @DisplayName("Vampiric Feast deals 4 damage to target player and controller gains 4 life")
    void deals4DamageToPlayerAndGains4Life() {
        harness.setHand(player1, List.of(new VampiricFeast()));
        harness.addMana(player1, ManaColor.BLACK, 7);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        harness.assertLife(player2, 16);
        harness.assertLife(player1, 24);
    }

    @Test
    @DisplayName("Vampiric Feast deals 4 damage to target creature, destroying a 3/3, and gains life")
    void deals4DamageToCreatureDestroysItAndGainsLife() {
        harness.addToBattlefield(player2, new HillGiant());
        harness.setHand(player1, List.of(new VampiricFeast()));
        harness.addMana(player1, ManaColor.BLACK, 7);
        harness.setLife(player1, 20);

        UUID targetId = harness.getPermanentId(player2, "Hill Giant");
        harness.castAndResolveSorcery(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Hill Giant");
        harness.assertLife(player1, 24);
    }

    @Test
    @DisplayName("Vampiric Feast destroys a creature with toughness exactly 4 and gains life")
    void destroysCreatureWithToughnessExactlyFour() {
        harness.addToBattlefield(player2, new SpinedWurm());
        harness.setHand(player1, List.of(new VampiricFeast()));
        harness.addMana(player1, ManaColor.BLACK, 7);
        harness.setLife(player1, 20);

        UUID targetId = harness.getPermanentId(player2, "Spined Wurm");
        harness.castAndResolveSorcery(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Spined Wurm");
        harness.assertLife(player1, 24);
    }

    @Test
    @DisplayName("Vampiric Feast does not destroy a creature with toughness greater than 4")
    void doesNotDestroyToughCreature() {
        harness.addToBattlefield(player2, new WhiptailWurm());
        harness.setHand(player1, List.of(new VampiricFeast()));
        harness.addMana(player1, ManaColor.BLACK, 7);

        UUID targetId = harness.getPermanentId(player2, "Whiptail Wurm");
        harness.castAndResolveSorcery(player1, 0, targetId);

        harness.assertOnBattlefield(player2, "Whiptail Wurm");
    }
}
