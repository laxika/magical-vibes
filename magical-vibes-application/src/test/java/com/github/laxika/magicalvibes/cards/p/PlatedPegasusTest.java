package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.cards.c.Combust;
import com.github.laxika.magicalvibes.cards.f.FledglingMawcor;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.o.OrcishCannonade;
import com.github.laxika.magicalvibes.cards.s.SlipstreamSerpent;
import com.github.laxika.magicalvibes.cards.s.SuddenShock;
import com.github.laxika.magicalvibes.cards.s.SuddenSpoiling;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PlatedPegasus.class, SuddenShock.class, FledglingMawcor.class,
        ChandraNalaar.class, Combust.class, SlipstreamSerpent.class, Island.class,
        OrcishCannonade.class, SuddenSpoiling.class})
class PlatedPegasusTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents 1 damage from a spell to a player")
    void preventsSpellDamageToPlayer() {
        harness.addToBattlefield(player1, new PlatedPegasus());
        harness.setHand(player2, List.of(new SuddenShock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player2, 0, player1.getId());

        harness.assertLife(player1, 19);
    }

    @Test
    @DisplayName("Prevents 1 damage from a spell to a creature")
    void preventsSpellDamageToCreature() {
        harness.addToBattlefield(player1, new PlatedPegasus());
        harness.addToBattlefield(player2, new Island());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new SlipstreamSerpent());
        harness.setHand(player1, List.of(new SuddenShock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, creature.getId());

        assertThat(creature.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Prevents 1 damage from a spell to a planeswalker")
    void preventsSpellDamageToPlaneswalker() {
        harness.addToBattlefield(player1, new PlatedPegasus());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new ChandraNalaar());
        planeswalker.setCounterCount(CounterType.LOYALTY, 5);
        harness.setHand(player1, List.of(new SuddenShock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, planeswalker.getId());

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
    }

    @Test
    @DisplayName("Does not prevent damage from an activated ability")
    void doesNotPreventAbilityDamage() {
        harness.addToBattlefield(player1, new PlatedPegasus());
        Permanent sorcerer = addCreatureReady(player2, new FledglingMawcor());
        harness.forceActivePlayer(player2);

        harness.activateAbility(player2, gd.playerBattlefields.get(player2.getId()).indexOf(sorcerer),
                null, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
    }

    @Test
    @DisplayName("Does not prevent combat damage")
    void doesNotPreventCombatDamage() {
        harness.addToBattlefield(player1, new PlatedPegasus());
        Permanent attacker = addCreatureReady(player2, new FledglingMawcor());

        declareAttackersAndPrepareBlockers(player2,
                List.of(gd.playerBattlefields.get(player2.getId()).indexOf(attacker)));
        gs.declareBlockers(gd, player1, List.of());
        resolveCombat(player2);

        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("Does not prevent damage that cannot be prevented")
    void doesNotPreventUnpreventableSpellDamage() {
        harness.addToBattlefield(player1, new PlatedPegasus());
        harness.addToBattlefield(player2, new Island());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new SlipstreamSerpent());
        harness.setHand(player1, List.of(new Combust()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, creature.getId());

        assertThat(creature.getMarkedDamage()).isEqualTo(5);
    }

    @Test
    @DisplayName("Prevents each damage event from a spell independently")
    void preventsEachSpellDamageEventIndependently() {
        harness.addToBattlefield(player1, new PlatedPegasus());
        harness.addToBattlefield(player2, new Island());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new SlipstreamSerpent());
        harness.setHand(player1, List.of(new OrcishCannonade()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, creature.getId());

        assertThat(creature.getMarkedDamage()).isEqualTo(1);
        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("Stops preventing damage after losing its abilities")
    void doesNotPreventDamageAfterLosingAbilities() {
        harness.addToBattlefield(player1, new PlatedPegasus());
        harness.setHand(player2, List.of(new SuddenSpoiling()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player2, 0, player1.getId());

        harness.setHand(player2, List.of(new SuddenShock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0, player1.getId());

        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("Multiple Pegasi prevent damage cumulatively for every spell")
    void multiplePegasiPreventDamageForEachSpell() {
        harness.addToBattlefield(player1, new PlatedPegasus());
        harness.addToBattlefield(player2, new PlatedPegasus());
        harness.setHand(player2, List.of(new SuddenShock(), new SuddenShock()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player2, 0, player1.getId());
        harness.castAndResolveInstant(player2, 0, player1.getId());

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Prevents spell damage to itself")
    void preventsSpellDamageToItself() {
        Permanent pegasus = harness.addToBattlefieldAndReturn(player1, new PlatedPegasus());
        harness.setHand(player2, List.of(new SuddenShock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player2, 0, pegasus.getId());

        assertThat(pegasus.getMarkedDamage()).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(pegasus);
    }

    @Test
    @DisplayName("Can enter in response to a spell and prevent its damage")
    void flashPreventsDamageFromPendingSpell() {
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new OrcishCannonade()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.setHand(player1, List.of(new PlatedPegasus()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player2, 0, player1.getId());
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.assertLife(player1, 19);
        harness.assertLife(player2, 18);
    }
}
