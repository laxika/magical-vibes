package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LordWindgrace.class, Forest.class, Mountain.class, GrizzlyBears.class})
class LordWindgraceTest extends BaseCardTest {

    @Test
    @DisplayName("+2 draws two cards when the discarded card is a land")
    void plusTwoDrawsAdditionalCardForLand() {
        Permanent windgrace = addReadyWindgrace(3);
        Card discardedLand = new Forest();
        Card firstDraw = new GrizzlyBears();
        Card secondDraw = new Forest();
        harness.setHand(player1, List.of(discardedLand));
        harness.setLibrary(player1, List.of(firstDraw, secondDraw));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);

        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstDraw, secondDraw);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discardedLand);
        assertThat(windgrace.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
    }

    @Test
    @DisplayName("+2 draws one card when the discarded card is not a land")
    void plusTwoDrawsOneForNonland() {
        Permanent windgrace = addReadyWindgrace(3);
        Card discardedCreature = new GrizzlyBears();
        Card drawnCard = new Forest();
        harness.setHand(player1, List.of(discardedCreature));
        harness.setLibrary(player1, List.of(drawnCard));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discardedCreature);
        assertThat(windgrace.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
    }

    @Test
    @DisplayName("-3 returns up to two targeted lands from the graveyard")
    void minusThreeReturnsTargetedLands() {
        Permanent windgrace = addReadyWindgrace(5);
        Card firstLand = new Forest();
        Card secondLand = new Forest();
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(firstLand, secondLand, creature));

        harness.activateAbilityWithMultiTargets(player1, 0, 1,
                List.of(firstLand.getId(), secondLand.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard() == firstLand || permanent.getCard() == secondLand)
                .hasSize(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(creature);
        assertThat(windgrace.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
    }

    @Test
    @DisplayName("-3 cannot target a nonland card")
    void minusThreeRejectsNonlandTarget() {
        addReadyWindgrace(5);
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(player1, 0, 1,
                List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("-11 destroys up to six nonland permanents and creates Cat Warrior tokens")
    void minusElevenDestroysNonlandsAndCreatesTokens() {
        Permanent windgrace = addReadyWindgrace(11);
        Permanent ownCreature = addReadyPermanent(player1, new GrizzlyBears());
        Permanent opposingCreature = addReadyPermanent(player2, new GrizzlyBears());
        addReadyPermanent(player1, new Forest());
        addReadyPermanent(player2, new Forest());

        harness.activateAbilityWithMultiTargets(player1, 0, 2,
                List.of(ownCreature.getId(), opposingCreature.getId()));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Forest");
        harness.assertOnBattlefield(player2, "Forest");

        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(tokens).hasSize(6);
        assertThat(tokens).allSatisfy(token -> {
            assertThat(token.getEffectivePower()).isEqualTo(2);
            assertThat(token.getEffectiveToughness()).isEqualTo(2);
            assertThat(token.getCard().getSubtypes())
                    .containsExactlyInAnyOrder(CardSubtype.CAT, CardSubtype.WARRIOR);
            assertThat(gqs.hasKeyword(gd, token, Keyword.FORESTWALK)).isTrue();
        });
        assertThat(windgrace.getCounterCount(CounterType.LOYALTY)).isZero();
    }

    private Permanent addReadyWindgrace(int loyalty) {
        return addReadyPermanent(player1, new LordWindgrace(), loyalty);
    }

    private Permanent addReadyPermanent(Player player, Card card) {
        return addReadyPermanent(player, card, 0);
    }

    private Permanent addReadyPermanent(Player player, Card card, int loyalty) {
        Permanent permanent = new Permanent(card);
        permanent.setCounterCount(CounterType.LOYALTY, loyalty);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return permanent;
    }
}
