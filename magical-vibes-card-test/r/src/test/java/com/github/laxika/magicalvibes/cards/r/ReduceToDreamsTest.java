package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GnarledMass;
import com.github.laxika.magicalvibes.cards.i.InTheWebOfWar;
import com.github.laxika.magicalvibes.cards.m.MarkOfTheOni;
import com.github.laxika.magicalvibes.cards.o.OrbOfDreams;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ReduceToDreams.class, OrbOfDreams.class, InTheWebOfWar.class, GnarledMass.class, MarkOfTheOni.class})
class ReduceToDreamsTest extends BaseCardTest {

    @Test
    @DisplayName("Returns all artifacts and enchantments on both sides to their owners' hands")
    void returnsAllArtifactsAndEnchantments() {
        harness.addToBattlefield(player1, new OrbOfDreams());
        harness.addToBattlefield(player1, new InTheWebOfWar());
        harness.addToBattlefield(player2, new OrbOfDreams());

        harness.castFromHand(player1, new ReduceToDreams(), "{3}{U}{U}");
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(c -> c.getName())
                .containsExactlyInAnyOrder("Orb of Dreams", "In the Web of War");
        assertThat(gd.playerHands.get(player2.getId()))
                .extracting(c -> c.getName())
                .contains("Orb of Dreams");
    }

    @Test
    @DisplayName("Leaves non-artifact non-enchantment permanents alone")
    void leavesOtherPermanentsAlone() {
        harness.addToBattlefield(player1, new GnarledMass());
        harness.addToBattlefield(player1, new OrbOfDreams());

        harness.castFromHand(player1, new ReduceToDreams(), "{3}{U}{U}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Gnarled Mass");
        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(c -> c.getName())
                .containsExactly("Orb of Dreams");
    }

    @Test
    @DisplayName("Returns a matching permanent to its owner's hand even when another player controls it")
    void returnsMatchingPermanentToItsOwner() {
        Permanent stolenArtifact = harness.addToBattlefieldAndReturn(player1, new OrbOfDreams());
        gd.stolenCreatures.put(stolenArtifact.getId(), player2.getId());

        harness.castFromHand(player1, new ReduceToDreams(), "{3}{U}{U}");
        harness.passBothPriorities();

        harness.assertInHand(player2, "Orb of Dreams");
        harness.assertNotInHand(player1, "Orb of Dreams");
    }

    @Test
    @DisplayName("Returns a control-changing Aura to hand and leaves its creature with its original controller")
    void returnsAuraWithoutReturningEnchantedCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GnarledMass());
        harness.setHand(player1, List.of(new MarkOfTheOni()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Gnarled Mass");
        harness.assertNotOnBattlefield(player2, "Gnarled Mass");

        harness.castFromHand(player1, new ReduceToDreams(), "{3}{U}{U}");
        harness.passBothPriorities();

        harness.assertInHand(player1, "Mark of the Oni");
        harness.assertNotInGraveyard(player1, "Mark of the Oni");
        harness.assertNotOnBattlefield(player1, "Mark of the Oni");
        harness.assertOnBattlefield(player2, "Gnarled Mass");
        harness.assertNotOnBattlefield(player1, "Gnarled Mass");
        harness.assertNotInHand(player1, "Gnarled Mass");
        harness.assertNotInHand(player2, "Gnarled Mass");
    }

    @Test
    @DisplayName("Resolves with nothing to bounce and goes to the graveyard")
    void resolvesWithEmptyBattlefield() {
        harness.castFromHand(player1, new ReduceToDreams(), "{3}{U}{U}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Reduce to Dreams");
    }
}
