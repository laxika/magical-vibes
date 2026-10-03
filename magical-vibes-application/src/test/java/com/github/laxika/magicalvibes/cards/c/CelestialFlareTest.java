package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CelestialFlare.class, GrizzlyBears.class, GiantSpider.class})
class CelestialFlareTest extends BaseCardTest {

    @Test
    @DisplayName("Target player's lone blocking creature is sacrificed")
    void loneBlockerIsSacrificed() {
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);

        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new CelestialFlare()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Target player's lone attacking creature is sacrificed")
    void loneAttackerIsSacrificed() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new CelestialFlare()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.passPriority(player2);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Creatures that are neither attacking nor blocking are untouched")
    void nonCombatCreaturesAreSafe() {
        Permanent idle = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        idle.setSummoningSick(false);

        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new CelestialFlare()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(harness.getGameData().stack).isEmpty();
    }

    @Test
    @DisplayName("Target player chooses which of several blockers to sacrifice")
    void targetPlayerChoosesAmongBlockers() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        bears.setSummoningSick(false);
        bears.setBlocking(true);
        Permanent spider = harness.addToBattlefieldAndReturn(player2, new GiantSpider());
        spider.setSummoningSick(false);
        spider.setBlocking(true);

        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new CelestialFlare()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        GameData gd = harness.getGameData();
        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.context()).isInstanceOf(MultiPermanentChoiceContext.ForcedSacrifice.class);

        harness.handleMultiplePermanentsChosen(player2, List.of(spider.getId()));

        harness.assertNotOnBattlefield(player2, "Giant Spider");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Giant Spider");
    }

    @Test
    @DisplayName("The caster can target themselves and sacrifice their own attacker")
    void canTargetSelf() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new GiantSpider());
        attacker.setAttacking(true);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new CelestialFlare()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveInstant(player1, 0, player1.getId());

        harness.assertNotOnBattlefield(player1, "Giant Spider");
        harness.assertInGraveyard(player1, "Giant Spider");
    }

    @Test
    @DisplayName("The target chooses exactly one attacker and cannot choose an idle creature")
    void targetPlayerChoosesAmongAttackers() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GiantSpider());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GiantSpider());
        Permanent idle = harness.addToBattlefieldAndReturn(player2, new GiantSpider());
        first.setAttacking(true);
        second.setAttacking(true);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new CelestialFlare()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.passPriority(player2);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        PendingInteraction.MultiPermanentChoice choice = harness.getGameData().interaction
                .activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.validIds()).containsExactlyInAnyOrder(first.getId(), second.getId());
        assertThat(choice.maxCount()).isEqualTo(1);
        harness.handleMultiplePermanentsChosen(player2, List.of(second.getId()));

        assertThat(harness.getGameData().playerBattlefields.get(player2.getId()))
                .contains(first, idle).doesNotContain(second);
        assertThat(harness.getGameData().playerGraveyards.get(player2.getId()))
                .containsExactly(second.getCard());
    }

    @Test
    @DisplayName("A creature that leaves combat before resolution is not sacrificed")
    void checksCombatStatusAtResolution() {
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new GiantSpider());
        blocker.setBlocking(true);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new CelestialFlare()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castInstant(player1, 0, player2.getId());
        blocker.setBlocking(false);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Giant Spider");
        harness.assertNotInGraveyard(player2, "Giant Spider");
        assertThat(harness.getGameData().stack).isEmpty();
    }

    @Test
    @DisplayName("A surviving blocker can be sacrificed during the end of combat step")
    void canSacrificeDuringEndOfCombat() {
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new GiantSpider());
        blocker.setBlocking(true);
        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new CelestialFlare()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertNotOnBattlefield(player2, "Giant Spider");
        harness.assertInGraveyard(player2, "Giant Spider");
    }
}
