package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.d.DarkthicketWolf;
import com.github.laxika.magicalvibes.cards.f.FesterhideBoar;
import com.github.laxika.magicalvibes.cards.h.HighAlert;
import com.github.laxika.magicalvibes.cards.p.PreyUpon;
import com.github.laxika.magicalvibes.cards.s.SkaabGoliath;
import com.github.laxika.magicalvibes.cards.t.TurnToFrog;
import com.github.laxika.magicalvibes.cards.w.WalkingCorpse;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GraveBramble.class, WalkingCorpse.class, FesterhideBoar.class, DarkthicketWolf.class,
        HighAlert.class, PreyUpon.class, SkaabGoliath.class, TurnToFrog.class, GrimgrinCorpseBorn.class})
class GraveBrambleTest extends BaseCardTest {

    @Test
    @DisplayName("Grave Bramble takes no combat damage from Zombie creature when blocking")
    void takesNoDamageFromZombie() {
        Permanent attacker = addCreatureReady(player1, new WalkingCorpse());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new GraveBramble());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.resolveCombatDamage();
        harness.runStateBasedActions();

        harness.assertInGraveyard(player1, "Walking Corpse");
        harness.assertOnBattlefield(player2, "Grave Bramble");
        assertThat(blocker.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Grave Bramble takes normal combat damage from non-Zombie creature when blocking")
    void takesNormalDamageFromNonZombie() {
        Permanent attacker = addCreatureReady(player1, new FesterhideBoar());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new GraveBramble());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.resolveCombatDamage();
        harness.withAutoStop(TurnStep.COMBAT_DAMAGE,
                () -> harness.handleCombatDamageAssigned(player1, 0, Map.of(blocker.getId(), 3)));
        harness.runStateBasedActions();

        harness.assertInGraveyard(player1, "Festerhide Boar");
        harness.assertOnBattlefield(player2, "Grave Bramble");
        assertThat(blocker.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("Zombie creature cannot block Grave Bramble when it is allowed to attack")
    void zombieCannotBlock() {
        addCreatureReady(player1, new GraveBramble());
        harness.addToBattlefield(player1, new HighAlert());
        addCreatureReady(player2, new WalkingCorpse());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("Non-Zombie creature can block Grave Bramble when it is allowed to attack")
    void nonZombieCanBlock() {
        addCreatureReady(player1, new GraveBramble());
        harness.addToBattlefield(player1, new HighAlert());
        Permanent blocker = addCreatureReady(player2, new DarkthicketWolf());

        declareAttackersAndPrepareBlockers(List.of(0));
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS,
                () -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Defender prevents Grave Bramble from attacking")
    void cannotAttackWithDefender() {
        Permanent bramble = addCreatureReady(player1, new GraveBramble());
        addCreatureReady(player2, new DarkthicketWolf());

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
        assertThat(bramble.isAttacking()).isFalse();
    }

    @Test
    @DisplayName("Protection prevents lethal noncombat damage from a Zombie during a fight")
    void preventsZombieFightDamage() {
        Permanent bramble = harness.addToBattlefieldAndReturn(player1, new GraveBramble());
        Permanent zombie = harness.addToBattlefieldAndReturn(player2, new SkaabGoliath());
        harness.setHand(player1, List.of(new PreyUpon()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveSorcery(player1, 0, List.of(bramble.getId(), zombie.getId()));

        harness.assertOnBattlefield(player1, "Grave Bramble");
        harness.assertOnBattlefield(player2, "Skaab Goliath");
        assertThat(bramble.getMarkedDamage()).isZero();
        assertThat(zombie.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("Protection prevents targeting by a Zombie's triggered ability")
    void cannotBeTargetedByZombieAbility() {
        addCreatureReady(player1, new GrimgrinCorpseBorn());
        Permanent bramble = addCreatureReady(player2, new GraveBramble());
        Permanent wolf = addCreatureReady(player2, new DarkthicketWolf());

        declareAttackers(List.of(0));

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, bramble.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, wolf.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Darkthicket Wolf");
        harness.assertOnBattlefield(player2, "Grave Bramble");
    }

    @Test
    @DisplayName("Protection does not prevent damage from a Zombie that became only a Frog")
    void takesDamageWhenSourceLosesZombieType() {
        Permanent attacker = addCreatureReady(player1, new WalkingCorpse());
        Permanent bramble = addCreatureReady(player2, new GraveBramble());
        harness.setHand(player1, List.of(new TurnToFrog()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, attacker.getId());

        attacker.setAttacking(true);
        bramble.setBlocking(true);
        bramble.addBlockingTarget(0);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.resolveCombatDamage();
        harness.runStateBasedActions();

        harness.assertInGraveyard(player1, "Walking Corpse");
        harness.assertOnBattlefield(player2, "Grave Bramble");
        assertThat(bramble.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Removing all abilities removes protection from Zombie combat damage")
    void losesProtectionWhenAllAbilitiesAreRemoved() {
        Permanent attacker = addCreatureReady(player1, new WalkingCorpse());
        Permanent bramble = addCreatureReady(player2, new GraveBramble());
        harness.setHand(player1, List.of(new TurnToFrog()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, bramble.getId());

        attacker.setAttacking(true);
        bramble.setBlocking(true);
        bramble.addBlockingTarget(0);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.resolveCombatDamage();
        harness.runStateBasedActions();

        harness.assertInGraveyard(player2, "Grave Bramble");
        harness.assertNotOnBattlefield(player2, "Grave Bramble");
        harness.assertOnBattlefield(player1, "Walking Corpse");
    }
}
