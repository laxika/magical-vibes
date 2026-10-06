package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.s.SidewinderNaga;
import com.github.laxika.magicalvibes.cards.k.KefnetsLastWord;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RhonassStalwart.class, SidewinderNaga.class, KefnetsLastWord.class})
class RhonassStalwartTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking offers the exert may prompt")
    void attackTriggersExertPrompt() {
        addReadyStalwart(player1);

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Exerting gives +1/+1 until end of turn")
    void exertBoosts() {
        Permanent stalwart = addReadyStalwart(player1);

        declareAttackers(List.of(0));
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, stalwart)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, stalwart)).isEqualTo(3);
    }

    @Test
    @DisplayName("Exerting keeps the creature tapped through its next untap step")
    void exertSkipsNextUntap() {
        Permanent stalwart = addReadyStalwart(player1);

        declareAttackers(List.of(0));
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(stalwart.isTapped()).isTrue();
        assertThat(stalwart.getSkipUntapCount()).isGreaterThan(0);
    }

    @Test
    @DisplayName("After exerting, can't be blocked by a creature with power 2 or less")
    void exertCannotBeBlockedByLowPower() {
        Permanent stalwart = addReadyStalwart(player1);
        Permanent bears = addCreatureReady(player2, new RhonassStalwart());

        declareAttackers(List.of(0));
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        prepareDeclareBlockers();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(bears);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(stalwart);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("After exerting, can be blocked by a creature with power 3 or greater")
    void exertCanBeBlockedByHighPower() {
        Permanent stalwart = addReadyStalwart(player1);
        Permanent giant = addCreatureReady(player2, new SidewinderNaga());

        declareAttackers(List.of(0));
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        prepareDeclareBlockers();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(giant);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(stalwart);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx)));

        assertThat(giant.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Declining exert leaves base stats and allows low-power blockers")
    void decliningExertDoesNothing() {
        Permanent stalwart = addReadyStalwart(player1);
        Permanent bears = addCreatureReady(player2, new RhonassStalwart());

        declareAttackers(List.of(0));
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, stalwart)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, stalwart)).isEqualTo(2);
        assertThat(stalwart.getSkipUntapCount()).isZero();

        prepareDeclareBlockers();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(bears);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(stalwart);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx)));

        assertThat(bears.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Exert is paid before the bonus trigger resolves")
    void exertIsPaidBeforeBonusResolves() {
        Permanent stalwart = addReadyStalwart(player1);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            harness.handleMayAbilityChosen(player1, true);

            assertThat(stalwart.getSkipUntapCount()).isPositive();
            assertThat(gqs.getEffectivePower(gd, stalwart)).isEqualTo(2);
            assertThat(gqs.getEffectiveToughness(gd, stalwart)).isEqualTo(2);
            assertThat(gd.stack).hasSize(1);
        });
        resolveAllTriggers();
        assertThat(gqs.getEffectivePower(gd, stalwart)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, stalwart)).isEqualTo(3);
    }

    @Test
    @DisplayName("Exert skips one untap step and then allows untapping")
    void exertSkipsOnlyOneUntapStep() {
        Permanent stalwart = addReadyStalwart(player1);

        declareAttackers(List.of(0));
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        harness.performUntapStep(player2);
        assertThat(stalwart.isTapped()).isTrue();
        harness.performUntapStep(player1);
        assertThat(stalwart.isTapped()).isTrue();
        harness.performUntapStep(player1);
        assertThat(stalwart.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Exert's boost and blocking restriction expire at end of turn")
    void exertBenefitsExpireAtEndOfTurn() {
        Permanent stalwart = addReadyStalwart(player1);
        Permanent blocker = addCreatureReady(player2, new RhonassStalwart());
        harness.setLibrary(player2, List.of(new RhonassStalwart()));

        declareAttackers(List.of(0));
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.getEffectivePower(gd, stalwart)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, stalwart)).isEqualTo(2);

        stalwart.untap();
        declareAttackers(List.of(0));
        harness.handleMayAbilityChosen(player1, false);
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Exert does not prevent untapping during a new controller's untap step")
    void exertRestrictionDoesNotFollowNewController() {
        Permanent stalwart = addReadyStalwart(player1);

        declareAttackers(List.of(0));
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new KefnetsLastWord()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.castSorcery(player2, 0, stalwart.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(stalwart);
        harness.performUntapStep(player2);
        assertThat(stalwart.isTapped()).isFalse();
    }

    private Permanent addReadyStalwart(Player player) {
        return addCreatureReady(player, new RhonassStalwart());
    }
}
