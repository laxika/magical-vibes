package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.m.MirriCatWarrior;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({IrenicussVileDuplication.class, MirriCatWarrior.class})
class IrenicussVileDuplicationTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a flying, nonlegendary token copy of a creature you control")
    void createsFlyingNonlegendaryTokenCopy() {
        Permanent mirri = harness.addToBattlefieldAndReturn(player1, new MirriCatWarrior());
        harness.setHand(player1, List.of(new IrenicussVileDuplication()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, mirri.getId());

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(token.getCard().hasKeyword(Keyword.FLYING)).isTrue();
        assertThat(token.getCard().getSupertypes()).doesNotContain(CardSupertype.LEGENDARY);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().getName().equals("Mirri, Cat Warrior"))
                .hasSize(2);
    }

    @Test
    @DisplayName("Cannot target an opponent's creature")
    void cannotTargetOpponentsCreature() {
        Permanent opponentMirri = harness.addToBattlefieldAndReturn(player2, new MirriCatWarrior());
        harness.setHand(player1, List.of(new IrenicussVileDuplication()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, opponentMirri.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void doesNotCopyCountersOrTappedStatus() {
        Permanent mirri = harness.addToBattlefieldAndReturn(player1, new MirriCatWarrior());
        mirri.tap();
        mirri.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.setHand(player1, List.of(new IrenicussVileDuplication()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, mirri.getId());

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()).findFirst().orElseThrow();
        assertThat(token.isTapped()).isFalse();
        assertThat(token.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(token.hasKeyword(Keyword.FIRST_STRIKE)).isTrue();
        assertThat(token.hasKeyword(Keyword.VIGILANCE)).isTrue();
        assertThat(token.hasKeyword(Keyword.FLYING)).isTrue();
        assertThat(mirri.hasKeyword(Keyword.FLYING)).isFalse();
        assertThat(mirri.getCard().getSupertypes()).contains(CardSupertype.LEGENDARY);
    }

    @Test
    void createsNoTokenWhenTargetChangesControllerBeforeResolution() {
        Permanent mirri = harness.addToBattlefieldAndReturn(player1, new MirriCatWarrior());
        harness.setHand(player1, List.of(new IrenicussVileDuplication()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castSorcery(player1, 0, mirri.getId());
        gd.playerBattlefields.get(player1.getId()).remove(mirri);
        gd.playerBattlefields.get(player2.getId()).add(mirri);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(mirri);
        harness.assertInGraveyard(player1, "Irenicus's Vile Duplication");
    }

    @Test
    void copiesFaceDownCharacteristicsInsteadOfUnderlyingCard() {
        Permanent manifestedMirri = harness.addToBattlefieldAndReturn(player1, new MirriCatWarrior());
        manifestedMirri.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        harness.setHand(player1, List.of(new IrenicussVileDuplication()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, manifestedMirri.getId());

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()).findFirst().orElseThrow();
        assertThat(token.isFaceDown()).isFalse();
        assertThat(token.getEffectivePower()).isEqualTo(2);
        assertThat(token.getEffectiveToughness()).isEqualTo(2);
        assertThat(token.hasKeyword(Keyword.FLYING)).isTrue();
        assertThat(token.hasKeyword(Keyword.FIRST_STRIKE)).isFalse();
        assertThat(token.hasKeyword(Keyword.VIGILANCE)).isFalse();
        assertThat(token.hasKeyword(Keyword.FORESTWALK)).isFalse();
    }
}
