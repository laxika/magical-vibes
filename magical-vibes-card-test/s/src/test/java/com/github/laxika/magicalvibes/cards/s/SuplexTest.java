package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AvatarOfMight;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.cards.t.Terror;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Suplex.class, AvatarOfMight.class, GrizzlyBears.class, Millstone.class, Terror.class, Unsummon.class})
class SuplexTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 3 damage and exiles a creature that dies this turn")
    void damagesAndExilesCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Suplex()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castModalSorcery(player1, 0, 0, List.of(harness.getPermanentId(player2, "Grizzly Bears")));
        harness.passBothPriorities();

        GameData gameData = harness.getGameData();
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        assertThat(gameData.exiledCards).anyMatch(exiled -> exiled.card().getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Marks a surviving creature for exile if it dies this turn")
    void marksSurvivingCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AvatarOfMight());
        harness.setHand(player1, List.of(new Suplex()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castModalSorcery(player1, 0, 0, List.of(target.getId()));
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(3);
        assertThat(target.isExileInsteadOfDieThisTurn()).isTrue();
    }

    @Test
    @DisplayName("Exiles a target artifact")
    void exilesArtifact() {
        harness.addToBattlefield(player2, new Millstone());
        harness.setHand(player1, List.of(new Suplex()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castModalSorcery(player1, 0, 1, List.of(harness.getPermanentId(player2, "Millstone")));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Millstone");
        harness.assertNotInGraveyard(player2, "Millstone");
        assertThat(harness.getGameData().exiledCards)
                .anyMatch(exiled -> exiled.card().getName().equals("Millstone"));
    }

    @Test
    @DisplayName("Each mode only accepts its legal target type")
    void modesRejectIllegalTargets() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Millstone());

        harness.setHand(player1, List.of(new Suplex()));
        harness.addMana(player1, ManaColor.RED, 2);
        assertThatThrownBy(() -> harness.castModalSorcery(player1, 0, 0, List.of(artifact.getId())))
                .isInstanceOf(IllegalStateException.class);

        harness.setHand(player1, List.of(new Suplex()));
        assertThatThrownBy(() -> harness.castModalSorcery(player1, 0, 1, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A surviving creature is exiled when destroyed later that turn")
    void exilesCreatureDestroyedLaterThisTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AvatarOfMight());
        harness.setHand(player1, List.of(new Suplex(), new Terror()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castModalSorcery(player1, 0, 0, List.of(target.getId()));
        harness.passBothPriorities();
        harness.assertOnBattlefield(player2, "Avatar of Might");

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player2, "Avatar of Might");
        harness.assertNotInGraveyard(player2, "Avatar of Might");
        assertThat(gd.exiledCards).anyMatch(exiled -> exiled.card() == target.getCard());
    }

    @Test
    @DisplayName("The exile replacement expires at the end of the turn")
    void replacementExpiresAfterTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AvatarOfMight());
        harness.setHand(player1, List.of(new Suplex(), new Terror()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castModalSorcery(player1, 0, 0, List.of(target.getId()));
        harness.passBothPriorities();
        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player2, "Avatar of Might");
        harness.assertInGraveyard(player2, "Avatar of Might");
        assertThat(gd.exiledCards).noneMatch(exiled -> exiled.card() == target.getCard());
    }

    @Test
    @DisplayName("The replacement does not exile a creature returned to hand")
    void markedCreatureCanReturnToHand() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AvatarOfMight());
        harness.setHand(player1, List.of(new Suplex(), new Unsummon()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castModalSorcery(player1, 0, 0, List.of(target.getId()));
        harness.passBothPriorities();
        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player2, "Avatar of Might");
        harness.assertInHand(player2, "Avatar of Might");
        assertThat(gd.exiledCards).noneMatch(exiled -> exiled.card() == target.getCard());
    }

    @Test
    @DisplayName("Suplex does not affect a creature that leaves before resolution")
    void targetLeavingBeforeResolutionIsUnaffected() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Suplex()));
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castModalSorcery(player1, 0, 0, List.of(target.getId()));
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, target.getId());
        harness.passBothPriorities();

        harness.assertInHand(player2, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.exiledCards).noneMatch(exiled -> exiled.card() == target.getCard());
        harness.assertInGraveyard(player1, "Suplex");
    }
}
