package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GravityWell.class, AirElemental.class, GrizzlyBears.class})
class GravityWellTest extends BaseCardTest {

    private Permanent setUpAttack(Card attackerCard) {
        harness.addToBattlefield(player1, new GravityWell());

        Permanent attacker = harness.addToBattlefieldAndReturn(player2, attackerCard);
        attacker.setSummoningSick(false);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        return attacker;
    }

    @Test
    @DisplayName("A creature with flying triggers Gravity Well when it attacks")
    void flyingCreatureTriggersAbility() {
        Permanent attacker = setUpAttack(new AirElemental());

        gs.declareAttackers(gd, player2, List.of(0));

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(entry.getControllerId()).isEqualTo(player1.getId());
        assertThat(entry.getTargetId()).isEqualTo(attacker.getId());
    }

    @Test
    @DisplayName("Resolving Gravity Well removes flying until end of turn")
    void removesFlyingFromAttackingCreature() {
        Permanent attacker = setUpAttack(new AirElemental());

        gs.declareAttackers(gd, player2, List.of(0));
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

        assertThat(gqs.hasKeyword(gd, attacker, Keyword.FLYING)).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, attacker, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("A creature without flying does not trigger Gravity Well")
    void nonFlyingCreatureDoesNotTrigger() {
        Permanent attacker = setUpAttack(new GrizzlyBears());

        gs.declareAttackers(gd, player2, List.of(0));

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.hasKeyword(gd, attacker, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Gravity Well also removes flying from its controller's attacking creature")
    void affectsItsControllersAttacker() {
        harness.addToBattlefield(player1, new GravityWell());
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        attacker.setSummoningSick(false);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        gs.declareAttackers(gd, player1, List.of(1));

        assertThat(gd.stack).hasSize(1);
        assertThat(gqs.hasKeyword(gd, attacker, Keyword.FLYING)).isTrue();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, attacker, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Each flying attacker triggers separately and nonattacking flyers keep flying")
    void triggersSeparatelyForEachFlyingAttacker() {
        Permanent first = setUpAttack(new AirElemental());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        Permanent ground = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent nonattacker = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        second.setSummoningSick(false);
        ground.setSummoningSick(false);

        gs.declareAttackers(gd, player2, List.of(0, 1, 2));

        assertThat(gd.stack).hasSize(2);
        assertThat(gqs.hasKeyword(gd, first, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, second, Keyword.FLYING)).isTrue();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, first, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, second, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, nonattacker, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("A pending trigger resolves even after Gravity Well leaves the battlefield")
    void triggerSurvivesSourceLeavingBattlefield() {
        Permanent attacker = setUpAttack(new AirElemental());
        gs.declareAttackers(gd, player2, List.of(0));
        Permanent source = gd.playerBattlefields.get(player1.getId()).getFirst();
        gd.playerBattlefields.get(player1.getId()).remove(source);
        gd.playerGraveyards.get(player1.getId()).add(source.getCard());

        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, attacker, Keyword.FLYING)).isFalse();
    }
}
