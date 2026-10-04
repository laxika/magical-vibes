package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.p.PhyrexianDebaser;
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

@CardUsed({FogOfGnats.class, PhyrexianDebaser.class})
class FogOfGnatsTest extends BaseCardTest {

    @Test
    @DisplayName("Activating regeneration records Fog of Gnats as its source")
    void activatingRegenerationRecordsSource() {
        Permanent gnats = addGnatsReady(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getSourcePermanentId()).isEqualTo(gnats.getId());
    }

    @Test
    @DisplayName("Resolving regeneration creates a regeneration shield")
    void resolvingRegenerationCreatesShield() {
        Permanent gnats = addGnatsReady(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gnats.getRegenerationShield()).isEqualTo(1);
        assertThat(gnats.isTapped()).isFalse();
    }

    @Test
    @DisplayName("A regeneration shield saves Fog of Gnats from lethal combat damage")
    void regenerationShieldSavesFromLethalCombatDamage() {
        Permanent gnats = addGnatsReady(player1);
        gnats.setRegenerationShield(1);
        gnats.setBlocking(true);
        gnats.addBlockingTarget(0);

        Permanent attacker = addCreatureReady(player2, new PhyrexianDebaser());
        attacker.setAttacking(true);

        resolveCombat(player2);

        harness.assertOnBattlefield(player1, "Fog of Gnats");
        assertThat(gnats.isTapped()).isTrue();
        assertThat(gnats.isBlocking()).isFalse();
        assertThat(gnats.getMarkedDamage()).isZero();
        assertThat(gnats.getRegenerationShield()).isZero();
    }

    @Test
    void diesWithoutRegenerationShield() {
        Permanent gnats = addGnatsReady(player1);
        gnats.setBlocking(true);
        gnats.addBlockingTarget(0);
        Permanent attacker = addCreatureReady(player2, new PhyrexianDebaser());
        attacker.setAttacking(true);

        resolveCombat(player2);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(gnats);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(gnats.getCard());
    }

    @Test
    void regenerationCanBeActivatedWhileTappedAndSummoningSick() {
        harness.addToBattlefield(player1, new FogOfGnats());
        Permanent gnats = findPermanent(player1, "Fog of Gnats");
        gnats.tap();
        gnats.setSummoningSick(true);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gnats.getRegenerationShield()).isEqualTo(1);
        assertThat(gnats.isTapped()).isTrue();
        harness.assertOnBattlefield(player1, "Fog of Gnats");
    }

    @Test
    void repeatedActivationsCreateSeparateShieldsOnlyForSource() {
        Permanent gnats = addGnatsReady(player1);
        Permanent otherGnats = addGnatsReady(player1);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gnats.getRegenerationShield()).isEqualTo(2);
        assertThat(otherGnats.getRegenerationShield()).isZero();
        assertThat(gnats.isTapped()).isFalse();
    }

    @Test
    void activatedShieldSavesSourceFromCombatDamage() {
        Permanent gnats = addGnatsReady(player1);
        Permanent attacker = addCreatureReady(player2, new PhyrexianDebaser());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        gnats.setBlocking(true);
        gnats.addBlockingTarget(0);
        attacker.setAttacking(true);

        resolveCombat(player2);

        harness.assertOnBattlefield(player1, "Fog of Gnats");
        assertThat(gnats.isTapped()).isTrue();
        assertThat(gnats.isBlocking()).isFalse();
        assertThat(gnats.getMarkedDamage()).isZero();
        assertThat(gnats.getRegenerationShield()).isZero();
    }

    @Test
    void regenerationDoesNotPreventDeathFromZeroToughness() {
        Permanent gnats = addGnatsReady(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        addCreatureReady(player1, new PhyrexianDebaser());

        harness.activateAbility(player1, 1, null, gnats.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(gnats);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(gnats.getCard());
    }

    private Permanent addGnatsReady(Player player) {
        return addCreatureReady(player, new FogOfGnats());
    }
}
