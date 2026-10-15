package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.a.AvatarOfMight;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.Recover;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
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

@CardUsed({WiltInTheHeat.class, GrizzlyBears.class, AvatarOfMight.class})
class WiltInTheHeatTest extends BaseCardTest {

    @Test
    @DisplayName("Kills the creature and exiles it instead of putting it into the graveyard")
    void killsAndExilesCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.setHand(player1, List.of(new WiltInTheHeat()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.GREEN, 2); // 2 generic (no cost reduction)

        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        // Exiled, not in the graveyard
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.exiledCards).anyMatch(e -> e.card().getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Marks a surviving creature so a later death this turn exiles it")
    void marksSurvivorForExile() {
        Permanent avatar = harness.addToBattlefieldAndReturn(player2, new AvatarOfMight());
        UUID targetId = harness.getPermanentId(player2, "Avatar of Might");
        harness.setHand(player1, List.of(new WiltInTheHeat()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAndResolveInstant(player1, 0, targetId);

        // Avatar (8/8) survives 5 damage but is flagged to be exiled if it dies this turn
        assertThat(avatar.getMarkedDamage()).isEqualTo(5);
        assertThat(avatar.isExileInsteadOfDieThisTurn()).isTrue();
    }

    @Test
    @DisplayName("Costs {2} less when a card left the graveyard this turn")
    void costReducedWhenCardLeftGraveyard() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.setHand(player1, List.of(new WiltInTheHeat()));
        // Only {R}{W} available, enough only if reduced by {2}
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        gd.playersWhoseCardsLeftGraveyardThisTurn.add(player1.getId());

        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot be cast with only {R}{W} when no card left the graveyard")
    void notCastableWithoutReduction() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.setHand(player1, List.of(new WiltInTheHeat()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void laterSacrificeExilesTheSurvivingCreature() {
        Permanent avatar = harness.addToBattlefieldAndReturn(player2, new AvatarOfMight());
        harness.setHand(player1, List.of(new WiltInTheHeat()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0, avatar.getId());
        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().sacrificePermanentToGraveyard(gd, avatar));

        harness.assertNotOnBattlefield(player2, "Avatar of Might");
        harness.assertNotInGraveyard(player2, "Avatar of Might");
        assertThat(gd.exiledCards).anyMatch(e -> e.card().getId().equals(avatar.getCard().getId()));
    }

    @Test
    void exileReplacementExpiresAfterTheTurn() {
        Permanent avatar = harness.addToBattlefieldAndReturn(player2, new AvatarOfMight());
        harness.setHand(player1, List.of(new WiltInTheHeat()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0, avatar.getId());
        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().sacrificePermanentToGraveyard(gd, avatar));

        harness.assertInGraveyard(player2, "Avatar of Might");
        assertThat(gd.exiledCards).noneMatch(e -> e.card().getId().equals(avatar.getCard().getId()));
    }

    @Test
    @CardUsed({Recover.class})
    void returningACardFromYourGraveyardEnablesTheReduction() {
        GrizzlyBears returned = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(returned));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new Recover(), new WiltInTheHeat()));
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()).getId();
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveSorcery(player1, 0, returned.getId());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.exiledCards).anyMatch(e -> e.card().getName().equals("Grizzly Bears"));
    }

    @Test
    void opponentsGraveyardDoesNotEnableTheReduction() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()).getId();
        harness.setHand(player1, List.of(new WiltInTheHeat()));
        gd.playersWhoseCardsLeftGraveyardThisTurn.add(player2.getId());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void reductionDoesNotRemoveColoredManaRequirements() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()).getId();
        harness.setHand(player1, List.of(new WiltInTheHeat()));
        gd.playersWhoseCardsLeftGraveyardThisTurn.add(player1.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canTargetYourOwnCreature() {
        UUID targetId = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()).getId();
        harness.setHand(player1, List.of(new WiltInTheHeat()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.exiledCards).anyMatch(e -> e.card().getName().equals("Grizzly Bears"));
    }

    @Test
    @CardUsed({Unsummon.class})
    void targetLeavingBattlefieldMakesTheSpellFailToResolve() {
        GrizzlyBears bear = new GrizzlyBears();
        UUID targetId = harness.addToBattlefieldAndReturn(player2, bear).getId();
        harness.setHand(player1, List.of(new WiltInTheHeat()));
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0, targetId);
        harness.castAndResolveInstant(player2, 0, targetId);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).contains(bear);
        harness.assertInGraveyard(player1, "Wilt in the Heat");
        assertThat(gd.exiledCards).isEmpty();
        assertThat(gd.stack).isEmpty();
    }
}
