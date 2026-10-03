package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AinokSurvivalist;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CloneLegion.class, Forest.class, GrizzlyBears.class, SerraAngel.class, AinokSurvivalist.class})
class CloneLegionTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a token copy for each creature the target player controls")
    void createsTokenCopyForEachTargetPlayersCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new SerraAngel());
        harness.addToBattlefield(player2, new Forest());

        castCloneLegion(player2);

        assertThat(tokenCount(player1)).isEqualTo(2);
        assertThat(tokenCount(player2)).isZero();
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Creates no tokens when the target player controls no creatures")
    void createsNoTokensWithoutTargetCreatures() {
        harness.addToBattlefield(player2, new Forest());

        castCloneLegion(player2);

        assertThat(tokenCount(player1)).isZero();
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Can target its controller without recursively copying the new tokens")
    void copiesOwnCreaturesOnce() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new SerraAngel());

        castCloneLegion(player1);

        assertThat(tokenCount(player1)).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(4);
        assertThat(tokenCount(player2)).isZero();
    }

    @Test
    @DisplayName("Copies do not inherit tapped status or counters")
    void doesNotCopyTappedStatusOrCounters() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        var original = gd.playerBattlefields.get(player2.getId()).getFirst();
        original.tap();
        original.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);

        castCloneLegion(player2);

        var token = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(token.getCard().isToken()).isTrue();
        assertThat(token.isTapped()).isFalse();
        assertThat(token.getPlusOnePlusOneCounters()).isZero();
        assertThat(original.isTapped()).isTrue();
        assertThat(original.getPlusOnePlusOneCounters()).isEqualTo(3);
    }

    @Test
    @DisplayName("Copies the face-down characteristics rather than the hidden card")
    void copiesFaceDownCharacteristics() {
        harness.setHand(player1, List.of(new AinokSurvivalist()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isFaceDown()).isTrue();

        castCloneLegion(player1);

        var tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(tokens).hasSize(1);
        var token = tokens.getFirst();
        assertThat(token.isFaceDown()).isFalse();
        assertThat(token.getBasePower()).isEqualTo(2);
        assertThat(token.getBaseToughness()).isEqualTo(2);
    }

    private void castCloneLegion(Player targetPlayer) {
        harness.setHand(player1, List.of(new CloneLegion()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 7);
        harness.castAndResolveSorcery(player1, 0, targetPlayer.getId());
    }

    private long tokenCount(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .map(permanent -> permanent.getCard())
                .filter(Card::isToken)
                .count();
    }
}
