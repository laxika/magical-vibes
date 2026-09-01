package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.b.Bonesplitter;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TitanOfIndustry.class, Bonesplitter.class, GrizzlyBears.class})
class TitanOfIndustryTest extends BaseCardTest {

    @Test
    void choosesTokenAndTargetPlayerLifeGain() {
        castTitan();
        chooseModes("Target player gains 5 life.", "Create a 4/4 green Rhino Warrior creature token.");
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(25);
        assertThat(findPermanents(player1, "Rhino Warrior")).hasSize(1);
    }

    @Test
    void choosesArtifactDestructionAndShieldCounter() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Bonesplitter());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castTitan();
        chooseModes("Destroy target artifact or enchantment.", "Put a shield counter on a creature you control.");
        harness.handlePermanentChosen(player1, artifact.getId());
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Bonesplitter");
        assertThat(creature.getCounterCount(CounterType.SHIELD)).isOne();
    }

    @Test
    void shieldModeTargetsOnlyCreatureYouControl() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castTitan();
        chooseModes("Create a 4/4 green Rhino Warrior creature token.", "Put a shield counter on a creature you control.");
        harness.handlePermanentChosen(player1, ownCreature.getId());
        harness.passBothPriorities();

        assertThat(ownCreature.getCounterCount(CounterType.SHIELD)).isOne();
        assertThat(opponentCreature.getCounterCount(CounterType.SHIELD)).isZero();
    }

    private void castTitan() {
        harness.setHand(player1, List.of(new TitanOfIndustry()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }

    private void chooseModes(String first, String second) {
        harness.handleListChoice(player1, first);
        harness.handleListChoice(player1, second);
    }
}
