package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ReigningVictor.class, FountainOfYouth.class, GrizzlyBears.class})
class ReigningVictorTest extends BaseCardTest {

    @Test
    @DisplayName("ETB gives target creature +1/+0 and indestructible until end of turn")
    void etbBoostsTargetCreatureAndGrantsIndestructible() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ReigningVictor()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castCreature(player1, 0, List.of(bears.getId()));
        resolveAllTriggers();

        assertThat(bears.getPowerModifier()).isEqualTo(1);
        assertThat(bears.getToughnessModifier()).isZero();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.INDESTRUCTIBLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bears.getPowerModifier()).isZero();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Mobilize creates a tapped and attacking Warrior token")
    void attackingCreatesTappedAndAttackingWarriorToken() {
        addCreatureReady(player1, new ReigningVictor());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            resolveAllTriggers();
        });

        List<Permanent> tokens = findPermanents(player1, "Warrior").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(tokens).hasSize(1);
        assertThat(tokens.getFirst().isTapped()).isTrue();
        assertThat(tokens.getFirst().isAttacking()).isTrue();
        assertThat(tokens.getFirst().getAttackTarget()).isEqualTo(player2.getId());
        assertThat(tokens.getFirst().isAttackedThisTurn()).isFalse();
    }

    @Test
    @DisplayName("Mobilized token is sacrificed at the beginning of the next end step")
    void mobilizedTokenIsSacrificedAtNextEndStep() {
        addCreatureReady(player1, new ReigningVictor());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Warrior").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .count()).isOne();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Warrior").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList()).isEmpty();
    }

    @Test
    @DisplayName("ETB cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new ReigningVictor()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        UUID targetId = harness.getPermanentId(player2, "Fountain of Youth");
        assertThatThrownBy(() -> harness.castCreature(player1, 0, List.of(targetId)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    @Test
    @DisplayName("The entering Victor can target itself")
    void etbCanTargetItself() {
        harness.setHand(player1, List.of(new ReigningVictor()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent victor = findPermanent(player1, "Reigning Victor");
        harness.handlePermanentChosen(player1, victor.getId());
        resolveAllTriggers();

        assertThat(victor.getPowerModifier()).isEqualTo(1);
        assertThat(victor.getToughnessModifier()).isZero();
        assertThat(gqs.hasKeyword(gd, victor, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(findPermanents(player1, "Warrior")).isEmpty();
    }

    @Test
    @DisplayName("ETB does not transfer its effects when the target leaves")
    void etbDoesNotAffectSourceWhenTargetLeaves() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ReigningVictor());
        harness.setHand(player1, List.of(new ReigningVictor()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.castCreature(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToHand(gd, target));
        resolveAllTriggers();

        Permanent victor = findPermanent(player1, "Reigning Victor");
        assertThat(victor.getPowerModifier()).isZero();
        assertThat(gqs.hasKeyword(gd, victor, Keyword.INDESTRUCTIBLE)).isFalse();
        harness.assertInHand(player2, "Reigning Victor");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Mobilize resolves and sacrifices its token even after Victor leaves")
    void mobilizeSurvivesSourceLeaving() {
        Permanent victor = addCreatureReady(player1, new ReigningVictor());
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            harness.inMutationScope(() -> harness.getPermanentRemovalService()
                    .removePermanentToHand(gd, victor));
            resolveAllTriggers();
        });

        assertThat(findPermanents(player1, "Warrior")).hasSize(1);
        Permanent token = findPermanent(player1, "Warrior");
        assertThat(token.isTapped()).isTrue();
        assertThat(token.isAttacking()).isTrue();
        harness.assertInHand(player1, "Reigning Victor");

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Warrior")).isEmpty();
    }
}
