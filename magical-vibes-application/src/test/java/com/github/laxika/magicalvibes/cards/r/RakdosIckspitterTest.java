package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RakdosIckspitter.class, GrizzlyBears.class, LlanowarElves.class, Forest.class})
class RakdosIckspitterTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 1 damage to a creature and its controller loses 1 life")
    void damagesCreatureAndItsControllerLosesLife() {
        Permanent ickspitter = addCreatureReady(player1, new RakdosIckspitter());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        GameData gameData = harness.getGameData();
        assertThat(ickspitter.isTapped()).isTrue();
        assertThat(target.getMarkedDamage()).isEqualTo(1);
        assertThat(gameData.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("The controller loses life even when the damage destroys the target")
    void controllerLosesLifeWhenDamageDestroysTarget() {
        addCreatureReady(player1, new RakdosIckspitter());
        Permanent target = addCreatureReady(player2, new LlanowarElves());
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Llanowar Elves");
        harness.assertInGraveyard(player2, "Llanowar Elves");
        assertThat(harness.getGameData().playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        addCreatureReady(player1, new RakdosIckspitter());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }
}
