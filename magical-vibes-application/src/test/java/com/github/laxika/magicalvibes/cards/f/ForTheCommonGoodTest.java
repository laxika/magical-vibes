package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(ForTheCommonGood.class)
class ForTheCommonGoodTest extends BaseCardTest {

    @Test
    @DisplayName("Creates X copies, grants indestructible to all controlled tokens, and gains life for each")
    void createsCopiesProtectsTokensAndGainsLife() {
        Permanent originalToken = addToken(player1, "Soldier Token");
        Permanent otherToken = addToken(player1, "Other Token");
        Permanent ownPermanent = addPermanent(player1, "Own Permanent", false);
        Permanent opponentToken = addToken(player2, "Opponent Token");

        harness.setHand(player1, List.of(new ForTheCommonGood()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castAndResolveSorcery(player1, 0, 2, originalToken.getId());

        List<Permanent> ownTokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(ownTokens).hasSize(4);
        assertThat(ownTokens).allSatisfy(token -> {
            assertThat(token.getCard().getPower()).isEqualTo(1);
            assertThat(token.getCard().getToughness()).isEqualTo(1);
            assertThat(gqs.hasKeyword(gd, token, Keyword.INDESTRUCTIBLE)).isTrue();
        });
        assertThat(ownTokens.stream().filter(token -> token.getCard().getName().equals("Soldier Token")).toList())
                .hasSize(3);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(otherToken, ownPermanent);
        assertThat(gqs.hasKeyword(gd, ownPermanent, Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponentToken, Keyword.INDESTRUCTIBLE)).isFalse();
        harness.assertLife(player1, 24);
    }

    @Test
    @DisplayName("Only targets a token controlled by the caster")
    void rejectsNonTokenAndOpponentTokenTargets() {
        Permanent ownPermanent = addPermanent(player1, "Own Permanent", false);
        Permanent opponentToken = addToken(player2, "Opponent Token");

        harness.setHand(player1, List.of(new ForTheCommonGood(), new ForTheCommonGood()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0, ownPermanent.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0, opponentToken.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void zeroXStillProtectsTokensAndGainsLife() {
        Permanent token = addToken(player1, "Rabbit Token");
        harness.setHand(player1, List.of(new ForTheCommonGood()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveSorcery(player1, 0, 0, token.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(token);
        assertThat(gqs.hasKeyword(gd, token, Keyword.INDESTRUCTIBLE)).isTrue();
        harness.assertLife(player1, 21);
    }

    @Test
    void missingTargetPreventsProtectionAndLifeGain() {
        Permanent target = addToken(player1, "Rabbit Token");
        Permanent other = addToken(player1, "Other Token");
        harness.setHand(player1, List.of(new ForTheCommonGood()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castSorcery(player1, 0, 1, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(target);

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(other);
        assertThat(gqs.hasKeyword(gd, other, Keyword.INDESTRUCTIBLE)).isFalse();
        harness.assertLife(player1, 20);
    }

    @Test
    void protectsNoncreatureTokensUntilCastersNextTurnButNotLaterTokens() {
        Card treasure = new Card() {};
        treasure.setName("Treasure");
        treasure.setType(CardType.ARTIFACT);
        treasure.setToken(true);
        Permanent target = harness.addToBattlefieldAndReturn(player1, treasure);
        harness.setHand(player1, List.of(new ForTheCommonGood()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, 1, target.getId());

        List<Permanent> protectedTokens = List.copyOf(gd.playerBattlefields.get(player1.getId()));
        assertThat(protectedTokens).hasSize(2);
        assertThat(protectedTokens).allSatisfy(token ->
                assertThat(gqs.hasKeyword(gd, token, Keyword.INDESTRUCTIBLE)).isTrue());
        harness.assertLife(player1, 22);
        Permanent laterToken = addToken(player1, "Later Token");
        assertThat(gqs.hasKeyword(gd, laterToken, Keyword.INDESTRUCTIBLE)).isFalse();

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        assertThat(protectedTokens).allSatisfy(token ->
                assertThat(gqs.hasKeyword(gd, token, Keyword.INDESTRUCTIBLE)).isTrue());
        harness.passUntil(player1, TurnStep.UPKEEP);
        assertThat(protectedTokens).allSatisfy(token ->
                assertThat(gqs.hasKeyword(gd, token, Keyword.INDESTRUCTIBLE)).isFalse());
    }

    private Permanent addToken(com.github.laxika.magicalvibes.model.Player player, String name) {
        return addPermanent(player, name, true);
    }

    private Permanent addPermanent(com.github.laxika.magicalvibes.model.Player player,
                                   String name, boolean token) {
        Card card = new Card() {
        };
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setColor(CardColor.WHITE);
        card.setPower(1);
        card.setToughness(1);
        card.setToken(token);

        Permanent permanent = harness.addToBattlefieldAndReturn(player, card);
        permanent.setSummoningSick(false);
        return permanent;
    }
}
