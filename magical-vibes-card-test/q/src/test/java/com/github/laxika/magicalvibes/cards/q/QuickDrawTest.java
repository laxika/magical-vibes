package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.b.BenalishKnight;
import com.github.laxika.magicalvibes.cards.f.FencingAce;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({QuickDraw.class, GrizzlyBears.class, BenalishKnight.class, FencingAce.class})
class QuickDrawTest extends BaseCardTest {

    @Test
    @DisplayName("Boosts a creature you control and removes first and double strike from an opponent's creatures")
    void appliesBothTargetedEffects() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent firstStrikeCreature = harness.addToBattlefieldAndReturn(player2, new BenalishKnight());
        Permanent doubleStrikeCreature = harness.addToBattlefieldAndReturn(player2, new FencingAce());

        castQuickDraw(ownCreature, player2.getId());

        assertThat(ownCreature.getEffectivePower()).isEqualTo(3);
        assertThat(ownCreature.getEffectiveToughness()).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, firstStrikeCreature, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, doubleStrikeCreature, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, doubleStrikeCreature, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("The temporary effects wear off at end of turn")
    void effectsWearOffAtEndOfTurn() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new FencingAce());

        castQuickDraw(ownCreature, player2.getId());
        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(ownCreature.getEffectivePower()).isEqualTo(2);
        assertThat(ownCreature.getEffectiveToughness()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Requires a creature you control and an opponent as targets")
    void rejectsIllegalTargets() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new QuickDraw()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                List.of(opponentCreature.getId(), player2.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature you control");

        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                List.of(ownCreature.getId(), player1.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("opponent");
    }

    @Test
    @DisplayName("Still removes strike abilities when the creature target leaves before resolution")
    void resolvesForOpponentWhenCreatureTargetLeaves() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent firstStrikeCreature = harness.addToBattlefieldAndReturn(player2, new BenalishKnight());
        Permanent doubleStrikeCreature = harness.addToBattlefieldAndReturn(player2, new FencingAce());
        harness.setHand(player1, List.of(new QuickDraw()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, List.of(ownCreature.getId(), player2.getId()));

        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, ownCreature);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, firstStrikeCreature, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, doubleStrikeCreature, Keyword.DOUBLE_STRIKE)).isFalse();
        harness.assertInGraveyard(player1, "Quick Draw");
    }

    @Test
    @DisplayName("Only creatures controlled by the targeted opponent at resolution lose strike abilities")
    void doesNotAffectOwnOrLaterEnteringCreatures() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent ownFirstStrikeCreature = harness.addToBattlefieldAndReturn(player1, new BenalishKnight());
        Permanent ownDoubleStrikeCreature = harness.addToBattlefieldAndReturn(player1, new FencingAce());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new FencingAce());

        castQuickDraw(ownCreature, player2.getId());
        Permanent laterFirstStrikeCreature = harness.addToBattlefieldAndReturn(player2, new BenalishKnight());
        Permanent laterDoubleStrikeCreature = harness.addToBattlefieldAndReturn(player2, new FencingAce());

        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.DOUBLE_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, ownFirstStrikeCreature, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, ownDoubleStrikeCreature, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, laterFirstStrikeCreature, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, laterDoubleStrikeCreature, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("A later first strike grant works after Quick Draw removes strike abilities")
    void laterGrantRestoresFirstStrike() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new FencingAce());
        castQuickDraw(ownCreature, player2.getId());
        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.DOUBLE_STRIKE)).isFalse();

        harness.setHand(player2, List.of(new QuickDraw()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, List.of(opponentCreature.getId(), player1.getId()));

        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.DOUBLE_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.FIRST_STRIKE)).isFalse();
    }

    private void castQuickDraw(Permanent ownCreature, java.util.UUID opponentId) {
        harness.setHand(player1, List.of(new QuickDraw()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, List.of(ownCreature.getId(), opponentId));
    }
}
