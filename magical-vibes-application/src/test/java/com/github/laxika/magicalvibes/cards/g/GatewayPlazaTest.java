package com.github.laxika.magicalvibes.cards.g;

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

class GatewayPlazaTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield tapped")
    void entersTapped() {
        playGatewayPlaza();

        assertThat(findPlaza(player1)).isNotNull();
        assertThat(findPlaza(player1).isTapped()).isTrue();
    }

    @Test
    @DisplayName("Paying {1} keeps Gateway Plaza on the battlefield")
    void payingKeepsIt() {
        playGatewayPlaza();
        resolveEnterTrigger();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(findPlaza(player1)).isNotNull();
        harness.assertNotInGraveyard(player1, "Gateway Plaza");
    }

    @Test
    @DisplayName("Declining to pay {1} sacrifices Gateway Plaza")
    void decliningSacrificesIt() {
        playGatewayPlaza();
        resolveEnterTrigger();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(findPlaza(player1)).isNull();
        harness.assertInGraveyard(player1, "Gateway Plaza");
    }

    @Test
    @DisplayName("Tap ability adds one mana of the chosen color")
    void tapAddsChosenColorMana() {
        Permanent plaza = addPlazaReady(player1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(plaza.isTapped()).isTrue();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);

        harness.handleListChoice(player1, "RED");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void playGatewayPlaza() {
        harness.setHand(player1, List.of(new GatewayPlaza()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.playLand(player1, 0);
    }

    private void resolveEnterTrigger() {
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private Permanent addPlazaReady(Player player) {
        Permanent plaza = new Permanent(new GatewayPlaza());
        plaza.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(plaza);
        return plaza;
    }

    private Permanent findPlaza(Player player) {
        return findPermanents(player, "Gateway Plaza").stream().findFirst().orElse(null);
    }
}
