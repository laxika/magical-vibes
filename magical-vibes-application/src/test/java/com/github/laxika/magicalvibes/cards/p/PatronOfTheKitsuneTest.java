package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GnarledMass;
import com.github.laxika.magicalvibes.cards.k.KitsunePalliator;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PatronOfTheKitsune.class, GnarledMass.class, KitsunePalliator.class})
class PatronOfTheKitsuneTest extends BaseCardTest {

    // "Whenever a creature attacks, you may gain 1 life."

    private Permanent addAttacker(com.github.laxika.magicalvibes.model.Player owner) {
        return addCreatureReady(owner, new GnarledMass());
    }

    @Test
    @DisplayName("Accepting the trigger on an opponent's attacker gains 1 life")
    void opponentAttackerAcceptGainsLife() {
        harness.addToBattlefield(player1, new PatronOfTheKitsune());
        addAttacker(player2);
        harness.setLife(player1, 20);

        declareAttackers(player2, List.of(0));
        harness.passBothPriorities();
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
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Fox offering sacrifices a Fox and pays the colored mana difference")
    void foxOfferingSacrificesFoxAndPaysDifference() {
        Permanent fox = harness.addToBattlefieldAndReturn(player1, new KitsunePalliator());
        harness.setHand(player1, List.of(new PatronOfTheKitsune()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreatureWithAlternateCost(player1, 0, List.of(fox.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Patron of the Kitsune");
        harness.assertNotOnBattlefield(player1, "Kitsune Palliator");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("The controller's own attacker triggers it too")
    void ownAttackerTriggers() {
        harness.addToBattlefield(player1, new PatronOfTheKitsune());
        addAttacker(player1);
        harness.setLife(player1, 20);

        // Patron is at index 0, the attacking Gnarled Mass at index 1.
        declareAttackers(player1, List.of(1));
        harness.passBothPriorities();
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

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player1, 22);
    }
}
