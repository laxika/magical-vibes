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

class SteamVentsTest extends BaseCardTest {

    @Test
    @DisplayName("Paying 2 life lets Steam Vents enter untapped")
    void payingLifeEntersUntapped() {
        playSteamVents(20);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
        assertThat(findSteamVents(player1).isTapped()).isFalse();
    }

    @Test
    @DisplayName("Declining the life payment makes Steam Vents enter tapped")
    void decliningPaymentEntersTapped() {
        playSteamVents(20);

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(findSteamVents(player1).isTapped()).isTrue();
    }

    @Test
    @DisplayName("Steam Vents enters tapped when its controller cannot pay 2 life")
    void insufficientLifeEntersTappedWithoutPrompt() {
        playSteamVents(1);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(findSteamVents(player1).isTapped()).isTrue();
    }

    @Test
    @DisplayName("Steam Vents produces blue mana")
    void producesBlueMana() {
        Permanent steamVents = addSteamVentsReady(player1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(steamVents.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Steam Vents produces red mana")
    void producesRedMana() {
        Permanent steamVents = addSteamVentsReady(player1);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(steamVents.isTapped()).isTrue();
    }

    private void playSteamVents(int life) {
        harness.setLife(player1, life);
        harness.setHand(player1, List.of(new SteamVents()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.playLand(player1, 0);
    }

    private Permanent addSteamVentsReady(Player player) {
        Permanent steamVents = new Permanent(new SteamVents());
        steamVents.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(steamVents);
        return steamVents;
    }

    private Permanent findSteamVents(Player player) {
        return findPermanent(player, "Steam Vents");
    }
}
