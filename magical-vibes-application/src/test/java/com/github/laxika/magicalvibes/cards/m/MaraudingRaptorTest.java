package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.c.ColossalDreadmaw;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MaraudingRaptorTest extends BaseCardTest {

    @Test
    void creatureSpellsCostOneLess() {
        harness.addToBattlefield(player1, new MaraudingRaptor());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void dinosaurEnteringIsDamagedAndBoostsMaraudingRaptor() {
        harness.addToBattlefield(player1, new MaraudingRaptor());
        Permanent raptor = findPermanent(player1, "Marauding Raptor");
        harness.setHand(player1, List.of(new ColossalDreadmaw()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent dreadmaw = findPermanent(player1, "Colossal Dreadmaw");
        assertThat(dreadmaw.getMarkedDamage()).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, raptor)).isEqualTo(4);
    }

    @Test
    void nonDinosaurEnteringIsDamagedWithoutBoostingMaraudingRaptor() {
        harness.addToBattlefield(player1, new MaraudingRaptor());
        Permanent raptor = findPermanent(player1, "Marauding Raptor");
        harness.setHand(player1, List.of(new HillGiant()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent hillGiant = findPermanent(player1, "Hill Giant");
        assertThat(hillGiant.getMarkedDamage()).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, raptor)).isEqualTo(2);
    }

    @Test
    void costReductionDoesNotApplyToOpponent() {
        harness.addToBattlefield(player1, new MaraudingRaptor());
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castCreature(player2, 0))
                .isInstanceOf(IllegalStateException.class);
    }
}
