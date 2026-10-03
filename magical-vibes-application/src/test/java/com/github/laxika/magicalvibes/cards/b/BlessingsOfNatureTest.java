package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.m.MoorlandInquisitor;
import com.github.laxika.magicalvibes.cards.t.TamiyoTheMoonSage;
import com.github.laxika.magicalvibes.cards.f.Forest;
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
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BlessingsOfNature.class, MoorlandInquisitor.class, Forest.class, TamiyoTheMoonSage.class})
class BlessingsOfNatureTest extends BaseCardTest {

    private void prepareCast() {
        harness.setHand(player1, List.of(new BlessingsOfNature()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
    }

    @Test
    @DisplayName("Distributes four +1/+1 counters as announced among several creatures")
    void distributesFourCountersAmongThreeCreatures() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new MoorlandInquisitor());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new MoorlandInquisitor());
        Permanent third = harness.addToBattlefieldAndReturn(player2, new MoorlandInquisitor());
        prepareCast();

        harness.castSorcery(player1, 0, Map.of(first.getId(), 2, second.getId(), 1, third.getId(), 1));
        harness.passBothPriorities();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(third.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(first.getEffectivePower()).isEqualTo(4);
        assertThat(first.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("All four counters may go on a single creature")
    void allFourOnOneCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new MoorlandInquisitor());
        prepareCast();

        harness.castSorcery(player1, 0, Map.of(bears.getId(), 4));
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    @DisplayName("Assignments must sum to four")
    void assignmentsMustSumToFour() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new MoorlandInquisitor());
        prepareCast();

        assertThatThrownBy(() ->
                harness.castSorcery(player1, 0, Map.of(bears.getId(), 3))
        ).isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Rejects a noncreature target")
    void rejectsNoncreatureTarget() {
        Permanent mountain = harness.addToBattlefieldAndReturn(player1, new Forest());
        prepareCast();

        assertThatThrownBy(() ->
                harness.castSorcery(player1, 0, Map.of(mountain.getId(), 4))
        ).isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Target enumeration offers creatures only — not planeswalkers and not players")
    void enumerationOffersCreaturesOnly() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new MoorlandInquisitor());
        Permanent mountain = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent liliana = harness.addToBattlefieldAndReturn(player2, new TamiyoTheMoonSage());
        prepareCast();

        var response = harness.getValidTargetService().computeValidTargetsForSpell(
                harness.getGameData(),
                harness.getGameData().playerHands.get(player1.getId()).getFirst(),
                player1.getId(), null);

        assertThat(response.validPermanentIds())
                .contains(bears.getId())
                .doesNotContain(mountain.getId(), liliana.getId());
        assertThat(response.validPlayerIds()).isEmpty();
    }

    @Test
    @DisplayName("Skips a target that left the battlefield, keeping the rest")
    void skipsTargetThatLeftTheBattlefield() {
        Permanent staying = harness.addToBattlefieldAndReturn(player1, new MoorlandInquisitor());
        Permanent leaving = harness.addToBattlefieldAndReturn(player1, new MoorlandInquisitor());
        prepareCast();

        harness.castSorcery(player1, 0, Map.of(staying.getId(), 2, leaving.getId(), 2));

        harness.getGameData().playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getId().equals(leaving.getId()));

        harness.passBothPriorities();

        assertThat(staying.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(leaving.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Drawing it as the first card this turn offers a miracle reveal")
    void firstDrawOffersMiracleReveal() {
        BlessingsOfNature blessings = new BlessingsOfNature();
        harness.setLibrary(player1, List.of(blessings));

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        harness.inMutationScope(() -> harness.getPlayerInputService().processNextMayAbility(gd));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(blessings.getId()));
    }

    @Test
    @DisplayName("A later draw this turn does not offer miracle")
    void laterDrawDoesNotOfferMiracle() {
        gd.cardsDrawnThisTurn.put(player1.getId(), 1);
        harness.setLibrary(player1, List.of(new BlessingsOfNature()));

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));

        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Each chosen creature must receive at least one counter")
    void rejectsZeroCounterAssignment() {
        Permanent receiving = harness.addToBattlefieldAndReturn(player1, new MoorlandInquisitor());
        Permanent excluded = harness.addToBattlefieldAndReturn(player2, new MoorlandInquisitor());
        prepareCast();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0,
                Map.of(receiving.getId(), 4, excluded.getId(), 0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Four creatures can each receive one counter")
    void distributesAmongFourCreatures() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new MoorlandInquisitor());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new MoorlandInquisitor());
        Permanent third = harness.addToBattlefieldAndReturn(player2, new MoorlandInquisitor());
        Permanent fourth = harness.addToBattlefieldAndReturn(player2, new MoorlandInquisitor());
        prepareCast();

        harness.castSorcery(player1, 0, Map.of(first.getId(), 1, second.getId(), 1,
                third.getId(), 1, fourth.getId(), 1));
        harness.passBothPriorities();

        for (Permanent creature : List.of(first, second, third, fourth)) {
            assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        }
    }

    @Test
    @DisplayName("Miracle casting for one green mana still places all four counters")
    void miracleCastPlacesFourCounters() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new MoorlandInquisitor());
        harness.setLibrary(player1, List.of(new BlessingsOfNature()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.forceStep(TurnStep.DRAW);

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        harness.inMutationScope(() -> harness.getPlayerInputService().processNextMayAbility(gd));
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.assertInGraveyard(player1, "Blessings of Nature");
    }

    @Test
    @DisplayName("A miracle cannot be cast without any legal creature targets")
    void miracleWithoutTargetsStaysInHand() {
        harness.setLibrary(player1, List.of(new BlessingsOfNature()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        harness.inMutationScope(() -> harness.getPlayerInputService().processNextMayAbility(gd));
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Blessings of Nature");
        harness.assertNotInGraveyard(player1, "Blessings of Nature");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }
}
