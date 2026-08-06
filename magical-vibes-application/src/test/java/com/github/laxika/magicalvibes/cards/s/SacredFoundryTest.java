package com.github.laxika.magicalvibes.cards.s;

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

class SacredFoundryTest extends BaseCardTest {

    @Test
    @DisplayName("Paying 2 life lets Sacred Foundry enter untapped")
    void payingLifeEntersUntapped() {
        playSacredFoundry(20);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
        assertThat(findSacredFoundry(player1).isTapped()).isFalse();
    }

    @Test
    @DisplayName("Declining the life payment makes Sacred Foundry enter tapped")
    void decliningPaymentEntersTapped() {
        playSacredFoundry(20);

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(findSacredFoundry(player1).isTapped()).isTrue();
    }

    @Test
    @DisplayName("Sacred Foundry enters tapped when its controller cannot pay 2 life")
    void insufficientLifeEntersTappedWithoutPrompt() {
        playSacredFoundry(1);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(findSacredFoundry(player1).isTapped()).isTrue();
    }

    @Test
    @DisplayName("Sacred Foundry produces red mana")
    void producesRedMana() {
        Permanent foundry = addSacredFoundryReady(player1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(foundry.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Sacred Foundry produces white mana")
    void producesWhiteMana() {
        Permanent foundry = addSacredFoundryReady(player1);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(foundry.isTapped()).isTrue();
    }

    private void playSacredFoundry(int life) {
        harness.setLife(player1, life);
        harness.setHand(player1, List.of(new SacredFoundry()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.playLand(player1, 0);
    }

    private Permanent addSacredFoundryReady(Player player) {
        Permanent foundry = new Permanent(new SacredFoundry());
        foundry.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(foundry);
        return foundry;
    }

    private Permanent findSacredFoundry(Player player) {
        return findPermanent(player, "Sacred Foundry");
    }
}
