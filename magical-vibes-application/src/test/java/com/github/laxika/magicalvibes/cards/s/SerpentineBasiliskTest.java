package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AnuridMurkdiver;
import com.github.laxika.magicalvibes.cards.t.ThoughtboundPrimoc;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SerpentineBasilisk.class, AnuridMurkdiver.class, ThoughtboundPrimoc.class})
class SerpentineBasiliskTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage to a creature destroys it at end of combat")
    void combatDamageDestroysCreatureAtEndOfCombat() {
        Permanent basilisk = addCreatureReady(player1, new SerpentineBasilisk());
        basilisk.setAttacking(true);
        addCreatureReady(player2, new ThoughtboundPrimoc());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        resolveCombat();

        harness.assertNotOnBattlefield(player2, "Thoughtbound Primoc");
        harness.assertInGraveyard(player2, "Thoughtbound Primoc");
    }

    @Test
    @DisplayName("Combat damage to a player does not trigger the ability")
    void combatDamageToPlayerDoesNotTrigger() {
        Permanent basilisk = addCreatureReady(player1, new SerpentineBasilisk());
        basilisk.setAttacking(true);
        addCreatureReady(player2, new ThoughtboundPrimoc());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());
        resolveCombat();

        harness.assertLife(player2, 18);
        harness.assertOnBattlefield(player2, "Thoughtbound Primoc");
    }

    @Test
    @DisplayName("Combat damage dealt by another creature does not trigger the ability")
    void combatDamageByAnotherCreatureDoesNotTrigger() {
        addCreatureReady(player1, new SerpentineBasilisk());
        Permanent attacker = addCreatureReady(player1, new ThoughtboundPrimoc());
        attacker.setAttacking(true);
        addCreatureReady(player2, new ThoughtboundPrimoc());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));

        resolveCombat();

        harness.assertOnBattlefield(player2, "Thoughtbound Primoc");
    }

    @Test
    @DisplayName("The trigger still applies if the Basilisk dies after dealing combat damage")
    void triggerSurvivesSourceLeavingBattlefield() {
        Permanent basilisk = addCreatureReady(player1, new SerpentineBasilisk());
        basilisk.setAttacking(true);
        addCreatureReady(player2, new AnuridMurkdiver());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        resolveCombat();

        harness.assertNotOnBattlefield(player1, "Serpentine Basilisk");

        harness.assertNotOnBattlefield(player2, "Anurid Murkdiver");
        harness.assertInGraveyard(player2, "Anurid Murkdiver");
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
