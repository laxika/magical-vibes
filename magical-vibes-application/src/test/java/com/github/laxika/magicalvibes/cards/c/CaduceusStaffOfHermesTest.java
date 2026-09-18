package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CaduceusStaffOfHermes.class, DoomBlade.class, GrizzlyBears.class, Shock.class})
class CaduceusStaffOfHermesTest extends BaseCardTest {

    @Test
    @DisplayName("Equipping Caduceus attaches it to a creature for {W}{W}")
    void equipsToCreature() {
        Permanent staff = addStaffReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(staff.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Equipped creature has lifelink but no rider below 30 life")
    void belowThirtyLifeOnlyGrantsLifelink() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent staff = addStaffReady(player1);
        staff.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.LIFELINK)).isTrue();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Equipped creature gets the full rider at exactly 30 life")
    void atThirtyLifeGetsFullRider() {
        harness.setLife(player1, 30);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent staff = addStaffReady(player1);
        staff.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(7);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.LIFELINK)).isTrue();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("The 30-life rider turns off when life drops below 30")
    void riderTurnsOffBelowThirtyLife() {
        harness.setLife(player1, 30);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent staff = addStaffReady(player1);
        staff.setAttachedTo(creature.getId());

        harness.setLife(player1, 29);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("The 30-life rider prevents noncombat damage to the equipped creature")
    void preventsNoncombatDamage() {
        harness.setLife(player1, 30);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent staff = addStaffReady(player1);
        staff.setAttachedTo(creature.getId());

        castAtCreature(player2, new Shock(), creature);

        assertThat(creature.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("The 30-life rider prevents combat damage to the equipped creature")
    void preventsCombatDamage() {
        harness.setLife(player1, 30);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent staff = addStaffReady(player1);
        staff.setAttachedTo(creature.getId());

        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        attacker.setAttacking(true);
        creature.setBlocking(true);
        creature.addBlockingTarget(0);

        resolveCombat(player2);

        assertThat(creature.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Indestructible from the rider prevents destruction at 30 life")
    void indestructiblePreventsDestruction() {
        harness.setLife(player1, 30);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent staff = addStaffReady(player1);
        staff.setAttachedTo(creature.getId());

        castAtCreature(player2, new DoomBlade(), creature);

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    private void castAtCreature(Player caster, com.github.laxika.magicalvibes.model.Card spell,
                                Permanent target) {
        harness.forceActivePlayer(caster);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(caster, List.of(spell));
        harness.addMana(caster, spell instanceof DoomBlade ? ManaColor.BLACK : ManaColor.RED,
                spell instanceof DoomBlade ? 2 : 1);
        harness.castAndResolveInstant(caster, 0, target.getId());
    }

    private Permanent addStaffReady(Player player) {
        Permanent perm = new Permanent(new CaduceusStaffOfHermes());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
