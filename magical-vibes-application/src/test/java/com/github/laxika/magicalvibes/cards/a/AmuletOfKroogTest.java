package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.c.CrawWurm;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AmuletOfKroog.class, GrizzlyBears.class, CrawWurm.class, LightningBolt.class})
class AmuletOfKroogTest extends BaseCardTest {

    private void addAmuletReady() {
        harness.addToBattlefieldAndReturn(player1, new AmuletOfKroog());
    }

    @Test
    @DisplayName("Prevention ability adds 1 shield to target creature")
    void preventsOnCreature() {
        addAmuletReady();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getDamagePreventionShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Prevention ability adds 1 shield to target player")
    void preventsOnPlayer() {
        addAmuletReady();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDamagePreventionShields.getOrDefault(player2.getId(), 0)).isEqualTo(1);
    }

    @Test
    @DisplayName("Prevention ability prevents the next damage dealt to the targeted player")
    void preventsNextDamageToPlayer() {
        addAmuletReady();
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.playerDamagePreventionShields.getOrDefault(player2.getId(), 0)).isZero();
    }

    @Test
    @DisplayName("Prevention ability prevents the next damage dealt to the targeted creature")
    void preventsNextDamageToCreature() {
        addAmuletReady();
        Permanent wurm = addCreatureReady(player2, new CrawWurm());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, wurm.getId());
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, wurm.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(wurm);
        assertThat(wurm.getMarkedDamage()).isEqualTo(2);
        assertThat(wurm.getDamagePreventionShield()).isZero();
    }

    @Test
    @DisplayName("Unused prevention shield expires at end of turn")
    void shieldExpiresAtEndOfTurn() {
        addAmuletReady();
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Prevention ability cannot activate without enough mana")
    void needsMana() {
        addAmuletReady();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Prevention ability cannot activate while Amulet of Kroog is tapped")
    void cannotActivateWhenTapped() {
        addAmuletReady();
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
