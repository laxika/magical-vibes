package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.l.LingeringSouls;
import com.github.laxika.magicalvibes.cards.s.SavingGrasp;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheTwelfthDoctor.class, LingeringSouls.class, SavingGrasp.class})
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

    @Test
    void bothPlayersGetCopiesButOnlyTheirOwnCopyGrowsTheirDoctor() {
        Permanent doctor = addDoctor();
        Permanent opposingDoctor = addCreatureReady(player2, new TheTwelfthDoctor());
        harness.setGraveyard(player1, List.of(new LingeringSouls()));
        addFlashbackMana();

        harness.castFromGraveyard(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Spirit")).isEqualTo(4);
        assertThat(countPermanents(player2, "Spirit")).isEqualTo(2);
        assertThat(doctor.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(opposingDoctor.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void decliningFirstDemonstrateDoesNotGiveSecondSpellDemonstrate() {
        Permanent doctor = addDoctor();
        harness.setGraveyard(player1, List.of(new LingeringSouls(), new LingeringSouls()));
        addFlashbackMana();
        harness.castFromGraveyard(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        addFlashbackMana();
        harness.castFromGraveyard(player1, 0);
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(countPermanents(player1, "Spirit")).isEqualTo(4);
        assertThat(countPermanents(player2, "Spirit")).isZero();
        assertThat(doctor.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void earlierOutsideHandSpellCountsEvenBeforeDoctorEnters() {
        harness.forceActivePlayer(player1);
        harness.setGraveyard(player1, List.of(new LingeringSouls(), new LingeringSouls()));
        addFlashbackMana();
        harness.castFromGraveyard(player1, 0);
        resolveAllTriggers();
        Permanent doctor = addDoctor();

        addFlashbackMana();
        harness.castFromGraveyard(player1, 0);
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(countPermanents(player1, "Spirit")).isEqualTo(4);
        assertThat(doctor.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void spellCastFromHandDoesNotConsumeDemonstrate() {
        Permanent doctor = addDoctor();
        harness.setHand(player1, List.of(new LingeringSouls()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castSorcery(player1, 0);
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        addFlashbackMana();
        harness.castFromGraveyard(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Spirit")).isEqualTo(6);
        assertThat(countPermanents(player2, "Spirit")).isEqualTo(2);
        assertThat(doctor.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void demonstrateOffersNewTargetsForTargetedSpellCopy() {
        Permanent doctor = addDoctor();
        harness.setGraveyard(player1, List.of(new SavingGrasp()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castFlashback(player1, 0, doctor.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.isAwaitingInput())
                .as("Demonstrate must let the copying player choose new targets before making the opponent's copy")
                .isTrue();
    }

    private Permanent addDoctor() {
        Permanent doctor = addCreatureReady(player1, new TheTwelfthDoctor());
        harness.forceActivePlayer(player1);
        return doctor;
    }

    private void addFlashbackMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
