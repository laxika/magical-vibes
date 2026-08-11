package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LiminalHoldTest extends BaseCardTest {

    private void prepareToCast() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new LiminalHold()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }

    private void castAndResolve(UUID targetId) {
        prepareToCast();
        harness.castEnchantment(player1, 0, targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("ETB exiles up to one target nonland permanent an opponent controls and gains 2 life")
    void etbExilesTargetAndGainsLife() {
        harness.setLife(player1, 15);
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        castAndResolve(targetId);

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(targetId));
        assertThat(gd.getPlayerExiledCards(player2.getId())).hasSize(1);
        harness.assertLife(player1, 17);
    }

    @Test
    @DisplayName("ETB may resolve with no target and still gains 2 life")
    void etbMayResolveWithNoTarget() {
        harness.setLife(player1, 15);
        prepareToCast();

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 17);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof LiminalHold);
    }

    @Test
    @DisplayName("Exiled permanent returns when Liminal Hold leaves the battlefield")
    void exiledPermanentReturnsWhenSourceLeaves() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        castAndResolve(targetId);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        UUID sourceId = harness.getPermanentId(player1, "Liminal Hold");

        harness.passPriority(player1);
        harness.castInstant(player2, 0, sourceId);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof GrizzlyBears);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Only an opponent's nonland permanent can be targeted")
    void rejectsLandAndOwnPermanentTargets() {
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID landId = harness.getPermanentId(player2, "Forest");
        UUID ownPermanentId = harness.getPermanentId(player1, "Grizzly Bears");

        prepareToCast();
        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, landId))
                .isInstanceOf(IllegalStateException.class);

        harness.setHand(player1, List.of(new LiminalHold()));
        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, ownPermanentId))
                .isInstanceOf(IllegalStateException.class);
    }
}
