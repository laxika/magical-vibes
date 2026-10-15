package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.c.CylianElf;
import com.github.laxika.magicalvibes.cards.i.Infest;
import com.github.laxika.magicalvibes.cards.m.Mosstodon;
import com.github.laxika.magicalvibes.cards.r.ResoundingWave;
import com.github.laxika.magicalvibes.cards.v.VithianStinger;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DeftDuelist.class, CylianElf.class, Mosstodon.class, ResoundingWave.class,
        VithianStinger.class, Infest.class})
class DeftDuelistTest extends BaseCardTest {

    @Test
    @DisplayName("First strike kills a 2/2 blocker before it deals regular damage")
    void firstStrikeKillsBlockerBeforeRegularDamage() {
        Permanent attacker = addCreatureReady(player1, new DeftDuelist());
        attacker.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new CylianElf());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        resolveCombat();

        // Deft Duelist deals 2 first strike damage, killing the 2/2 before it can deal damage back.
        harness.assertOnBattlefield(player1, "Deft Duelist");
        harness.assertNotOnBattlefield(player2, "Cylian Elf");
    }

    @Test
    @DisplayName("Deft Duelist still dies if the blocker survives first strike damage")
    void diesIfBlockerSurvivesFirstStrike() {
        Permanent attacker = addCreatureReady(player1, new DeftDuelist());
        attacker.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new Mosstodon());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        resolveCombat();

        // Mosstodon survives 2 first strike damage and kills the Duelist in regular damage.
        harness.assertNotOnBattlefield(player1, "Deft Duelist");
        harness.assertOnBattlefield(player2, "Mosstodon");
    }

    @Test
    @DisplayName("Opponent spells cannot target Deft Duelist")
    void opponentSpellsCannotTarget() {
        harness.forceActivePlayer(player2);
        harness.addToBattlefield(player1, new DeftDuelist());
        // Add valid target so the spell is playable.
        harness.addToBattlefield(player1, new CylianElf());
        harness.setHand(player2, List.of(new ResoundingWave()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0,
                harness.getPermanentId(player1, "Deft Duelist")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shroud");
    }

    @Test
    @DisplayName("Your own spells cannot target Deft Duelist")
    void ownSpellsCannotTarget() {
        harness.addToBattlefield(player1, new DeftDuelist());
        harness.addToBattlefield(player1, new CylianElf());
        harness.setHand(player1, List.of(new ResoundingWave()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                harness.getPermanentId(player1, "Deft Duelist")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shroud");
    }

    @Test
    @DisplayName("First strike also kills an attacker before it damages the blocking Duelist")
    void firstStrikeWorksWhileBlocking() {
        Permanent attacker = addCreatureReady(player1, new CylianElf());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new DeftDuelist());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        harness.assertInGraveyard(player1, "Cylian Elf");
        harness.assertOnBattlefield(player2, "Deft Duelist");
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("An unblocked Duelist deals combat damage only once")
    void firstStrikeDoesNotDealRegularDamageAgain() {
        Permanent attacker = addCreatureReady(player1, new DeftDuelist());
        attacker.setAttacking(true);

        resolveCombat();

        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Shroud prevents an opponent's activated ability from targeting the Duelist")
    void opponentAbilityCannotTarget() {
        harness.addToBattlefield(player1, new DeftDuelist());
        addCreatureReady(player2, new VithianStinger());

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null,
                harness.getPermanentId(player1, "Deft Duelist")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shroud");
    }

    @Test
    @DisplayName("Shroud prevents its controller's activated ability from targeting the Duelist")
    void ownAbilityCannotTarget() {
        harness.addToBattlefield(player1, new DeftDuelist());
        addCreatureReady(player1, new VithianStinger());

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null,
                harness.getPermanentId(player1, "Deft Duelist")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shroud");
    }

    @Test
    @DisplayName("Shroud does not protect the Duelist from a spell that does not target")
    void nonTargetedSpellStillAffectsDuelist() {
        harness.addToBattlefield(player1, new DeftDuelist());
        harness.setHand(player1, List.of(new Infest()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Deft Duelist");
        harness.assertInGraveyard(player1, "Deft Duelist");
    }
}
