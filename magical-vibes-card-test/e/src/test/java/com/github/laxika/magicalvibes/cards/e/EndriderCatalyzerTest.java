package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EndriderCatalyzer.class})
class EndriderCatalyzerTest extends BaseCardTest {

    @Test
    void startsEnginesWhenItEntersTheBattlefield() {
        harness.castFromHand(player1, new EndriderCatalyzer(), "{1}{R}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerSpeeds.get(player1.getId())).isEqualTo(1);
    }

    @Test
    void addsTwoRedManaAtMaxSpeed() {
        Permanent catalyzer = addCreatureReady(player1, new EndriderCatalyzer());
        gd.playerSpeeds.put(player1.getId(), 4);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(2);
        assertThat(catalyzer.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateBeforeMaxSpeed() {
        Permanent catalyzer = addCreatureReady(player1, new EndriderCatalyzer());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("max speed");
        assertThat(catalyzer.isTapped()).isFalse();
    }

    @Test
    void cannotActivateAtSpeedThreeEvenIfOpponentHasMaxSpeed() {
        Permanent catalyzer = addCreatureReady(player1, new EndriderCatalyzer());
        gd.playerSpeeds.put(player1.getId(), 3);
        gd.playerSpeeds.put(player2.getId(), 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("max speed");
        assertThat(catalyzer.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }

    @Test
    void cannotActivateWhileSummoningSickAtMaxSpeed() {
        Permanent catalyzer = harness.addToBattlefieldAndReturn(player1, new EndriderCatalyzer());
        catalyzer.setSummoningSick(true);
        gd.playerSpeeds.put(player1.getId(), 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
        assertThat(catalyzer.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }

    @Test
    void cannotActivateAgainWithoutUntapping() {
        addCreatureReady(player1, new EndriderCatalyzer());
        gd.playerSpeeds.put(player1.getId(), 4);
        harness.activateAbility(player1, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(2);
    }

    @Test
    void enteringDoesNotResetExistingSpeed() {
        gd.playerSpeeds.put(player1.getId(), 3);

        harness.castFromHand(player1, new EndriderCatalyzer(), "{1}{R}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerSpeeds.get(player1.getId())).isEqualTo(3);
    }

    @Test
    void combatDamageIncreasesSpeedDuringControllersTurn() {
        addCreatureReady(player1, new EndriderCatalyzer());
        harness.runStateBasedActions();

        declareAttackers(List.of(0));
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerSpeeds.get(player1.getId())).isEqualTo(2);
    }
}
