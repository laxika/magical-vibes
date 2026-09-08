package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class KariZevSkyshipRaiderTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking creates a legendary Ragavan token tapped and attacking")
    void attackCreatesRagavanToken() {
        addCreatureReady(player1, new KariZevSkyshipRaider());
        preventAutoPass(player2);

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        Permanent ragavan = findPermanents(player1, "Ragavan").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(ragavan.getCard().getPower()).isEqualTo(2);
        assertThat(ragavan.getCard().getToughness()).isEqualTo(1);
        assertThat(ragavan.getCard().getColor()).isEqualTo(CardColor.RED);
        assertThat(ragavan.getCard().getSubtypes()).containsExactly(CardSubtype.MONKEY);
        assertThat(ragavan.getCard().getSupertypes()).contains(CardSupertype.LEGENDARY);
        assertThat(ragavan.isTapped()).isTrue();
        assertThat(ragavan.isAttackedThisTurn()).isTrue();
    }

    @Test
    @DisplayName("Ragavan is exiled at end of combat")
    void ragavanExiledAtEndOfCombat() {
        addCreatureReady(player1, new KariZevSkyshipRaider());
        preventAutoPass(player2);

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();
        Permanent ragavan = findPermanents(player1, "Ragavan").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(ragavan);
    }

    private void preventAutoPass(Player player) {
        harness.setHand(player, List.of(new GiantGrowth()));
        harness.addMana(player, ManaColor.GREEN, 1);
    }
}
