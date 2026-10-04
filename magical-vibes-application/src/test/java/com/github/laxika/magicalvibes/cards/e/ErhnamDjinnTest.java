package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.a.AnuridBarkripper;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WallOfStone;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AnuridBarkripper.class, ErhnamDjinn.class, Forest.class, GrizzlyBears.class, WallOfStone.class})
class ErhnamDjinnTest extends BaseCardTest {

    @Test
    @DisplayName("Upkeep trigger targets a non-Wall creature an opponent controls")
    void upkeepTriggerTargetsNonWallOpponentCreature() {
        addCreatureReady(player1, new ErhnamDjinn());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        Permanent wall = addCreatureReady(player2, new WallOfStone());

        advanceToUpkeep(player1);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(target.getId())
                .doesNotContain(wall.getId());

        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.FORESTWALK)).isTrue();
    }

    @Test
    @DisplayName("Upkeep trigger excludes your creatures and noncreature permanents")
    void upkeepTriggerExcludesOwnCreaturesAndNoncreatures() {
        addCreatureReady(player1, new ErhnamDjinn());
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        Permanent wall = addCreatureReady(player2, new WallOfStone());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());

        advanceToUpkeep(player1);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(target.getId())
                .doesNotContain(wall.getId(), ownCreature.getId(), land.getId());

        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Forestwalk expires at the beginning of the controller's next upkeep")
    void forestwalkLastsUntilNextUpkeep() {
        addCreatureReady(player1, new ErhnamDjinn());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        grantForestwalk(target);
        assertThat(gqs.hasKeyword(gd, target, Keyword.FORESTWALK)).isTrue();

        advanceToUpkeep(player2);
        assertThat(gqs.hasKeyword(gd, target, Keyword.FORESTWALK)).isTrue();

        advanceToUpkeep(player1);
        assertThat(gqs.hasKeyword(gd, target, Keyword.FORESTWALK)).isFalse();
    }

    private void grantForestwalk(Permanent target) {
        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Forestwalk expires at the beginning of the source controller's next upkeep")
    void forestwalkLastsUntilSourceControllerNextUpkeep() {
        addCreatureReady(player1, new ErhnamDjinn());
        Permanent target = addCreatureReady(player2, new AnuridBarkripper());

        grantForestwalk(target);
        assertThat(gqs.hasKeyword(gd, target, Keyword.FORESTWALK)).isTrue();

        advanceToUpkeep(player2);
        assertThat(gqs.hasKeyword(gd, target, Keyword.FORESTWALK)).isTrue();

        advanceToUpkeep(player1);
        assertThat(gqs.hasKeyword(gd, target, Keyword.FORESTWALK)).isFalse();
    }

    @Test
    @DisplayName("No target choice is offered when the opponent controls only Walls")
    void noLegalTargetDoesNotPrompt() {
        addCreatureReady(player1, new ErhnamDjinn());
        Permanent wall = addCreatureReady(player2, new WallOfStone());
        harness.addToBattlefield(player2, new Forest());

        advanceToUpkeep(player1);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gqs.hasKeyword(gd, wall, Keyword.FORESTWALK)).isFalse();
    }

    @Test
    @DisplayName("Erhnam Djinn does not trigger during an opponent's upkeep")
    void doesNotTriggerDuringOpponentUpkeep() {
        addCreatureReady(player1, new ErhnamDjinn());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        advanceToUpkeep(player2);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gqs.hasKeyword(gd, target, Keyword.FORESTWALK)).isFalse();
    }

    @Test
    @DisplayName("The trigger resolves after the Djinn leaves and still expires at its controller's next upkeep")
    void sourceLeavingDoesNotPreventOrEndGrant() {
        Permanent djinn = addCreatureReady(player1, new ErhnamDjinn());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, target.getId());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, djinn));
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.FORESTWALK)).isTrue();
        advanceToUpkeep(player2);
        assertThat(gqs.hasKeyword(gd, target, Keyword.FORESTWALK)).isTrue();
        advanceToUpkeep(player1);
        assertThat(gqs.hasKeyword(gd, target, Keyword.FORESTWALK)).isFalse();
    }

    @Test
    @DisplayName("Granted forestwalk prevents blocking only while the defender controls a Forest")
    void forestwalkDependsOnDefendersForest() {
        Permanent djinn = addCreatureReady(player1, new ErhnamDjinn());
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        grantForestwalk(attacker);

        assertThat(bls.canBlockAttacker(gd, djinn, attacker, gd.playerBattlefields.get(player1.getId()))).isTrue();
        harness.addToBattlefield(player2, new Forest());
        assertThat(bls.canBlockAttacker(gd, djinn, attacker, gd.playerBattlefields.get(player1.getId()))).isTrue();
        harness.addToBattlefield(player1, new Forest());
        assertThat(bls.canBlockAttacker(gd, djinn, attacker, gd.playerBattlefields.get(player1.getId()))).isFalse();
    }

    @Test
    @DisplayName("The trigger does not grant forestwalk when its target leaves before resolution")
    void removedTargetDoesNotReceiveGrant() {
        addCreatureReady(player1, new ErhnamDjinn());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        Permanent otherCreature = addCreatureReady(player2, new GrizzlyBears());
        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, target.getId());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, target));
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.hasKeyword(gd, otherCreature, Keyword.FORESTWALK)).isFalse();
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(target.getCard());
    }
}
