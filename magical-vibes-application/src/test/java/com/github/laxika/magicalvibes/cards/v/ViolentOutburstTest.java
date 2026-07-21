package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ViolentOutburstTest extends BaseCardTest {

    // ===== Pump =====

    @Test
    @DisplayName("Creatures you control get +1/+0; opponents' creatures unaffected")
    void pumpsOnlyOwnCreatures() {
        Permanent mine = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());   // 2/2
        Permanent theirs = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()); // 2/2

        castViolentOutburst();
        harness.passBothPriorities(); // resolve the cascade trigger (no hit, bottoms lands)
        harness.passBothPriorities(); // resolve the spell -> pump

        assertThat(mine.getEffectivePower()).isEqualTo(3);
        assertThat(mine.getEffectiveToughness()).isEqualTo(2);
        assertThat(theirs.getEffectivePower()).isEqualTo(2);
        assertThat(theirs.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOff() {
        Permanent mine = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castViolentOutburst();
        harness.passBothPriorities(); // cascade trigger
        harness.passBothPriorities(); // spell
        assertThat(mine.getEffectivePower()).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(mine.getEffectivePower()).isEqualTo(2);
    }

    // ===== Cascade =====

    @Test
    @DisplayName("Cascade digs past a land and a costlier nonland to the first nonland with lesser mana value")
    void cascadeDigsToFirstLesserNonland() {
        Permanent mine = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        // Violent Outburst is {1}{R}{G} = mana value 3. Dig skips the Mountain and the MV-4 Hill Giant,
        // stops at Grizzly Bears (MV 2 < 3), and never touches the Llanowar Elves beneath it.
        LlanowarElves belowHit = new LlanowarElves();
        Mountain land = new Mountain();
        HillGiant skipped = new HillGiant();
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(land, skipped, new GrizzlyBears(), belowHit));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new ViolentOutburst()));
        addManaForCast();
        harness.castInstant(player1, 0);
        harness.passBothPriorities(); // resolve the cascade trigger

        // The single castable card offered is Grizzly Bears (the qualifying hit).
        List<String> castable = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().cards().stream().map(Card::getName).toList();
        assertThat(castable).containsExactly("Grizzly Bears");

        // Cast the offered hit for free.
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));
        assertThat(gd.stack).anyMatch(se -> se.getCard().getName().equals("Grizzly Bears")
                && se.getEntryType() == StackEntryType.CREATURE_SPELL);

        // Non-hit exiled cards (land + Hill Giant) go to the bottom; the below-hit card remains.
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(belowHit, land, skipped);

        // The pump still applies to the caster's creature once everything resolves.
        harness.passBothPriorities(); // resolve Grizzly Bears
        harness.passBothPriorities(); // resolve Violent Outburst
        assertThat(mine.getEffectivePower()).isEqualTo(3);
    }

    // ===== Helpers =====

    private void castViolentOutburst() {
        // Library holds only lands so cascade finds no hit and prompts nothing.
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(new Mountain(), new Forest()));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new ViolentOutburst()));
        addManaForCast();
        harness.castInstant(player1, 0);
    }

    private void addManaForCast() {
        harness.addMana(player1, ManaColor.RED, 2);   // {1} + {R}
        harness.addMana(player1, ManaColor.GREEN, 1); // {G}
    }
}
