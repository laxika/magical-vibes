package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzledLeotau;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.m.MaelstromPulse;
import com.github.laxika.magicalvibes.cards.r.Redirect;
import com.github.laxika.magicalvibes.cards.t.Terminate;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ClovenCasting.class, GrizzledLeotau.class, GrizzlyBears.class, LightningBolt.class,
        MaelstromPulse.class, Redirect.class, Terminate.class})
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

    @Test
    void opponentCastingMulticoloredInstantDoesNotTrigger() {
        harness.addToBattlefield(player1, new ClovenCasting());
        Permanent victim = harness.addToBattlefieldAndReturn(player1, new GrizzledLeotau());
        harness.setHand(player2, List.of(new Terminate()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.RED, 1);

        harness.passPriority(player1);
        harness.castInstant(player2, 0, victim.getId());

        assertThat(hasClovenTrigger()).isFalse();
    }

    @Test
    void payingCopiesMulticoloredSorcery() {
        harness.addToBattlefield(player1, new ClovenCasting());
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new GrizzledLeotau());
        harness.setHand(player1, List.of(new MaelstromPulse()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, victim.getId());
        assertThat(hasClovenTrigger()).isTrue();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.stack.stream().filter(e -> e.getCard().getName().equals("Maelstrom Pulse")))
                .hasSize(2);
        assertThat(hasClovenTrigger()).isFalse();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player2, "Grizzled Leotau");
        harness.passBothPriorities();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void copyCanDestroyADifferentCreatureWithoutChangingOriginalTarget() {
        harness.addToBattlefield(player1, new ClovenCasting());
        Permanent originalVictim = harness.addToBattlefieldAndReturn(player2, new GrizzledLeotau());
        Permanent copyVictim = harness.addToBattlefieldAndReturn(player2, new GrizzledLeotau());
        harness.setHand(player1, List.of(new Terminate()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, originalVictim.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, copyVictim.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(originalVictim).doesNotContain(copyVictim);
        assertThat(hasClovenTrigger()).isFalse();
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(originalVictim);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void copyKeepsTargetsChangedBeforeTheCopyTriggerResolves() {
        harness.addToBattlefield(player1, new ClovenCasting());
        Permanent originalVictim = harness.addToBattlefieldAndReturn(player2, new GrizzledLeotau());
        Permanent newVictim = harness.addToBattlefieldAndReturn(player2, new GrizzledLeotau());
        Terminate terminate = new Terminate();
        harness.setHand(player1, List.of(terminate));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setHand(player2, List.of(new Redirect()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0, originalVictim.getId());
        harness.passPriority(player1);
        harness.castInstant(player2, 0, terminate.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.handlePermanentChosen(player2, newVictim.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(originalVictim).doesNotContain(newVictim);
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(originalVictim);
        assertThat(gd.stack).isEmpty();
    }
}
