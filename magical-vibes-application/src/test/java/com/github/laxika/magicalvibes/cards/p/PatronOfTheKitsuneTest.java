package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GnarledMass;
import com.github.laxika.magicalvibes.cards.k.KitsunePalliator;
import com.github.laxika.magicalvibes.cards.s.SphereOfResistance;
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

@CardUsed({PatronOfTheKitsune.class, GnarledMass.class, KitsunePalliator.class, SphereOfResistance.class})
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

    @Test
    void offeringCanBeCastDuringOpponentsUpkeep() {
        Permanent fox = harness.addToBattlefieldAndReturn(player1, new KitsunePalliator());
        harness.setHand(player1, List.of(new PatronOfTheKitsune()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);

        harness.castCreatureWithAlternateCost(player1, 0, List.of(fox.getId()));
        harness.assertInGraveyard(player1, "Kitsune Palliator");
        harness.assertNotOnBattlefield(player1, "Patron of the Kitsune");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Patron of the Kitsune");
    }

    @Test
    void normalCastDoesNotRequireSacrificingFox() {
        harness.addToBattlefield(player1, new KitsunePalliator());
        harness.setHand(player1, List.of(new PatronOfTheKitsune()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Patron of the Kitsune");
        harness.assertOnBattlefield(player1, "Kitsune Palliator");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void offeringCannotSacrificeNonFox() {
        Permanent spirit = harness.addToBattlefieldAndReturn(player1, new GnarledMass());
        harness.setHand(player1, List.of(new PatronOfTheKitsune()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castCreatureWithAlternateCost(player1, 0, List.of(spirit.getId())))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Gnarled Mass");
        harness.assertInHand(player1, "Patron of the Kitsune");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void normalCastHasNoInstantSpeedPermissionFromOffering() {
        harness.setHand(player1, List.of(new PatronOfTheKitsune()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Patron of the Kitsune");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void offeringPaysAdditionalGenericSpellCost() {
        harness.addToBattlefield(player2, new SphereOfResistance());
        Permanent fox = harness.addToBattlefieldAndReturn(player1, new KitsunePalliator());
        harness.setHand(player1, List.of(new PatronOfTheKitsune()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithAlternateCost(player1, 0, List.of(fox.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Patron of the Kitsune");
        harness.assertInGraveyard(player1, "Kitsune Palliator");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }
}
