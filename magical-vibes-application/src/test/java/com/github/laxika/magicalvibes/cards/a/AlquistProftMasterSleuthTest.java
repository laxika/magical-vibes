package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AlquistProftMasterSleuth.class, Forest.class})
class AlquistProftMasterSleuthTest extends BaseCardTest {

    @Test
    void entersAndInvestigates() {
        harness.castFromHand(player1, new AlquistProftMasterSleuth(), "{1}{W}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    void sacrificesAClueToDrawAndGainLifeForX() {
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest()));
        harness.castFromHand(player1, new AlquistProftMasterSleuth(), "{1}{W}{U}");
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent proft = findPermanent(player1, "Alquist Proft, Master Sleuth");
        proft.setSummoningSick(false);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.activateAbility(player1, 0, 0, 2, null);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Clue")).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 2);
        assertThat(proft.isTapped()).isTrue();
    }

    @Test
    void zeroXStillRequiresTappingAndSacrificingAClue() {
        harness.castFromHand(player1, new AlquistProftMasterSleuth(), "{1}{W}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        Permanent proft = findPermanent(player1, "Alquist Proft, Master Sleuth");
        proft.setSummoningSick(false);
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 2);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.activateAbility(player1, 0, 0, 0, null);

        assertThat(proft.isTapped()).isTrue();
        assertThat(findPermanents(player1, "Clue")).isEmpty();
        harness.passBothPriorities();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertLife(player1, lifeBefore);
    }

    @Test
    void cannotActivateWithoutAClueEvenWithZeroX() {
        Permanent proft = addCreatureReady(player1, new AlquistProftMasterSleuth());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, 0, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(proft.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateWhileSummoningSick() {
        harness.castFromHand(player1, new AlquistProftMasterSleuth(), "{1}{W}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, 0, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(findPermanent(player1, "Alquist Proft, Master Sleuth").isTapped()).isFalse();
        assertThat(findPermanents(player1, "Clue")).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void investigatedClueCanBeSacrificedForItsOwnDrawAbility() {
        harness.castFromHand(player1, new AlquistProftMasterSleuth(), "{1}{W}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.activateAbility(player1, 1, 0, null, null, null);
        assertThat(findPermanents(player1, "Clue")).isEmpty();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        harness.assertLife(player1, lifeBefore);
        assertThat(findPermanent(player1, "Alquist Proft, Master Sleuth").isTapped()).isFalse();
    }

    @Test
    void cannotSacrificeAnOpponentsClue() {
        Permanent proft = addCreatureReady(player1, new AlquistProftMasterSleuth());
        harness.enterBattlefieldAndReturn(player2, new AlquistProftMasterSleuth());
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThat(findPermanents(player2, "Clue")).hasSize(1);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, 0, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(findPermanents(player2, "Clue")).hasSize(1);
        assertThat(proft.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }
}
