package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.m.MistralCharger;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RakdosRiteknife.class, MistralCharger.class})
class RakdosRiteknifeTest extends BaseCardTest {

    @Test
    void equippedCreatureGetsPowerForEachBloodCounter() {
        Permanent creature = addCreatureReady(player1, new MistralCharger());
        Permanent riteknife = harness.addToBattlefieldAndReturn(player1, new RakdosRiteknife());
        riteknife.setAttachedTo(creature.getId());
        riteknife.setCounterCount(CounterType.BLOOD, 2);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(1);
    }

    @Test
    void equippedCreatureTapsAndSacrificesAnotherCreatureToAddBloodCounter() {
        Permanent riteknife = harness.addToBattlefieldAndReturn(player1, new RakdosRiteknife());
        Permanent creature = addCreatureReady(player1, new MistralCharger());
        Permanent sacrifice = addCreatureReady(player1, new MistralCharger());
        riteknife.setAttachedTo(creature.getId());

        harness.activateAbility(player1, 1, null, null);
        harness.handlePermanentChosen(player1, sacrifice.getId());
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isTrue();
        assertThat(riteknife.isTapped()).isFalse();
        assertThat(riteknife.getCounterCount(CounterType.BLOOD)).isEqualTo(1);
        assertThat(creature.getCounterCount(CounterType.BLOOD)).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature).doesNotContain(sacrifice);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(1);
    }

    @Test
    void equippedCreatureCanSacrificeItselfToPutCounterOnEquipment() {
        Permanent riteknife = harness.addToBattlefieldAndReturn(player1, new RakdosRiteknife());
        Permanent creature = addCreatureReady(player1, new MistralCharger());
        riteknife.setAttachedTo(creature.getId());

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(riteknife);
        assertThat(riteknife.getCounterCount(CounterType.BLOOD)).isEqualTo(1);
        assertThat(riteknife.isTapped()).isFalse();
        harness.assertInGraveyard(player1, "Mistral Charger");
    }

    @Test
    void unequippedEquipmentCannotTapToAddBloodCounter() {
        Permanent riteknife = harness.addToBattlefieldAndReturn(player1, new RakdosRiteknife());
        Permanent creature = addCreatureReady(player1, new MistralCharger());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(riteknife.isTapped()).isFalse();
        assertThat(riteknife.getCounterCount(CounterType.BLOOD)).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(riteknife, creature);
    }

    @Test
    void summoningSickEquippedCreatureCannotActivateGrantedTapAbility() {
        Permanent riteknife = harness.addToBattlefieldAndReturn(player1, new RakdosRiteknife());
        Permanent creature = addCreatureReady(player1, new MistralCharger());
        creature.setSummoningSick(true);
        riteknife.setAttachedTo(creature.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(creature.isTapped()).isFalse();
        assertThat(riteknife.getCounterCount(CounterType.BLOOD)).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(riteknife, creature);
    }

    @Test
    void tappedEquipmentDoesNotPreventCreatureActivatingGrantedAbility() {
        Permanent riteknife = harness.addToBattlefieldAndReturn(player1, new RakdosRiteknife());
        Permanent creature = addCreatureReady(player1, new MistralCharger());
        riteknife.setAttachedTo(creature.getId());
        riteknife.tap();

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(riteknife);
        assertThat(riteknife.getCounterCount(CounterType.BLOOD)).isEqualTo(1);
        assertThat(riteknife.isTapped()).isTrue();
    }

    @Test
    void creaturesControllerCanActivateAbilityOfEquipmentOwnedByOpponent() {
        Permanent riteknife = harness.addToBattlefieldAndReturn(player1, new RakdosRiteknife());
        Permanent creature = addCreatureReady(player2, new MistralCharger());
        riteknife.setAttachedTo(creature.getId());

        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(riteknife);
        assertThat(riteknife.getCounterCount(CounterType.BLOOD)).isEqualTo(1);
        assertThat(riteknife.isTapped()).isFalse();
        harness.assertInGraveyard(player2, "Mistral Charger");
    }

    @Test
    void sacrificingRiteknifeMakesTargetPlayerSacrificeForEachBloodCounter() {
        Permanent riteknife = harness.addToBattlefieldAndReturn(player1, new RakdosRiteknife());
        riteknife.setCounterCount(CounterType.BLOOD, 2);
        addCreatureReady(player2, new MistralCharger());
        addCreatureReady(player2, new MistralCharger());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 1, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(riteknife);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void targetPlayerSacrificesAllPermanentsWhenCountersExceedTheirPermanentCount() {
        Permanent riteknife = harness.addToBattlefieldAndReturn(player1, new RakdosRiteknife());
        riteknife.setCounterCount(CounterType.BLOOD, 3);
        addCreatureReady(player2, new MistralCharger());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 1, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Rakdos Riteknife");
        harness.assertInGraveyard(player2, "Mistral Charger");
    }

    @Test
    void controllerCanTargetThemselvesWithSacrificeAbility() {
        Permanent riteknife = harness.addToBattlefieldAndReturn(player1, new RakdosRiteknife());
        riteknife.setCounterCount(CounterType.BLOOD, 1);
        addCreatureReady(player1, new MistralCharger());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 1, null, player1.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        harness.assertInGraveyard(player1, "Rakdos Riteknife");

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Mistral Charger");
    }

    @Test
    void targetPlayerChoosesWhichPermanentsToSacrifice() {
        Permanent riteknife = harness.addToBattlefieldAndReturn(player1, new RakdosRiteknife());
        riteknife.setCounterCount(CounterType.BLOOD, 1);
        Permanent first = addCreatureReady(player2, new MistralCharger());
        Permanent second = addCreatureReady(player2, new MistralCharger());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 1, null, player2.getId());
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.maxCount()).isEqualTo(1);

        harness.handleMultiplePermanentsChosen(player2,
                List.of(first.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(second);
    }

    @Test
    void targetPlayerMaySacrificeANoncreaturePermanent() {
        Permanent riteknife = harness.addToBattlefieldAndReturn(player1, new RakdosRiteknife());
        riteknife.setCounterCount(CounterType.BLOOD, 1);
        Permanent creature = addCreatureReady(player2, new MistralCharger());
        Permanent equipment = harness.addToBattlefieldAndReturn(player2, new RakdosRiteknife());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 1, null, player2.getId());
        harness.passBothPriorities();

        harness.handleMultiplePermanentsChosen(player2, List.of(equipment.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(creature);
    }

    @Test
    void sacrificingWithNoBloodCountersDoesNotMakeTargetPlayerSacrifice() {
        Permanent riteknife = harness.addToBattlefieldAndReturn(player1, new RakdosRiteknife());
        Permanent creature = addCreatureReady(player2, new MistralCharger());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 1, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(riteknife);
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(creature);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void equipAbilityAttachesRiteknifeToTargetCreature() {
        Permanent riteknife = harness.addToBattlefieldAndReturn(player1, new RakdosRiteknife());
        Permanent creature = addCreatureReady(player1, new MistralCharger());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 2, null, creature.getId());
        harness.passBothPriorities();

        assertThat(riteknife.getAttachedTo()).isEqualTo(creature.getId());
    }
}
