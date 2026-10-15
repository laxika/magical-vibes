package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.a.AncestralKatana;
import com.github.laxika.magicalvibes.cards.b.BearerOfMemory;
import com.github.laxika.magicalvibes.cards.s.ShortCircuit;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TowashiGuideBot.class, BearerOfMemory.class, Forest.class, AncestralKatana.class, ShortCircuit.class})
class TowashiGuideBotTest extends BaseCardTest {

    @Test
    @DisplayName("ETB puts a +1/+1 counter on a creature you control")
    void etbPutsCounterOnControlledCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new BearerOfMemory());

        harness.setHand(player1, List.of(new TowashiGuideBot()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castCreature(player1, 0, 0, target.getId());
        resolveAllTriggers();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("ETB cannot target an opponent's creature")
    void etbCannotTargetOpponentCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BearerOfMemory());

        harness.setHand(player1, List.of(new TowashiGuideBot()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");
    }

    @Test
    @DisplayName("Draw ability costs less for each modified creature you control")
    void drawAbilityCostsLessForModifiedCreatures() {
        Permanent guideBot = addCreatureReady(player1, new TowashiGuideBot());
        Permanent modifiedCreature = addCreatureReady(player1, new BearerOfMemory());
        modifiedCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Forest drawnCard = new Forest();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        assertThat(guideBot.isTapped()).isTrue();
    }

    @Test
    void entersWithCounterOnItselfWhenItIsTheOnlyCreature() {
        harness.setHand(player1, List.of(new TowashiGuideBot()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        Permanent guideBot = findPermanent(player1, "Towashi Guide-Bot");
        harness.handlePermanentChosen(player1, guideBot.getId());
        resolveAllTriggers();

        assertThat(guideBot.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void enteringWithoutBeingCastStillPutsACounterOnAControlledCreature() {
        Permanent target = addCreatureReady(player1, new BearerOfMemory());

        harness.enterBattlefieldAndReturn(player1, new TowashiGuideBot());
        resolveAllTriggers();
        harness.handlePermanentChosen(player1, target.getId());
        resolveAllTriggers();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void unmodifiedCreaturesRequireFullActivationCost() {
        Permanent guideBot = addCreatureReady(player1, new TowashiGuideBot());
        addCreatureReady(player1, new BearerOfMemory());
        Forest drawnCard = new Forest();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(guideBot.isTapped()).isFalse();

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        assertThat(guideBot.isTapped()).isTrue();
    }

    @ParameterizedTest
    @ValueSource(ints = {4, 5})
    void fourOrMoreModifiedCreaturesAllowActivationWithoutMana(int creatureCount) {
        Permanent guideBot = addCreatureReady(player1, new TowashiGuideBot());
        guideBot.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        for (int i = 1; i < creatureCount; i++) {
            Permanent creature = addCreatureReady(player1, new BearerOfMemory());
            creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
            creature.tap();
        }
        Forest drawnCard = new Forest();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawnCard));

        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        assertThat(guideBot.isTapped()).isTrue();
    }

    @Test
    void multipleModificationsOnOneCreatureReduceCostOnlyOnce() {
        Permanent guideBot = addCreatureReady(player1, new TowashiGuideBot());
        guideBot.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new AncestralKatana());
        equipment.setAttachedTo(guideBot.getId());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new ShortCircuit());
        aura.setAttachedTo(guideBot.getId());
        Forest drawnCard = new Forest();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(guideBot.isTapped()).isFalse();

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void equipmentReducesCostRegardlessOfItsController(boolean ownEquipment) {
        Permanent guideBot = addCreatureReady(player1, new TowashiGuideBot());
        Permanent equipment = harness.addToBattlefieldAndReturn(
                ownEquipment ? player1 : player2, new AncestralKatana());
        equipment.setAttachedTo(guideBot.getId());
        Forest drawnCard = new Forest();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        assertThat(guideBot.isTapped()).isTrue();
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void onlyAnAuraControlledByTheCreatureControllerReducesCost(boolean ownAura) {
        Permanent guideBot = addCreatureReady(player1, new TowashiGuideBot());
        Permanent aura = harness.addToBattlefieldAndReturn(
                ownAura ? player1 : player2, new ShortCircuit());
        aura.setAttachedTo(guideBot.getId());
        Forest drawnCard = new Forest();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        if (!ownAura) {
            assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                    .isInstanceOf(IllegalStateException.class);
            assertThat(guideBot.isTapped()).isFalse();
            harness.addMana(player1, ManaColor.COLORLESS, 1);
        }
        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
    }

    @Test
    void opponentsModifiedCreaturesAndModifiedNoncreaturesDoNotReduceCost() {
        Permanent guideBot = addCreatureReady(player1, new TowashiGuideBot());
        Permanent opposingCreature = addCreatureReady(player2, new BearerOfMemory());
        opposingCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        land.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(guideBot.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }
}
