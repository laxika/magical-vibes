package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.n.NightshadeStinger;
import com.github.laxika.magicalvibes.cards.w.WoodlandChangeling;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Cloudthresher.class, NightshadeStinger.class, WoodlandChangeling.class})
class CloudthresherTest extends BaseCardTest {

    private void addMana(com.github.laxika.magicalvibes.model.Player player) {
        harness.addMana(player, ManaColor.GREEN, 4);
        harness.addMana(player, ManaColor.COLORLESS, 2);
    }

    @Test
    @DisplayName("Hardcast ETB: 2 damage to each player, kills flyers, spares non-flyers, and stays on the battlefield")
    void hardcastEtbDamage() {
        harness.addToBattlefield(player2, new NightshadeStinger());
        harness.addToBattlefield(player2, new WoodlandChangeling());
        harness.setHand(player1, List.of(new Cloudthresher()));
        addMana(player1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        // 2 damage to each player
        harness.assertLife(player1, 18);
        harness.assertLife(player2, 18);

        // flyer destroyed, non-flyer survives
        harness.assertNotOnBattlefield(player2, "Nightshade Stinger");
        harness.assertOnBattlefield(player2, "Woodland Changeling");

        // Cloudthresher has Reach (not flying), so it is unaffected and remains
        harness.assertOnBattlefield(player1, "Cloudthresher");
    }

    @Test
    @DisplayName("Evoke: ETB still deals 2 damage to each player and Cloudthresher is sacrificed when its trigger resolves")
    void evokeSacrificesSelfButEtbFires() {
        harness.addToBattlefield(player2, new NightshadeStinger());
        harness.setHand(player1, List.of(new Cloudthresher()));
        addMana(player1); // evoke costs {2}{G}{G}; leftover mana is irrelevant

        harness.castCreatureWithEvoke(player1, 0, null);
        resolveAllTriggers();

        // ETB damage happened
        harness.assertLife(player1, 18);
        harness.assertLife(player2, 18);
        harness.assertNotOnBattlefield(player2, "Nightshade Stinger");

        // Cloudthresher is sacrificed by its separate triggered ability.
        harness.assertNotOnBattlefield(player1, "Cloudthresher");
        harness.assertInGraveyard(player1, "Cloudthresher");
    }

    @Test
    void damagesFriendlyFlyersAsWellAsOpposingFlyers() {
        harness.addToBattlefield(player1, new NightshadeStinger());
        harness.addToBattlefield(player2, new NightshadeStinger());
        harness.setHand(player1, List.of(new Cloudthresher()));
        addMana(player1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Nightshade Stinger");
        harness.assertInGraveyard(player2, "Nightshade Stinger");
        harness.assertLife(player1, 18);
        harness.assertLife(player2, 18);
    }

    @Test
    void canBeEvokedWithExactlyFourManaDuringOpponentsUpkeep() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);
        harness.setHand(player1, List.of(new Cloudthresher()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreatureWithEvoke(player1, 0, null);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Cloudthresher");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);

        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Cloudthresher");
        harness.assertLife(player1, 18);
        harness.assertLife(player2, 18);
    }

    @Test
    void canBeHardcastDuringOpponentsUpkeepWithoutBeingSacrificed() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);
        harness.setHand(player1, List.of(new Cloudthresher()));
        addMana(player1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Cloudthresher");
        harness.assertLife(player1, 18);
        harness.assertLife(player2, 18);
    }

    @Test
    void reachAllowsBlockingAFlyingAttacker() {
        addCreatureReady(player1, new NightshadeStinger());
        Permanent cloudthresher = addCreatureReady(player2, new Cloudthresher());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(cloudthresher.isBlocking()).isTrue();
    }
}
