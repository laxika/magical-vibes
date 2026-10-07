package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.cards.v.Victimize;
import com.github.laxika.magicalvibes.cards.w.WorldheartPhoenix;
import com.github.laxika.magicalvibes.cards.z.Zombify;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TwilightDiviner.class, GrizzlyBears.class, Zombify.class, WorldheartPhoenix.class,
        Unsummon.class, Victimize.class})
class TwilightDivinerTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield creates a surveil 2 interaction")
    void surveilsTwoOnEnter() {
        GameData gd = harness.getGameData();
        gd.playerDecks.get(player1.getId()).add(0, new GrizzlyBears());
        gd.playerDecks.get(player1.getId()).add(0, new GrizzlyBears());
        harness.setHand(player1, List.of(new TwilightDiviner()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(2);
    }

    @Test
    @DisplayName("A creature entering from a graveyard creates one token copy")
    void reanimatedCreatureCreatesTokenCopy() {
        Card firstCreature = new GrizzlyBears();
        harness.addToBattlefield(player1, new TwilightDiviner());
        harness.setGraveyard(player1, List.of(firstCreature));
        harness.setHand(player1, List.of(new Zombify()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, firstCreature.getId());
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Grizzly Bears")).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("The graveyard trigger does not fire for an ordinary creature or twice in one turn")
    void onlyTriggersOnceForGraveyardOrigin() {
        Card ordinaryCreature = new GrizzlyBears();
        Card firstCreature = new GrizzlyBears();
        Card secondCreature = new GrizzlyBears();
        harness.addToBattlefield(player1, new TwilightDiviner());
        harness.setHand(player1, List.of(ordinaryCreature));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        assertThat(countPermanents(player1, "Grizzly Bears")).isEqualTo(1);
        assertThat(gd.stack).isEmpty();

        harness.setGraveyard(player1, List.of(firstCreature, secondCreature));
        harness.setHand(player1, List.of(new Zombify(), new Zombify()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.castAndResolveSorcery(player1, 0, firstCreature.getId());
        harness.passBothPriorities();

        harness.castAndResolveSorcery(player1, 0, secondCreature.getId());

        assertThat(countPermanents(player1, "Grizzly Bears")).isEqualTo(4);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A creature cast from a graveyard creates a token copy")
    void castFromGraveyardCreatesTokenCopy() {
        harness.addToBattlefield(player1, new TwilightDiviner());
        harness.setGraveyard(player1, List.of(new WorldheartPhoenix()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castFromGraveyard(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Worldheart Phoenix")).isEqualTo(2);
    }

    @Test
    void copyDoesNotInheritCountersFromGraveyardCasting() {
        harness.addToBattlefield(player1, new TwilightDiviner());
        harness.setGraveyard(player1, List.of(new WorldheartPhoenix()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castFromGraveyard(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().getName().equals("Worldheart Phoenix")
                        && !permanent.getCard().isToken())
                .singleElement()
                .satisfies(permanent -> assertThat(permanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .singleElement()
                .satisfies(permanent -> assertThat(permanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero());
    }

    @Test
    void surveilCanPutOneCardIntoGraveyard() {
        Card kept = new GrizzlyBears();
        Card discarded = new GrizzlyBears();
        harness.setLibrary(player1, List.of(kept, discarded));
        harness.setHand(player1, List.of(new TwilightDiviner()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of(1)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(kept);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discarded).doesNotContain(kept);
    }

    @Test
    void surveilCanReorderBothCardsOnTop() {
        Card first = new GrizzlyBears();
        Card second = new GrizzlyBears();
        harness.setLibrary(player1, List.of(first, second));
        harness.setHand(player1, List.of(new TwilightDiviner()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(1, 0), List.of()));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second, first);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(first, second);
    }

    @Test
    void surveilCanPutBothCardsIntoGraveyard() {
        Card first = new GrizzlyBears();
        Card second = new GrizzlyBears();
        harness.setLibrary(player1, List.of(first, second));
        harness.setHand(player1, List.of(new TwilightDiviner()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(0, 1)));

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(first, second);
    }

    @Test
    void opponentGraveyardEntryDoesNotTrigger() {
        Card creature = new GrizzlyBears();
        harness.addToBattlefield(player2, new TwilightDiviner());
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new Zombify()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, creature.getId());

        assertThat(gd.stack).isEmpty();
        assertThat(countPermanents(player1, "Grizzly Bears")).isEqualTo(1);
        assertThat(countPermanents(player2, "Grizzly Bears")).isZero();
    }

    @Test
    void itsOwnGraveyardEntryDoesNotCreateCopy() {
        Card diviner = new TwilightDiviner();
        harness.setLibrary(player1, List.of());
        harness.setGraveyard(player1, List.of(diviner));
        harness.setHand(player1, List.of(new Zombify()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, diviner.getId());
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Twilight Diviner")).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void graveyardTriggerCanFireAgainOnALaterTurn() {
        Card first = new GrizzlyBears();
        Card second = new GrizzlyBears();
        harness.addToBattlefield(player1, new TwilightDiviner());
        harness.setGraveyard(player1, List.of(first, second));
        harness.setHand(player1, List.of(new Zombify()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castAndResolveSorcery(player1, 0, first.getId());
        harness.passBothPriorities();

        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        harness.passUntilWithNoAttackers(player1, TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Zombify()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castAndResolveSorcery(player1, 0, second.getId());
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Grizzly Bears")).isEqualTo(4);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken()).hasSize(2);
    }

    @Test
    void copiesCreatureThatLeavesBeforeTriggerResolves() {
        Card creature = new GrizzlyBears();
        harness.addToBattlefield(player1, new TwilightDiviner());
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new Zombify()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castAndResolveSorcery(player1, 0, creature.getId());

        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player2, 0, harness.getPermanentId(player1, "Grizzly Bears"));
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Grizzly Bears")).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerHands.get(player1.getId())).contains(creature);
    }

    @Test
    void mayChooseEitherCreatureFromSimultaneousGraveyardEntry() {
        Card first = new GrizzlyBears();
        Card second = new WorldheartPhoenix();
        harness.addToBattlefield(player1, new TwilightDiviner());
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(first, second));
        harness.setHand(player1, List.of(new Victimize()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0);
        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, sacrifice.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Worldheart Phoenix"));

        assertThat(countPermanents(player1, "Grizzly Bears")).isEqualTo(1);
        assertThat(countPermanents(player1, "Worldheart Phoenix")).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .singleElement()
                .satisfies(permanent -> assertThat(permanent.isTapped()).isFalse());
    }
}
