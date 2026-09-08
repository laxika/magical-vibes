package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.w.Wasteland;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RazorlashTransmograntTest extends BaseCardTest {

    @Test
    @DisplayName("Can't block")
    void cantBlock() {
        Permanent transmogrant = new Permanent(new RazorlashTransmogrant());
        gd.playerBattlefields.get(player1.getId()).add(transmogrant);
        Permanent blocker = new Permanent(new RazorlashTransmogrant());
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        assertThat(bls.canBlockAttacker(gd, transmogrant, blocker,
                gd.playerBattlefields.get(player1.getId()))).isFalse();
    }

    @Test
    @DisplayName("Returns from the graveyard with a +1/+1 counter for the full cost")
    void returnsForFullCost() {
        RazorlashTransmogrant transmogrant = new RazorlashTransmogrant();
        harness.setGraveyard(player1, List.of(transmogrant));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Razorlash Transmogrant");
        assertThat(returned.getCounterCount(com.github.laxika.magicalvibes.model.CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(1);
    }

    @Test
    @DisplayName("Costs only the two black mana when an opponent controls four nonbasic lands")
    void costsLessWithFourOpponentNonbasicLands() {
        for (int i = 0; i < 4; i++) {
            harness.addToBattlefield(player2, new Wasteland());
        }
        RazorlashTransmogrant transmogrant = new RazorlashTransmogrant();
        harness.setGraveyard(player1, List.of(transmogrant));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Razorlash Transmogrant")
                .getCounterCount(com.github.laxika.magicalvibes.model.CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(1);
    }

    @Test
    @DisplayName("Basic lands do not satisfy the cost reduction condition")
    void basicLandsDoNotReduceCost() {
        for (int i = 0; i < 4; i++) {
            harness.addToBattlefield(player2, new Island());
        }
        harness.setGraveyard(player1, List.of(new RazorlashTransmogrant()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }
}
