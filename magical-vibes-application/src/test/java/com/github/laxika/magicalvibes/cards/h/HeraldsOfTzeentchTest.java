package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HeraldsOfTzeentch.class, Forest.class, GrizzlyBears.class})
class HeraldsOfTzeentchTest extends BaseCardTest {

    @Test
    @DisplayName("Cascade finds the first nonland card with lesser mana value")
    void cascadeFindsFirstLesserNonland() {
        setupCasterTurn();
        GrizzlyBears hit = new GrizzlyBears();
        harness.setLibrary(player1, List.of(new Forest(), new HeraldsOfTzeentch(), hit));

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
        harness.setLibrary(player1, List.of(land, skipped, hit));

        castHeralds();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.stack).anyMatch(entry -> entry.getCard() == hit
                && entry.getEntryType() == StackEntryType.CREATURE_SPELL);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(land, skipped);
    }

    @Test
    @DisplayName("Cascade bottoms all exiled cards when no qualifying card is found")
    void cascadeWithNoHitBottomsEverything() {
        setupCasterTurn();
        harness.setLibrary(player1, List.of(new Forest(), new HeraldsOfTzeentch()));

        castHeralds();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Declining cascade returns the hit and skipped cards below the untouched library")
    void decliningCascadeBottomsAllExiledCards() {
        setupCasterTurn();
        Forest skipped = new Forest();
        GrizzlyBears hit = new GrizzlyBears();
        Forest untouched = new Forest();
        harness.setLibrary(player1, List.of(skipped, hit, untouched));

        castHeralds();
        harness.passBothPriorities();
        assertThat(gd.findExiledCard(skipped.getId())).isSameAs(skipped);
        assertThat(gd.findExiledCard(hit.getId())).isSameAs(hit);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(untouched);

        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(untouched);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(untouched, skipped, hit);
        assertThat(gd.findExiledCard(skipped.getId())).isNull();
        assertThat(gd.findExiledCard(hit.getId())).isNull();
        assertThat(gd.stack).noneMatch(entry -> entry.getCard() == hit);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Heralds of Tzeentch");
    }

    @Test
    @DisplayName("Cascade with an empty library does not prevent the original spell from resolving")
    void cascadeWithEmptyLibrary() {
        setupCasterTurn();
        harness.setLibrary(player1, List.of());

        castHeralds();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Heralds of Tzeentch");
    }

    @Test
    @DisplayName("Entering the battlefield without being cast does not trigger cascade")
    void enteringWithoutCastingDoesNotCascade() {
        setupCasterTurn();
        GrizzlyBears top = new GrizzlyBears();
        harness.setLibrary(player1, List.of(top));

        harness.enterBattlefieldAndReturn(player1, new HeraldsOfTzeentch());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top);
    }

    private void setupCasterTurn() {
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player1);
    }

    private void castHeralds() {
        harness.castFromHand(player1, new HeraldsOfTzeentch(), "{4}{U}");
    }
}
