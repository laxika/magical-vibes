package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AkkiBlizzardHerderTest extends BaseCardTest {

    private void setupCombatWhereHerderDies() {
        Permanent perm = findPermanent(player1, "Akki Blizzard-Herder");
        perm.setSummoningSick(false);
        perm.setAttacking(true);

        GrizzlyBears bigBear = new GrizzlyBears();
        bigBear.setPower(3);
        bigBear.setToughness(3);
        Permanent blockerPerm = new Permanent(bigBear);
        blockerPerm.setSummoningSick(false);
        blockerPerm.setBlocking(true);
        blockerPerm.addBlockingTarget(0);
        gd.playerBattlefields.get(player2.getId()).add(blockerPerm);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
    }

    @Test
    @DisplayName("When it dies, each player with exactly one land loses it")
    void eachPlayerWithOneLandLosesIt() {
        harness.addToBattlefield(player1, new AkkiBlizzardHerder());
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player2, new Forest());

        setupCombatWhereHerderDies();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Mountain");
        harness.assertNotOnBattlefield(player2, "Forest");
    }

    @Test
    @DisplayName("A player with multiple lands chooses which one to sacrifice")
    void playerWithMultipleLandsChooses() {
        harness.addToBattlefield(player1, new AkkiBlizzardHerder());
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new Mountain());

        setupCombatWhereHerderDies();
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.maxCount()).isEqualTo(1);
        assertThat(choice.context()).isInstanceOf(MultiPermanentChoiceContext.ForcedSacrifice.class);

        Permanent forest = findPermanent(player2, "Forest");
        harness.handleMultiplePermanentsChosen(player2, List.of(forest.getId()));

        harness.assertNotOnBattlefield(player2, "Forest");
        harness.assertOnBattlefield(player2, "Mountain");
    }

    @Test
    @DisplayName("Non-land permanents are never sacrificed")
    void nonLandPermanentsAreNotSacrificed() {
        harness.addToBattlefield(player1, new AkkiBlizzardHerder());
        harness.addToBattlefield(player2, new GrizzlyBears());

        setupCombatWhereHerderDies();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Trigger only fires on death, not while it is on the battlefield")
    void noSacrificeWhileAlive() {
        harness.addToBattlefield(player1, new AkkiBlizzardHerder());
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player2, new Forest());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Mountain");
        harness.assertOnBattlefield(player2, "Forest");
    }
}
