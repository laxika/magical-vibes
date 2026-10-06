package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.n.NickFuryAgentOfSHIELD;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RickJonesDestinedSidekick.class, NickFuryAgentOfSHIELD.class,
        GloriousAnthem.class, Forest.class, Shock.class})
class RickJonesDestinedSidekickTest extends BaseCardTest {

    @Test
    @DisplayName("Mills four cards and may return a milled Hero")
    void millsAndReturnsHero() {
        NickFuryAgentOfSHIELD hero = new NickFuryAgentOfSHIELD();
        setTopCards(hero, new Forest(), new Shock(), new Shock());
        addReadyRick();

        activateRick();

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(4);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).contains(hero);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("May return a milled enchantment")
    void returnsEnchantment() {
        GloriousAnthem enchantment = new GloriousAnthem();
        setTopCards(enchantment, new Forest(), new Shock(), new Shock());
        addReadyRick();

        activateRick();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).contains(enchantment);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Does not offer a non-Hero non-enchantment card")
    void noMatchingMilledCard() {
        setTopCards(new Forest(), new Shock(), new Shock(), new Forest());
        addReadyRick();

        activateRick();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(4);
    }

    @Test
    void mayDeclineReturningEligibleCard() {
        NickFuryAgentOfSHIELD hero = new NickFuryAgentOfSHIELD();
        setTopCards(hero, new Forest(), new Forest(), new Forest());
        addReadyRick();

        activateRick();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(4).contains(hero);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(hero);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void returnsOnlyOneOfMultipleEligibleCards() {
        NickFuryAgentOfSHIELD hero = new NickFuryAgentOfSHIELD();
        GloriousAnthem enchantment = new GloriousAnthem();
        setTopCards(hero, enchantment, new Forest(), new Forest());
        addReadyRick();

        activateRick();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).contains(hero).doesNotContain(enchantment);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3).contains(enchantment);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void canDeclineFirstEligibleCardAndReturnLaterOne() {
        NickFuryAgentOfSHIELD hero = new NickFuryAgentOfSHIELD();
        GloriousAnthem enchantment = new GloriousAnthem();
        setTopCards(hero, enchantment, new Forest(), new Forest());
        addReadyRick();

        activateRick();
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).contains(enchantment).doesNotContain(hero);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3).contains(hero);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void millsShortLibraryAndReturnsEligibleCard() {
        NickFuryAgentOfSHIELD hero = new NickFuryAgentOfSHIELD();
        Forest land = new Forest();
        setTopCards(hero, land);
        addReadyRick();

        activateRick();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(hero, land);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(land);
        assertThat(gd.playerHands.get(player1.getId())).contains(hero);
    }

    @Test
    void cannotReturnEligibleCardsAlreadyInGraveyardOrBelowTopFour() {
        NickFuryAgentOfSHIELD oldHero = new NickFuryAgentOfSHIELD();
        GloriousAnthem unMilledEnchantment = new GloriousAnthem();
        harness.setGraveyard(player1, List.of(oldHero));
        setTopCards(new Forest(), new Forest(), new Forest(), new Forest(), unMilledEnchantment);
        addReadyRick();

        activateRick();

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(5).contains(oldHero);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(unMilledEnchantment);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(oldHero, unMilledEnchantment);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void activateRick() {
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
    }

    private Permanent addReadyRick() {
        Permanent rick = addCreatureReady(player1, new RickJonesDestinedSidekick());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return rick;
    }

    private void setTopCards(Card... cards) {
        harness.setLibrary(player1, List.of(cards));
    }
}
