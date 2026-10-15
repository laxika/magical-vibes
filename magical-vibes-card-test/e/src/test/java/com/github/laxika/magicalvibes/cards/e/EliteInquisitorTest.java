package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.d.DarkthicketWolf;
import com.github.laxika.magicalvibes.cards.m.MarkovPatrician;
import com.github.laxika.magicalvibes.cards.o.OliviaVoldaren;
import com.github.laxika.magicalvibes.cards.p.PitchburnDevils;
import com.github.laxika.magicalvibes.cards.s.StromkirkPatrol;
import com.github.laxika.magicalvibes.cards.v.VillagersOfEstwald;
import com.github.laxika.magicalvibes.cards.w.WalkingCorpse;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EliteInquisitor.class, MarkovPatrician.class, VillagersOfEstwald.class,
        WalkingCorpse.class, DarkthicketWolf.class, StromkirkPatrol.class,
        PitchburnDevils.class, OliviaVoldaren.class})
class EliteInquisitorTest extends BaseCardTest {

    @Test
    @DisplayName("Vampire creature cannot block Elite Inquisitor")
    void vampireCannotBlock() {
        Permanent attacker = addCreatureReady(player1, new EliteInquisitor());
        attacker.setAttacking(true);

        addCreatureReady(player2, new MarkovPatrician());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("Werewolf creature cannot block Elite Inquisitor")
    void werewolfCannotBlock() {
        Permanent attacker = addCreatureReady(player1, new EliteInquisitor());
        attacker.setAttacking(true);

        addCreatureReady(player2, new VillagersOfEstwald());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("Zombie creature cannot block Elite Inquisitor")
    void zombieCannotBlock() {
        Permanent attacker = addCreatureReady(player1, new EliteInquisitor());
        attacker.setAttacking(true);

        addCreatureReady(player2, new WalkingCorpse());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("Non-protected creature can block Elite Inquisitor")
    void regularCreatureCanBlock() {
        Permanent attacker = addCreatureReady(player1, new EliteInquisitor());
        attacker.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new DarkthicketWolf());

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Elite Inquisitor takes no combat damage from Vampire creature")
    void takesNoDamageFromVampire() {
        Permanent attacker = addCreatureReady(player1, new StromkirkPatrol());
        attacker.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new EliteInquisitor());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        // Elite Inquisitor has first strike: deals 2 damage first (Vampire survives with 2 damage marked)
        // Vampire's 4 damage to Elite Inquisitor is prevented (protection from Vampires)
        // Both creatures survive
        harness.assertOnBattlefield(player1, "Stromkirk Patrol");
        harness.assertOnBattlefield(player2, "Elite Inquisitor");
    }

    @Test
    @DisplayName("Elite Inquisitor takes normal combat damage from non-protected creature")
    void takesNormalDamageFromRegularCreature() {
        Permanent attacker = addCreatureReady(player1, new PitchburnDevils());
        attacker.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new EliteInquisitor());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        // Elite Inquisitor deals 2 first strike damage (Pitchburn Devils survives with 2 damage marked)
        // Pitchburn Devils deals 3 regular damage (kills 2/2 Elite Inquisitor)
        harness.assertOnBattlefield(player1, "Pitchburn Devils");
        harness.assertNotOnBattlefield(player2, "Elite Inquisitor");
    }

    @Test
    @DisplayName("First strike kills an unprotected blocker before it can deal damage")
    void firstStrikeKillsBlockerBeforeRegularDamage() {
        addCreatureReady(player1, new EliteInquisitor());
        addCreatureReady(player2, new DarkthicketWolf());

        declareAttackersAndPrepareBlockers(List.of(0));
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS,
                () -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))));
        harness.passUntil(TurnStep.END_OF_COMBAT);

        harness.assertOnBattlefield(player1, "Elite Inquisitor");
        harness.assertInGraveyard(player2, "Darkthicket Wolf");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Attacking with vigilance does not tap Elite Inquisitor")
    void vigilanceLeavesAttackerUntapped() {
        Permanent inquisitor = addCreatureReady(player1, new EliteInquisitor());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThat(inquisitor.isAttacking()).isTrue();
        assertThat(inquisitor.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Protection prevents targeting by a Vampire's activated ability")
    void vampireAbilityCannotTargetInquisitor() {
        Permanent inquisitor = addCreatureReady(player1, new EliteInquisitor());
        addCreatureReady(player2, new OliviaVoldaren());
        harness.addMana(player2, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, 0, null, inquisitor.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }
}
