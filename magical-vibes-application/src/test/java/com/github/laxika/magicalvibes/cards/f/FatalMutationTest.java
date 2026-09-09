package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

@CardUsed({FatalMutation.class, GrizzlyBears.class})
class FatalMutationTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys the enchanted creature when it is turned face up")
    void destroysEnchantedCreatureWhenTurnedFaceUp() {
        Permanent creature = addFaceDownCreature();
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new FatalMutation());
        aura.setAttachedTo(creature.getId());
        creature.setRegenerationShield(1);

        gs.turnPermanentFaceUpWithoutPayingManaCost(gd, creature);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Fatal Mutation");
    }

    @Test
    @DisplayName("Does not trigger when another permanent is turned face up")
    void ignoresAnotherPermanentTurningFaceUp() {
        Permanent enchantedCreature = addFaceDownCreature();
        Permanent otherCreature = addFaceDownCreature();
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new FatalMutation());
        aura.setAttachedTo(enchantedCreature.getId());

        gs.turnPermanentFaceUpWithoutPayingManaCost(gd, otherCreature);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Fatal Mutation");
    }

    private Permanent addFaceDownCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        creature.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        return creature;
    }
}
