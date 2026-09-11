package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EternalStudentTest extends BaseCardTest {

    @Test
    @DisplayName("Graveyard ability exiles Eternal Student and creates two flying Inkling tokens")
    void graveyardAbilityExilesSourceAndCreatesTokens() {
        EternalStudent eternalStudent = new EternalStudent();
        harness.setGraveyard(player1, List.of(eternalStudent));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateGraveyardAbility(player1, 0);

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .containsExactly(eternalStudent);

        harness.passBothPriorities();

        List<Permanent> inklings = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(inklings).hasSize(2);
        for (Permanent inkling : inklings) {
            assertThat(gqs.getEffectivePower(gd, inkling)).isEqualTo(1);
            assertThat(gqs.getEffectiveToughness(gd, inkling)).isEqualTo(1);
            assertThat(inkling.getCard().getColors())
                    .containsExactlyInAnyOrder(CardColor.WHITE, CardColor.BLACK);
            assertThat(inkling.getCard().getSubtypes()).contains(CardSubtype.INKLING);
            assertThat(gqs.hasKeyword(gd, inkling, Keyword.FLYING)).isTrue();
        }
    }
}
