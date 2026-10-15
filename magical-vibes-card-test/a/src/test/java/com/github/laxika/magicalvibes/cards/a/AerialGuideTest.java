package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.FrilledSandwalla;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AerialGuide.class, FrilledSandwalla.class})
class AerialGuideTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking grants flying to another attacking creature")
    void grantsFlyingToAnotherAttackingCreature() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        addCreatureReady(player1, new AerialGuide());
        Permanent otherAttacker = addCreatureReady(player1, new FrilledSandwalla());

        declareAttackers(player1, List.of(0, 1));

        harness.handlePermanentChosen(player1, otherAttacker.getId());
        harness.passBothPriorities();

        assertThat(otherAttacker.getGrantedKeywords()).contains(Keyword.FLYING);
    }

    @Test
    @DisplayName("Granted flying wears off at end of turn")
    void flyingWearsOffAtEndOfTurn() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        addCreatureReady(player1, new AerialGuide());
        Permanent otherAttacker = addCreatureReady(player1, new FrilledSandwalla());

        declareAttackers(player1, List.of(0, 1));

        harness.handlePermanentChosen(player1, otherAttacker.getId());
        harness.passBothPriorities();

        assertThat(otherAttacker.getGrantedKeywords()).contains(Keyword.FLYING);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(otherAttacker.getGrantedKeywords()).doesNotContain(Keyword.FLYING);
    }

    @Test
    @DisplayName("Cannot target itself")
    void cannotTargetItself() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        Permanent aerialGuide = addCreatureReady(player1, new AerialGuide());
        addCreatureReady(player1, new FrilledSandwalla());

        declareAttackers(player1, List.of(0, 1));

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, aerialGuide.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a non-attacking creature")
    void cannotTargetNonAttackingCreature() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        addCreatureReady(player1, new AerialGuide());
        addCreatureReady(player1, new FrilledSandwalla());
        Permanent nonAttacker = addCreatureReady(player1, new FrilledSandwalla());

        // Only Aerial Guide (0) and the first Frilled Sandwalla (1) attack; the third stays back.
        declareAttackers(player1, List.of(0, 1));

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, nonAttacker.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Attacking alone leaves no legal target and no pending choice")
    void attackingAloneHasNoLegalTarget() {
        Permanent guide = addCreatureReady(player1, new AerialGuide());

        declareAttackers(player1, List.of(0));

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(guide.getGrantedKeywords()).doesNotContain(Keyword.FLYING);
    }

    @Test
    @DisplayName("Other creatures attacking does not trigger a Guide that stays back")
    void doesNotTriggerWhenGuideDoesNotAttack() {
        addCreatureReady(player1, new AerialGuide());
        Permanent attacker = addCreatureReady(player1, new FrilledSandwalla());

        declareAttackers(player1, List.of(1));

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(attacker.getGrantedKeywords()).doesNotContain(Keyword.FLYING);
    }

    @Test
    @DisplayName("Target that stops attacking before resolution does not gain flying")
    void targetMustStillBeAttackingOnResolution() {
        addCreatureReady(player1, new AerialGuide());
        Permanent attacker = addCreatureReady(player1, new FrilledSandwalla());
        declareAttackers(player1, List.of(0, 1));
        harness.handlePermanentChosen(player1, attacker.getId());

        attacker.setAttacking(false);
        harness.passBothPriorities();

        assertThat(attacker.getGrantedKeywords()).doesNotContain(Keyword.FLYING);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Attack trigger resolves even after Aerial Guide leaves the battlefield")
    void triggerResolvesAfterSourceLeavesBattlefield() {
        Permanent guide = addCreatureReady(player1, new AerialGuide());
        Permanent attacker = addCreatureReady(player1, new FrilledSandwalla());
        declareAttackers(player1, List.of(0, 1));
        harness.handlePermanentChosen(player1, attacker.getId());

        gd.playerBattlefields.get(player1.getId()).remove(guide);
        gd.playerGraveyards.get(player1.getId()).add(guide.getCard());
        harness.passBothPriorities();

        assertThat(attacker.getGrantedKeywords()).contains(Keyword.FLYING);
    }
}
