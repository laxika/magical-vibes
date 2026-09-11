package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BoilingBlood.class, BenalishKnight.class})
class BoilingBloodTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving forces the target to attack this turn if able and draws a card")
    void resolvingForcesAttackAndDraws() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BenalishKnight());
        harness.setHand(player1, List.of(new BoilingBlood()));
        harness.setLibrary(player1, List.of(new BenalishKnight()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.isMustAttackThisTurn()).isTrue();
        // No specific defender is required, unlike Alluring Siren.
        assertThat(target.getMustAttackTargetId()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertInGraveyard(player1, "Boiling Blood");
    }

    @Test
    @DisplayName("Can target your own creature")
    void canTargetOwnCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new BenalishKnight());
        harness.setHand(player1, List.of(new BoilingBlood()));
        harness.setLibrary(player1, List.of(new BenalishKnight()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.isMustAttackThisTurn()).isTrue();
    }

    @Test
    @DisplayName("The requirement wears off at end of turn")
    void requirementWearsOff() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BenalishKnight());
        harness.setHand(player1, List.of(new BoilingBlood()));
        harness.setLibrary(player1, List.of(new BenalishKnight()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.isMustAttackThisTurn()).isFalse();
    }

    @Test
    @DisplayName("Fizzles if the target leaves the battlefield before resolution")
    void fizzlesIfTargetRemoved() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BenalishKnight());
        harness.setHand(player1, List.of(new BoilingBlood()));
        harness.setLibrary(player1, List.of(new BenalishKnight()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castInstant(player1, 0, target.getId());
        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Requires the target to attack when it is able")
    void requiresTargetToAttackWhenAble() {
        Permanent target = addCreatureReady(player2, new BenalishKnight());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.setHand(player1, List.of(new BoilingBlood()));
        harness.setLibrary(player1, List.of(new BenalishKnight()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThatThrownBy(() -> declareAttackers(player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");

        declareAttackers(player2, List.of(0));

        assertThat(target.isAttackedThisTurn()).isTrue();
    }

    @Test
    @DisplayName("Does not require the target to attack when it cannot attack")
    void doesNotRequireUnableTargetToAttack() {
        Permanent target = addCreatureReady(player2, new BenalishKnight());
        target.tap();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.setHand(player1, List.of(new BoilingBlood()));
        harness.setLibrary(player1, List.of(new BenalishKnight()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveInstant(player1, 0, target.getId());

        declareAttackers(player2, List.of());

        assertThat(target.isAttackedThisTurn()).isFalse();
        assertThat(target.isMustAttackThisTurn()).isTrue();
    }
}
