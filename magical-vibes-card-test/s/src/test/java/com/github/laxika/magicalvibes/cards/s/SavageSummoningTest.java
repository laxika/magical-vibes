package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.k.KalonianHydra;
import com.github.laxika.magicalvibes.cards.k.KalonianTusker;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SavageSummoning.class, Cancel.class, KalonianTusker.class, KalonianHydra.class, Divination.class})
class SavageSummoningTest extends BaseCardTest {

    private void resolveSavageSummoning() {
        harness.castFromHand(player1, new SavageSummoning(), "{G}");
        harness.passBothPriorities();
    }

    private Permanent tuskerOnBattlefield() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Kalonian Tusker"))
                .findFirst()
                .orElseThrow();
    }

    @Test
    @DisplayName("The next creature enters with an additional +1/+1 counter")
    void nextCreatureEntersWithExtraCounter() {
        resolveSavageSummoning();

        harness.castFromHand(player1, new KalonianTusker(), "{G}{G}");
        harness.passBothPriorities();

        assertThat(tuskerOnBattlefield().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("The next creature spell can be cast at instant speed")
    void nextCreatureSpellGainsFlash() {
        resolveSavageSummoning();

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.castFromHand(player1, new KalonianTusker(), "{G}{G}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Kalonian Tusker");
    }

    @Test
    @DisplayName("The next creature spell can't be countered")
    void nextCreatureSpellCantBeCountered() {
        resolveSavageSummoning();

        KalonianTusker tusker = new KalonianTusker();
        harness.setHand(player1, List.of(tusker));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, tusker.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Kalonian Tusker");
        harness.assertInGraveyard(player2, "Cancel");
        assertThat(tuskerOnBattlefield().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Savage Summoning itself can't be countered")
    void savageSummoningCantBeCountered() {
        SavageSummoning summoning = new SavageSummoning();
        harness.setHand(player1, List.of(summoning));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castInstant(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, summoning.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Savage Summoning");
        harness.assertInGraveyard(player2, "Cancel");

        harness.castFromHand(player1, new KalonianTusker(), "{G}{G}");
        harness.passBothPriorities();

        assertThat(tuskerOnBattlefield().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Only the first creature spell is empowered")
    void grantAppliesOnlyToTheFirstCreatureSpell() {
        resolveSavageSummoning();

        harness.setHand(player1, List.of(new KalonianTusker(), new KalonianTusker()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        List<Permanent> tuskers = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Kalonian Tusker"))
                .toList();
        assertThat(tuskers).hasSize(2);
        assertThat(tuskers.getFirst().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(tuskers.getLast().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Without the grant a creature spell is still sorcery-speed")
    void doesNotGrantFlashBeforeResolving() {
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new KalonianTusker()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Multiple Savage Summonings empower the same next creature")
    void multipleGrantsApplyTogether() {
        resolveSavageSummoning();
        resolveSavageSummoning();

        harness.castFromHand(player1, new KalonianTusker(), "{G}{G}");
        harness.passBothPriorities();
        assertThat(tuskerOnBattlefield().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        assertThatThrownBy(() -> harness.castFromHand(player1, new KalonianTusker(), "{G}{G}"))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The additional counter combines with the creature's own entry counters")
    void addsToExistingEntryCounters() {
        resolveSavageSummoning();
        harness.castFromHand(player1, new KalonianHydra(), "{3}{G}{G}");
        harness.passBothPriorities();

        Permanent hydra = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(hydra.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
    }

    @Test
    @DisplayName("An intervening noncreature spell does not consume the creature grants")
    void noncreatureSpellDoesNotConsumeGrants() {
        resolveSavageSummoning();
        harness.setLibrary(player1, List.of(new KalonianTusker(), new KalonianTusker()));
        harness.castFromHand(player1, new Divination(), "{2}{U}");
        harness.passBothPriorities();

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.castFromHand(player1, new KalonianTusker(), "{G}{G}");
        harness.passBothPriorities();
        assertThat(tuskerOnBattlefield().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("The grants work on an opponent's turn and only for their controller")
    void grantsBelongOnlyToController() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        resolveSavageSummoning();

        assertThatThrownBy(() -> harness.castFromHand(player2, new KalonianTusker(), "{G}{G}"))
                .isInstanceOf(IllegalStateException.class);
        harness.castFromHand(player1, new KalonianTusker(), "{G}{G}");
        harness.passBothPriorities();
        assertThat(tuskerOnBattlefield().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Unused grants expire at the end of the turn")
    void unusedGrantsExpire() {
        resolveSavageSummoning();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.setLibrary(player2, List.of(new KalonianTusker()));
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);

        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.clearPriorityPassed();
        assertThatThrownBy(() -> harness.castFromHand(player1, new KalonianTusker(), "{G}{G}"))
                .isInstanceOf(IllegalStateException.class);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player1, new KalonianTusker(), "{G}{G}");
        harness.passBothPriorities();
        assertThat(tuskerOnBattlefield().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("The second creature spell can be countered normally")
    void secondCreatureSpellCanBeCountered() {
        resolveSavageSummoning();
        harness.castFromHand(player1, new KalonianTusker(), "{G}{G}");
        harness.passBothPriorities();

        KalonianTusker second = new KalonianTusker();
        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player2, ManaColor.BLUE, 3);
        harness.castFromHand(player1, second, "{G}{G}");
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, second.getId());

        harness.assertInGraveyard(player1, "Kalonian Tusker");
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }
}
