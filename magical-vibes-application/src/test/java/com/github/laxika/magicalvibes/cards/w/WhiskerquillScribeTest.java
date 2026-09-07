package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WhiskerquillScribe.class, Forest.class, GiantGrowth.class})
class WhiskerquillScribeTest extends BaseCardTest {

    @Test
    void valiantMayDiscardThenDraw() {
        Forest discarded = new Forest();
        Forest drawn = new Forest();
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).add(drawn);
        Permanent scribe = harness.addToBattlefieldAndReturn(player1, new WhiskerquillScribe());
        harness.setHand(player1, new ArrayList<>(List.of(new GiantGrowth(), discarded)));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, scribe.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(discarded);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
    }

    @Test
    void valiantCanBeDeclined() {
        Forest discarded = new Forest();
        Forest drawn = new Forest();
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).add(drawn);
        Permanent scribe = harness.addToBattlefieldAndReturn(player1, new WhiskerquillScribe());
        harness.setHand(player1, new ArrayList<>(List.of(new GiantGrowth(), discarded)));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, scribe.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(discarded);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(drawn);
    }

    @Test
    void valiantTriggersOnlyOncePerTurn() {
        Forest discarded = new Forest();
        Permanent scribe = harness.addToBattlefieldAndReturn(player1, new WhiskerquillScribe());
        harness.setHand(player1, new ArrayList<>(List.of(new GiantGrowth(), new GiantGrowth(), discarded)));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castInstant(player1, 0, scribe.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.castInstant(player1, 0, scribe.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    void opponentsSpellsDoNotTriggerValiant() {
        Permanent scribe = harness.addToBattlefieldAndReturn(player1, new WhiskerquillScribe());
        harness.setHand(player2, List.of(new GiantGrowth()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.castInstant(player2, 0, scribe.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }
}
