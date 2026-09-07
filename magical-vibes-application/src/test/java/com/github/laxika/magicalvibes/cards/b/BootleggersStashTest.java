package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BootleggersStash.class, Forest.class})
class BootleggersStashTest extends BaseCardTest {

    @Test
    void controlledLandsCanTapToCreateTreasure() {
        harness.addToBattlefield(player1, new BootleggersStash());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        forest.setSummoningSick(false);

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(forest.isTapped()).isTrue();
        assertThat(countPermanents(player1, "Treasure")).isEqualTo(1);
    }

    @Test
    void doesNotGrantAbilityToOpponentLands() {
        harness.addToBattlefield(player1, new BootleggersStash());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        forest.setSummoningSick(false);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no activated ability");
    }

    @Test
    void landsLoseGrantedAbilityWhenStashLeavesBattlefield() {
        Permanent stash = harness.addToBattlefieldAndReturn(player1, new BootleggersStash());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        forest.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).remove(stash);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no activated ability");
    }
}
