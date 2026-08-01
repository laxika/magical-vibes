package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BloodCryptTest extends BaseCardTest {

    @Test
    @DisplayName("Paying 2 life lets Blood Crypt enter untapped")
    void payingLifeEntersUntapped() {
        playBloodCrypt(20);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
        assertThat(findCrypt(player1).isTapped()).isFalse();
    }

    @Test
    @DisplayName("Declining the life payment makes Blood Crypt enter tapped")
    void decliningPaymentEntersTapped() {
        playBloodCrypt(20);

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(findCrypt(player1).isTapped()).isTrue();
    }

    @Test
    @DisplayName("Blood Crypt enters tapped when its controller cannot pay 2 life")
    void insufficientLifeEntersTappedWithoutPrompt() {
        playBloodCrypt(1);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(findCrypt(player1).isTapped()).isTrue();
    }

    @Test
    @DisplayName("Blood Crypt produces black mana")
    void producesBlackMana() {
        Permanent crypt = addCryptReady(player1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(crypt.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Blood Crypt produces red mana")
    void producesRedMana() {
        Permanent crypt = addCryptReady(player1);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(crypt.isTapped()).isTrue();
    }

    private void playBloodCrypt(int life) {
        harness.setLife(player1, life);
        harness.setHand(player1, List.of(new BloodCrypt()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.playLand(player1, 0);
    }

    private Permanent addCryptReady(Player player) {
        Permanent crypt = new Permanent(new BloodCrypt());
        crypt.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(crypt);
        return crypt;
    }

    private Permanent findCrypt(Player player) {
        return findPermanent(player, "Blood Crypt");
    }
}
