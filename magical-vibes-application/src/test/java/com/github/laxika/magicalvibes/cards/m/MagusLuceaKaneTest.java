package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.d.DevilsPlay;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HormagauntHorde;
import com.github.laxika.magicalvibes.cards.k.KnollspineInvocation;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MagusLuceaKane.class, GrizzlyBears.class, DevilsPlay.class, KnollspineInvocation.class})
class MagusLuceaKaneTest extends BaseCardTest {

    @Test
    @DisplayName("Beginning of combat puts a +1/+1 counter on a target creature")
    void putsCounterAtBeginningOfCombat() {
        addCreatureReady(player1, new MagusLuceaKane());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.BEGINNING_OF_COMBAT);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Copies the next spell with X in its mana cost")
    void copiesNextXSpell() {
        addCreatureReady(player1, new MagusLuceaKane());
        harness.setHand(player1, List.of(new DevilsPlay()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.castSorcery(player1, 0, 2, player2.getId());
        harness.passBothPriorities();
        if (gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class) != null) {
            harness.handleMayAbilityChosen(player1, false);
        }
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Copies the next activated ability with X in its activation cost")
    void copiesNextXActivatedAbility() {
        addCreatureReady(player1, new MagusLuceaKane());
        harness.addToBattlefield(player1, new KnollspineInvocation());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.activateAbility(player1, 1, 2, player2.getId());
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        if (gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class) != null) {
            harness.handleMayAbilityChosen(player1, false);
        }
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("An X spell consumes the single delayed trigger, including its ability alternative")
    void xSpellConsumesAbilityAlternative() {
        addCreatureReady(player1, new MagusLuceaKane());
        harness.addToBattlefield(player1, new KnollspineInvocation());
        harness.setHand(player1, List.of(new DevilsPlay(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.castSorcery(player1, 0, 2, player2.getId());
        resolveCopyKeepingTargets();
        harness.assertLife(player2, 16);

        harness.activateAbility(player1, 1, 2, player2.getId());
        harness.handleCardChosen(player1, 0);
        resolveCopyKeepingTargets();

        harness.assertLife(player2, 14);
    }

    @Test
    @DisplayName("An X ability consumes the single delayed trigger, including its spell alternative")
    void xAbilityConsumesSpellAlternative() {
        addCreatureReady(player1, new MagusLuceaKane());
        harness.addToBattlefield(player1, new KnollspineInvocation());
        harness.setHand(player1, List.of(new GrizzlyBears(), new DevilsPlay()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.activateAbility(player1, 1, 2, player2.getId());
        harness.handleCardChosen(player1, 0);
        resolveCopyKeepingTargets();
        harness.assertLife(player2, 16);

        harness.castSorcery(player1, 0, 2, player2.getId());
        resolveCopyKeepingTargets();

        harness.assertLife(player2, 14);
    }

    @Test
    @DisplayName("Only the first qualifying X spell is copied")
    void secondXSpellIsNotCopied() {
        addCreatureReady(player1, new MagusLuceaKane());
        harness.setHand(player1, List.of(new DevilsPlay(), new DevilsPlay()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.castSorcery(player1, 0, 2, player2.getId());
        resolveCopyKeepingTargets();
        harness.castSorcery(player1, 0, 2, player2.getId());
        resolveCopyKeepingTargets();

        harness.assertLife(player2, 14);
    }

    @Test
    @CardUsed({MagusLuceaKane.class})
    @DisplayName("Spiritual Leader can target an opponent's creature")
    void countersOpponentsCreature() {
        addCreatureReady(player1, new MagusLuceaKane());
        Permanent target = addCreatureReady(player2, new MagusLuceaKane());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.passUntil(TurnStep.BEGINNING_OF_COMBAT);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @CardUsed({MagusLuceaKane.class})
    @DisplayName("Spiritual Leader does not trigger during an opponent's combat")
    void doesNotTriggerInOpponentsCombat() {
        Permanent magus = addCreatureReady(player1, new MagusLuceaKane());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.passUntil(TurnStep.BEGINNING_OF_COMBAT);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(magus.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @CardUsed({MagusLuceaKane.class, HormagauntHorde.class})
    @DisplayName("An X permanent spell becomes a token copy with the same X")
    void copiesPermanentSpellAsToken() {
        addCreatureReady(player1, new MagusLuceaKane());
        harness.setHand(player1, List.of(new HormagauntHorde()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.castCreature(player1, 0, 2);
        resolveCopyKeepingTargets();

        List<Permanent> hordes = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Hormagaunt Horde"))
                .toList();
        assertThat(hordes).hasSize(2);
        assertThat(hordes).allSatisfy(p ->
                assertThat(p.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2));
        assertThat(hordes.stream().filter(p -> p.getCard().isToken()).count()).isEqualTo(1);
    }

    @Test
    @CardUsed({MagusLuceaKane.class, HormagauntHorde.class})
    @DisplayName("X zero still qualifies for Psychic Stimulus")
    void copiesSpellWithZeroX() {
        addCreatureReady(player1, new MagusLuceaKane());
        harness.setHand(player1, List.of(new HormagauntHorde()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.castCreature(player1, 0, 0);
        resolveCopyKeepingTargets();

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Hormagaunt Horde")).toList()).hasSize(2);
    }

    private void resolveCopyKeepingTargets() {
        for (int round = 0; round < 10 && !gd.stack.isEmpty(); round++) {
            harness.passBothPriorities();
            if (gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class) != null) {
                harness.handleMayAbilityChosen(player1, false);
            }
        }
        assertThat(gd.stack).isEmpty();
    }
}
