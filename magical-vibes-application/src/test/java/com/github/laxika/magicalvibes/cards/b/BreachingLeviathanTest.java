package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Persuasion;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BreachingLeviathan.class, BeaconOfUnrest.class, AirElemental.class, GrizzlyBears.class, Persuasion.class})
class BreachingLeviathanTest extends BaseCardTest {

    @Test
    @DisplayName("Cast from hand taps nonblue creatures and skips their next untap")
    void castFromHandTapsNonblueCreaturesAndSkipsUntap() {
        Permanent nonblue = addCreatureReady(player1, new GrizzlyBears());
        Permanent blue = addCreatureReady(player2, new AirElemental());

        harness.setHand(player1, List.of(new BreachingLeviathan()));
        harness.addMana(player1, ManaColor.COLORLESS, 7);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(nonblue.isTapped()).isTrue();
        assertThat(blue.isTapped()).isFalse();

        advanceToUpkeep(player1);

        assertThat(nonblue.isTapped()).isTrue();
        assertThat(blue.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Entering from the graveyard does not trigger the hand-cast ability")
    void enteringFromGraveyardDoesNotTriggerAbility() {
        Permanent nonblue = addCreatureReady(player1, new GrizzlyBears());

        BreachingLeviathan leviathan = new BreachingLeviathan();
        harness.setGraveyard(player1, List.of(leviathan));
        harness.setHand(player1, List.of(new BeaconOfUnrest()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castAndResolveSorcery(player1, 0, 0, leviathan.getId());

        assertThat(nonblue.isTapped()).isFalse();
        harness.assertOnBattlefield(player1, "Breaching Leviathan");
    }

    @Test
    @DisplayName("Already tapped creatures of both players skip exactly their next untap")
    void alreadyTappedCreaturesSkipEachControllersNextUntapOnly() {
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposingCreature = addCreatureReady(player2, new GrizzlyBears());
        Permanent blueCreature = addCreatureReady(player2, new AirElemental());
        ownCreature.setTapped(true);
        opposingCreature.setTapped(true);
        blueCreature.setTapped(true);

        harness.setHand(player1, List.of(new BreachingLeviathan()));
        harness.addMana(player1, ManaColor.COLORLESS, 7);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.performUntapStep(player2);
        assertThat(opposingCreature.isTapped()).isTrue();
        assertThat(blueCreature.isTapped()).isFalse();
        assertThat(ownCreature.isTapped()).isTrue();

        harness.performUntapStep(player1);
        assertThat(ownCreature.isTapped()).isTrue();

        harness.performUntapStep(player2);
        assertThat(opposingCreature.isTapped()).isFalse();
        harness.performUntapStep(player1);
        assertThat(ownCreature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Creatures entering after the ability resolves are not prevented from untapping")
    void creaturesEnteringAfterResolutionAreNotAffected() {
        Permanent affected = addCreatureReady(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new BreachingLeviathan()));
        harness.addMana(player1, ManaColor.COLORLESS, 7);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        Permanent laterCreature = addCreatureReady(player2, new GrizzlyBears());
        laterCreature.setTapped(true);

        harness.performUntapStep(player2);
        assertThat(affected.isTapped()).isTrue();
        assertThat(laterCreature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The ability selects nonblue creatures when it resolves")
    void creaturesEnteringBeforeTriggerResolutionAreAffected() {
        harness.setHand(player1, List.of(new BreachingLeviathan()));
        harness.addMana(player1, ManaColor.COLORLESS, 7);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        resolveAllTriggers();

        assertThat(creature.isTapped()).isTrue();
        harness.performUntapStep(player2);
        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The untap restriction follows a creature to its new controller")
    void changedControllerSkipsNewControllersNextUntap() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new BreachingLeviathan(), new Persuasion()));
        harness.addMana(player1, ManaColor.COLORLESS, 7);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castEnchantment(player1, 0, creature.getId());
        resolveAllTriggers();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(creature);

        harness.performUntapStep(player2);
        harness.performUntapStep(player1);
        assertThat(creature.isTapped()).isTrue();

        harness.performUntapStep(player2);
        harness.performUntapStep(player1);
        assertThat(creature.isTapped()).isFalse();
    }
}
