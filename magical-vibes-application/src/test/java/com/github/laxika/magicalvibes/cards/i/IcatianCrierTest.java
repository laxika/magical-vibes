package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({IcatianCrier.class, Forest.class})
class IcatianCrierTest extends BaseCardTest {

    @Test
    void discardingACardCreatesTwoCitizenTokensAndTapsIcatianCrier() {
        Permanent crier = addCreatureReady(player1, new IcatianCrier());
        harness.setHand(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(crier.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(tokens).hasSize(2);
        assertThat(tokens).allSatisfy(token -> {
            assertThat(token.getEffectivePower()).isEqualTo(1);
            assertThat(token.getEffectiveToughness()).isEqualTo(1);
            assertThat(token.getCard().getSubtypes()).contains(CardSubtype.CITIZEN);
        });
    }

    @Test
    void abilityCannotBeActivatedWithoutACardToDiscard() {
        addCreatureReady(player1, new IcatianCrier());
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void discardsTheChosenCardAndCreatesWhiteCitizenCreatureTokens() {
        addCreatureReady(player1, new IcatianCrier());
        Forest keptForest = new Forest();
        Forest discardedForest = new Forest();
        harness.setHand(player1, List.of(keptForest, discardedForest));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(keptForest);
        List<Permanent> tokens = findPermanents(player1, "Citizen");
        assertThat(tokens).hasSize(2).allSatisfy(token -> {
            assertThat(token.getCard().isToken()).isTrue();
            assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
            assertThat(token.getCard().getColor()).isEqualTo(CardColor.WHITE);
            assertThat(token.getCard().getName()).isEqualTo("Citizen");
            assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.CITIZEN);
            assertThat(token.isTapped()).isFalse();
        });
    }

    @Test
    void abilityCannotBeActivatedWhileIcatianCrierIsTapped() {
        Permanent crier = addCreatureReady(player1, new IcatianCrier());
        crier.tap();
        harness.setHand(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
