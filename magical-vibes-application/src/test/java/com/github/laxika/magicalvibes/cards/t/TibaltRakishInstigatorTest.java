package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TibaltRakishInstigator.class, Shock.class})
class TibaltRakishInstigatorTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents opponents from gaining life but not its controller")
    void preventsOpponentsFromGainingLife() {
        addReadyTibalt(player1, 5);

        assertThat(gqs.canPlayerGainLife(gd, player1.getId())).isTrue();
        assertThat(gqs.canPlayerGainLife(gd, player2.getId())).isFalse();
    }

    @Test
    @DisplayName("-2 creates a 1/1 red Devil token")
    void minusTwoCreatesDevil() {
        Permanent tibalt = addReadyTibalt(player1, 5);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(tibalt.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
        Permanent devil = findPermanents(player1, "Devil").getFirst();
        assertThat(devil.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(devil.getCard().getColor()).isEqualTo(CardColor.RED);
        assertThat(devil.getCard().getSubtypes()).containsExactly(CardSubtype.DEVIL);
        assertThat(devil.getCard().getPower()).isEqualTo(1);
        assertThat(devil.getCard().getToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("A Devil deals 1 damage to any target when it dies")
    void devilDealsDamageWhenItDies() {
        addReadyTibalt(player1, 5);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        Permanent devil = findPermanents(player1, "Devil").getFirst();

        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, devil.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    private Permanent addReadyTibalt(Player player, int loyalty) {
        Permanent tibalt = new Permanent(new TibaltRakishInstigator());
        tibalt.setCounterCount(CounterType.LOYALTY, loyalty);
        tibalt.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(tibalt);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return tibalt;
    }
}
