package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(KorSkyClimber.class)
class KorSkyClimberTest extends BaseCardTest {

    @Test
    void payingManaGrantsFlying() {
        Permanent skyClimber = addCreatureReady(player1, new KorSkyClimber());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        assertThat(gqs.hasKeyword(gd, skyClimber, Keyword.FLYING)).isFalse();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, skyClimber, Keyword.FLYING)).isTrue();
    }

    @Test
    void flyingWearsOffAtEndOfTurn() {
        Permanent skyClimber = addCreatureReady(player1, new KorSkyClimber());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, skyClimber, Keyword.FLYING)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, skyClimber, Keyword.FLYING)).isFalse();
    }

    @Test
    void cannotActivateWithoutEnoughMana() {
        addCreatureReady(player1, new KorSkyClimber());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    void cannotPayWhiteManaWithOnlyColorlessMana() {
        addCreatureReady(player1, new KorSkyClimber());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    void canActivateWhileTappedAndSummoningSickOnOpponentsTurn() {
        Permanent skyClimber = harness.addToBattlefieldAndReturn(player1, new KorSkyClimber());
        skyClimber.setSummoningSick(true);
        skyClimber.tap();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, skyClimber, Keyword.FLYING)).isTrue();
        assertThat(skyClimber.isTapped()).isTrue();
    }

    @Test
    void grantsFlyingOnlyToTheActivatingPermanent() {
        Permanent skyClimber = addCreatureReady(player1, new KorSkyClimber());
        Permanent otherClimber = addCreatureReady(player1, new KorSkyClimber());
        Permanent opposingClimber = addCreatureReady(player2, new KorSkyClimber());
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, skyClimber, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, otherClimber, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, opposingClimber, Keyword.FLYING)).isFalse();
    }

    @Test
    void removedSourceDoesNotGrantFlyingToAnotherCopy() {
        Permanent skyClimber = addCreatureReady(player1, new KorSkyClimber());
        Permanent otherClimber = addCreatureReady(player1, new KorSkyClimber());
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.activateAbility(player1, 0, null, null);
        gd.playerBattlefields.get(player1.getId()).remove(skyClimber);
        gd.playerGraveyards.get(player1.getId()).add(skyClimber.getCard());

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.hasKeyword(gd, otherClimber, Keyword.FLYING)).isFalse();
    }
}
