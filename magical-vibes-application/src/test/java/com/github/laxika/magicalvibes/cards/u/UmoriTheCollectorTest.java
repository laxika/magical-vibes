package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UmoriTheCollector.class, GrizzlyBears.class, Shock.class})
class UmoriTheCollectorTest extends BaseCardTest {

    @Test
    void choosesCardTypeAsItEnters() {
        harness.setHand(player1, List.of(new UmoriTheCollector()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, CardType.INSTANT.name());

        assertThat(findPermanent(player1, "Umori, the Collector").getChosenCardType())
                .isEqualTo(CardType.INSTANT);
    }

    @Test
    void spellsOfChosenTypeCostOneLessToCast() {
        Permanent umori = addReadyUmori(player1, CardType.INSTANT);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, target.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(umori.getChosenCardType()).isEqualTo(CardType.INSTANT);
    }

    @Test
    void spellsWithoutChosenTypeAreNotReduced() {
        addReadyUmori(player1, CardType.INSTANT);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    private Permanent addReadyUmori(Player player, CardType chosenType) {
        Permanent umori = new Permanent(new UmoriTheCollector());
        umori.setSummoningSick(false);
        umori.setChosenCardType(chosenType);
        gd.playerBattlefields.get(player.getId()).add(umori);
        return umori;
    }
}
