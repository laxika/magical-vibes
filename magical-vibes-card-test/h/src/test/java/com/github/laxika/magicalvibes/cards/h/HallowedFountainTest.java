package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.e.EverybodyLives;
import com.github.laxika.magicalvibes.cards.f.Farseek;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HallowedFountain.class, Farseek.class, EverybodyLives.class})
class HallowedFountainTest extends BaseCardTest {

    @Test
    @DisplayName("Paying 2 life lets Hallowed Fountain enter untapped")
    void payingLifeEntersUntapped() {
        playHallowedFountain(20);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
        assertThat(findFountain(player1).isTapped()).isFalse();
    }

    @Test
    @DisplayName("Declining the life payment makes Hallowed Fountain enter tapped")
    void decliningPaymentEntersTapped() {
        playHallowedFountain(20);

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(findFountain(player1).isTapped()).isTrue();
    }

    @Test
    @DisplayName("Hallowed Fountain enters tapped when its controller cannot pay 2 life")
    void insufficientLifeEntersTappedWithoutPrompt() {
        playHallowedFountain(1);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(findFountain(player1).isTapped()).isTrue();
    }

    @Test
    @DisplayName("Paying exactly 2 life lets Hallowed Fountain enter untapped")
    void payingExactLifeTotalEntersUntapped() {
        playHallowedFountain(2);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getLife(player1.getId())).isZero();
        assertThat(findFountain(player1).isTapped()).isFalse();
    }

    @Test
    @DisplayName("Hallowed Fountain produces white mana")
    void producesWhiteMana() {
        Permanent fountain = addCreatureReady(player1, new HallowedFountain());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(fountain.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Hallowed Fountain produces blue mana")
    void producesBlueMana() {
        Permanent fountain = addCreatureReady(player1, new HallowedFountain());

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(fountain.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Paying life also works when Hallowed Fountain is put onto the battlefield")
    void putOntoBattlefieldPayingLifeEntersUntapped() {
        harness.setLife(player1, 20);

        Permanent fountain = harness.enterBattlefieldAndReturn(player1, new HallowedFountain());
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
        assertThat(fountain.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Declining payment also applies when Hallowed Fountain is put onto the battlefield")
    void putOntoBattlefieldDecliningPaymentEntersTapped() {
        harness.setLife(player1, 20);

        Permanent fountain = harness.enterBattlefieldAndReturn(player1, new HallowedFountain());
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(fountain.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Farseek puts Hallowed Fountain onto the battlefield tapped even when life is paid")
    void farseekKeepsFountainTappedAfterPayment() {
        harness.setLife(player1, 20);
        harness.setLibrary(player1, List.of(new HallowedFountain()));
        harness.setHand(player1, List.of(new Farseek()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleCardChosen(player1, 0);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
        assertThat(findFountain(player1).isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Farseek");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Hallowed Fountain enters tapped when Everybody Lives prevents paying life")
    void cannotLoseLifeEntersTappedWithoutPaymentChoice() {
        harness.setHand(player1, List.of(new EverybodyLives()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0);

        playHallowedFountain(20);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(findFountain(player1).isTapped()).isTrue();
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }

    private void playHallowedFountain(int life) {
        harness.setLife(player1, life);
        harness.setHand(player1, List.of(new HallowedFountain()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.playLand(player1, 0);
    }

    private Permanent findFountain(Player player) {
        return findPermanent(player, "Hallowed Fountain");
    }
}
