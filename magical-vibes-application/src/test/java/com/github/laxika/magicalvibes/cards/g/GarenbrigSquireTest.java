package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.r.RimrockKnight;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GarenbrigSquire.class, RimrockKnight.class, GrizzlyBears.class})
class GarenbrigSquireTest extends BaseCardTest {

    @Test
    void getsBiggerWhenCreatureFaceOfAdventureCardIsCast() {
        Permanent squire = addCreatureReady(player1, new GarenbrigSquire());
        harness.setHand(player1, List.of(new RimrockKnight()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(squire.getEffectivePower()).isEqualTo(3);
        assertThat(squire.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    void doesNotTriggerForNonAdventureCreatureOrAdventureFace() {
        Permanent squire = addCreatureReady(player1, new GarenbrigSquire());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(squire.getEffectivePower()).isEqualTo(2);
        assertThat(squire.getEffectiveToughness()).isEqualTo(2);

        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new RimrockKnight()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAdventure(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(squire.getEffectivePower()).isEqualTo(2);
        assertThat(squire.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    void boostWearsOffAtEndOfTurn() {
        Permanent squire = addCreatureReady(player1, new GarenbrigSquire());
        harness.setHand(player1, List.of(new RimrockKnight()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(squire.getEffectivePower()).isEqualTo(2);
        assertThat(squire.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    void doesNotTriggerForOpponentsAdventureCreature() {
        Permanent squire = addCreatureReady(player1, new GarenbrigSquire());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new RimrockKnight()));
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThat(squire.getEffectivePower()).isEqualTo(2);
        assertThat(squire.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    void eachAdventureCreatureCastAddsAnotherBoost() {
        Permanent squire = addCreatureReady(player1, new GarenbrigSquire());
        harness.setHand(player1, List.of(new RimrockKnight(), new RimrockKnight()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(squire.getEffectivePower()).isEqualTo(4);
        assertThat(squire.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    void triggersWhenCreatureIsCastFromExileAfterItsAdventure() {
        Permanent squire = addCreatureReady(player1, new GarenbrigSquire());
        Permanent target = addCreatureReady(player1, new RimrockKnight());
        RimrockKnight knight = new RimrockKnight();
        harness.setHand(player1, List.of(knight));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAdventure(player1, 0, target.getId());
        harness.passBothPriorities();
        assertThat(squire.getEffectivePower()).isEqualTo(2);
        assertThat(squire.getEffectiveToughness()).isEqualTo(2);

        harness.castFromExile(player1, knight.getId());
        harness.passBothPriorities();

        assertThat(squire.getEffectivePower()).isEqualTo(3);
        assertThat(squire.getEffectiveToughness()).isEqualTo(3);
    }
}
