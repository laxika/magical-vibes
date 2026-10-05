package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.IceStorm;
import com.github.laxika.magicalvibes.cards.w.WildGrowth;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
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
        harness.castAndResolveSorcery(player1, 0, land.getId());

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(land);
        assertThat(land.getMarkedDamage()).isZero();
        assertThat(land.getLandDestructionShield()).isZero();
        assertThat(land.isTapped()).isFalse();
    }

    @Test
    void choosesTargetDuringActivationBeforeOpponentsCanRespond() {
        harness.addToBattlefield(player1, new Pyramids());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, land.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(land.getId());
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.passBothPriorities();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void protectionOnlyReplacesTheNextDestruction() {
        harness.addToBattlefield(player1, new Pyramids());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        chooseModeAndTarget(PROTECT_LAND_MODE, land);
        harness.setHand(player1, List.of(new IceStorm(), new IceStorm()));
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.castAndResolveSorcery(player1, 0, land.getId());
        harness.assertOnBattlefield(player2, "Forest");
        harness.castAndResolveSorcery(player1, 0, land.getId());

        harness.assertNotOnBattlefield(player2, "Forest");
        harness.assertInGraveyard(player2, "Forest");
    }

    @Test
    void protectionExpiresAtEndOfTurn() {
        harness.addToBattlefield(player1, new Pyramids());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        chooseModeAndTarget(PROTECT_LAND_MODE, land);

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new IceStorm()));
        harness.addMana(player2, ManaColor.GREEN, 3);
        harness.castAndResolveSorcery(player2, 0, land.getId());

        harness.assertNotOnBattlefield(player2, "Forest");
        harness.assertInGraveyard(player2, "Forest");
    }

    private void chooseModeAndTarget(String mode, Permanent target) {
        int modeIndex = DESTROY_AURA_MODE.equals(mode) ? 0 : 1;
        harness.activateAbility(player1, 0, modeIndex, target.getId());
        harness.passBothPriorities();
    }
}
