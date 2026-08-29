package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({QuagmireDruid.class, GloriousAnthem.class, GrizzlyBears.class})
class QuagmireDruidTest extends BaseCardTest {

    @Test
    void sacrificesCreatureTapsAndDestroysTargetEnchantment() {
        Permanent druid = harness.addToBattlefieldAndReturn(player1, new QuagmireDruid());
        druid.setSummoningSick(false);
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID fodderId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.addToBattlefield(player2, new GloriousAnthem());
        UUID targetId = harness.getPermanentId(player2, "Glorious Anthem");
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 0, null, targetId);
        harness.handlePermanentChosen(player1, fodderId);
        harness.passBothPriorities();

        assertThat(druid.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Glorious Anthem");
        harness.assertInGraveyard(player2, "Glorious Anthem");
    }

    @Test
    void cannotTargetCreature() {
        Permanent druid = harness.addToBattlefieldAndReturn(player1, new QuagmireDruid());
        druid.setSummoningSick(false);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class);
    }
}
