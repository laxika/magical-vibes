package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DevilthornFox;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.t.ThrabenGargoyle;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StructuralDistortion.class, ThrabenGargoyle.class, DevilthornFox.class, Island.class, Plains.class})
class StructuralDistortionTest extends BaseCardTest {

    @Test
    void exilesTargetArtifactAndDealsDamageToItsController() {
        harness.addToBattlefield(player2, new ThrabenGargoyle());
        harness.setHand(player1, List.of(new StructuralDistortion()));
        harness.addMana(player1, ManaColor.RED, 4);

        UUID targetId = harness.getPermanentId(player2, "Thraben Gargoyle");
        harness.castAndResolveSorcery(player1, 0, targetId);

        GameData gd = harness.getGameData();
        harness.assertNotOnBattlefield(player2, "Thraben Gargoyle");
        assertThat(gd.exiledCards).anyMatch(e -> e.card().getName().equals("Thraben Gargoyle"));
        harness.assertLife(player2, 18);
    }

    @Test
    void exilesTargetLandAndDealsDamageToItsController() {
        harness.addToBattlefield(player2, new Island());
        harness.setHand(player1, List.of(new StructuralDistortion()));
        harness.addMana(player1, ManaColor.RED, 4);

        UUID targetId = harness.getPermanentId(player2, "Island");
        harness.castAndResolveSorcery(player1, 0, targetId);

        GameData gd = harness.getGameData();
        harness.assertNotOnBattlefield(player2, "Island");
        assertThat(gd.exiledCards).anyMatch(e -> e.card().getName().equals("Island"));
        harness.assertLife(player2, 18);
    }

    @Test
    void cannotTargetNonArtifactCreature() {
        harness.addToBattlefield(player2, new DevilthornFox());
        harness.setHand(player1, List.of(new StructuralDistortion()));
        harness.addMana(player1, ManaColor.RED, 4);

        UUID targetId = harness.getPermanentId(player2, "Devilthorn Fox");
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("artifact or land");
    }

    @Test
    void doesNothingIfTargetLeavesBeforeResolution() {
        harness.addToBattlefield(player2, new Plains());
        harness.setHand(player1, List.of(new StructuralDistortion()));
        harness.addMana(player1, ManaColor.RED, 4);

        UUID targetId = harness.getPermanentId(player2, "Plains");
        harness.castSorcery(player1, 0, targetId);
        harness.getGameData().playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        harness.assertLife(player2, 20);
    }

    @Test
    void canExileOwnLandAndDealsDamageOnlyToItsController() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Plains());
        harness.setHand(player1, List.of(new StructuralDistortion()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castAndResolveSorcery(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player1, "Plains");
        assertThat(gd.exiledCards).anyMatch(e -> e.card().getId().equals(target.getCard().getId()));
        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Structural Distortion");
    }

    @Test
    void damagesControllerAtResolutionRatherThanOwnerOrControllerAtCasting() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.setHand(player1, List.of(new StructuralDistortion()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castSorcery(player1, 0, target.getId());

        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerBattlefields.get(player1.getId()).add(target);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Island");
        harness.assertNotOnBattlefield(player2, "Island");
        assertThat(gd.exiledCards).anyMatch(e -> e.card().getId().equals(target.getCard().getId()));
        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
    }
}
