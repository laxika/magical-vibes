package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SerpentineBasilisk.class, GiantSpider.class})
class SerpentineBasiliskTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage to a creature destroys it at end of combat")
    void combatDamageDestroysCreatureAtEndOfCombat() {
        Permanent basilisk = addCreatureReady(player1, new SerpentineBasilisk());
        basilisk.setAttacking(true);
        addCreatureReady(player2, new GiantSpider());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Giant Spider");
        harness.assertInGraveyard(player2, "Giant Spider");
    }

    @Test
    @DisplayName("Combat damage to a player does not trigger the ability")
    void combatDamageToPlayerDoesNotTrigger() {
        Permanent basilisk = addCreatureReady(player1, new SerpentineBasilisk());
        basilisk.setAttacking(true);
        addCreatureReady(player2, new GiantSpider());

        prepareDeclareBlockers();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Giant Spider");
    }

    @Test
    @DisplayName("Can be cast face down and turned face up for its morph cost")
    void morphsFaceDownAndCanBeTurnedFaceUp() {
        harness.setHand(player1, List.of(new SerpentineBasilisk()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent basilisk = findPermanent(player1, "Serpentine Basilisk");
        assertThat(basilisk.isFaceDown()).isTrue();

        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        int basiliskIndex = gd.playerBattlefields.get(player1.getId()).indexOf(basilisk);
        harness.turnFaceUp(player1, basiliskIndex);
        harness.passBothPriorities();

        assertThat(basilisk.isFaceDown()).isFalse();
    }
}
