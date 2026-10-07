package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.ColossalDreadmaw;
import com.github.laxika.magicalvibes.cards.c.ColossalRattlewurm;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.r.ReturnTheFavor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ThunderSalvo.class, LightningBolt.class, ColossalDreadmaw.class,
        ColossalRattlewurm.class, ReturnTheFavor.class})
class ThunderSalvoTest extends BaseCardTest {

    @Test
    void dealsTwoPlusOtherSpellsCastThisTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ColossalDreadmaw());
        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt(), new ThunderSalvo()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(4);
    }

    @Test
    void cannotTargetAPlayer() {
        harness.setHand(player1, List.of(new ThunderSalvo()));
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void firstSpellDealsTwoDamage() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ColossalRattlewurm());
        harness.setHand(player1, List.of(new ThunderSalvo()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    void countsSpellsCastInResponseAndStillOnTheStack() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new ColossalRattlewurm());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new ColossalRattlewurm());
        harness.setHand(player1, List.of(new ThunderSalvo(), new ThunderSalvo()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castInstant(player1, 0, first.getId());
        harness.castInstant(player1, 0, second.getId());
        harness.passBothPriorities();
        assertThat(second.getMarkedDamage()).isEqualTo(3);
        harness.passBothPriorities();
        assertThat(first.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    void doesNotCountOpponentsSpells() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new ColossalRattlewurm());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new ColossalRattlewurm());
        harness.setHand(player2, List.of(new ThunderSalvo()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.castInstant(player2, 0, first.getId());
        harness.passBothPriorities();
        harness.setHand(player1, List.of(new ThunderSalvo()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, second.getId());
        harness.passBothPriorities();

        assertThat(first.getMarkedDamage()).isEqualTo(2);
        assertThat(second.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    void copyCountsOriginalSpellAndCopyingSpell() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ColossalRattlewurm());
        ThunderSalvo salvo = new ThunderSalvo();
        harness.setHand(player1, List.of(salvo, new ReturnTheFavor()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.castInstant(player1, 0, target.getId());
        harness.castModalInstantWithModes(player1, 0, 1, 2, new int[]{0}, List.of(salvo.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(4);
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
    }

    @Test
    void copyOfOpponentsSpellCountsOnlyCopyControllersSpells() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ColossalRattlewurm());
        ThunderSalvo salvo = new ThunderSalvo();
        harness.setHand(player2, List.of(salvo));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.castInstant(player2, 0, target.getId());
        harness.setHand(player1, List.of(new ReturnTheFavor()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castModalInstantWithModes(player1, 0, 1, 2, new int[]{0}, List.of(salvo.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(3);
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
    }
}
