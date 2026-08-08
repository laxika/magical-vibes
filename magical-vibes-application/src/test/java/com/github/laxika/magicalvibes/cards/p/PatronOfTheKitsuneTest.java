package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PatronOfTheKitsuneTest extends BaseCardTest {

    // "Whenever a creature attacks, you may gain 1 life."

    private Permanent addAttacker(com.github.laxika.magicalvibes.model.Player owner) {
        Permanent attacker = new Permanent(new GrizzlyBears());
        attacker.setSummoningSick(false);
        gd.playerBattlefields.get(owner.getId()).add(attacker);
        return attacker;
    }

    @Test
    @DisplayName("Accepting the trigger on an opponent's attacker gains 1 life")
    void opponentAttackerAcceptGainsLife() {
        harness.addToBattlefield(player1, new PatronOfTheKitsune());
        addAttacker(player2);
        harness.setLife(player1, 20);

        declareAttackers(player2, List.of(0));
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player1, 21);
    }

    @Test
    @DisplayName("Declining the trigger gains no life")
    void declineGainsNoLife() {
        harness.addToBattlefield(player1, new PatronOfTheKitsune());
        addAttacker(player2);
        harness.setLife(player1, 20);

        declareAttackers(player2, List.of(0));
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));
        harness.handleMayAbilityChosen(player1, false);

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("The controller's own attacker triggers it too")
    void ownAttackerTriggers() {
        harness.addToBattlefield(player1, new PatronOfTheKitsune());
        addAttacker(player1);
        harness.setLife(player1, 20);

        // Patron is at index 0, the attacking Grizzly Bears at index 1.
        declareAttackers(player1, List.of(1));
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player1, 21);
    }

    @Test
    @DisplayName("Fires once per attacking creature")
    void firesOncePerAttacker() {
        harness.addToBattlefield(player1, new PatronOfTheKitsune());
        addAttacker(player2);
        addAttacker(player2);
        harness.setLife(player1, 20);

        declareAttackers(player2, List.of(0, 1));

        assertThat(gd.stack).hasSize(2);

        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));
        harness.handleMayAbilityChosen(player1, true);
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player1, 22);
    }
}
