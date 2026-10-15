package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Dalkovan Packbeasts")
@CardUsed(DalkovanPackbeasts.class)
class DalkovanPackbeastsTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking creates three tapped and attacking red Warrior tokens")
    void attackingCreatesWarriorTokens() {
        addCreatureReady(player1, new DalkovanPackbeasts());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            resolveAllTriggers();
        });

        List<Permanent> tokens = findPermanents(player1, "Warrior").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(tokens).hasSize(3);
        assertThat(tokens).allSatisfy(token -> {
            assertThat(token.getCard().getPower()).isEqualTo(1);
            assertThat(token.getCard().getToughness()).isEqualTo(1);
            assertThat(token.getCard().getColor()).isEqualTo(CardColor.RED);
            assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.WARRIOR);
            assertThat(token.isTapped()).isTrue();
            assertThat(token.isAttacking()).isTrue();
        });
    }

    @Test
    @DisplayName("Attack tokens are sacrificed at the beginning of the next end step")
    void attackTokensAreSacrificedAtNextEndStep() {
        addCreatureReady(player1, new DalkovanPackbeasts());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            resolveAllTriggers();
        });
        assertThat(findPermanents(player1, "Warrior").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .count()).isEqualTo(3);

        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Warrior").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList()).isEmpty();
    }

    @Test
    @DisplayName("Vigilance leaves Packbeasts untapped while its Warriors deal combat damage")
    void vigilanceAndWarriorCombatDamage() {
        Permanent packbeasts = addCreatureReady(player1, new DalkovanPackbeasts());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            resolveAllTriggers();
        });
        resolveCombat();

        assertThat(packbeasts.isTapped()).isFalse();
        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Mobilize resolves and sacrifices its tokens even if Packbeasts leaves in response")
    void mobilizeSurvivesSourceLeavingBattlefield() {
        Permanent packbeasts = addCreatureReady(player1, new DalkovanPackbeasts());

        declareAttackers(List.of(0));
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToHand(gd, packbeasts));
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Warrior")).isEqualTo(3);
        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Warrior");
        harness.assertInHand(player1, "Dalkovan Packbeasts");
    }

    @Test
    @DisplayName("Mobilize creates one delayed trigger that sacrifices all three Warriors together")
    void sacrificesWarriorsInOneDelayedTrigger() {
        addCreatureReady(player1, new DalkovanPackbeasts());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            resolveAllTriggers();
        });
        harness.passUntil(TurnStep.END_STEP);

        assertThat(countPermanents(player1, "Warrior")).isEqualTo(3);
        assertThat(gd.stack).hasSize(1);
        harness.withAutoStop(TurnStep.END_STEP, () -> harness.passBothPriorities());

        harness.assertNotOnBattlefield(player1, "Warrior");
        assertThat(gd.stack).isEmpty();
    }
}
