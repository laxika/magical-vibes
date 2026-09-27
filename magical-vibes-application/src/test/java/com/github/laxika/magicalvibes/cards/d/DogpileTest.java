package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.w.Watchwolf;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Dogpile.class, Watchwolf.class})
class DogpileTest extends BaseCardTest {

    @Test
    @DisplayName("Deals damage equal to the number of your attacking creatures")
    void dealsDamageEqualToAttackingCreatures() {
        addCreatureReady(player1, new Watchwolf());
        addCreatureReady(player1, new Watchwolf());
        addCreatureReady(player2, new Watchwolf());
        addCreatureReady(player2, new Watchwolf());
        declareAttackers(List.of(0, 1));
        prepareDeclareBlockers();

        harness.setHand(player1, List.of(new Dogpile()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 1)));
        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Does not count your creatures that are not attacking")
    void doesNotCountNonAttackingCreatures() {
        addCreatureReady(player1, new Watchwolf());
        addCreatureReady(player1, new Watchwolf());
        addCreatureReady(player2, new Watchwolf());
        declareAttackers(List.of(0));
        prepareDeclareBlockers();

        harness.setHand(player1, List.of(new Dogpile()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Does not count attacking creatures controlled by an opponent")
    void doesNotCountOpponentsAttackingCreatures() {
        addCreatureReady(player1, new Watchwolf());
        addCreatureReady(player2, new Watchwolf());
        declareAttackers(player2, List.of(0));
        prepareDeclareBlockers(player2);

        harness.setHand(player1, List.of(new Dogpile()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));
        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Deals zero damage when you control no attacking creatures")
    void dealsZeroDamageWithoutAttackers() {
        harness.setHand(player1, List.of(new Dogpile()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Can target a creature")
    void canTargetCreature() {
        addCreatureReady(player1, new Watchwolf());
        addCreatureReady(player1, new Watchwolf());
        addCreatureReady(player2, new Watchwolf());
        addCreatureReady(player2, new Watchwolf());
        Permanent target = addCreatureReady(player2, new Watchwolf());
        declareAttackers(List.of(0, 1));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 1)));

        harness.setHand(player1, List.of(new Dogpile()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getMarkedDamage()).isEqualTo(2);
    }
}
