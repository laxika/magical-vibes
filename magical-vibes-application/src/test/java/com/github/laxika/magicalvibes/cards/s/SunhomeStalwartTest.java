package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SunhomeStalwartTest extends BaseCardTest {

    @Test
    @DisplayName("Mentor targets only an attacking creature with lesser power")
    void mentorTargetsAttackingCreatureWithLesserPower() {
        addCreatureReady(player1, new SunhomeStalwart());
        Permanent attackingWizard = addCreatureReady(player1, new FugitiveWizard());
        Permanent nonAttackingWizard = addCreatureReady(player1, new FugitiveWizard());
        Permanent equalPowerCreature = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0, 1, 3));

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactly(attackingWizard.getId());

        harness.handlePermanentChosen(player1, attackingWizard.getId());
        resolveAllTriggers();

        assertThat(attackingWizard.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(nonAttackingWizard.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(equalPowerCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Mentor uses the source's last known power if it leaves before resolution")
    void mentorUsesSourceLastKnownPower() {
        Permanent stalwart = addCreatureReady(player1, new SunhomeStalwart());
        Permanent wizard = addCreatureReady(player1, new FugitiveWizard());

        declareAttackers(List.of(0, 1));
        harness.handlePermanentChosen(player1, wizard.getId());
        gd.playerBattlefields.get(player1.getId()).remove(stalwart);
        resolveAllTriggers();

        assertThat(wizard.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }
}
