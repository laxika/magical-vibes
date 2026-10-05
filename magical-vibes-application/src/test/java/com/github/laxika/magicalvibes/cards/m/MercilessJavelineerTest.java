package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.b.BitterbladeWarrior;
import com.github.laxika.magicalvibes.cards.e.EdificeOfAuthority;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MercilessJavelineer.class, BitterbladeWarrior.class, EdificeOfAuthority.class})
class MercilessJavelineerTest extends BaseCardTest {

    @Test
    @DisplayName("Ability puts a -1/-1 counter on target creature and makes it unable to block")
    void abilityShrinksAndPreventsBlock() {
        addCreatureReady(player1, new MercilessJavelineer());
        harness.setHand(player1, List.of(new BitterbladeWarrior()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        Permanent target = addCreatureReady(player2, new BitterbladeWarrior());

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.handleCardChosen(player1, 0); // pay the discard cost
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(target.getEffectivePower()).isEqualTo(1);
        assertThat(target.getEffectiveToughness()).isEqualTo(1);
        assertThat(target.isCantBlockThisTurn()).isTrue();
        // Discard cost paid: the card moved from hand to graveyard.
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Bitterblade Warrior");
    }

    @Test
    @DisplayName("Can't-block restriction wears off at end of turn")
    void cantBlockWearsOff() {
        addCreatureReady(player1, new MercilessJavelineer());
        harness.setHand(player1, List.of(new BitterbladeWarrior()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        Permanent target = addCreatureReady(player2, new BitterbladeWarrior());

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(target.isCantBlockThisTurn()).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.isCantBlockThisTurn()).isFalse();
        // The -1/-1 counter is permanent — it stays.
        assertThat(target.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Activating starts the discard-cost choice")
    void activatingStartsDiscardChoice() {
        addCreatureReady(player1, new MercilessJavelineer());
        harness.setHand(player1, List.of(new BitterbladeWarrior()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        Permanent target = addCreatureReady(player2, new BitterbladeWarrior());

        harness.activateAbility(player1, 0, 0, null, target.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardCostChoice.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot activate the ability with an empty hand")
    void cannotActivateWithEmptyHand() {
        addCreatureReady(player1, new MercilessJavelineer());
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        Permanent target = addCreatureReady(player2, new BitterbladeWarrior());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Ability cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        addCreatureReady(player1, new MercilessJavelineer());
        harness.setHand(player1, List.of(new BitterbladeWarrior()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        UUID artifactId = harness.addToBattlefieldAndReturn(player2, new EdificeOfAuthority()).getId();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, artifactId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Tapped and summoning-sick Javelineer can target itself")
    void tappedSummoningSickSourceCanTargetItself() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new MercilessJavelineer());
        source.setTapped(true);
        source.setSummoningSick(true);
        harness.setHand(player1, List.of(new EdificeOfAuthority()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, source.getId());
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(source.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(source.isCantBlockThisTurn()).isTrue();
        assertThat(source.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Edifice of Authority");
    }

    @Test
    @DisplayName("Ability can be activated repeatedly and lethal counters kill the target")
    void repeatedActivationsKillTarget() {
        Permanent source = addCreatureReady(player1, new MercilessJavelineer());
        Permanent target = addCreatureReady(player2, new BitterbladeWarrior());
        harness.setHand(player1, List.of(new EdificeOfAuthority(), new EdificeOfAuthority()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        assertThat(target.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Bitterblade Warrior");
        harness.assertInGraveyard(player2, "Bitterblade Warrior");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        assertThat(source.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Ability resolves independently after its source leaves the battlefield")
    void resolvesAfterSourceLeavesBattlefield() {
        Permanent source = addCreatureReady(player1, new MercilessJavelineer());
        Permanent target = addCreatureReady(player2, new BitterbladeWarrior());
        harness.setHand(player1, List.of(new EdificeOfAuthority()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.handleCardChosen(player1, 0);
        gd.playerBattlefields.get(player1.getId()).remove(source);
        gd.playerGraveyards.get(player1.getId()).add(source.getCard());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(target.isCantBlockThisTurn()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot activate with less than two mana")
    void cannotActivateWithInsufficientMana() {
        addCreatureReady(player1, new MercilessJavelineer());
        Permanent target = addCreatureReady(player2, new BitterbladeWarrior());
        harness.setHand(player1, List.of(new EdificeOfAuthority()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
        assertThat(target.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
    }

    @Test
    @DisplayName("An absent target makes the ability fail without refunding its discard cost")
    void absentTargetDoesNotRefundDiscard() {
        addCreatureReady(player1, new MercilessJavelineer());
        Permanent target = addCreatureReady(player2, new BitterbladeWarrior());
        harness.setHand(player1, List.of(new EdificeOfAuthority()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.handleCardChosen(player1, 0);
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerHands.get(player2.getId()).add(target.getCard());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        assertThat(target.isCantBlockThisTurn()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Edifice of Authority");
        assertThat(gd.stack).isEmpty();
    }
}
