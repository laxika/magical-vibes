package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CauldronOfSouls.class, Confiscate.class, DoomBlade.class, GrizzlyBears.class, Mountain.class})
class CauldronOfSoulsTest extends BaseCardTest {

    /** Resolves the stack until the game pauses for input or the stack empties. */
    private void resolveUntilInputOrEmpty() {
        for (int i = 0; i < 12; i++) {
            GameData g = harness.getGameData();
            if (g.interaction.isAwaitingInput() || g.stack.isEmpty()) {
                return;
            }
            harness.passBothPriorities();
        }
    }

    /** Adds Cauldron of Souls, ready to tap, at battlefield index 0. */
    private Permanent addCauldronReady() {
        Permanent cauldron = harness.addToBattlefieldAndReturn(player1, new CauldronOfSouls());
        cauldron.setSummoningSick(false); // artifacts aren't affected, but keeps activation clean
        return cauldron;
    }

    @Test
    @DisplayName("Granted persist returns a killed creature with a -1/-1 counter")
    void grantedPersistReturnsKilledCreature() {
        addCauldronReady();
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(bears.getId()));
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.PERSIST)).isTrue();

        harness.setHand(player1, List.of(new DoomBlade()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castInstant(player1, 0, bears.getId());
        resolveUntilInputOrEmpty();

        Permanent returned = findPermanent(player1, "Grizzly Bears");
        assertThat(returned).isNotNull();
        assertThat(returned.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("A creature not granted persist stays dead")
    void ungrantedCreatureStaysDead() {
        addCauldronReady();
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new DoomBlade()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castInstant(player1, 0, bears.getId());
        resolveUntilInputOrEmpty();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Any number of creatures can be granted persist at once")
    void grantsPersistToMultipleCreatures() {
        addCauldronReady();
        Permanent bears1 = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent bears2 = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(bears1.getId(), bears2.getId()));
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears1, Keyword.PERSIST)).isTrue();
        assertThat(gqs.hasKeyword(gd, bears2, Keyword.PERSIST)).isTrue();
    }

    @Test
    @DisplayName("Granted persist wears off at end of turn")
    void persistWearsOffAtEndOfTurn() {
        addCauldronReady();
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(bears.getId()));
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.PERSIST)).isTrue();

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.hasKeyword(gd, bears, Keyword.PERSIST)).isFalse();
    }

    @Test
    @DisplayName("Ability can only target creatures")
    void cannotTargetNonCreature() {
        addCauldronReady();
        harness.addToBattlefield(player1, new Mountain());

        UUID mountainId = harness.getPermanentId(player1, "Mountain");

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(mountainId)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Activating with zero targets still taps the Cauldron")
    void canChooseZeroTargets() {
        Permanent cauldron = addCauldronReady();
        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of());
        assertThat(cauldron.isTapped()).isTrue();
        harness.passBothPriorities();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Any number includes more than ninety-nine creatures")
    void canTargetOneHundredCreatures() {
        addCauldronReady();
        List<Permanent> creatures = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            creatures.add(harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()));
        }
        harness.activateAbilityWithMultiTargets(player1, 0, 0,
                creatures.stream().map(Permanent::getId).toList());
        harness.passBothPriorities();
        assertThat(creatures).allSatisfy(creature ->
                assertThat(gqs.hasKeyword(gd, creature, Keyword.PERSIST)).isTrue());
    }

    @Test
    @DisplayName("A creature with a -1/-1 counter does not return through granted persist")
    void persistDoesNotTriggerWithMinusOneCounter() {
        addCauldronReady();
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bears.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(bears.getId()));
        harness.passBothPriorities();
        harness.setHand(player1, List.of(new DoomBlade()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castAndResolveInstant(player1, 0, bears.getId());
        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Each separately granted instance of persist triggers")
    void separatePersistGrantsTriggerSeparately() {
        addCauldronReady();
        harness.addToBattlefield(player1, new CauldronOfSouls());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(bears.getId()));
        harness.passBothPriorities();
        harness.activateAbilityWithMultiTargets(player1, 1, 0, List.of(bears.getId()));
        harness.passBothPriorities();
        harness.setHand(player1, List.of(new DoomBlade()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castAndResolveInstant(player1, 0, bears.getId());
        assertThat(gd.stack).hasSize(2);
        resolveUntilInputOrEmpty();
        assertThat(findPermanents(player1, "Grizzly Bears"))
                .singleElement().satisfies(returned -> {
                    assertThat(returned.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
                    assertThat(gqs.hasKeyword(gd, returned, Keyword.PERSIST)).isFalse();
                });
    }

    @Test
    @DisplayName("The dying creature's controller controls persist, and its owner receives it")
    void stolenCreaturePersistIsControlledByItsController() {
        addCauldronReady();
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Confiscate()));
        harness.addMana(player1, ManaColor.BLUE, 6);
        harness.castEnchantment(player1, 0, bears.getId());
        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, harness::passBothPriorities);
        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(bears.getId()));
        harness.passBothPriorities();
        harness.setHand(player1, List.of(new DoomBlade()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castAndResolveInstant(player1, 0, bears.getId());
        assertThat(gd.stack).singleElement().satisfies(trigger ->
                assertThat(trigger.getControllerId()).isEqualTo(player1.getId()));
        resolveUntilInputOrEmpty();
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("An opponent's creature can receive persist")
    void canGrantPersistToOpponentsCreature() {
        addCauldronReady();
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(bears.getId()));
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.PERSIST)).isTrue();
        harness.setHand(player1, List.of(new DoomBlade()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castInstant(player1, 0, bears.getId());
        resolveUntilInputOrEmpty();
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player2.getId())).singleElement()
                .satisfies(returned -> assertThat(returned.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1));
    }

    @Test
    @DisplayName("Removing one target leaves the other target able to gain persist")
    void resolvesForRemainingLegalTarget() {
        addCauldronReady();
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(first.getId(), second.getId()));
        harness.setHand(player1, List.of(new DoomBlade()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castAndResolveInstant(player1, 0, first.getId());
        resolveUntilInputOrEmpty();
        assertThat(findPermanents(player1, "Grizzly Bears")).singleElement().isSameAs(second);
        assertThat(gqs.hasKeyword(gd, second, Keyword.PERSIST)).isTrue();
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }
}
