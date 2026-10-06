package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Glorybringer.class, GreaterSandwurm.class})
class GlorybringerTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking offers exert before choosing a damage target")
    void attackQueuesTargetSelection() {
        addCreatureReady(player1, new Glorybringer());
        addCreatureReady(player2, new GreaterSandwurm());

        declareAttackers(List.of(0));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
    }

    @Test
    @DisplayName("Glorybringer stays exerted even if its damage target leaves before the ability resolves")
    void exertStandsWhenTargetLeaves() {
        Permanent glorybringer = addCreatureReady(player1, new Glorybringer());
        Permanent sandwurm = addCreatureReady(player2, new GreaterSandwurm());

        declareAttackers(List.of(0));
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, sandwurm.getId());
        gd.playerBattlefields.get(player2.getId()).remove(sandwurm);
        resolveAllTriggers();

        assertThat(glorybringer.getSkipUntapCount()).isPositive();
        harness.performUntapStep(player1);
        assertThat(glorybringer.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Exerting deals 4 damage to the target and skips the dragon's next untap")
    void exertDealsFourDamageAndSkipsUntap() {
        Permanent glorybringer = addCreatureReady(player1, new Glorybringer());
        Permanent sandwurm = addCreatureReady(player2, new GreaterSandwurm());

        declareAttackers(List.of(0));
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, sandwurm.getId());
        resolveAllTriggers();

        assertThat(sandwurm.getMarkedDamage()).isEqualTo(4);
        harness.performUntapStep(player2);
        harness.performUntapStep(player1);
        assertThat(glorybringer.isTapped()).isTrue();
        harness.performUntapStep(player1);
        assertThat(glorybringer.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Declining exert deals no damage and does not skip untap")
    void decliningExertDoesNothing() {
        Permanent glorybringer = addCreatureReady(player1, new Glorybringer());
        Permanent sandwurm = addCreatureReady(player2, new GreaterSandwurm());

        declareAttackers(List.of(0));
        harness.handleMayAbilityChosen(player1, false);
        assertThat(gd.stack).isEmpty();

        assertThat(sandwurm.getMarkedDamage()).isZero();
        harness.performUntapStep(player1);
        assertThat(glorybringer.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Only non-Dragon creatures an opponent controls are legal targets")
    void targetFilterExcludesDragonsAndOwnCreatures() {
        addCreatureReady(player1, new Glorybringer());
        Permanent ownSandwurm = addCreatureReady(player1, new GreaterSandwurm());
        Permanent opponentSandwurm = addCreatureReady(player2, new GreaterSandwurm());
        Permanent opponentDragon = addCreatureReady(player2, new Glorybringer());

        declareAttackers(List.of(0));

        harness.handleMayAbilityChosen(player1, true);
        PendingInteraction interaction = gd.interaction.activeInteraction();
        assertThat(interaction).isInstanceOf(PendingInteraction.PermanentChoice.class);
        PendingInteraction.PermanentChoice choice = (PendingInteraction.PermanentChoice) interaction;

        assertThat(choice.validIds()).contains(opponentSandwurm.getId());
        assertThat(choice.validIds()).doesNotContain(opponentDragon.getId(), ownSandwurm.getId());
    }

    @Test
    @DisplayName("Glorybringer can exert without a legal damage target")
    void canExertWithoutLegalTargets() {
        Permanent glorybringer = addCreatureReady(player1, new Glorybringer());
        addCreatureReady(player2, new Glorybringer());

        declareAttackers(List.of(0));
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.stack).isEmpty();
        harness.performUntapStep(player1);
        assertThat(glorybringer.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Losing the damage target does not undo exert")
    void losingDamageTargetDoesNotUndoExert() {
        Permanent glorybringer = addCreatureReady(player1, new Glorybringer());
        Permanent sandwurm = addCreatureReady(player2, new GreaterSandwurm());

        declareAttackers(List.of(0));
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, sandwurm.getId());
        gd.playerBattlefields.get(player2.getId()).remove(sandwurm);
        gd.playerGraveyards.get(player2.getId()).add(sandwurm.getCard());
        resolveAllTriggers();

        harness.performUntapStep(player1);
        assertThat(glorybringer.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Exert does not prevent untapping during a new controller's untap step")
    void exertRestrictionBelongsToExertingPlayer() {
        Permanent glorybringer = addCreatureReady(player1, new Glorybringer());
        Permanent sandwurm = addCreatureReady(player2, new GreaterSandwurm());

        declareAttackers(List.of(0));
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, sandwurm.getId());
        resolveAllTriggers();
        gd.playerBattlefields.get(player1.getId()).remove(glorybringer);
        gd.playerBattlefields.get(player2.getId()).add(glorybringer);

        harness.performUntapStep(player2);
        assertThat(glorybringer.isTapped()).isFalse();
    }
}
