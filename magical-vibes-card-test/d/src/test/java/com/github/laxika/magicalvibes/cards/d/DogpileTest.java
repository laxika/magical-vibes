package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.l.LastGasp;
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

@CardUsed({Dogpile.class, Watchwolf.class, LastGasp.class})
class DogpileTest extends BaseCardTest {

    @Test
    @DisplayName("Deals damage equal to the number of your attacking creatures")
    void dealsDamageEqualToAttackingCreatures() {
        addCreatureReady(player1, new Watchwolf());
        addCreatureReady(player1, new Watchwolf());
        addCreatureReady(player2, new Watchwolf());
        addCreatureReady(player2, new Watchwolf());
        declareAttackersAndPrepareBlockers(List.of(0, 1));

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
        declareAttackersAndPrepareBlockers(List.of(0));

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
        declareAttackersAndPrepareBlockers(player2, List.of(0));

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
        declareAttackersAndPrepareBlockers(List.of(0, 1));
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 1)));

        harness.setHand(player1, List.of(new Dogpile()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Counts attackers at resolution after an attacker dies in response")
    void countsAttackersAtResolution() {
        Permanent attacker = addCreatureReady(player1, new Watchwolf());
        addCreatureReady(player1, new Watchwolf());
        addCreatureReady(player2, new Watchwolf());
        addCreatureReady(player2, new Watchwolf());
        harness.setHand(player1, List.of(new Dogpile()));
        harness.setHand(player2, List.of(new LastGasp()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        declareAttackersAndPrepareBlockers(List.of(0, 1));
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 1)));

        harness.castInstant(player1, 0, player2.getId());
        harness.castAndResolveInstant(player2, 0, attacker.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(attacker);
        resolveAllTriggers();

        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Does not resolve when its creature target dies in response")
    void doesNotResolveWithMissingTarget() {
        addCreatureReady(player1, new Watchwolf());
        addCreatureReady(player2, new Watchwolf());
        Permanent target = addCreatureReady(player2, new Watchwolf());
        harness.setHand(player1, List.of(new Dogpile()));
        harness.setHand(player2, List.of(new LastGasp()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        harness.castInstant(player1, 0, target.getId());
        harness.castAndResolveInstant(player2, 0, target.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card instanceof Dogpile);
    }
}
