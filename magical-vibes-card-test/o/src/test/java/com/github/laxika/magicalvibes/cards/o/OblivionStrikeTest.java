package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.w.Wastes;
import com.github.laxika.magicalvibes.cards.s.StalkingDrone;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OblivionStrike.class, Wastes.class, StalkingDrone.class})
class OblivionStrikeTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles target creature")
    void exilesTargetCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new StalkingDrone());
        prepareOblivionStrike();
        harness.castAndResolveSorcery(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player2, "Stalking Drone");
        harness.assertNotInGraveyard(player2, "Stalking Drone");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getId().equals(target.getCard().getId()));
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Wastes());

        assertThatThrownBy(() -> castOblivionStrike(target))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Fizzles if the target leaves before resolution")
    void fizzlesIfTargetLeavesBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new StalkingDrone());
        castOblivionStrike(target);
        gd.playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .noneMatch(card -> card.getId().equals(target.getCard().getId()));
    }

    @Test
    @DisplayName("Can exile a creature you control")
    void canExileOwnCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new StalkingDrone());
        prepareOblivionStrike();

        harness.castAndResolveSorcery(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player1, "Stalking Drone");
        harness.assertNotInGraveyard(player1, "Stalking Drone");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(target.getCard().getId()));
        harness.assertInGraveyard(player1, "Oblivion Strike");
        assertThat(gd.stack).isEmpty();
    }

    private void castOblivionStrike(Permanent target) {
        prepareOblivionStrike();
        harness.castSorcery(player1, 0, target.getId());
    }

    private void prepareOblivionStrike() {
        harness.setHand(player1, List.of(new OblivionStrike()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
