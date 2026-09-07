package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.h.HulkingCyclops;
import com.github.laxika.magicalvibes.cards.p.Python;
import com.github.laxika.magicalvibes.cards.w.WandOfDenial;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DeathWatch.class, Python.class, HulkingCyclops.class, WandOfDenial.class})
class DeathWatchTest extends BaseCardTest {

    @Test
    @DisplayName("When enchanted creature dies, controller loses life = power and you gain life = toughness")
    void enchantedCreatureDeathDrainsPowerGainsToughness() {
        // Python is 3/2 — loss tracks power (3), gain tracks toughness (2).
        Permanent python = harness.addToBattlefieldAndReturn(player2, new Python());
        Permanent deathWatch = new Permanent(new DeathWatch());
        deathWatch.setAttachedTo(python.getId());
        gd.playerBattlefields.get(player1.getId()).add(deathWatch);

        int p1Before = gd.getLife(player1.getId());
        int p2Before = gd.getLife(player2.getId());

        python.setMarkedDamage(2);
        harness.runStateBasedActions();
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(p2Before - 3);
        assertThat(gd.getLife(player1.getId())).isEqualTo(p1Before + 2);
    }

    @Test
    @DisplayName("Enchanting your own creature applies both halves to you")
    void ownCreatureBothHalves() {
        Permanent cyclops = harness.addToBattlefieldAndReturn(player1, new HulkingCyclops());
        Permanent deathWatch = new Permanent(new DeathWatch());
        deathWatch.setAttachedTo(cyclops.getId());
        gd.playerBattlefields.get(player1.getId()).add(deathWatch);

        int lifeBefore = gd.getLife(player1.getId());

        cyclops.setMarkedDamage(5);
        harness.runStateBasedActions();
        harness.passBothPriorities();

        // 5/5: lose 5, gain 5 → net zero
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Uses the enchanted creature's last-known effective power and toughness")
    void usesLastKnownEffectivePowerAndToughness() {
        Permanent python = harness.addToBattlefieldAndReturn(player2, new Python());
        python.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent deathWatch = new Permanent(new DeathWatch());
        deathWatch.setAttachedTo(python.getId());
        gd.playerBattlefields.get(player1.getId()).add(deathWatch);

        int p1Before = gd.getLife(player1.getId());
        int p2Before = gd.getLife(player2.getId());

        python.setMarkedDamage(3);
        harness.runStateBasedActions();
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(p2Before - 4);
        assertThat(gd.getLife(player1.getId())).isEqualTo(p1Before + 3);
    }

    @Test
    @DisplayName("Resolves both life changes before checking state-based actions")
    void resolvesBothLifeChangesBeforeCheckingStateBasedActions() {
        Permanent cyclops = harness.addToBattlefieldAndReturn(player1, new HulkingCyclops());
        Permanent deathWatch = new Permanent(new DeathWatch());
        deathWatch.setAttachedTo(cyclops.getId());
        gd.playerBattlefields.get(player1.getId()).add(deathWatch);
        gd.playerLifeTotals.put(player1.getId(), 5);

        cyclops.setMarkedDamage(5);
        harness.runStateBasedActions();
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(5);
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    @Test
    @DisplayName("The controller's life gain still happens when the creature's controller reaches zero")
    void gainsLifeAfterCreatureControllerReachesZero() {
        Permanent python = harness.addToBattlefieldAndReturn(player2, new Python());
        Permanent deathWatch = new Permanent(new DeathWatch());
        deathWatch.setAttachedTo(python.getId());
        gd.playerBattlefields.get(player1.getId()).add(deathWatch);
        gd.playerLifeTotals.put(player2.getId(), 3);

        python.setMarkedDamage(2);
        harness.runStateBasedActions();
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(22);
    }

    @Test
    @DisplayName("Cannot enchant a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player1, new WandOfDenial());
        harness.setHand(player1, List.of(new DeathWatch()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        Permanent artifact = findPermanent(player1, "Wand of Denial");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Does not trigger when the enchanted creature is exiled")
    void doesNotTriggerWhenEnchantedCreatureIsExiled() {
        Permanent python = harness.addToBattlefieldAndReturn(player2, new Python());
        Permanent deathWatch = new Permanent(new DeathWatch());
        deathWatch.setAttachedTo(python.getId());
        gd.playerBattlefields.get(player1.getId()).add(deathWatch);
        int p1Before = gd.getLife(player1.getId());
        int p2Before = gd.getLife(player2.getId());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToExile(gd, python));

        assertThat(gd.getLife(player1.getId())).isEqualTo(p1Before);
        assertThat(gd.getLife(player2.getId())).isEqualTo(p2Before);
        assertThat(gd.stack).isEmpty();
    }
}
