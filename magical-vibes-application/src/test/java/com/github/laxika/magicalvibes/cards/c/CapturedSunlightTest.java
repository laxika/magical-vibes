package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CapturedSunlightTest extends BaseCardTest {

    // ===== Gain life =====

    @Test
    @DisplayName("Controller gains 4 life on resolution")
    void gainsFourLife() {
        harness.setLife(player1, 20);

        castCapturedSunlight(); // library holds only lands so cascade finds no hit
        harness.passBothPriorities(); // resolve the cascade trigger (bottoms the lands)
        harness.passBothPriorities(); // resolve the spell -> gain 4 life

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(24);
    }

    // ===== Cascade =====

    @Test
    @DisplayName("Cascade digs past a land and an equal-cost nonland to the first nonland with lesser mana value")
    void cascadeDigsToFirstLesserNonland() {
        harness.setLife(player1, 20);

        // Captured Sunlight is {2}{G}{W} = mana value 4. Dig skips the Plains and the MV-4 Hill Giant,
        // stops at Grizzly Bears (MV 2 < 4), and never touches the Llanowar Elves beneath it.
        LlanowarElves belowHit = new LlanowarElves();
        Plains land = new Plains();
        HillGiant skipped = new HillGiant();
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(land, skipped, new GrizzlyBears(), belowHit));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new CapturedSunlight()));
        addManaForCast();
        harness.castSorcery(player1, 0, 0);
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

        // The life gain still applies to the caster once everything resolves.
        harness.passBothPriorities(); // resolve Grizzly Bears
        harness.passBothPriorities(); // resolve Captured Sunlight
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(24);
    }

    // ===== Helpers =====

    private void castCapturedSunlight() {
        // Library holds only lands so cascade finds no hit and prompts nothing.
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(new Plains(), new Forest()));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new CapturedSunlight()));
        addManaForCast();
        harness.castSorcery(player1, 0, 0);
    }

    private void addManaForCast() {
        harness.addMana(player1, ManaColor.GREEN, 3); // {G} + {2}
        harness.addMana(player1, ManaColor.WHITE, 1); // {W}
    }
}
