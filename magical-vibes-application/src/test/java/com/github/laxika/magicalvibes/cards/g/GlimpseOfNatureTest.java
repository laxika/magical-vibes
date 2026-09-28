package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.l.LanternKami;
import com.github.laxika.magicalvibes.cards.l.LavaSpike;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GlimpseOfNature.class, LanternKami.class, LavaSpike.class})
class GlimpseOfNatureTest extends BaseCardTest {

    @Test
    @DisplayName("Each creature spell cast afterwards draws a card")
    void drawsOnEachCreatureSpell() {
        harness.setLibrary(player1, List.of(new LavaSpike(), new LavaSpike()));
        harness.castFromHand(player1, new GlimpseOfNature(), "{G}");

        harness.passBothPriorities();

        harness.setHand(player1, List.of(new LanternKami(), new LanternKami()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.clearPriorityPassed();
        harness.castCreature(player1, 0);
        resolveAllTriggers();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);

        harness.clearPriorityPassed();
        harness.castCreature(player1, 0);
        resolveAllTriggers();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Noncreature spells cast afterwards do not draw")
    void noDrawForNoncreatureSpells() {
        harness.setLibrary(player1, List.of(new LanternKami()));
        harness.castFromHand(player1, new GlimpseOfNature(), "{G}");

        harness.passBothPriorities();

        harness.setHand(player1, List.of(new LavaSpike()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.clearPriorityPassed();
        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("The delayed trigger stops working after the turn ends")
    void wearsOffAtEndOfTurn() {
        harness.setLibrary(player1, List.of(new LavaSpike(), new LavaSpike()));
        harness.castFromHand(player1, new GlimpseOfNature(), "{G}");

        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new LanternKami()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.clearPriorityPassed();
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Each Glimpse of Nature draws for the same creature spell")
    void multipleGlimpsesDrawForEachCreatureSpell() {
        harness.setLibrary(player1, List.of(new LavaSpike(), new LavaSpike()));
        harness.setHand(player1, List.of(new GlimpseOfNature(), new GlimpseOfNature()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new LanternKami()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.clearPriorityPassed();
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }
}
