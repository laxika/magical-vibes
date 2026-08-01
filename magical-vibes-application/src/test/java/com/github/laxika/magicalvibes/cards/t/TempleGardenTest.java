package com.github.laxika.magicalvibes.cards.t;

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

class TempleGardenTest extends BaseCardTest {

    @Test
    @DisplayName("Paying 2 life lets Temple Garden enter untapped")
    void payingLifeEntersUntapped() {
        playTempleGarden(20);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
        assertThat(findGarden(player1).isTapped()).isFalse();
    }

    @Test
    @DisplayName("Declining the life payment makes Temple Garden enter tapped")
    void decliningPaymentEntersTapped() {
        playTempleGarden(20);

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(findGarden(player1).isTapped()).isTrue();
    }

    @Test
    @DisplayName("Temple Garden enters tapped when its controller cannot pay 2 life")
    void insufficientLifeEntersTappedWithoutPrompt() {
        playTempleGarden(1);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(findGarden(player1).isTapped()).isTrue();
    }

    @Test
    @DisplayName("Temple Garden produces green mana")
    void producesGreenMana() {
        Permanent garden = addGardenReady(player1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(garden.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Temple Garden produces white mana")
    void producesWhiteMana() {
        Permanent garden = addGardenReady(player1);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(garden.isTapped()).isTrue();
    }

    private void playTempleGarden(int life) {
        harness.setLife(player1, life);
        harness.setHand(player1, List.of(new TempleGarden()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.playLand(player1, 0);
    }

    private Permanent addGardenReady(Player player) {
        Permanent garden = new Permanent(new TempleGarden());
        garden.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(garden);
        return garden;
    }

    private Permanent findGarden(Player player) {
        return findPermanent(player, "Temple Garden");
    }
}
