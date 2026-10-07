package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.p.PledgeOfUnity;
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

@CardUsed({TibaltRakishInstigator.class, Shock.class, PledgeOfUnity.class})
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
        Permanent devil = findPermanent(player1, "Devil");
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
        Permanent devil = findPermanent(player1, "Devil");

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

    @Test
    @DisplayName("Controller gains life from a resolved spell while Tibalt is present")
    void controllerCanGainLife() {
        addReadyTibalt(player1, 5);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.setLife(player1, 20);

        castPledgeOfUnity(player1);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
        assertThat(findPermanent(player1, "Devil").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Opponent's life gain is prevented but the rest of its spell resolves")
    void opponentCannotGainLifeFromSpell() {
        addReadyTibalt(player1, 5);
        addReadyTibalt(player2, 5);
        harness.activateAbility(player2, 0, 0, null, null);
        harness.passBothPriorities();
        harness.setLife(player2, 20);

        castPledgeOfUnity(player2);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(findPermanent(player2, "Devil").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Spending the last loyalty still creates a Devil whose death trigger survives Tibalt")
    void devilAbilitySurvivesTibaltLeavingBattlefield() {
        addReadyTibalt(player1, 2);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Tibalt, Rakish Instigator")).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).anyMatch(card -> card instanceof TibaltRakishInstigator);
        assertThat(gqs.canPlayerGainLife(gd, player2.getId())).isTrue();
        Permanent devil = findPermanent(player1, "Devil");
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, devil.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
        assertThat(findPermanents(player1, "Devil")).isEmpty();
    }

    private void castPledgeOfUnity(Player player) {
        harness.setHand(player, List.of(new PledgeOfUnity()));
        harness.addMana(player, ManaColor.COLORLESS, 1);
        harness.addMana(player, ManaColor.GREEN, 1);
        harness.addMana(player, ManaColor.WHITE, 1);
        harness.castInstant(player, 0);
        harness.passBothPriorities();
    }

    private Permanent addReadyTibalt(Player player, int loyalty) {
        Permanent tibalt = harness.addToBattlefieldAndReturn(player, new TibaltRakishInstigator());
        tibalt.setCounterCount(CounterType.LOYALTY, loyalty);
        tibalt.setSummoningSick(false);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return tibalt;
    }
}
