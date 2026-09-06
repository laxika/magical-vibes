package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RealityShiftTest extends BaseCardTest {

    @Test
    void exilesTargetCreatureAndItsControllerManifestsTheirTopCard() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new RealityShift()));
        harness.setLibrary(player2, List.of(new LlanowarElves()));
        addRealityShiftMana();

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getId().equals(target.getCard().getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.isManifested() && permanent.isFaceDown());
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(Permanent::isManifested);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    void manifestsNothingForTargetControllerWithEmptyLibrary() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new RealityShift()));
        harness.setLibrary(player2, List.of());
        addRealityShiftMana();

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getId().equals(target.getCard().getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(Permanent::isManifested);
    }

    @Test
    void cannotTargetNoncreaturePermanent() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Mountain());
        harness.setHand(player1, List.of(new RealityShift()));
        addRealityShiftMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    private void addRealityShiftMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
    }
}
