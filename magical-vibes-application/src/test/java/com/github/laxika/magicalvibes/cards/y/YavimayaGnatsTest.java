package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.b.BalduvianBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({YavimayaGnats.class, BalduvianBears.class})
class YavimayaGnatsTest extends BaseCardTest {

    @Test
    @DisplayName("Activating regeneration puts the ability on the stack for Yavimaya Gnats")
    void activatingRegenPutsOnStackForSource() {
        Permanent perm = addCreatureReady(player1, new YavimayaGnats());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getTargetId()).isEqualTo(perm.getId());
    }

    @Test
    @DisplayName("Resolving regeneration grants a regeneration shield")
    void resolvingRegenGrantsShield() {
        addCreatureReady(player1, new YavimayaGnats());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent gnats = findPermanent(player1, "Yavimaya Gnats");
        assertThat(gnats.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Regeneration shield saves Yavimaya Gnats from lethal combat damage")
    void regenSavesFromLethalCombat() {
        Permanent perm = addCreatureReady(player1, new YavimayaGnats());
        perm.setRegenerationShield(1);
        perm.setBlocking(true);
        perm.addBlockingTarget(0);

        Permanent attacker = addCreatureReady(player2, 5, 5);
        attacker.setAttacking(true);

        resolveCombat(player2);

        harness.assertOnBattlefield(player1, "Yavimaya Gnats");
        Permanent gnats = findPermanent(player1, "Yavimaya Gnats");
        assertThat(gnats.isTapped()).isTrue();
        assertThat(gnats.isBlocking()).isFalse();
        assertThat(gnats.getRegenerationShield()).isEqualTo(0);
    }

    @Test
    @DisplayName("Yavimaya Gnats dies without a regeneration shield")
    void diesWithoutRegenShield() {
        Permanent perm = addCreatureReady(player1, new YavimayaGnats());
        perm.setBlocking(true);
        perm.addBlockingTarget(0);

        Permanent attacker = addCreatureReady(player2, 5, 5);
        attacker.setAttacking(true);

        resolveCombat(player2);

        harness.assertNotOnBattlefield(player1, "Yavimaya Gnats");
        harness.assertInGraveyard(player1, "Yavimaya Gnats");
    }

    private Permanent addCreatureReady(Player player, int power, int toughness) {
        BalduvianBears card = new BalduvianBears();
        card.setPower(power);
        card.setToughness(toughness);
        return addCreatureReady(player, card);
    }
}
