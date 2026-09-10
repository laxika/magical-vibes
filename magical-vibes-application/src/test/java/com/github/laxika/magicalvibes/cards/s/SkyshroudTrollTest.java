package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.m.MoggFanatic;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SkyshroudTroll.class, MoggFanatic.class})
class SkyshroudTrollTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving the activated ability grants a regeneration shield")
    void resolvingRegenGrantsShield() {
        addCreatureReady(player1, new SkyshroudTroll());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent troll = findPermanent(player1, "Skyshroud Troll");
        assertThat(troll.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("The regeneration ability can be activated while Skyshroud Troll is tapped")
    void canActivateWhileTapped() {
        Permanent troll = addCreatureReady(player1, new SkyshroudTroll());
        troll.tap();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(troll.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Regeneration shield saves Skyshroud Troll from lethal combat damage")
    void regenSavesFromLethalCombat() {
        Permanent perm = addCreatureReady(player1, new SkyshroudTroll());
        perm.setRegenerationShield(1);
        perm.setBlocking(true);
        perm.addBlockingTarget(0);

        Permanent attacker = addCreatureReady(player2, 5, 5);
        attacker.setAttacking(true);

        resolveCombat(player2);

        harness.assertOnBattlefield(player1, "Skyshroud Troll");
        Permanent troll = findPermanent(player1, "Skyshroud Troll");
        assertThat(troll.isTapped()).isTrue();
        assertThat(troll.isBlocking()).isFalse();
        assertThat(troll.getMarkedDamage()).isZero();
        assertThat(troll.getRegenerationShield()).isEqualTo(0);
    }

    @Test
    @DisplayName("Skyshroud Troll dies without a regeneration shield")
    void diesWithoutRegenShield() {
        Permanent perm = addCreatureReady(player1, new SkyshroudTroll());
        perm.setBlocking(true);
        perm.addBlockingTarget(0);

        Permanent attacker = addCreatureReady(player2, 5, 5);
        attacker.setAttacking(true);

        resolveCombat(player2);

        harness.assertNotOnBattlefield(player1, "Skyshroud Troll");
        harness.assertInGraveyard(player1, "Skyshroud Troll");
    }

    private Permanent addCreatureReady(Player player, int power, int toughness) {
        MoggFanatic card = new MoggFanatic();
        card.setPower(power);
        card.setToughness(toughness);
        return addCreatureReady(player, card);
    }
}
