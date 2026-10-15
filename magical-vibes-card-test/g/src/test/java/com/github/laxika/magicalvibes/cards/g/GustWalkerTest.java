package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.l.LayClaim;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GustWalker.class, LayClaim.class})
class GustWalkerTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking offers the exert may prompt")
    void attackTriggersExertPrompt() {
        addReadyWalker(player1);

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Exerting gives +1/+1 and flying until end of turn")
    void exertBoostsAndGrantsFlying() {
        Permanent walker = addReadyWalker(player1);

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, walker)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, walker)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, walker, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Exerting keeps the creature tapped through its next untap step")
    void exertSkipsNextUntap() {
        Permanent walker = addReadyWalker(player1);

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(walker.isTapped()).isTrue();
        assertThat(walker.getSkipUntapCount()).isGreaterThan(0);
    }

    @Test
    @DisplayName("Declining exert leaves base stats and grants no flying")
    void decliningExertDoesNothing() {
        Permanent walker = addReadyWalker(player1);

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gqs.getEffectivePower(gd, walker)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, walker)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, walker, Keyword.FLYING)).isFalse();
        assertThat(walker.getSkipUntapCount()).isZero();
    }

    @Test
    @DisplayName("Exert is paid before the bonus trigger resolves")
    void exertIsPaidBeforeBonusResolves() {
        Permanent walker = addReadyWalker(player1);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            harness.passBothPriorities();
            harness.handleMayAbilityChosen(player1, true);

            assertThat(walker.getSkipUntapCount()).isPositive();
            assertThat(gqs.getEffectivePower(gd, walker)).isEqualTo(2);
            assertThat(gqs.hasKeyword(gd, walker, Keyword.FLYING)).isFalse();
        });
    }

    @Test
    @DisplayName("Exert skips the next controller untap step only")
    void exertSkipsOnlyOneUntapStep() {
        Permanent walker = addReadyWalker(player1);

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        harness.performUntapStep(player2);
        assertThat(walker.isTapped()).isTrue();
        harness.performUntapStep(player1);
        assertThat(walker.isTapped()).isTrue();
        harness.performUntapStep(player1);
        assertThat(walker.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The boost and flying expire at end of turn")
    void exertBonusExpiresAtEndOfTurn() {
        Permanent walker = addReadyWalker(player1);
        harness.setLibrary(player2, List.of(new GustWalker()));

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.getEffectivePower(gd, walker)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, walker)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, walker, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Exert does not prevent untapping during a new controller's untap step")
    void exertRestrictionDoesNotFollowNewController() {
        Permanent walker = addReadyWalker(player1);

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new LayClaim()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 5);
        harness.ensurePriority(player2);
        harness.castEnchantment(player2, 0, walker.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(walker);
        harness.performUntapStep(player2);
        assertThat(walker.isTapped()).isFalse();
    }

    private Permanent addReadyWalker(Player player) {
        return addCreatureReady(player, new GustWalker());
    }
}
