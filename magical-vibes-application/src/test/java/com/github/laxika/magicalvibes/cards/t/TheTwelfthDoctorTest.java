package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.l.LingeringSouls;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheTwelfthDoctor.class, LingeringSouls.class})
class TheTwelfthDoctorTest extends BaseCardTest {

    @Test
    void firstSpellCastFromOutsideHandHasDemonstrate() {
        Permanent doctor = addDoctor();
        harness.setGraveyard(player1, List.of(new LingeringSouls()));
        addFlashbackMana();

        harness.castFromGraveyard(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(doctor.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void acceptedDemonstrateCopiesSpellAndDoctorGrowsWhenItCopies() {
        Permanent doctor = addDoctor();
        harness.setGraveyard(player1, List.of(new LingeringSouls()));
        addFlashbackMana();

        harness.castFromGraveyard(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(doctor.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getCard)
                .filteredOn(card -> card.getName().equals("Spirit"))
                .hasSize(4);
    }

    private Permanent addDoctor() {
        Permanent doctor = new Permanent(new TheTwelfthDoctor());
        doctor.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(doctor);
        harness.forceActivePlayer(player1);
        return doctor;
    }

    private void addFlashbackMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
