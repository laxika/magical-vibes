package com.github.laxika.magicalvibes.cards.h;

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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HeraldsOfTzeentch.class, Forest.class, Mountain.class, GrizzlyBears.class})
class HeraldsOfTzeentchTest extends BaseCardTest {

    @Test
    @DisplayName("Cascade finds the first nonland card with lesser mana value")
    void cascadeFindsFirstLesserNonland() {
        setupCasterTurn();
        gd.playerDecks.get(player1.getId()).clear();
        GrizzlyBears hit = new GrizzlyBears();
        gd.playerDecks.get(player1.getId()).addAll(List.of(new Forest(), new HeraldsOfTzeentch(), hit));

        castHeralds();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        List<Card> offered = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().cards();
        assertThat(offered).containsExactly(hit);
    }

    @Test
    @DisplayName("Choosing the cascade hit casts it without paying and bottoms the exiled cards")
    void cascadeCastsHitForFree() {
        setupCasterTurn();
        Forest land = new Forest();
        HeraldsOfTzeentch skipped = new HeraldsOfTzeentch();
        GrizzlyBears hit = new GrizzlyBears();
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(land, skipped, hit));

        castHeralds();
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.stack).anyMatch(entry -> entry.getCard() == hit
                && entry.getEntryType() == StackEntryType.CREATURE_SPELL);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(land, skipped);
    }

    @Test
    @DisplayName("Cascade bottoms all exiled cards when no qualifying card is found")
    void cascadeWithNoHitBottomsEverything() {
        setupCasterTurn();
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(new Forest(), new HeraldsOfTzeentch()));

        castHeralds();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
    }

    private void setupCasterTurn() {
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player1);
    }

    private void castHeralds() {
        harness.setHand(player1, List.of(new HeraldsOfTzeentch()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castCreature(player1, 0);
    }
}
