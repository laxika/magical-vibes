package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.v.VernadiShieldmate;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MarchOfTheMultitudes.class, VernadiShieldmate.class})
class MarchOfTheMultitudesTest extends BaseCardTest {

    @Test
    @DisplayName("Convoke creates X white Soldier tokens with lifelink")
    void convokeCreatesLifelinkSoldierTokens() {
        Permanent firstConvokeCreature = harness.addToBattlefieldAndReturn(player1, new VernadiShieldmate());
        Permanent secondConvokeCreature = harness.addToBattlefieldAndReturn(player1, new VernadiShieldmate());
        harness.setHand(player1, List.of(new MarchOfTheMultitudes()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 2);

        gs.playCard(gd, player1, 0, 2, null, null,
                List.of(), List.of(firstConvokeCreature.getId(), secondConvokeCreature.getId()));
        harness.passBothPriorities();

        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(tokens).hasSize(2);
        assertThat(firstConvokeCreature.isTapped()).isTrue();
        assertThat(secondConvokeCreature.isTapped()).isTrue();

        for (Permanent token : tokens) {
            assertThat(token.getCard().getPower()).isEqualTo(1);
            assertThat(token.getCard().getToughness()).isEqualTo(1);
            assertThat(token.getCard().getColor()).isEqualTo(CardColor.WHITE);
            assertThat(token.getCard().getSubtypes()).contains(CardSubtype.SOLDIER);
            assertThat(gqs.hasKeyword(gd, token, Keyword.LIFELINK)).isTrue();
        }
    }

    @Test
    @DisplayName("X can be zero and creates no tokens")
    void zeroXCreatesNoTokens() {
        harness.setHand(player1, List.of(new MarchOfTheMultitudes()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castInstant(player1, 0, 0, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "March of the Multitudes");
    }

    @Test
    @DisplayName("An instant cast on an opponent's turn creates tokens for its controller")
    void nonactivePlayerReceivesTokens() {
        harness.setHand(player2, List.of(new MarchOfTheMultitudes()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.WHITE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castInstant(player2, 0, 1, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(1)
                .allSatisfy(token -> assertThat(token.getCard().isToken()).isTrue());
        harness.assertInGraveyard(player2, "March of the Multitudes");
    }

    @Test
    @DisplayName("Mana alone pays for X and the tokens gain life when dealing combat damage")
    void manaOnlyCastCreatesTokensWithWorkingLifelink() {
        harness.setHand(player1, List.of(new MarchOfTheMultitudes()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castInstant(player1, 0, 3, null);
        harness.passBothPriorities();

        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId());
        assertThat(tokens).hasSize(3).allSatisfy(token -> {
            assertThat(token.getCard().isToken()).isTrue();
            assertThat(token.isTapped()).isFalse();
            token.setSummoningSick(false);
        });
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();

        declareAttackers(List.of(0, 1, 2));
        resolveCombat();

        harness.assertLife(player1, 23);
        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Newly entered multicolored creatures can pay both colored and X costs")
    void summoningSickCreaturesCanConvokeEntireCost() {
        List<Permanent> creatures = java.util.stream.IntStream.range(0, 5)
                .mapToObj(i -> harness.addToBattlefieldAndReturn(player1, new VernadiShieldmate()))
                .toList();
        harness.setHand(player1, List.of(new MarchOfTheMultitudes()));

        gs.playCard(gd, player1, 0, 2, null, null, List.of(),
                creatures.stream().map(Permanent::getId).toList());
        harness.passBothPriorities();

        assertThat(creatures).allSatisfy(creature -> assertThat(creature.isTapped()).isTrue());
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())).hasSize(2);
        harness.assertInGraveyard(player1, "March of the Multitudes");
    }

    @Test
    @DisplayName("One multicolored creature cannot pay two colored mana")
    void multicoloredCreatureOnlyPaysOneMana() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new VernadiShieldmate());
        harness.setHand(player1, List.of(new MarchOfTheMultitudes()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, null, null,
                List.of(), List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "March of the Multitudes");
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())).isEmpty();
    }
}
