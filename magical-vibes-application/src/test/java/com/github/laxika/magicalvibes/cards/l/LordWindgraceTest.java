package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LordWindgrace.class, Forest.class, Mountain.class, GrizzlyBears.class})
class LordWindgraceTest extends BaseCardTest {

    @Test
    @DisplayName("+2 draws an extra card when the discarded card is a land")
    void plusTwoDrawsExtraForDiscardedLand() {
        Permanent windgrace = addReadyLordWindgrace(player1, 5);
        Card discarded = new Forest();
        Card drawnOne = new GrizzlyBears();
        Card drawnTwo = new Mountain();
        harness.setHand(player1, List.of(discarded));
        harness.setLibrary(player1, List.of(drawnOne, drawnTwo));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(drawnOne, drawnTwo);
        harness.assertInGraveyard(player1, "Forest");
        assertThat(windgrace.getCounterCount(CounterType.LOYALTY)).isEqualTo(7);
    }

    @Test
    @DisplayName("+2 draws only one card when the discarded card is not a land")
    void plusTwoDrawsOneForDiscardedNonland() {
        Permanent windgrace = addReadyLordWindgrace(player1, 5);
        Card discarded = new GrizzlyBears();
        Card drawn = new Forest();
        Card remaining = new Mountain();
        harness.setHand(player1, List.of(discarded));
        harness.setLibrary(player1, List.of(drawn, remaining));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remaining);
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(windgrace.getCounterCount(CounterType.LOYALTY)).isEqualTo(7);
    }

    @Test
    @DisplayName("-3 returns up to two target lands from the graveyard")
    void minusThreeReturnsTwoLands() {
        Permanent windgrace = addReadyLordWindgrace(player1, 5);
        Card forest = new Forest();
        Card mountain = new Mountain();
        harness.setGraveyard(player1, List.of(forest, mountain));

        harness.activateAbilityWithGraveyardTargets(
                player1, 0, 1, List.of(forest.getId(), mountain.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().hasType(CardType.LAND)))
                .hasSize(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(windgrace.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
    }

    @Test
    @DisplayName("-3 rejects a nonland graveyard target")
    void minusThreeRejectsNonlandTarget() {
        addReadyLordWindgrace(player1, 5);
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, 0, 1, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("-11 destroys nonland permanents and creates six Cat Warrior tokens with forestwalk")
    void minusElevenDestroysAndCreatesTokens() {
        Permanent windgrace = addReadyLordWindgrace(player1, 11);
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new Forest());
        List<UUID> targets = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(permanent -> permanent.getCard().hasType(CardType.CREATURE))
                .map(Permanent::getId)
                .toList();

        harness.activateAbilityWithMultiTargets(player1, 0, 2, targets);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()).stream()
                .noneMatch(permanent -> permanent.getCard().getName().equals("Grizzly Bears"))).isTrue();
        assertThat(gd.playerBattlefields.get(player2.getId()).stream()
                .anyMatch(permanent -> permanent.getCard().getName().equals("Forest"))).isTrue();

        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(tokens).hasSize(6);
        assertThat(tokens).allSatisfy(token -> {
            assertThat(token.getCard().getSubtypes()).containsExactlyInAnyOrder(CardSubtype.CAT, CardSubtype.WARRIOR);
            assertThat(token.getCard().getKeywords()).contains(Keyword.FORESTWALK);
            assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(2);
            assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(2);
        });
        assertThat(windgrace.getCounterCount(CounterType.LOYALTY)).isEqualTo(0);
    }

    @Test
    @DisplayName("-11 rejects a land target")
    void minusElevenRejectsLandTarget() {
        addReadyLordWindgrace(player1, 11);
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 2, List.of(forest.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyLordWindgrace(Player player, int loyalty) {
        Permanent permanent = new Permanent(new LordWindgrace());
        permanent.setCounterCount(CounterType.LOYALTY, loyalty);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return permanent;
    }
}
