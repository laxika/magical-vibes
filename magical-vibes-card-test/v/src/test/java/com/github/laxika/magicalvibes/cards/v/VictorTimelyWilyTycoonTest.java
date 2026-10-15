package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.b.BraidwoodCup;
import com.github.laxika.magicalvibes.cards.c.Cloudshift;
import com.github.laxika.magicalvibes.cards.c.Counterspell;
import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.Fling;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JunkDiver;
import com.github.laxika.magicalvibes.cards.o.Opportunity;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VictorTimelyWilyTycoon.class, BraidwoodCup.class, Cloudshift.class, Counterspell.class,
        Divination.class, Fling.class, Forest.class,
        GrizzlyBears.class, JunkDiver.class, Opportunity.class, Shock.class})
class VictorTimelyWilyTycoonTest extends BaseCardTest {

    @Test
    @DisplayName("ETB targets nonland artifacts, instants, and sorceries with mana value 4 or less")
    void etbTargetsMatchingCards() {
        Card artifact = new BraidwoodCup();
        Card artifactCreature = new JunkDiver();
        Card instant = new Shock();
        Card sorcery = new Divination();
        Card creature = new GrizzlyBears();
        Card land = new Forest();
        Card expensive = new Opportunity();
        harness.setGraveyard(player1, List.of(artifact, artifactCreature, instant, sorcery,
                creature, land, expensive));

        castVictor();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(
                artifact.getId(), artifactCreature.getId(), instant.getId(), sorcery.getId());
    }

    @Test
    @DisplayName("ETB casts the chosen card for free and exiles it afterward")
    void castsChosenSorceryForFreeAndExilesIt() {
        Divination divination = new Divination();
        harness.setGraveyard(player1, List.of(divination));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));

        castVictor();
        harness.handleMultipleCardsChosen(player1, List.of(divination.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(divination.getId()));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(divination.getId()));
    }

    @Test
    @DisplayName("ETB does not trigger when the graveyard has no matching card")
    void noMatchingCardDoesNotPrompt() {
        Card creature = new GrizzlyBears();
        Card land = new Forest();
        Card expensive = new Opportunity();
        harness.setGraveyard(player1, List.of(creature, land, expensive));

        castVictor();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .containsExactly(creature.getId(), land.getId(), expensive.getId());
    }

    @Test
    void canDeclineCastingTheChosenCard() {
        Divination divination = new Divination();
        harness.setGraveyard(player1, List.of(divination));

        castVictor();
        harness.handleMultipleCardsChosen(player1, List.of(divination.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Divination");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    void castsTargetedInstantForFree() {
        Shock shock = new Shock();
        harness.setGraveyard(player1, List.of(shock));

        castVictor();
        harness.handleMultipleCardsChosen(player1, List.of(shock.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        harness.assertNotInGraveyard(player1, "Shock");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getId).contains(shock.getId());
    }

    @Test
    void cannotTargetCardsInOpponentsGraveyard() {
        Divination own = new Divination();
        Shock opponents = new Shock();
        harness.setGraveyard(player1, List.of(own));
        harness.setGraveyard(player2, List.of(opponents));

        castVictor();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(own.getId());
    }

    @Test
    void resolvedArtifactCreatureGoesToGraveyardWhenItDies() {
        JunkDiver diver = new JunkDiver();
        harness.setGraveyard(player1, List.of(diver));

        castVictor();
        harness.handleMultipleCardsChosen(player1, List.of(diver.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Junk Diver");

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, harness.getPermanentId(player1, "Junk Diver"));

        harness.assertInGraveyard(player1, "Junk Diver");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getId).doesNotContain(diver.getId());
    }

    @Test
    void freeCastStillRequiresSacrificingACreatureForFling() {
        Fling fling = new Fling();
        harness.setGraveyard(player1, List.of(fling));

        castVictor();
        var victorId = harness.getPermanentId(player1, "Victor Timely, Wily Tycoon");
        harness.handleMultipleCardsChosen(player1, List.of(fling.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        for (int i = 0; i < 2; i++) {
            PendingInteraction.PermanentChoice choice =
                    gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
            assertThat(choice).isNotNull();
            harness.handlePermanentChosen(player1,
                    choice.validPermanentIds().contains(player2.getId()) ? player2.getId() : victorId);
        }
        harness.assertNotOnBattlefield(player1, "Victor Timely, Wily Tycoon");
        harness.assertInGraveyard(player1, "Victor Timely, Wily Tycoon");
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getId).contains(fling.getId());
    }

    @Test
    void canCastCounterspellTargetingASpellBelowTheEnterTrigger() {
        Counterspell counterspell = new Counterspell();
        Shock shock = new Shock();
        harness.addToBattlefield(player1, new VictorTimelyWilyTycoon());
        harness.setGraveyard(player1, List.of(counterspell));
        harness.setHand(player2, List.of(shock));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, player1.getId());
        harness.setHand(player1, List.of(new Cloudshift()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castInstant(player1, 0,
                harness.getPermanentId(player1, "Victor Timely, Wily Tycoon"));
        harness.passBothPriorities();

        harness.handleMultipleCardsChosen(player1, List.of(counterspell.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, shock.getId());

        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getId().equals(counterspell.getId()));
        harness.passBothPriorities();
        harness.assertLife(player1, 20);
        harness.assertInGraveyard(player2, "Shock");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getId).contains(counterspell.getId());
    }

    private void castVictor() {
        harness.castFromHand(player1, new VictorTimelyWilyTycoon(), "{4}{U}");
        harness.passBothPriorities();
    }
}
