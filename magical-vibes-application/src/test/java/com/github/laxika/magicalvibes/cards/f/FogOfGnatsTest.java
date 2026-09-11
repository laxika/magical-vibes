package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({FogOfGnats.class, GrizzlyBears.class})
class FogOfGnatsTest extends BaseCardTest {

    @Test
    @DisplayName("Activating regeneration targets Fog of Gnats")
    void activatingRegenerationTargetsSelf() {
        Permanent gnats = addGnatsReady(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getTargetId()).isEqualTo(gnats.getId());
    }

    @Test
    @DisplayName("Resolving regeneration creates a regeneration shield")
    void resolvingRegenerationCreatesShield() {
        Permanent gnats = addGnatsReady(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gnats.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("A regeneration shield saves Fog of Gnats from lethal combat damage")
    void regenerationShieldSavesFromLethalCombatDamage() {
        Permanent gnats = addGnatsReady(player1);
        gnats.setRegenerationShield(1);
        gnats.setBlocking(true);
        gnats.addBlockingTarget(0);

        Permanent attacker = addCreatureReady(player2, 5, 5);
        attacker.setAttacking(true);

        resolveCombat(player2);

        harness.assertOnBattlefield(player1, "Fog of Gnats");
        assertThat(gnats.isTapped()).isTrue();
        assertThat(gnats.isBlocking()).isFalse();
        assertThat(gnats.getRegenerationShield()).isZero();
    }

    @Test
    void diesWithoutRegenerationShield() {
        Permanent gnats = addGnatsReady(player1);
        gnats.setBlocking(true);
        gnats.addBlockingTarget(0);
        Permanent attacker = addCreatureReady(player2, 5, 5);
        attacker.setAttacking(true);

        resolveCombat(player2);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(gnats);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(gnats.getCard());
    }

    private Permanent addGnatsReady(Player player) {
        return addCreatureReady(player, new FogOfGnats());
    }

    private Permanent addCreatureReady(Player player, int power, int toughness) {
        GrizzlyBears card = new GrizzlyBears();
        card.setPower(power);
        card.setToughness(toughness);
        return addCreatureReady(player, card);
    }
}
