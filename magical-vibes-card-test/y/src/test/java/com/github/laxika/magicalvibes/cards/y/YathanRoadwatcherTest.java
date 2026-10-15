package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GatherSpecimens;
import com.github.laxika.magicalvibes.cards.h.HardenedTactician;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.o.Opt;
import com.github.laxika.magicalvibes.cards.z.Zombify;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({YathanRoadwatcher.class, Forest.class, GrizzlyBears.class, HillGiant.class, Opt.class, Zombify.class, GatherSpecimens.class, HardenedTactician.class})
class YathanRoadwatcherTest extends BaseCardTest {

    @Test
    @DisplayName("When cast, mills four cards then returns a target small creature")
    void castMillsThenReturnsTargetCreature() {
        GrizzlyBears eligible = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(eligible, new Opt(), new HillGiant()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest()));

        castYathan();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)
                .validCardIds()).containsExactly(eligible.getId());
        harness.handleMultipleCardsChosen(player1, List.of(eligible.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(6)
                .extracting(Card::getName)
                .contains("Opt", "Hill Giant");
    }

    @Test
    @DisplayName("Does not return a card when the graveyard has no matching creature")
    void doesNothingWithoutMatchingCreature() {
        harness.setGraveyard(player1, List.of(new Opt(), new HillGiant()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest()));

        castYathan();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(6);
    }

    @Test
    @DisplayName("Does not mill when it enters the battlefield without being cast")
    void doesNotTriggerWhenNotCast() {
        harness.setGraveyard(player1, List.of(new YathanRoadwatcher()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest()));
        harness.setHand(player1, List.of(new Zombify()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveSorcery(player1, 0, gd.playerGraveyards.get(player1.getId()).getFirst().getId());

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(4);
    }

    @Test
    @DisplayName("A newly milled creature with mana value exactly three is a legal target")
    void returnsNewlyMilledCreatureAtManaValueLimit() {
        HardenedTactician eligible = new HardenedTactician();
        HardenedTactician opposing = new HardenedTactician();
        YathanRoadwatcher tooLarge = new YathanRoadwatcher();
        harness.setGraveyard(player1, List.of(tooLarge));
        harness.setGraveyard(player2, List.of(opposing));
        harness.setLibrary(player1, List.of(eligible, new Forest(), new Forest(), new Forest()));

        castYathan();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)
                .validCardIds()).containsExactly(eligible.getId());
        harness.assertNotOnBattlefield(player1, "Hardened Tactician");
        harness.handleMultipleCardsChosen(player1, List.of(eligible.getId()));
        harness.assertNotOnBattlefield(player1, "Hardened Tactician");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Hardened Tactician");
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(tooLarge).doesNotContain(eligible);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opposing);
    }

    @Test
    @DisplayName("A library with fewer than four cards still permits the reflexive return")
    void returnsCreatureAfterMillingShortLibrary() {
        HardenedTactician eligible = new HardenedTactician();
        harness.setGraveyard(player1, List.of());
        harness.setLibrary(player1, List.of(eligible, new Forest()));

        castYathan();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)
                .validCardIds()).containsExactly(eligible.getId());
        harness.handleMultipleCardsChosen(player1, List.of(eligible.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Hardened Tactician");
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Does not trigger when Gather Specimens redirects another player's cast creature")
    void doesNotTriggerWhenEnteringControllerDidNotCastIt() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new GatherSpecimens()));
        harness.addMana(player2, ManaColor.BLUE, 6);
        harness.castAndResolveInstant(player2, 0);
        harness.setLibrary(player2, List.of(new Forest(), new Forest(), new Forest(), new Forest()));
        harness.setGraveyard(player2, List.of(new HardenedTactician()));

        castYathan();

        harness.assertOnBattlefield(player2, "Yathan Roadwatcher");
        harness.assertNotOnBattlefield(player1, "Yathan Roadwatcher");
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(4);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    private void castYathan() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new YathanRoadwatcher()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
