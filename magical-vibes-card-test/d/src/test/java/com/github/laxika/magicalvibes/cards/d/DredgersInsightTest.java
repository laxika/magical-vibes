package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AlpineMoon;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.Reminisce;
import com.github.laxika.magicalvibes.cards.r.RestInPeace;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
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

@CardUsed({DredgersInsight.class, AlpineMoon.class, Forest.class, GrizzlyBears.class,
        Reminisce.class, RestInPeace.class, Shock.class, Spellbook.class})
class DredgersInsightTest extends BaseCardTest {

    @Test
    @DisplayName("Mills four cards and offers an eligible milled card")
    void millsAndOffersEligibleCard() {
        Card forest = new Forest();
        setDeck(forest, new Shock(), new Shock(), new Shock());

        castInsight();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Forest");
        harness.assertInGraveyard(player1, "Shock");
    }

    @Test
    @DisplayName("Does not offer an enchantment milled by its enters ability")
    void doesNotOfferEnchantment() {
        setDeck(new AlpineMoon(), new Shock(), new Shock(), new Shock());

        castInsight();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Alpine Moon");
        harness.assertNotInHand(player1, "Alpine Moon");
    }

    @Test
    @DisplayName("Gains life once when multiple artifact cards leave the graveyard together")
    void gainsLifeOnceForMultipleArtifactsLeavingTogether() {
        harness.addToBattlefield(player1, new DredgersInsight());
        harness.setLife(player1, 20);
        harness.setGraveyard(player1, List.of(new Spellbook(), new Spellbook()));
        harness.setHand(player1, List.of(new Reminisce()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castAndResolveSorcery(player1, 0, player1.getId());

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
    }

    @Test
    @DisplayName("Does not gain life when a land leaves the graveyard")
    void doesNotGainLifeForLandLeaving() {
        harness.addToBattlefield(player1, new DredgersInsight());
        harness.setLife(player1, 20);
        harness.setGraveyard(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new Reminisce()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castAndResolveSorcery(player1, 0, player1.getId());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    void mayDeclineToReturnEligibleCard() {
        setDeck(new GrizzlyBears(), new Shock(), new Shock(), new Shock());
        harness.setLife(player1, 20);

        castInsight();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotInHand(player1, "Grizzly Bears");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    void returnsCreatureAndTriggersLifeGain() {
        setDeck(new GrizzlyBears(), new Shock(), new Shock(), new Shock());
        harness.setLife(player1, 20);

        castInsight();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        harness.passBothPriorities();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
    }

    @Test
    void returnsOnlyOneOfMultipleEligibleCards() {
        setDeck(new Spellbook(), new GrizzlyBears(), new Forest(), new Shock(), new Shock());
        harness.setLife(player1, 20);

        castInsight();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Spellbook");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Forest");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.passBothPriorities();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
    }

    @Test
    void doesNotOfferCardsAlreadyInGraveyard() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new Spellbook()));
        setDeck(new Shock(), new Shock(), new Shock(), new Shock());

        castInsight();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertNotInHand(player1, "Grizzly Bears");
        harness.assertNotInHand(player1, "Spellbook");
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(6);
    }

    @Test
    void millsRemainingCardsWhenLibraryHasFewerThanFour() {
        setDeck(new Forest(), new Shock());

        castInsight();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.assertInHand(player1, "Forest");
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void doesNotGainLifeForOpponentsCardsLeavingGraveyard() {
        harness.addToBattlefield(player1, new DredgersInsight());
        harness.setLife(player1, 20);
        harness.setGraveyard(player2, List.of(new Spellbook(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new Reminisce()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    void canRecoverCardMilledDirectlyIntoExile() {
        harness.addToBattlefield(player2, new RestInPeace());
        harness.setLife(player1, 20);
        Card creature = new GrizzlyBears();
        setDeck(creature, new Shock(), new Shock(), new Shock());

        castInsight();

        assertThat(gd.exiledCards).anyMatch(entry -> entry.card().getId().equals(creature.getId()));
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(gd.exiledCards).noneMatch(entry -> entry.card().getId().equals(creature.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    private void castInsight() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new DredgersInsight()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void setDeck(Card... cards) {
        harness.setLibrary(player1, List.of(cards));
    }
}
