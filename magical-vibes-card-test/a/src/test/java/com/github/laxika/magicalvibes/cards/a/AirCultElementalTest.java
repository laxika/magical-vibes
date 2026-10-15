package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.h.HillGiantHerdgorger;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AirCultElemental.class, Forest.class, HillGiantHerdgorger.class})
class AirCultElementalTest extends BaseCardTest {

    @Test
    @DisplayName("Returns up to one other target creature to its owner's hand")
    void returnsTargetCreatureToItsOwnersHand() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new HillGiantHerdgorger());
        castAirCultElemental(creature.getId());

        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Air-Cult Elemental");
        harness.assertNotOnBattlefield(player2, "Hill Giant Herdgorger");
        harness.assertInHand(player2, "Hill Giant Herdgorger");
    }

    @Test
    @DisplayName("Can decline the optional creature target")
    void canDeclineTarget() {
        harness.addToBattlefield(player2, new HillGiantHerdgorger());
        harness.castFromHand(player1, new AirCultElemental(), "{4}{U}{U}");
        resolveAllTriggers();
        harness.handlePermanentChosen(player1, player1.getId());
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Air-Cult Elemental");
        harness.assertOnBattlefield(player2, "Hill Giant Herdgorger");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new AirCultElemental()));
        addManaForAirCultElemental();

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, forest.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("another creature");
    }

    @Test
    @DisplayName("Can enter when there are no other creatures")
    void canEnterWithoutOtherCreatures() {
        harness.castFromHand(player1, new AirCultElemental(), "{4}{U}{U}");
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Air-Cult Elemental");
        harness.assertNotInHand(player1, "Air-Cult Elemental");
    }

    @Test
    @DisplayName("Can return another Air-Cult Elemental you control")
    void canReturnAnotherCopyYouControl() {
        Permanent other = harness.addToBattlefieldAndReturn(player1, new AirCultElemental());
        castAirCultElemental(other.getId());
        resolveAllTriggers();

        harness.assertInHand(player1, "Air-Cult Elemental");
        harness.assertOnBattlefield(player1, "Air-Cult Elemental");
        assertThat(countPermanents(player1, "Air-Cult Elemental")).isEqualTo(1);
    }

    @Test
    @DisplayName("Returns a creature to its owner rather than its current controller")
    void returnsCreatureToOwnerDespiteDifferentController() {
        HillGiantHerdgorger card = new HillGiantHerdgorger();
        card.setOwnerId(player1.getId());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, card);
        castAirCultElemental(creature.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Hill Giant Herdgorger");
        harness.assertInHand(player1, "Hill Giant Herdgorger");
        harness.assertNotInHand(player2, "Hill Giant Herdgorger");
    }

    @Test
    @DisplayName("An Elemental entering without being cast cannot target itself")
    void enteringWithoutBeingCastCannotTargetItself() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new HillGiantHerdgorger());
        Permanent elemental = harness.enterBattlefieldAndReturn(player1, new AirCultElemental());
        resolveAllTriggers();

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, elemental.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, creature.getId());
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Air-Cult Elemental");
        harness.assertInHand(player2, "Hill Giant Herdgorger");
        harness.assertNotOnBattlefield(player2, "Hill Giant Herdgorger");
    }

    @Test
    @DisplayName("Does not return a target that leaves before the trigger resolves")
    void targetLeavingInResponseIsNotReturned() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new HillGiantHerdgorger());
        castAirCultElemental(creature.getId());
        harness.passBothPriorities();
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, creature));
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Hill Giant Herdgorger");
        harness.assertNotInHand(player2, "Hill Giant Herdgorger");
        harness.assertOnBattlefield(player1, "Air-Cult Elemental");
    }

    private void castAirCultElemental(java.util.UUID targetId) {
        harness.setHand(player1, List.of(new AirCultElemental()));
        addManaForAirCultElemental();
        harness.castCreature(player1, 0, 0, targetId);
    }

    private void addManaForAirCultElemental() {
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }
}
