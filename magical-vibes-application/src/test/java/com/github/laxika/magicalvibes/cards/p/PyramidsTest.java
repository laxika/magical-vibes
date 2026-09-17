package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.IceStorm;
import com.github.laxika.magicalvibes.cards.w.WildGrowth;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Pyramids.class, Forest.class, WildGrowth.class, IceStorm.class})
class PyramidsTest extends BaseCardTest {

    private static final String DESTROY_AURA_MODE = "Destroy target Aura attached to a land.";
    private static final String PROTECT_LAND_MODE =
            "The next time target land would be destroyed this turn, remove all damage marked on it instead.";

    @Test
    void destroysAuraAttachedToLand() {
        harness.addToBattlefield(player1, new Pyramids());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new WildGrowth());
        aura.setAttachedTo(land.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        chooseModeAndTarget(DESTROY_AURA_MODE, aura);

        harness.assertNotOnBattlefield(player2, "Wild Growth");
        harness.assertInGraveyard(player2, "Wild Growth");
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(land);
    }

    @Test
    void removesDamageInsteadOfDestroyingTargetLand() {
        harness.addToBattlefield(player1, new Pyramids());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        land.setMarkedDamage(1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        chooseModeAndTarget(PROTECT_LAND_MODE, land);

        harness.setHand(player1, List.of(new IceStorm()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castSorcery(player1, 0, land.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(land);
        assertThat(land.getMarkedDamage()).isZero();
        assertThat(land.getLandDestructionShield()).isZero();
    }

    private void chooseModeAndTarget(String mode, Permanent target) {
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, mode);
        harness.handlePermanentChosen(player1, target.getId());
    }
}
