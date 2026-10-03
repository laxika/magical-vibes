package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.AvatarOfMight;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.InfernalGrasp;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BurnTheAccursed.class, AvatarOfMight.class, GrizzlyBears.class, InfernalGrasp.class})
class BurnTheAccursedTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 5 damage, exiles a dying creature, and deals 2 damage to its controller")
    void killsAndExilesCreatureAndDamagesController() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.setHand(player1, List.of(new BurnTheAccursed()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.setLife(player2, 20);

        harness.castAndResolveInstant(player1, 0, targetId);

        GameData gd = harness.getGameData();
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.exiledCards).anyMatch(e -> e.card().getName().equals("Grizzly Bears"));
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Marks a surviving creature for exile if it dies later this turn")
    void marksSurvivingCreatureForExile() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AvatarOfMight());
        UUID targetId = harness.getPermanentId(player2, "Avatar of Might");
        harness.setHand(player1, List.of(new BurnTheAccursed()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.setLife(player2, 20);

        harness.castAndResolveInstant(player1, 0, targetId);

        assertThat(target.getMarkedDamage()).isEqualTo(5);
        assertThat(target.isExileInsteadOfDieThisTurn()).isTrue();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Cannot target a player")
    void cannotTargetPlayer() {
        harness.setHand(player1, List.of(new BurnTheAccursed()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void exilesSurvivorDestroyedLaterInTheSameTurn() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new AvatarOfMight()).getId();
        harness.setHand(player1, List.of(new BurnTheAccursed(), new InfernalGrasp()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveInstant(player1, 0, targetId);
        harness.assertOnBattlefield(player2, "Avatar of Might");
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Avatar of Might");
        harness.assertNotInGraveyard(player2, "Avatar of Might");
        assertThat(gd.exiledCards).anyMatch(e -> e.card().getName().equals("Avatar of Might"));
    }

    @Test
    void replacementExpiresAfterTheTurn() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new AvatarOfMight()).getId();
        harness.setHand(player1, List.of(new BurnTheAccursed(), new InfernalGrasp()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Avatar of Might");
        harness.assertInGraveyard(player2, "Avatar of Might");
        assertThat(gd.exiledCards).noneMatch(e -> e.card().getName().equals("Avatar of Might"));
    }

    @Test
    void illegalTargetAtResolutionPreventsControllerDamage() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()).getId();
        harness.setHand(player1, List.of(new BurnTheAccursed(), new InfernalGrasp()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.setLife(player2, 20);

        harness.castInstant(player1, 0, targetId);
        harness.castAndResolveInstant(player1, 0, targetId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.exiledCards).noneMatch(e -> e.card().getName().equals("Grizzly Bears"));
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    void canTargetOwnCreatureAndDamagesItsController() {
        UUID targetId = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()).getId();
        harness.setHand(player1, List.of(new BurnTheAccursed()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.exiledCards).anyMatch(e -> e.card().getName().equals("Grizzly Bears"));
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }
}
