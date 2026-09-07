package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.d.DarksteelCitadel;
import com.github.laxika.magicalvibes.cards.d.Disenchant;
import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.s.SupremeVerdict;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GrandArbiterAugustinIV.class, Disenchant.class, Divination.class, SupremeVerdict.class,
        DarksteelCitadel.class})
class GrandArbiterAugustinIVTest extends BaseCardTest {

    @Test
    @DisplayName("White spells you cast cost {1} less")
    void whiteSpellsCostOneLess() {
        harness.addToBattlefield(player1, new GrandArbiterAugustinIV());
        harness.addToBattlefield(player2, new DarksteelCitadel());
        harness.setHand(player1, List.of(new Disenchant()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, gd.playerBattlefields.get(player2.getId()).getFirst().getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Blue spells you cast cost {1} less")
    void blueSpellsCostOneLess() {
        harness.addToBattlefield(player1, new GrandArbiterAugustinIV());
        harness.setHand(player1, List.of(new Divination()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("White-blue spells get both cost reductions")
    void whiteBlueSpellsGetBothReductions() {
        harness.addToBattlefield(player1, new GrandArbiterAugustinIV());
        harness.setHand(player1, List.of(new SupremeVerdict()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castSorcery(player1, 0, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Spells opponents cast cost {1} more")
    void opponentSpellsCostOneMore() {
        harness.addToBattlefield(player1, new GrandArbiterAugustinIV());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Divination()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castSorcery(player2, 0, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }
}
