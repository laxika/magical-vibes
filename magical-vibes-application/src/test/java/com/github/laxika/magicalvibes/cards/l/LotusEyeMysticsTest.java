package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.a.AngelicChorus;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LotusEyeMysticsTest extends BaseCardTest {

    @Test
    @DisplayName("Prowess gives Lotus-Eye Mystics +1/+1 until end of turn")
    void prowessBoostsUntilEndOfTurn() {
        Permanent mystics = addMystics();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, mystics)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, mystics)).isEqualTo(3);
    }

    @Test
    @DisplayName("ETB returns a targeted enchantment card from the graveyard to hand")
    void etbReturnsEnchantmentToHand() {
        AngelicChorus chorus = new AngelicChorus();
        harness.setGraveyard(player1, List.of(chorus));

        castMystics();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(chorus.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Angelic Chorus");
        harness.assertNotInGraveyard(player1, "Angelic Chorus");
    }

    @Test
    @DisplayName("Only enchantment cards are legal ETB targets")
    void onlyEnchantmentsAreLegalTargets() {
        AngelicChorus chorus = new AngelicChorus();
        GrizzlyBears bears = new GrizzlyBears();
        Shock shock = new Shock();
        harness.setGraveyard(player1, List.of(chorus, bears, shock));

        castMystics();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)
                .validCardIds()).containsExactly(chorus.getId());
    }

    private Permanent addMystics() {
        Permanent mystics = new Permanent(new LotusEyeMystics());
        mystics.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(mystics);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return mystics;
    }

    private void castMystics() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new LotusEyeMystics()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }
}
