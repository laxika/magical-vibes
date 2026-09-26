package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.l.LowlandGiant;
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

@CardUsed({Inkshield.class, LowlandGiant.class})
class InkshieldTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents combat damage to you and creates one flying Inkling per damage prevented")
    void preventsCombatDamageAndCreatesInklings() {
        addCreatureReady(player2, new LowlandGiant());
        castOnOpponentsTurn();

        declareAttackers(player2, List.of(0));
        resolveCombat(player2);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        List<Permanent> inklings = findPermanents(player1, "Inkling");
        assertThat(inklings).hasSize(4);
        for (Permanent inkling : inklings) {
            assertThat(gqs.getEffectivePower(gd, inkling)).isEqualTo(2);
            assertThat(gqs.getEffectiveToughness(gd, inkling)).isEqualTo(1);
            assertThat(inkling.getCard().getColors())
                    .containsExactlyInAnyOrder(CardColor.WHITE, CardColor.BLACK);
            assertThat(inkling.getCard().getSubtypes()).contains(CardSubtype.INKLING);
            assertThat(gqs.hasKeyword(gd, inkling, Keyword.FLYING)).isTrue();
        }
    }

    private void castOnOpponentsTurn() {
        harness.forceActivePlayer(player2);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Inkshield()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castAndResolveInstant(player1, 0);
    }
}
