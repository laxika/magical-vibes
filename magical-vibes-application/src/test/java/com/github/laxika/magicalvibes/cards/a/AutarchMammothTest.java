package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AutarchMammothTest extends BaseCardTest {

    @Test
    @DisplayName("Entering Autarch Mammoth creates an Elephant token")
    void entersCreatesElephant() {
        harness.setHand(player1, List.of(new AutarchMammoth()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Elephant")).hasSize(1);
    }

    @Test
    @DisplayName("Attacking while saddled creates an Elephant token")
    void attacksWhileSaddledCreatesElephant() {
        Permanent mammoth = addCreatureReady(player1, new AutarchMammoth());
        mammoth.setSaddled(true);

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Elephant")).hasSize(1);
    }

    @Test
    @DisplayName("Attacking while not saddled does not create an Elephant token")
    void attacksWhileNotSaddledDoesNotCreateElephant() {
        addCreatureReady(player1, new AutarchMammoth());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Elephant")).isEmpty();
    }

    @Test
    @DisplayName("Saddle 5 taps other creatures and saddles Autarch Mammoth")
    void saddleTapsOtherCreatures() {
        Permanent mammoth = addCreatureReady(player1, new AutarchMammoth());
        Permanent hillGiant = addCreatureReady(player1, new HillGiant());
        Permanent grizzlyBears = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(mammoth.isSaddled()).isTrue();
        assertThat(hillGiant.isTapped()).isTrue();
        assertThat(grizzlyBears.isTapped()).isTrue();
    }
}
