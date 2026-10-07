package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.CounselOfTheSoratami;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.Reclaim;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheLocustGod.class, CounselOfTheSoratami.class, GrizzlyBears.class, WrathOfGod.class,
        Reclaim.class, TragicLesson.class})
class TheLocustGodTest extends BaseCardTest {

    private long insectTokenCount(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().isToken() && p.getCard().getName().equals("Insect"))
                .count();
    }

    private Permanent findInsect(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().isToken() && p.getCard().getName().equals("Insect"))
                .findFirst()
                .orElseThrow();
    }

    private void advanceToDraw(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        gd.turnNumber = 2; // avoid first-turn draw skip
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advances from UPKEEP to DRAW
    }

    @Test
    @DisplayName("Draw step draw creates a 1/1 blue-red Insect with flying and haste")
    void drawCreatesInsectToken() {
        harness.addToBattlefield(player1, new TheLocustGod());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        advanceToDraw(player1);
        harness.passBothPriorities(); // resolve the draw trigger

        assertThat(insectTokenCount(player1)).isEqualTo(1);
        Permanent insect = findInsect(player1);
        assertThat(insect.getCard().getPower()).isEqualTo(1);
        assertThat(insect.getCard().getToughness()).isEqualTo(1);
        assertThat(insect.getCard().getColors()).containsExactlyInAnyOrder(CardColor.BLUE, CardColor.RED);
        assertThat(insect.getCard().getSubtypes()).contains(CardSubtype.INSECT);
        assertThat(insect.hasKeyword(Keyword.FLYING)).isTrue();
        assertThat(insect.hasKeyword(Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Drawing multiple cards creates one Insect per card drawn")
    void createsOneInsectPerCardDrawn() {
        harness.addToBattlefield(player1, new TheLocustGod());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player1, List.of(new CounselOfTheSoratami()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities(); // resolve Counsel (draws 2)
        harness.passBothPriorities(); // first Insect trigger
        harness.passBothPriorities(); // second Insect trigger

        assertThat(insectTokenCount(player1)).isEqualTo(2);
    }

    @Test
    @DisplayName("Opponent drawing does not create Insect tokens")
    void doesNotTriggerOnOpponentDraw() {
        harness.addToBattlefield(player1, new TheLocustGod());
        harness.setLibrary(player2, List.of(new GrizzlyBears()));

        advanceToDraw(player2);

        assertThat(insectTokenCount(player1)).isZero();
    }

    @Test
    @DisplayName("Activated ability draws then discards, and the draw creates an Insect")
    void lootAbilityDrawsDiscardsAndCreatesInsect() {
        harness.addToBattlefield(player1, new TheLocustGod());
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities(); // resolve loot: draw + discard prompt

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0); // discard

        harness.passBothPriorities(); // resolve Insect trigger from the draw

        assertThat(insectTokenCount(player1)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("When The Locust God dies, it returns to hand at the beginning of the next end step")
    void diesReturnsToHandAtNextEndStep() {
        Permanent locust = harness.addToBattlefieldAndReturn(player1, new TheLocustGod());
        Card locustCard = locust.getCard();

        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castSorcery(player1, 0);
        harness.passBothPriorities(); // Wrath resolves — Locust dies, death trigger on stack
        harness.passBothPriorities(); // resolve death trigger — register delayed return

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(locustCard.getId()));

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.forceActivePlayer(player1);
        gs.advanceStep(gd);

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(locustCard);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(locustCard.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getId().equals(locustCard.getId()));
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Looting with an empty hand discards the newly drawn card and still creates an Insect")
    void lootWithEmptyHandDiscardsDrawnCard() {
        harness.addToBattlefield(player1, new TheLocustGod());
        Card drawn = new GrizzlyBears();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawn));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.DiscardChoice) {
            harness.handleCardChosen(player1, 0);
        }
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(drawn);
        assertThat(insectTokenCount(player1)).isEqualTo(1);
    }

    @Test
    @DisplayName("The delayed return cannot retrieve The Locust God after it leaves the graveyard")
    void delayedReturnDoesNotReturnCardMovedToLibrary() {
        Card locust = new TheLocustGod();
        harness.addToBattlefield(player1, locust);
        harness.setHand(player1, List.of(new WrathOfGod(), new Reclaim()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castSorcery(player1, 0);
        resolveAllTriggers();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        gs.advanceStep(gd);
        assertThat(gd.stack).hasSize(1);

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castInstant(player1, 0, locust.getId());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(locust);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isEqualTo(locust);
    }

    @Test
    @DisplayName("A death trigger cannot track a card that leaves and reenters the graveyard before it resolves")
    void deathTriggerDoesNotTrackNewGraveyardObject() {
        Card locust = new TheLocustGod();
        harness.addToBattlefield(player1, locust);
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new WrathOfGod(), new Reclaim(), new TragicLesson()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castSorcery(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castInstant(player1, 0, locust.getId());
        harness.passBothPriorities();
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(locust);

        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(locust);
        resolveAllTriggers();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        gs.advanceStep(gd);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(locust);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(locust);
    }
}
