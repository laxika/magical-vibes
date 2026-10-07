package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.h.HumbleNaturalist;
import com.github.laxika.magicalvibes.cards.a.AlmightyBrushwagg;
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

@CardUsed({UmoriTheCollector.class, HumbleNaturalist.class, AlmightyBrushwagg.class})
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
        Permanent umori = addReadyUmori(player1, CardType.CREATURE);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new HumbleNaturalist()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(umori.getChosenCardType()).isEqualTo(CardType.CREATURE);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void spellsWithoutChosenTypeAreNotReduced() {
        addReadyUmori(player1, CardType.INSTANT);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new HumbleNaturalist()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    private Permanent addReadyUmori(Player player, CardType chosenType) {
        Permanent umori = harness.addToBattlefieldAndReturn(player, new UmoriTheCollector());
        umori.setChosenCardType(chosenType);
        return umori;
    }

    @Test
    void chosenTypeDoesNotReduceColoredMana() {
        addReadyUmori(player1, CardType.CREATURE);
        harness.setHand(player1, List.of(new AlmightyBrushwagg()));

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Almighty Brushwagg")).isNotNull();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void opponentsSpellsAreNotReduced() {
        addReadyUmori(player1, CardType.CREATURE);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new HumbleNaturalist()));
        harness.addMana(player2, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castCreature(player2, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    void reductionStopsWhenUmoriLeavesBattlefield() {
        Permanent umori = addReadyUmori(player1, CardType.CREATURE);
        gd.playerBattlefields.get(player1.getId()).remove(umori);
        gd.playerGraveyards.get(player1.getId()).add(umori.getCard());
        harness.setHand(player1, List.of(new HumbleNaturalist()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }
}
