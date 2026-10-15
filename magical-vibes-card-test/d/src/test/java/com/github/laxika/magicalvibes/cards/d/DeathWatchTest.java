package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.h.HulkingCyclops;
import com.github.laxika.magicalvibes.cards.h.Hushbringer;
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
        Permanent deathWatch = harness.addToBattlefieldAndReturn(player1, new DeathWatch());
        deathWatch.setAttachedTo(python.getId());

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
        Permanent deathWatch = harness.addToBattlefieldAndReturn(player1, new DeathWatch());
        deathWatch.setAttachedTo(cyclops.getId());

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
        Permanent deathWatch = harness.addToBattlefieldAndReturn(player1, new DeathWatch());
        deathWatch.setAttachedTo(python.getId());

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
        Permanent deathWatch = harness.addToBattlefieldAndReturn(player1, new DeathWatch());
        deathWatch.setAttachedTo(cyclops.getId());
        harness.setLife(player1, 5);

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
        Permanent deathWatch = harness.addToBattlefieldAndReturn(player1, new DeathWatch());
        deathWatch.setAttachedTo(python.getId());
        harness.setLife(player2, 3);

        python.setMarkedDamage(2);
        harness.runStateBasedActions();
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(22);
    }

    @Test
    @DisplayName("Cannot enchant a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new WandOfDenial());
        harness.setHand(player1, List.of(new DeathWatch()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Does not trigger when the enchanted creature is exiled")
    void doesNotTriggerWhenEnchantedCreatureIsExiled() {
        Permanent python = harness.addToBattlefieldAndReturn(player2, new Python());
        Permanent deathWatch = harness.addToBattlefieldAndReturn(player1, new DeathWatch());
        deathWatch.setAttachedTo(python.getId());
        int p1Before = gd.getLife(player1.getId());
        int p2Before = gd.getLife(player2.getId());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToExile(gd, python));

        assertThat(gd.getLife(player1.getId())).isEqualTo(p1Before);
        assertThat(gd.getLife(player2.getId())).isEqualTo(p2Before);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Casting Death Watch attaches it and its ability survives the Aura going to the graveyard")
    void castAuraTriggersAfterCreatureDies() {
        Permanent python = harness.addToBattlefieldAndReturn(player2, new Python());
        harness.setHand(player1, List.of(new DeathWatch()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castEnchantment(player1, 0, python.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Death Watch").getAttachedTo()).isEqualTo(python.getId());
        python.setMarkedDamage(2);
        harness.runStateBasedActions();

        assertThat(countPermanents(player1, "Death Watch")).isZero();
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(22);
        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Negative power causes no life loss but positive toughness still grants life")
    void negativePowerDoesNotReverseLifeLoss() {
        Permanent python = harness.addToBattlefieldAndReturn(player2, new Python());
        python.setPowerModifier(-4);
        Permanent deathWatch = harness.addToBattlefieldAndReturn(player1, new DeathWatch());
        deathWatch.setAttachedTo(python.getId());

        python.setMarkedDamage(2);
        harness.runStateBasedActions();
        resolveAllTriggers();

        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
        assertThat(gd.getLife(player1.getId())).isEqualTo(22);
    }

    @Test
    @DisplayName("Negative toughness causes no life gain but positive power still causes life loss")
    void negativeToughnessDoesNotReverseLifeGain() {
        Permanent python = harness.addToBattlefieldAndReturn(player2, new Python());
        Permanent deathWatch = harness.addToBattlefieldAndReturn(player1, new DeathWatch());
        deathWatch.setAttachedTo(python.getId());
        python.setToughnessModifier(-3);

        harness.runStateBasedActions();
        resolveAllTriggers();

        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Each Death Watch attached to the same creature triggers independently")
    void multipleAurasEachTrigger() {
        Permanent python = harness.addToBattlefieldAndReturn(player2, new Python());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new DeathWatch());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new DeathWatch());
        first.setAttachedTo(python.getId());
        second.setAttachedTo(python.getId());

        python.setMarkedDamage(2);
        harness.runStateBasedActions();
        resolveAllTriggers();

        assertThat(gd.getLife(player2.getId())).isEqualTo(14);
        assertThat(gd.getLife(player1.getId())).isEqualTo(24);
    }

    @Test
    @CardUsed({DeathWatch.class, Python.class, Hushbringer.class})
    @DisplayName("Hushbringer prevents Death Watch from triggering when the enchanted creature dies")
    void hushbringerSuppressesDeathTrigger() {
        harness.addToBattlefield(player1, new Hushbringer());
        Permanent python = harness.addToBattlefieldAndReturn(player2, new Python());
        Permanent deathWatch = harness.addToBattlefieldAndReturn(player1, new DeathWatch());
        deathWatch.setAttachedTo(python.getId());

        python.setMarkedDamage(2);
        harness.runStateBasedActions();
        resolveAllTriggers();

        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.stack).isEmpty();
    }
}
