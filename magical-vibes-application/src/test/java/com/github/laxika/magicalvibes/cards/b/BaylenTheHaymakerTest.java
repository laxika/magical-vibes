package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BaylenTheHaymaker.class, Forest.class})
class BaylenTheHaymakerTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping two tokens adds one mana of the chosen color")
    void tapsTwoTokensForMana() {
        addBaylen();
        List<Permanent> tokens = addTokens(2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, ManaColor.BLUE.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(tokens).allMatch(Permanent::isTapped);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Tapping three tokens draws a card")
    void tapsThreeTokensToDraw() {
        addBaylen();
        List<Permanent> tokens = addTokens(3);
        Card libraryCard = new Forest();
        harness.setLibrary(player1, List.of(libraryCard));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
        assertThat(tokens).allMatch(Permanent::isTapped);
    }

    @Test
    @DisplayName("Tapping four tokens puts counters on Baylen and grants trample until end of turn")
    void tapsFourTokensToGrowAndTrample() {
        Permanent baylen = addBaylen();
        List<Permanent> tokens = addTokens(4);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(tokens).allMatch(Permanent::isTapped);
        assertThat(baylen.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, baylen)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, baylen)).isEqualTo(6);
        assertThat(gqs.hasKeyword(gd, baylen, Keyword.TRAMPLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(baylen.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, baylen, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Cannot pay the abilities with non-token or tapped permanents")
    void requiresEnoughUntappedTokens() {
        addBaylen();
        addTokens(1);
        addCreature(player1, false);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        Permanent token = addToken(player1);
        token.tap();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void summoningSickTokensCanPayForTappedBaylensAbility() {
        Permanent baylen = addBaylen();
        baylen.setSummoningSick(true);
        baylen.tap();
        List<Permanent> tokens = addTokens(2);
        tokens.forEach(token -> token.setSummoningSick(true));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, ManaColor.RED.name());

        assertThat(tokens).allMatch(Permanent::isTapped);
        assertThat(baylen.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void opponentsTokensCannotPayTheCost() {
        addBaylen();
        Permanent ownToken = addToken(player1);
        Permanent opposingToken = addToken(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(ownToken.isTapped()).isFalse();
        assertThat(opposingToken.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void noncreatureTokensCanPayTheCost() {
        addBaylen();
        Forest first = new Forest();
        first.setToken(true);
        Forest second = new Forest();
        second.setToken(true);
        Permanent firstToken = harness.addToBattlefieldAndReturn(player1, first);
        Permanent secondToken = harness.addToBattlefieldAndReturn(player1, second);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, ManaColor.WHITE.name());

        assertThat(firstToken.isTapped()).isTrue();
        assertThat(secondToken.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
    }

    @Test
    void growthDoesNotAffectBaylenThatReplacesTheSourceBeforeResolution() {
        Permanent original = addBaylen();
        List<Permanent> tokens = addTokens(4);
        harness.activateAbility(player1, 0, 2, null, null);
        assertThat(tokens).allMatch(Permanent::isTapped);
        assertThat(original.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        gd.playerBattlefields.get(player1.getId()).remove(original);
        Permanent replacement = harness.addToBattlefieldAndReturn(player1, new BaylenTheHaymaker());
        harness.passBothPriorities();

        assertThat(replacement.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.hasKeyword(gd, replacement, Keyword.TRAMPLE)).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addBaylen() {
        return addCreature(player1, new BaylenTheHaymaker(), true);
    }

    private List<Permanent> addTokens(int count) {
        return java.util.stream.IntStream.range(0, count)
                .mapToObj(ignored -> addToken(player1))
                .toList();
    }

    private Permanent addToken(Player player) {
        return addCreature(player, true, true);
    }

    private Permanent addCreature(Player player, boolean token) {
        return addCreature(player, token, true);
    }

    private Permanent addCreature(Player player, boolean token, boolean ready) {
        Card card = new Card() {
        };
        card.setType(CardType.CREATURE);
        card.setColor(CardColor.GREEN);
        card.setPower(1);
        card.setToughness(1);
        card.setToken(token);
        return addCreature(player, card, ready);
    }

    private Permanent addCreature(Player player, Card card, boolean ready) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, card);
        permanent.setSummoningSick(!ready);
        return permanent;
    }
}
