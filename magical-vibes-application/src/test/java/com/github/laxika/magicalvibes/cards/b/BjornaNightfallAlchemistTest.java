package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BjornaNightfallAlchemist.class, GrizzlyBears.class, FountainOfYouth.class, Spellbook.class})
class BjornaNightfallAlchemistTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices an artifact, deals 1 damage, and goads the target")
    void sacrificesArtifactDealsDamageAndGoadsTarget() {
        Permanent bjorna = addReadyBjorna();
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Spellbook());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(artifact);
        assertThat(target.getMarkedDamage()).isEqualTo(1);

        assertThatThrownBy(() -> declareAttackers(player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
        assertThat(bjorna.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Only the targeted creature is goaded")
    void onlyTargetIsGoaded() {
        addReadyBjorna();
        harness.addToBattlefield(player1, new Spellbook());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        Permanent other = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        target.tap();

        int otherIndex = gd.playerBattlefields.get(player2.getId()).indexOf(other);
        declareAttackers(player2, List.of(otherIndex));

        assertThat(other.isAttackedThisTurn()).isTrue();
    }

    @Test
    @DisplayName("Goad expires at Bjorna's controller's next turn")
    void goadExpiresAtControllerNextTurn() {
        addReadyBjorna();
        harness.addToBattlefield(player1, new Spellbook());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        declareAttackers(player2, List.of());
        assertThat(target.isAttacking()).isFalse();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreaturePermanent() {
        addReadyBjorna();
        harness.addToBattlefield(player1, new Spellbook());
        Permanent artifact = addCreatureReady(player2, new FountainOfYouth());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, artifact.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyBjorna() {
        return addCreatureReady(player1, new BjornaNightfallAlchemist());
    }

    @Test
    void cannotPaySacrificeCostWithOpponentsArtifact() {
        Permanent bjorna = addReadyBjorna();
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Spellbook());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(bjorna.isTapped()).isFalse();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(artifact);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void summoningSickBjornaCannotActivate() {
        harness.addToBattlefield(player1, new BjornaNightfallAlchemist());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Spellbook());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(artifact);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canTargetItself() {
        Permanent bjorna = addReadyBjorna();
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Spellbook());

        harness.activateAbility(player1, 0, null, bjorna.getId());
        harness.passBothPriorities();

        assertThat(bjorna.getMarkedDamage()).isEqualTo(1);
        assertThat(bjorna.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(artifact);
    }

    @Test
    void abilityResolvesAndGoadPersistsAfterSourceLeavesBattlefield() {
        Permanent bjorna = addReadyBjorna();
        harness.addToBattlefield(player1, new Spellbook());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(bjorna);
        gd.playerGraveyards.get(player1.getId()).add(bjorna.getCard());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(1);
        assertThatThrownBy(() -> declareAttackers(player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

}
