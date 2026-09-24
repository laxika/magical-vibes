package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GoblinBrigand;
import com.github.laxika.magicalvibes.cards.s.Stabilizer;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MisguidedRage.class, GoblinBrigand.class, Stabilizer.class})
class MisguidedRageTest extends BaseCardTest {

    @Test
    void targetPlayerSacrificesTheirOnlyPermanent() {
        harness.addToBattlefield(player2, new GoblinBrigand());
        castMisguidedRage(player2.getId());

        harness.assertNotOnBattlefield(player2, "Goblin Brigand");
        harness.assertInGraveyard(player2, "Goblin Brigand");
        harness.assertInGraveyard(player1, "Misguided Rage");
    }

    @Test
    void controllerCanBeTheTargetPlayer() {
        harness.addToBattlefield(player1, new GoblinBrigand());
        castMisguidedRage(player1.getId());

        harness.assertNotOnBattlefield(player1, "Goblin Brigand");
        harness.assertInGraveyard(player1, "Goblin Brigand");
        harness.assertInGraveyard(player1, "Misguided Rage");
    }

    @Test
    void targetPlayerWithoutPermanentsIsUnaffected() {
        castMisguidedRage(player2.getId());

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Misguided Rage");
    }

    @Test
    void targetPlayerChoosesPermanentToSacrifice() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GoblinBrigand());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GoblinBrigand());
        castMisguidedRage(player2.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        harness.handleMultiplePermanentsChosen(player2, List.of(first.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(second);
        harness.assertInGraveyard(player2, "Goblin Brigand");
    }

    @Test
    void targetPlayerMayChooseANoncreaturePermanent() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GoblinBrigand());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Stabilizer());
        castMisguidedRage(player2.getId());

        harness.handleMultiplePermanentsChosen(player2, List.of(artifact.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(creature);
        harness.assertInGraveyard(player2, "Stabilizer");
    }

    @Test
    void cannotTargetPermanent() {
        Permanent permanent = harness.addToBattlefieldAndReturn(player2, new GoblinBrigand());
        harness.setHand(player1, List.of(new MisguidedRage()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, permanent.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castMisguidedRage(java.util.UUID targetId) {
        harness.setHand(player1, List.of(new MisguidedRage()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();
    }
}
