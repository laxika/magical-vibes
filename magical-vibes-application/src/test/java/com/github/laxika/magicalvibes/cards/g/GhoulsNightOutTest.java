package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.AvacynsPilgrim;
import com.github.laxika.magicalvibes.cards.c.ChampionOfLambholt;
import com.github.laxika.magicalvibes.cards.m.MortuaryMire;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GhoulsNightOut.class, AvacynsPilgrim.class, MortuaryMire.class,
        ChampionOfLambholt.class, GorexTheTombshell.class})
class GhoulsNightOutTest extends BaseCardTest {

    @Test
    void controllerChoosesOneCreatureFromEachGraveyardAndTheyBecomeBlackDecayedZombies() {
        Card ownCreature = new AvacynsPilgrim();
        Card opposingCreature = new AvacynsPilgrim();
        harness.setGraveyard(player1, List.of(new MortuaryMire(), ownCreature));
        harness.setGraveyard(player2, List.of(new MortuaryMire(), opposingCreature));
        harness.setHand(player1, List.of(new GhoulsNightOut()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castAndResolveSorcery(player1, 0, 0);

        chooseOnlyCreatureFromGraveyard(player1.getId());
        chooseOnlyCreatureFromGraveyard(player1.getId());

        Permanent ownPermanent = findPermanent(ownCreature);
        Permanent opposingPermanent = findPermanent(opposingCreature);
        assertThat(gqs.hasColor(gd, ownPermanent, CardColor.BLACK)).isTrue();
        assertThat(gqs.hasColor(gd, ownPermanent, CardColor.GREEN)).isTrue();
        assertThat(gqs.hasColor(gd, opposingPermanent, CardColor.BLACK)).isTrue();
        assertThat(GameQueryService.permanentHasSubtype(ownPermanent, CardSubtype.ZOMBIE)).isTrue();
        assertThat(GameQueryService.permanentHasSubtype(opposingPermanent, CardSubtype.ZOMBIE)).isTrue();
        assertThat(GameQueryService.permanentHasSubtype(ownPermanent, CardSubtype.HUMAN)).isTrue();
        assertThat(GameQueryService.permanentHasSubtype(ownPermanent, CardSubtype.MONK)).isTrue();
        assertThat(gqs.hasKeyword(gd, ownPermanent, Keyword.DECAYED)).isTrue();
        assertThat(gqs.hasKeyword(gd, opposingPermanent, Keyword.DECAYED)).isTrue();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    void ignoresNoncreatureCardsInEachGraveyard() {
        harness.setGraveyard(player1, List.of(new MortuaryMire()));
        harness.setGraveyard(player2, List.of(new MortuaryMire()));
        harness.setHand(player1, List.of(new GhoulsNightOut()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
    }

    @Test
    void choosesOneCreaturePerGraveyardBeforeAnyEnter() {
        Card ownChosen = new AvacynsPilgrim();
        Card ownRemaining = new AvacynsPilgrim();
        Card opposingRemaining = new AvacynsPilgrim();
        Card opposingChosen = new AvacynsPilgrim();
        harness.setGraveyard(player1, List.of(ownChosen, ownRemaining));
        harness.setGraveyard(player2, List.of(opposingRemaining, opposingChosen));
        harness.setHand(player1, List.of(new GhoulsNightOut()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleGraveyardCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        PendingInteraction.GraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.cardPool()).containsExactly(opposingRemaining, opposingChosen);
        harness.handleGraveyardCardChosen(player1, 1);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getCard).containsExactlyInAnyOrder(ownChosen, opposingChosen);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(ownRemaining).doesNotContain(ownChosen);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opposingRemaining);
    }

    @Test
    void skipsEmptyControllerGraveyardAndReturnsOpponentsCreature() {
        Card creature = new AvacynsPilgrim();
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of(creature));
        harness.setHand(player1, List.of(new GhoulsNightOut()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castAndResolveSorcery(player1, 0, 0);
        chooseOnlyCreatureFromGraveyard(player1.getId());

        assertThat(findPermanent(creature).isTapped()).isFalse();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void simultaneouslyReturnedCreatureSeesOtherCreatureEnter() {
        Card champion = new ChampionOfLambholt();
        harness.setGraveyard(player1, List.of(new AvacynsPilgrim()));
        harness.setGraveyard(player2, List.of(champion));
        harness.setHand(player1, List.of(new GhoulsNightOut()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castAndResolveSorcery(player1, 0, 0);
        chooseOnlyCreatureFromGraveyard(player1.getId());
        chooseOnlyCreatureFromGraveyard(player1.getId());
        resolveAllTriggers();

        assertThat(findPermanent(champion).getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void returnedCreatureCannotBlockBecauseItHasDecayed() {
        Card blocker = new AvacynsPilgrim();
        harness.setGraveyard(player1, List.of(blocker));
        harness.setGraveyard(player2, List.of());
        harness.setHand(player1, List.of(new GhoulsNightOut()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.castAndResolveSorcery(player1, 0, 0);
        chooseOnlyCreatureFromGraveyard(player1.getId());
        addCreatureReady(player2, new AvacynsPilgrim());
        declareAttackersAndPrepareBlockers(player2, List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void returnedCreatureWithPrintedAttackTriggerStillGetsSacrificedForDecayed() {
        Card gorex = new GorexTheTombshell();
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of(gorex));
        harness.setHand(player1, List.of(new GhoulsNightOut()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.castAndResolveSorcery(player1, 0, 0);
        chooseOnlyCreatureFromGraveyard(player1.getId());
        Permanent attacker = findPermanent(gorex);
        attacker.setSummoningSick(false);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            resolveAllTriggers();
            assertThat(gd.playerBattlefields.get(player1.getId())).contains(attacker);
        });
        harness.passUntil(player1, TurnStep.END_OF_COMBAT);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(attacker);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(gorex);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(gorex);
    }

    private void chooseOnlyCreatureFromGraveyard(java.util.UUID chooserId) {
        PendingInteraction.GraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(chooserId);
        assertThat(choice.cardPool()).hasSize(1);
        harness.handleGraveyardCardChosen(player1, 0);
    }

    private Permanent findPermanent(Card card) {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(card.getId()))
                .findFirst()
                .orElseThrow();
    }
}
