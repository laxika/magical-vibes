package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.b.BatteringRam;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GolgothianSylex.class, BatteringRam.class, Forest.class, GrizzlyBears.class})
class GolgothianSylexTest extends BaseCardTest {

    private void prepareSylex() {
        harness.addToBattlefield(player1, new GolgothianSylex());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private void activateSylex() {
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Sacrifices every matching nontoken permanent on both battlefields")
    void sacrificesMatchingNontokenPermanents() {
        prepareSylex();
        harness.addToBattlefield(player1, new BatteringRam());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new BatteringRam());
        harness.addToBattlefield(player2, new Forest());

        activateSylex();

        harness.assertInGraveyard(player1, "Golgothian Sylex");
        harness.assertInGraveyard(player1, "Battering Ram");
        harness.assertInGraveyard(player2, "Battering Ram");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Forest");
    }

    @Test
    @DisplayName("Does not sacrifice tokens, even when their names were printed in ATQ")
    void sparesMatchingTokens() {
        prepareSylex();
        Card token = new BatteringRam();
        token.setToken(true);
        harness.addToBattlefield(player2, token);

        activateSylex();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(permanent -> permanent.getCard().getName())
                .containsExactly("Battering Ram");
    }

    @Test
    @DisplayName("Matches a permanent by name even when its card is a later printing")
    void matchesRenamedPermanentByName() {
        prepareSylex();
        Card renamedPermanent = new GrizzlyBears();
        renamedPermanent.setName("Mishra's Factory");
        harness.addToBattlefield(player2, renamedPermanent);

        activateSylex();

        harness.assertInGraveyard(player2, "Mishra's Factory");
    }
}
