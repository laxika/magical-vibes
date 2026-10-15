package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
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

@CardUsed({RoadkillRodney.class, GrizzlyBears.class})
class RoadkillRodneyTest extends BaseCardTest {

    @Test
    @DisplayName("Squad creates one token copy for each additional payment")
    void squadCreatesTokenCopies() {
        harness.setHand(player1, List.of(new RoadkillRodney()));
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.castEnchantmentWithRepeatedCosts(player1, 0, List.of("{3}", "{3}"));
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Roadkill Rodney")).hasSize(3);
        assertThat(findPermanents(player1, "Roadkill Rodney"))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .hasSize(2);
    }

    @Test
    @DisplayName("Combat damage to a player creates a Mutagen token")
    void combatDamageCreatesMutagen() {
        Permanent rodney = addCreatureReady(player1, new RoadkillRodney());
        rodney.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Mutagen")).hasSize(1);
    }

    @Test
    @DisplayName("Blocked combat damage does not create a Mutagen token")
    void blockedCombatDoesNotCreateMutagen() {
        Permanent rodney = addCreatureReady(player1, new RoadkillRodney());
        rodney.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Mutagen")).isEmpty();
    }

    @Test
    void squadWithoutPaymentCreatesNoCopies() {
        harness.setHand(player1, List.of(new RoadkillRodney()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Roadkill Rodney")).hasSize(1);
    }

    @Test
    void enteringWithoutBeingCastDoesNotTriggerSquad() {
        harness.enterBattlefieldAndReturn(player1, new RoadkillRodney());

        assertThat(gd.stack).isEmpty();
        assertThat(findPermanents(player1, "Roadkill Rodney")).hasSize(1);
    }

    @Test
    void squadCopiesAlsoCreateMutagenOnCombatDamage() {
        harness.setHand(player1, List.of(new RoadkillRodney()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.castCreatureWithRepeatedCosts(player1, 0, List.of("{3}"));
        resolveAllTriggers();
        assertThat(findPermanents(player1, "Roadkill Rodney")).hasSize(2);
        for (Permanent rodney : findPermanents(player1, "Roadkill Rodney")) {
            rodney.setSummoningSick(false);
            rodney.setAttacking(true);
        }

        resolveCombat();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Mutagen")).hasSize(2);
        assertThat(findPermanents(player2, "Mutagen")).isEmpty();
    }

    @Test
    void createdTokenHasMutagenSubtype() {
        Permanent mutagen = createMutagen();

        assertThat(mutagen.getCard().getSubtypes()).contains(CardSubtype.MUTAGEN);
    }

    @Test
    void deathtouchKillsLargerBlocker() {
        Permanent rodney = addCreatureReady(player1, new RoadkillRodney());
        rodney.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        blocker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(findPermanents(player1, "Mutagen")).isEmpty();
    }

    @Test
    void mutagenCannotActivateWithoutManaOrWhileTapped() {
        Permanent mutagen = createMutagen();
        Permanent target = findPermanent(player1, "Roadkill Rodney");
        int index = gd.playerBattlefields.get(player1.getId()).indexOf(mutagen);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, index, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(mutagen.isTapped()).isFalse();
        assertThat(findPermanents(player1, "Mutagen")).containsExactly(mutagen);

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        mutagen.tap();
        assertThatThrownBy(() -> harness.activateAbility(player1, index, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(findPermanents(player1, "Mutagen")).containsExactly(mutagen);
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void newlyCreatedMutagenCanTargetOpponentsCreature() {
        Permanent mutagen = createMutagen();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RoadkillRodney());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(mutagen),
                0, null, target.getId());

        assertThat(findPermanents(player1, "Mutagen")).isEmpty();
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        resolveAllTriggers();
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void mutagenRequiresSorceryTiming() {
        Permanent mutagen = createMutagen();
        Permanent target = findPermanent(player1, "Roadkill Rodney");
        int index = gd.playerBattlefields.get(player1.getId()).indexOf(mutagen);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateAbility(player1, index, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player2);
        assertThatThrownBy(() -> harness.activateAbility(player1, index, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new RoadkillRodney()));
        harness.castCreature(player1, 0);
        assertThatThrownBy(() -> harness.activateAbility(player1, index, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(mutagen.isTapped()).isFalse();
        assertThat(findPermanents(player1, "Mutagen")).containsExactly(mutagen);
        resolveAllTriggers();

        harness.activateAbility(player1, index, 0, null, target.getId());
        resolveAllTriggers();
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    private Permanent createMutagen() {
        Permanent rodney = addCreatureReady(player1, new RoadkillRodney());
        rodney.setAttacking(true);
        resolveCombat();
        resolveAllTriggers();
        return findPermanent(player1, "Mutagen");
    }
}
