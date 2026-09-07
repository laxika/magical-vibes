package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AvatarOfMight;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Suplex.class, AvatarOfMight.class, GrizzlyBears.class, Millstone.class})
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
}
