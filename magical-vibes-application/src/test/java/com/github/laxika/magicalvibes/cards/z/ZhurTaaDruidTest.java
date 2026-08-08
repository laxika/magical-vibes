package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ZhurTaaDruidTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping for mana adds {G} and deals 1 damage to each opponent")
    void tappingForManaDamagesOpponent() {
        Permanent druid = harness.addToBattlefieldAndReturn(player1, new ZhurTaaDruid());
        druid.setSummoningSick(false);
        int startingLife = gd.getLife(player2.getId());

        gs.tapPermanent(gd, player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        // CR 603.3: the trigger waits until a player would next receive priority.
        assertThat(gd.getLife(player2.getId())).isEqualTo(startingLife);

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(startingLife - 1);
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Tapping to attack does not trigger the damage ability")
    void attackingDoesNotTrigger() {
        Permanent druid = harness.addToBattlefieldAndReturn(player1, new ZhurTaaDruid());
        druid.setSummoningSick(false);
        int startingLife = gd.getLife(player2.getId());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        declareAttackers(java.util.List.of(0));
        harness.passBothPriorities();

        assertThat(druid.isTapped()).isTrue();
        assertThat(gd.getLife(player2.getId())).isGreaterThanOrEqualTo(startingLife - 1);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.pendingManaAbilityTriggers).isEmpty();
    }
}
