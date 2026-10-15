package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GraniteWitness.class, GrizzlyBears.class})
class GraniteWitnessTest extends BaseCardTest {

    @Test
    void turningFaceUpMayTapTargetCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castFaceDown();

        turnFaceUp();
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(bears.isTapped()).isTrue();
    }

    @Test
    void turningFaceUpMayUntapTargetCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        bears.tap();
        castFaceDown();

        turnFaceUp();
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(bears.isTapped()).isFalse();
    }

    @Test
    void mayDeclineTappingOrUntappingTargetCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castFaceDown();

        turnFaceUp();
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(bears.isTapped()).isFalse();
    }

    @Test
    void canPayDisguiseWithBlueManaAndTargetItself() {
        castFaceDown();
        Permanent witness = findPermanent(player1, "Granite Witness");
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(witness));
        harness.handlePermanentChosen(player1, witness.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(witness.isFaceDown()).isFalse();
        assertThat(witness.isTapped()).isTrue();
    }

    @Test
    void canPayDisguiseWithMixedManaAndUntapItself() {
        castFaceDown();
        Permanent witness = findPermanent(player1, "Granite Witness");
        witness.tap();
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(witness));
        harness.handlePermanentChosen(player1, witness.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(witness.isFaceDown()).isFalse();
        assertThat(witness.isTapped()).isFalse();
    }

    @Test
    void mayLeaveTappedCreatureTapped() {
        castFaceDown();
        Permanent witness = findPermanent(player1, "Granite Witness");
        witness.tap();

        turnFaceUp();
        harness.handlePermanentChosen(player1, witness.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(witness.isTapped()).isTrue();
    }

    private void castFaceDown() {
        harness.setHand(player1, List.of(new GraniteWitness()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    private void turnFaceUp() {
        Permanent witness = findPermanent(player1, "Granite Witness");
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(witness));
    }
}
