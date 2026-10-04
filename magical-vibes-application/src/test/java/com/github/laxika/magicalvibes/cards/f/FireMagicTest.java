package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

@CardUsed({FireMagic.class, FugitiveWizard.class, GrizzlyBears.class, HillGiant.class})
class FireMagicTest extends BaseCardTest {

    @Test
    void fireDealsOneDamageToEachCreature() {
        harness.addToBattlefield(player1, new FugitiveWizard());
        harness.addToBattlefield(player2, new GrizzlyBears());

        cast(0, 0);

        harness.assertNotOnBattlefield(player1, "Fugitive Wizard");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    void firaDealsTwoDamageToEachCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new HillGiant());

        cast(1, 2);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Hill Giant");
    }

    @Test
    void firagaDealsThreeDamageToEachCreature() {
        harness.addToBattlefield(player2, new HillGiant());

        cast(2, 5);

        harness.assertNotOnBattlefield(player2, "Hill Giant");
    }

    @ParameterizedTest
    @CsvSource({"0, 0", "1, 2", "2, 5"})
    void eachModeDamagesBothControllersCreaturesWithoutDamagingPlayers(int mode, int additionalMana) {
        harness.addToBattlefield(player1, new FugitiveWizard());
        harness.addToBattlefield(player2, new FugitiveWizard());
        harness.setLife(player1, 17);
        harness.setLife(player2, 14);

        cast(mode, additionalMana);

        harness.assertNotOnBattlefield(player1, "Fugitive Wizard");
        harness.assertNotOnBattlefield(player2, "Fugitive Wizard");
        harness.assertLife(player1, 17);
        harness.assertLife(player2, 14);
    }

    @ParameterizedTest
    @CsvSource({"1, 1", "2, 4"})
    void cannotCastWithoutPayingTheFullAdditionalCost(int mode, int availableGenericMana) {
        harness.setHand(player1, List.of(new FireMagic()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, availableGenericMana);

        assertThatThrownBy(() -> harness.castModalInstant(player1, 0, mode, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void fireDamageRemainsMarkedUntilEndOfTurn() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        cast(0, 0);

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Grizzly Bears");

        cast(0, 0);

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }
    private void cast(int mode, int colorlessMana) {
        harness.setHand(player1, List.of(new FireMagic()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, colorlessMana);
        harness.castModalInstant(player1, 0, mode, List.of());
        harness.passBothPriorities();
    }
}
