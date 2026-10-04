package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.w.WoodlandChangeling;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Glarewielder.class, WoodlandChangeling.class})
class GlarewielderTest extends BaseCardTest {

    private Permanent addBears(Player player) {
        return harness.addToBattlefieldAndReturn(player, new WoodlandChangeling());
    }

    @Test
    @DisplayName("Hardcast: ETB makes both target creatures unable to block; Glarewielder stays")
    void hardcastTwoCreaturesCantBlock() {
        Permanent bear1 = addBears(player2);
        Permanent bear2 = addBears(player2);
        harness.setHand(player1, List.of(new Glarewielder()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0, List.of(bear1.getId(), bear2.getId()));
        resolveAllTriggers();

        assertThat(bear1.isCantBlockThisTurn()).isTrue();
        assertThat(bear2.isCantBlockThisTurn()).isTrue();
        harness.assertOnBattlefield(player1, "Glarewielder");
    }

    @Test
    @DisplayName("Evoke: one target can't block and Glarewielder is sacrificed by its separate trigger")
    void evokeSacrificesSelfAndCantBlock() {
        Permanent bear = addBears(player2);
        harness.setHand(player1, List.of(new Glarewielder()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreatureWithEvoke(player1, 0, bear.getId());
        resolveAllTriggers();

        assertThat(bear.isCantBlockThisTurn()).isTrue();
        harness.assertNotOnBattlefield(player1, "Glarewielder");
        harness.assertInGraveyard(player1, "Glarewielder");
    }

    @Test
    @DisplayName("Can enter with zero targets (up to two)")
    void canEnterWithNoTargets() {
        harness.setHand(player1, List.of(new Glarewielder()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0, List.<UUID>of());
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Glarewielder");
    }

    @Test
    @DisplayName("Evoke with no targets still sacrifices Glarewielder")
    void evokeWithNoTargetsStillSacrifices() {
        harness.setHand(player1, List.of(new Glarewielder()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreatureWithEvoke(player1, 0, null);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Glarewielder");
        harness.assertInGraveyard(player1, "Glarewielder");
    }

    @Test
    @DisplayName("A remaining legal target is affected when the other target leaves")
    void remainingTargetStillCantBlock() {
        Permanent bear1 = addBears(player2);
        Permanent bear2 = addBears(player2);
        harness.setHand(player1, List.of(new Glarewielder()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0, List.of(bear1.getId(), bear2.getId()));
        harness.passBothPriorities();
        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, bear1);
        resolveAllTriggers();

        assertThat(bear2.isCantBlockThisTurn()).isTrue();
        harness.assertOnBattlefield(player1, "Glarewielder");
    }

    @Test
    @DisplayName("Evoke sacrifice still resolves when the can't-block target leaves")
    void evokeSacrificeIsIndependentOfTargetLegality() {
        Permanent creature = addBears(player2);
        harness.setHand(player1, List.of(new Glarewielder()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreatureWithEvoke(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, creature);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Glarewielder");
        harness.assertInGraveyard(player1, "Glarewielder");
    }
}
