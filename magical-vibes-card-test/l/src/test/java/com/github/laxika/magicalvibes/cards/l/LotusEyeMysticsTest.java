package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.a.AngelicChorus;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LotusEyeMystics.class, AngelicChorus.class, GrizzlyBears.class, Shock.class})
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

        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);

        assertThat(gqs.getEffectivePower(gd, mystics)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, mystics)).isEqualTo(2);
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

    @Test
    void prowessResolvesBeforeTheSpellAndStacks() {
        Permanent mystics = addMystics();
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, mystics)).isEqualTo(4);
        harness.assertLife(player2, 20);

        harness.passBothPriorities();
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, mystics)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, mystics)).isEqualTo(4);
        harness.assertLife(player2, 16);
    }

    @Test
    void creatureSpellDoesNotTriggerProwess() {
        Permanent mystics = addMystics();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, mystics)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, mystics)).isEqualTo(2);
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void opponentsSpellDoesNotTriggerProwess() {
        Permanent mystics = addMystics();
        harness.forceActivePlayer(player2);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, mystics)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, mystics)).isEqualTo(2);
        harness.assertLife(player1, 18);
    }

    @Test
    void etbCannotTargetOpponentsEnchantment() {
        AngelicChorus own = new AngelicChorus();
        AngelicChorus opponents = new AngelicChorus();
        harness.setGraveyard(player1, List.of(own));
        harness.setGraveyard(player2, List.of(opponents));

        castMystics();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)
                .validCardIds()).containsExactly(own.getId());
        harness.handleMultipleCardsChosen(player1, List.of(own.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Angelic Chorus");
        harness.assertInGraveyard(player2, "Angelic Chorus");
    }

    @Test
    void etbWithNoLegalTargetStillEntersBattlefield() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new Shock()));
        harness.setGraveyard(player2, List.of(new AngelicChorus()));

        castMystics();

        harness.assertOnBattlefield(player1, "Lotus-Eye Mystics");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Shock");
        harness.assertInGraveyard(player2, "Angelic Chorus");
    }

    @Test
    void etbDoesNotReturnTargetThatLeftTheGraveyard() {
        AngelicChorus chorus = new AngelicChorus();
        harness.setGraveyard(player1, List.of(chorus));
        castMystics();
        harness.handleMultipleCardsChosen(player1, List.of(chorus.getId()));

        harness.setGraveyard(player1, List.of());
        harness.setExile(player1, List.of(chorus));
        harness.passBothPriorities();

        harness.assertNotInHand(player1, "Angelic Chorus");
        harness.assertNotInGraveyard(player1, "Angelic Chorus");
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addMystics() {
        Permanent mystics = harness.addToBattlefieldAndReturn(player1, new LotusEyeMystics());
        mystics.setSummoningSick(false);
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
