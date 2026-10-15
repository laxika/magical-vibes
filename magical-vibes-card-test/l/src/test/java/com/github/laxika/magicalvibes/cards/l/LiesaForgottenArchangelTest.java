package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.d.DayOfJudgment;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.ReassemblingSkeleton;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.t.TurnToFrog;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LiesaForgottenArchangel.class, GrizzlyBears.class, Shock.class,
        TurnToFrog.class, ReassemblingSkeleton.class, DayOfJudgment.class})
class LiesaForgottenArchangelTest extends BaseCardTest {

    private Permanent addTokenCreature(Player player) {
        Card tokenCard = new Card();
        tokenCard.setName("Bear Token");
        tokenCard.setType(CardType.CREATURE);
        tokenCard.setManaCost("");
        tokenCard.setToken(true);
        tokenCard.setColor(CardColor.GREEN);
        tokenCard.setPower(2);
        tokenCard.setToughness(2);
        tokenCard.setSubtypes(List.of(CardSubtype.BEAR));
        return harness.addToBattlefieldAndReturn(player, tokenCard);
    }

    private boolean isExiled(String cardName) {
        return gd.exiledCards.stream().anyMatch(e -> e.card().getName().equals(cardName));
    }

    @Test
    @DisplayName("Own nontoken creature returns when the next end step's delayed trigger resolves")
    void ownNontokenCreatureReturnsAtEndStep() {
        harness.addToBattlefield(player1, new LiesaForgottenArchangel());
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID bearsPermId = harness.getPermanentId(player1, "Grizzly Bears");

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, bearsPermId);

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.stack).anyMatch(e ->
                e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && e.getCard().getName().equals("Liesa, Forgotten Archangel"));

        harness.passBothPriorities();
        harness.assertNotInHand(player1, "Grizzly Bears");
        harness.passUntil(TurnStep.END_STEP);
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotInHand(player1, "Grizzly Bears");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Own token creature dying does not trigger the delayed return")
    void ownTokenCreatureDoesNotTrigger() {
        harness.addToBattlefield(player1, new LiesaForgottenArchangel());
        Permanent token = addTokenCreature(player1);

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, token.getId());

        assertThat(gd.stack).noneMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY);
        harness.passUntil(TurnStep.END_STEP);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Liesa itself dying does not trigger its own return")
    void liesaItselfDoesNotTrigger() {
        harness.addToBattlefield(player1, new LiesaForgottenArchangel());
        UUID liesaPermId = harness.getPermanentId(player1, "Liesa, Forgotten Archangel");

        harness.setHand(player2, List.of(new Shock(), new Shock(), new Shock()));
        harness.addMana(player2, ManaColor.RED, 3);
        harness.castAndResolveInstant(player2, 0, liesaPermId);
        harness.castAndResolveInstant(player2, 0, liesaPermId);
        harness.castAndResolveInstant(player2, 0, liesaPermId);

        harness.assertInGraveyard(player1, "Liesa, Forgotten Archangel");
        assertThat(gd.stack).isEmpty();
        harness.passUntil(TurnStep.END_STEP);
        assertThat(gd.stack).isEmpty();
        harness.assertNotInHand(player1, "Liesa, Forgotten Archangel");
    }

    @Test
    @DisplayName("Opponent's dying creature is exiled instead of going to its graveyard")
    void opponentCreatureIsExiledInsteadOfDying() {
        harness.addToBattlefield(player1, new LiesaForgottenArchangel());
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsPermId = harness.getPermanentId(player2, "Grizzly Bears");

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, bearsPermId);

        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        assertThat(isExiled("Grizzly Bears")).isTrue();
    }

    @Test
    @DisplayName("Without Liesa, an opponent's dying creature goes to its graveyard normally")
    void withoutLiesaOpponentCreatureGoesToGraveyard() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsPermId = harness.getPermanentId(player2, "Grizzly Bears");

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, bearsPermId);

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(isExiled("Grizzly Bears")).isFalse();
    }

    @Test
    @DisplayName("Liesa losing all abilities stops her exile replacement")
    void lostAbilitiesStopExileReplacement() {
        harness.addToBattlefield(player1, new LiesaForgottenArchangel());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player2, List.of(new TurnToFrog(), new Shock()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0,
                harness.getPermanentId(player1, "Liesa, Forgotten Archangel"));
        harness.castAndResolveInstant(player2, 0,
                harness.getPermanentId(player2, "Grizzly Bears"));

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(isExiled("Grizzly Bears")).isFalse();
    }

    @Test
    @DisplayName("A simultaneous destruction returns allies and exiles opponents even when Liesa dies")
    void simultaneousDeathsUseLiesasAbilitiesBeforeSheLeaves() {
        harness.addToBattlefield(player1, new LiesaForgottenArchangel());
        GrizzlyBears ownBears = new GrizzlyBears();
        GrizzlyBears opposingBears = new GrizzlyBears();
        harness.addToBattlefield(player1, ownBears);
        harness.addToBattlefield(player2, opposingBears);
        harness.setHand(player1, List.of(new DayOfJudgment()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Liesa, Forgotten Archangel");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.exiledCards).anyMatch(e -> e.card().getId().equals(opposingBears.getId()));
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.passUntil(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(ownBears);
        harness.assertNotInHand(player1, "Liesa, Forgotten Archangel");
        harness.assertNotInHand(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Opposing Liesas exile a creature without triggering its controller's Liesa")
    void opposingLiesasPreventDeathTrigger() {
        harness.addToBattlefield(player1, new LiesaForgottenArchangel());
        harness.addToBattlefield(player2, new LiesaForgottenArchangel());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0,
                harness.getPermanentId(player1, "Grizzly Bears"));

        assertThat(isExiled("Grizzly Bears")).isTrue();
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.stack).isEmpty();
        harness.passUntil(TurnStep.END_STEP);
        assertThat(gd.stack).isEmpty();
        harness.assertNotInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("An unresolved death trigger cannot track a creature through another graveyard entry")
    void originalDeathTriggerDoesNotReturnNewGraveyardObject() {
        harness.addToBattlefield(player1, new LiesaForgottenArchangel());
        harness.addToBattlefield(player1, new ReassemblingSkeleton());
        harness.setHand(player2, List.of(new Shock(), new TurnToFrog(), new Shock()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player2, 0,
                harness.getPermanentId(player1, "Reassembling Skeleton"));
        assertThat(gd.stack).hasSize(1);

        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Reassembling Skeleton");
        assertThat(gd.stack).hasSize(1);

        harness.castAndResolveInstant(player2, 0,
                harness.getPermanentId(player1, "Liesa, Forgotten Archangel"));
        harness.castAndResolveInstant(player2, 0,
                harness.getPermanentId(player1, "Reassembling Skeleton"));
        harness.assertInGraveyard(player1, "Reassembling Skeleton");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.passUntil(TurnStep.END_STEP);
        if (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }
        harness.assertInGraveyard(player1, "Reassembling Skeleton");
        harness.assertNotInHand(player1, "Reassembling Skeleton");
    }

    @Test
    @DisplayName("A controlled creature returns to its owner's hand rather than its controller's")
    void controlledCreatureReturnsToOwnersHand() {
        harness.addToBattlefield(player1, new LiesaForgottenArchangel());
        GrizzlyBears bears = new GrizzlyBears();
        bears.setOwnerId(player2.getId());
        Permanent stolenBears = harness.addToBattlefieldAndReturn(player1, bears);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, stolenBears.getId());
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.passBothPriorities();
        harness.passUntil(TurnStep.END_STEP);
        harness.passBothPriorities();

        harness.assertInHand(player2, "Grizzly Bears");
        harness.assertNotInHand(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("A delayed return does not move a creature that has left the graveyard")
    void delayedReturnDoesNotMoveReanimatedCreature() {
        harness.addToBattlefield(player1, new LiesaForgottenArchangel());
        harness.addToBattlefield(player1, new ReassemblingSkeleton());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0,
                harness.getPermanentId(player1, "Reassembling Skeleton"));
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();
        harness.passUntil(TurnStep.END_STEP);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Reassembling Skeleton");
        harness.assertNotInHand(player1, "Reassembling Skeleton");
        harness.assertNotInGraveyard(player1, "Reassembling Skeleton");
    }

    @Test
    @DisplayName("A creature dying after the end step begins waits until the following end step")
    void deathDuringEndStepReturnsAtFollowingEndStep() {
        harness.addToBattlefield(player1, new LiesaForgottenArchangel());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player2, List.of(new Shock()));
        harness.passUntil(TurnStep.END_STEP);
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0,
                harness.getPermanentId(player1, "Grizzly Bears"));
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotInHand(player1, "Grizzly Bears");

        harness.passUntilWithNoAttackers(player2, TurnStep.END_STEP);
        harness.assertNotInHand(player1, "Grizzly Bears");
        harness.passBothPriorities();
        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }
}
