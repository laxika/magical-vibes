package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PortTownTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped when you have no Plains or Island card in hand")
    void entersTappedWithoutPlainsOrIsland() {
        harness.setHand(player1, List.of(new PortTown(), new Forest()));
        playLand();

        assertThat(findLand(player1).isTapped()).isTrue();
    }

    @Test
    @DisplayName("Revealing a Plains lets it enter untapped")
    void entersUntappedWhenRevealingPlains() {
        harness.setHand(player1, List.of(new PortTown(), new Plains()));
        playLand();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(findLand(player1).isTapped()).isFalse();
    }

    @Test
    @DisplayName("Revealing an Island lets it enter untapped")
    void entersUntappedWhenRevealingIsland() {
        harness.setHand(player1, List.of(new PortTown(), new Island()));
        playLand();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(findLand(player1).isTapped()).isFalse();
    }

    @Test
    @DisplayName("Declining to reveal makes it enter tapped")
    void entersTappedWhenDeclining() {
        harness.setHand(player1, List.of(new PortTown(), new Plains()));
        playLand();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(findLand(player1).isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping for white mana produces one white")
    void tappingProducesWhiteMana() {
        addLandReady(player1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping for blue mana produces one blue")
    void tappingProducesBlueMana() {
        addLandReady(player1);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    private void playLand() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.playLand(player1, 0);
    }

    private void addLandReady(Player player) {
        Permanent permanent = new Permanent(new PortTown());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
    }

    private Permanent findLand(Player player) {
        return findPermanent(player, "Port Town");
    }
}
