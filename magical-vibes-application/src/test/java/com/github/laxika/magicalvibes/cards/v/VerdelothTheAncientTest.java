package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.i.IronrootTreefolk;
import com.github.laxika.magicalvibes.cards.n.NomadicElf;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IronrootTreefolk.class, NomadicElf.class, VerdelothTheAncient.class})
class VerdelothTheAncientTest extends BaseCardTest {

    @Test
    @DisplayName("Other Treefolk get +1/+1 regardless of controller")
    void buffsOtherTreefolk() {
        Permanent ownTreefolk = harness.addToBattlefieldAndReturn(player1,
                new IronrootTreefolk());
        Permanent opponentTreefolk = harness.addToBattlefieldAndReturn(player2,
                new IronrootTreefolk());
        Permanent unrelated = harness.addToBattlefieldAndReturn(player2,
                new NomadicElf());
        Permanent source = harness.addToBattlefieldAndReturn(player1, new VerdelothTheAncient());

        assertThat(gqs.getEffectivePower(gd, ownTreefolk)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, ownTreefolk)).isEqualTo(6);
        assertThat(gqs.getEffectivePower(gd, opponentTreefolk)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, opponentTreefolk)).isEqualTo(6);
        assertThat(gqs.getEffectivePower(gd, unrelated)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, unrelated)).isEqualTo(2);

        int sourcePower = source.getCard().getPower();
        int sourceToughness = source.getCard().getToughness();
        assertThat(gqs.getEffectivePower(gd, source)).isEqualTo(sourcePower);
        assertThat(gqs.getEffectiveToughness(gd, source)).isEqualTo(sourceToughness);
    }

    @Test
    @DisplayName("An un-kicked Verdeloth creates no Saproling tokens")
    void unKickedCreatesNoTokens() {
        harness.setHand(player1, List.of(new VerdelothTheAncient()));
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken())).isEmpty();
    }

    @Test
    @DisplayName("Kicker X pays X and creates X Saproling tokens")
    void kickedCreatesXTokensAndPaysX() {
        harness.setHand(player1, List.of(new VerdelothTheAncient()));
        harness.addMana(player1, ManaColor.GREEN, 9);
        harness.ensurePriority(player1);

        gs.playCard(gd, player1, 0, 3, null, null, List.of(), List.of(), false,
                null, null, null, null, null, true);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();

        resolveAllTriggers();

        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken())
                .toList();
        assertThat(tokens).hasSize(3);
        assertThat(tokens).allSatisfy(token -> {
            assertThat(token.getCard().getColor()).isEqualTo(CardColor.GREEN);
            assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.SAPROLING);
            assertThat(token.getCard().getPower()).isEqualTo(1);
            assertThat(token.getCard().getToughness()).isEqualTo(1);
            assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(2);
            assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(2);
        });
    }

    @Test
    @DisplayName("Kicker X=0 creates no Saproling tokens")
    void kickedWithZeroXCreatesNoTokens() {
        harness.setHand(player1, List.of(new VerdelothTheAncient()));
        harness.addMana(player1, ManaColor.GREEN, 6);
        harness.ensurePriority(player1);

        gs.playCard(gd, player1, 0, 0, null, null, List.of(), List.of(), false,
                null, null, null, null, null, true);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();

        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken())).isEmpty();
    }
}
