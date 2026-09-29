package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.c.ColossalDreadmaw;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IntoTheTimeVortex.class, Mountain.class, Forest.class, ColossalDreadmaw.class, GrizzlyBears.class})
class IntoTheTimeVortexTest extends BaseCardTest {

    @Test
    void cascadeCastsFirstCheaperNonland() {
        Mountain skippedLand = new Mountain();
        ColossalDreadmaw skippedNonland = new ColossalDreadmaw();
        GrizzlyBears hit = new GrizzlyBears();
        Forest belowHit = new Forest();
        harness.setLibrary(player1, List.of(skippedLand, skippedNonland, hit, belowHit));
        harness.setHand(player1, List.of(new IntoTheTimeVortex()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .extracting(Card::getName)
                .containsExactly("Grizzly Bears");

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.stack).anyMatch(entry -> entry.getCard() == hit
                && entry.getEntryType() == StackEntryType.CREATURE_SPELL);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(belowHit, skippedLand, skippedNonland);
    }

    @Test
    void reboundOffersAFreeCastAtNextUpkeep() {
        IntoTheTimeVortex card = new IntoTheTimeVortex();
        harness.setLibrary(player1, List.of(new Mountain()));
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(card.getId())).isNotNull();

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(card.getId())).isNull();
        harness.assertInGraveyard(player1, "Into the Time Vortex");
    }
}
