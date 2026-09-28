package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.m.MirriCatWarrior;
import com.github.laxika.magicalvibes.model.CardSupertype;
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

@CardUsed({IrenicussVileDuplication.class, MirriCatWarrior.class})
class IrenicussVileDuplicationTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a flying, nonlegendary token copy of a creature you control")
    void createsFlyingNonlegendaryTokenCopy() {
        Permanent mirri = harness.addToBattlefieldAndReturn(player1, new MirriCatWarrior());
        harness.setHand(player1, List.of(new IrenicussVileDuplication()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castSorcery(player1, 0, mirri.getId());
        harness.passBothPriorities();

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
}
