package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RecklessFireweaver;
import com.github.laxika.magicalvibes.cards.s.SurgeNode;
import com.github.laxika.magicalvibes.cards.s.SuturePriest;
import com.github.laxika.magicalvibes.cards.m.MakeshiftBinding;
import com.github.laxika.magicalvibes.cards.n.NoviceInspector;
import com.github.laxika.magicalvibes.cards.o.Opalescence;
import com.github.laxika.magicalvibes.cards.w.WitnessProtection;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DoorkeeperThrull.class, GrizzlyBears.class, SuturePriest.class,
        RecklessFireweaver.class, SurgeNode.class, NoviceInspector.class,
        MakeshiftBinding.class, Opalescence.class, WitnessProtection.class})
class DoorkeeperThrullTest extends BaseCardTest {

    @Test
    @DisplayName("A creature entering does not cause creature-enter triggers")
    void suppressesCreatureEnteringTriggers() {
        harness.addToBattlefield(player1, new DoorkeeperThrull());
        harness.addToBattlefield(player1, new SuturePriest());
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("A noncreature artifact entering does not cause artifact-enter triggers")
    void suppressesNoncreatureArtifactEnteringTriggers() {
        harness.addToBattlefield(player1, new DoorkeeperThrull());
        harness.addToBattlefield(player1, new RecklessFireweaver());
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new SurgeNode()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }
    @Test
    @DisplayName("Creatures entering under either player's control have no own enter trigger")
    void suppressesOpponentsOwnEnterTrigger() {
        harness.addToBattlefield(player2, new DoorkeeperThrull());
        harness.setHand(player1, List.of(new NoviceInspector()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Novice Inspector");
        assertThat(countPermanents(player1, "Clue")).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Thrull suppresses triggers caused by its own arrival")
    void suppressesItsOwnArrival() {
        harness.addToBattlefield(player1, new SuturePriest());
        harness.setHand(player1, List.of(new DoorkeeperThrull()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Doorkeeper Thrull");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Entering with counters is unaffected by trigger suppression")
    void preservesEntryReplacementEffects() {
        harness.addToBattlefield(player1, new DoorkeeperThrull());
        harness.setHand(player1, List.of(new SurgeNode()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Surge Node").getCounterCount(CounterType.CHARGE)).isEqualTo(6);
    }

    @Test
    @DisplayName("A nonartifact noncreature enchantment still triggers when it enters")
    void allowsOrdinaryEnchantmentEnterTrigger() {
        harness.addToBattlefield(player1, new DoorkeeperThrull());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new NoviceInspector());
        harness.setHand(player1, List.of(new MakeshiftBinding()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castEnchantment(player1, 0, target.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Novice Inspector");
        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("An enchantment entering as a creature cannot cause enter triggers")
    void suppressesContinuouslyAnimatedEnchantmentEnterTrigger() {
        harness.addToBattlefield(player1, new DoorkeeperThrull());
        harness.addToBattlefield(player1, new Opalescence());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new NoviceInspector());
        harness.setHand(player1, List.of(new MakeshiftBinding()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castEnchantment(player1, 0, target.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Makeshift Binding");
        harness.assertOnBattlefield(player2, "Novice Inspector");
        harness.assertLife(player1, 20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Thrull stops suppressing triggers when an Aura removes its abilities")
    void abilityRemovalDisablesSuppression() {
        Permanent thrull = harness.addToBattlefieldAndReturn(player1, new DoorkeeperThrull());
        harness.setHand(player1, List.of(new WitnessProtection(), new NoviceInspector()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castEnchantment(player1, 0, thrull.getId());
        harness.passBothPriorities();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Novice Inspector");
        assertThat(countPermanents(player1, "Clue")).isEqualTo(1);
    }

    @Test
    @DisplayName("Flashing Thrull in before a creature resolves prevents its enter trigger")
    void flashBeforeCreatureResolvesPreventsTrigger() {
        harness.setHand(player1, List.of(new NoviceInspector()));
        harness.setHand(player2, List.of(new DoorkeeperThrull()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.WHITE, 2);

        harness.castCreature(player1, 0);
        harness.castCreature(player2, 0);
        resolveAllTriggers();

        harness.assertOnBattlefield(player2, "Doorkeeper Thrull");
        harness.assertOnBattlefield(player1, "Novice Inspector");
        assertThat(countPermanents(player1, "Clue")).isZero();
    }

    @Test
    @DisplayName("Flashing Thrull in does not remove an enter trigger already on the stack")
    void flashAfterCreatureEntersDoesNotUndoTrigger() {
        harness.setHand(player1, List.of(new NoviceInspector()));
        harness.setHand(player2, List.of(new DoorkeeperThrull()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.WHITE, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);
        harness.castCreature(player2, 0);
        resolveAllTriggers();

        harness.assertOnBattlefield(player2, "Doorkeeper Thrull");
        assertThat(countPermanents(player1, "Clue")).isEqualTo(1);
    }

}
