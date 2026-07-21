package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzledLeotau;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.t.Terminate;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ClovenCastingTest extends BaseCardTest {

    private boolean hasClovenTrigger() {
        return gd.stack.stream().anyMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getCard().getName().equals("Cloven Casting"));
    }

    private long terminateCount() {
        return gd.stack.stream().filter(e -> e.getCard().getName().equals("Terminate")).count();
    }

    @Test
    @DisplayName("Casting a multicolored instant queues the copy trigger")
    void multicoloredInstantTriggers() {
        harness.addToBattlefield(player1, new ClovenCasting());
        Permanent victim = addCreatureReady(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new Terminate()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, victim.getId());

        assertThat(hasClovenTrigger()).isTrue();
    }

    @Test
    @DisplayName("Casting a monocolored instant does not trigger")
    void monocoloredInstantDoesNotTrigger() {
        harness.addToBattlefield(player1, new ClovenCasting());

        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());

        assertThat(hasClovenTrigger()).isFalse();
    }

    @Test
    @DisplayName("Casting a multicolored creature spell does not trigger")
    void multicoloredCreatureDoesNotTrigger() {
        harness.addToBattlefield(player1, new ClovenCasting());

        harness.setHand(player1, List.of(new GrizzledLeotau()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);

        assertThat(hasClovenTrigger()).isFalse();
    }

    @Test
    @DisplayName("Paying {1} copies the multicolored spell")
    void payingCopiesSpell() {
        harness.addToBattlefield(player1, new ClovenCasting());
        Permanent victim = addCreatureReady(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new Terminate()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, victim.getId());
        harness.passBothPriorities(); // resolve MayPayManaEffect -> may-pay prompt

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.handleMayAbilityChosen(player1, true); // pay {1} -> resolve copy

        assertThat(terminateCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("Declining the may-pay prompt creates no copy")
    void decliningCreatesNoCopy() {
        harness.addToBattlefield(player1, new ClovenCasting());
        Permanent victim = addCreatureReady(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new Terminate()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, victim.getId());
        harness.passBothPriorities(); // resolve MayPayManaEffect -> may-pay prompt
        harness.handleMayAbilityChosen(player1, false);

        assertThat(terminateCount()).isEqualTo(1);
    }
}
