package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.Atog;
import com.github.laxika.magicalvibes.cards.b.BatteringRam;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MishrasFactory;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GolgothianSylex.class, BatteringRam.class, Forest.class, GrizzlyBears.class,
        Atog.class, MishrasFactory.class})
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

    @Test
    @DisplayName("Sacrifices matching lands and nonartifact creatures")
    void sacrificesMatchingNonartifactPermanents() {
        prepareSylex();
        harness.addToBattlefield(player1, new Atog());
        harness.addToBattlefield(player2, new MishrasFactory());

        activateSylex();

        harness.assertInGraveyard(player1, "Atog");
        harness.assertInGraveyard(player2, "Mishra's Factory");
        harness.assertNotOnBattlefield(player1, "Atog");
        harness.assertNotOnBattlefield(player2, "Mishra's Factory");
    }

    @Test
    @DisplayName("Pays the mana and tap costs before sacrificing anything")
    void paysCostsBeforeResolution() {
        prepareSylex();
        harness.addToBattlefield(player2, new BatteringRam());

        harness.activateAbility(player1, 0, null, null);

        assertThat(findPermanent(player1, "Golgothian Sylex").isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        harness.assertOnBattlefield(player1, "Golgothian Sylex");
        harness.assertOnBattlefield(player2, "Battering Ram");
        harness.assertNotInGraveyard(player1, "Golgothian Sylex");
        harness.assertNotInGraveyard(player2, "Battering Ram");

        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Golgothian Sylex");
        harness.assertInGraveyard(player2, "Battering Ram");
    }

    @Test
    @DisplayName("Cannot activate while tapped")
    void cannotActivateWhileTapped() {
        prepareSylex();
        findPermanent(player1, "Golgothian Sylex").tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Golgothian Sylex");
    }

    @Test
    @DisplayName("A token Sylex can activate but is not sacrificed by its own ability")
    void tokenSourceSurvivesItsAbility() {
        Card token = new GolgothianSylex();
        token.setToken(true);
        harness.addToBattlefield(player1, token);
        harness.addToBattlefield(player2, new BatteringRam());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        activateSylex();

        harness.assertOnBattlefield(player1, "Golgothian Sylex");
        harness.assertNotInGraveyard(player1, "Golgothian Sylex");
        harness.assertInGraveyard(player2, "Battering Ram");
    }

    @Test
    @DisplayName("The ability still resolves after Sylex is sacrificed in response")
    void resolvesAfterSourceLeavesBattlefield() {
        prepareSylex();
        harness.addToBattlefield(player1, new Atog());
        harness.addToBattlefield(player2, new BatteringRam());

        harness.activateAbility(player1, 0, null, null);
        harness.activateAbility(player1, 1, null, null);

        harness.assertInGraveyard(player1, "Golgothian Sylex");
        harness.assertOnBattlefield(player1, "Atog");
        harness.assertOnBattlefield(player2, "Battering Ram");

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Atog");
        harness.assertInGraveyard(player2, "Battering Ram");
        assertThat(gd.stack).isEmpty();
    }
}
