package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.r.RavensCrime;
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

@CardUsed({Necrogoyf.class, GrizzlyBears.class, Plains.class, RavensCrime.class})
class NecrogoyfTest extends BaseCardTest {

    @Test
    @DisplayName("Necrogoyf's power counts creature cards in all graveyards and toughness is four")
    void powerCountsCreaturesInAllGraveyards() {
        Permanent necrogoyf = addCreatureReady(player1, new Necrogoyf());
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new Plains()));
        harness.setGraveyard(player2, List.of(new GrizzlyBears(), new Plains()));

        assertThat(gqs.getEffectivePower(gd, necrogoyf)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, necrogoyf)).isEqualTo(4);
    }

    @Test
    @DisplayName("Each player's upkeep makes that player discard a card")
    void eachPlayersUpkeepCausesThatPlayerToDiscard() {
        addCreatureReady(player1, new Necrogoyf());
        GrizzlyBears player1Card = new GrizzlyBears();
        Plains player2Card = new Plains();
        harness.setHand(player1, List.of(player1Card));
        harness.setHand(player2, List.of(player2Card));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(player2Card);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(player1Card);

        advanceToUpkeep(player2);
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(player2Card);
    }

    @Test
    @DisplayName("Discarding Necrogoyf offers its madness cost")
    void discardOffersMadnessCast() {
        Necrogoyf necrogoyf = discardViaRavensCrime();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(necrogoyf.getId()));

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("Accepting Necrogoyf's madness cost casts it")
    void acceptingMadnessCastsCreature() {
        Necrogoyf necrogoyf = discardViaRavensCrime();
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(necrogoyf.getId()));
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    private Necrogoyf discardViaRavensCrime() {
        Necrogoyf necrogoyf = new Necrogoyf();
        harness.setHand(player1, List.of(necrogoyf));
        harness.setHand(player2, List.of(new RavensCrime()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castAndResolveSorcery(player2, 0, player1.getId());
        harness.handleCardChosen(player1, 0);
        return necrogoyf;
    }

    @Test
    @DisplayName("Power updates when creature cards enter or leave either graveyard")
    void powerUpdatesWithGraveyards() {
        Permanent necrogoyf = addCreatureReady(player1, new Necrogoyf());
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of());

        assertThat(gqs.getEffectivePower(gd, necrogoyf)).isZero();

        harness.setGraveyard(player2, List.of(new Necrogoyf(), new Necrogoyf()));
        assertThat(gqs.getEffectivePower(gd, necrogoyf)).isEqualTo(2);

        harness.setGraveyard(player1, List.of(new Necrogoyf()));
        assertThat(gqs.getEffectivePower(gd, necrogoyf)).isEqualTo(3);

        harness.setGraveyard(player2, List.of());
        assertThat(gqs.getEffectivePower(gd, necrogoyf)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, necrogoyf)).isEqualTo(4);
    }

    @Test
    @DisplayName("Necrogoyf defines its power in hand and counts itself in the graveyard")
    void powerWorksOutsideBattlefield() {
        Necrogoyf necrogoyf = new Necrogoyf();
        harness.setHand(player1, List.of(necrogoyf));
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of(new Necrogoyf()));

        assertThat(gqs.getEffectiveCardPower(gd, necrogoyf)).isEqualTo(1);

        harness.setHand(player1, List.of());
        harness.setGraveyard(player1, List.of(necrogoyf));

        assertThat(gqs.getEffectiveCardPower(gd, necrogoyf)).isEqualTo(2);
    }

    @Test
    @DisplayName("An empty hand does not prevent the upkeep trigger from resolving")
    void upkeepWithEmptyHandResolves() {
        addCreatureReady(player1, new Necrogoyf());
        harness.setHand(player2, List.of());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Declining madness after upkeep discard puts Necrogoyf in its owner's graveyard")
    void decliningUpkeepMadnessIncreasesPower() {
        Permanent source = addCreatureReady(player1, new Necrogoyf());
        Necrogoyf discarded = new Necrogoyf();
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of());
        harness.setHand(player2, List.of(discarded));

        advanceToUpkeep(player2);
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);

        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(discarded);
        assertThat(gqs.getEffectivePower(gd, source)).isZero();

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.getPlayerExiledCards(player2.getId())).doesNotContain(discarded);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(discarded);
        assertThat(gqs.getEffectivePower(gd, source)).isEqualTo(1);
    }
}
