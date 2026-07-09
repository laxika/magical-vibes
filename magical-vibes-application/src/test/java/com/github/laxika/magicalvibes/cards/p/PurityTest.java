package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PurityTest extends BaseCardTest {

    @Test
    @DisplayName("Noncombat damage to Purity's controller is prevented and they gain that much life")
    void preventsNoncombatDamageAndGainsLife() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player2, new Purity());

        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        // 3 damage prevented (no life lost) + 3 life gained for the prevented damage.
        harness.assertLife(player2, 23);
    }

    @Test
    @DisplayName("When Purity is put into the graveyard it enters first, then a triggered ability shuffles it into its library")
    void diesThenTriggerShufflesIntoLibrary() {
        harness.setLibrary(player2, new java.util.ArrayList<>());
        Permanent purity = harness.addToBattlefieldAndReturn(player2, new Purity());
        // Mark lethal damage (6/6) and let state-based actions destroy it.
        purity.setMarkedDamage(6);

        harness.runStateBasedActions();

        // Triggered ability (not a replacement): Purity actually enters the graveyard and its
        // "put into graveyard from anywhere" ability is waiting on the stack.
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Purity"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Purity"));
        assertThat(gd.stack).isNotEmpty();

        harness.passBothPriorities();

        // After the trigger resolves, Purity is shuffled into its owner's library.
        harness.assertNotInGraveyard(player2, "Purity");
        assertThat(gd.playerDecks.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Purity"));
    }
}
