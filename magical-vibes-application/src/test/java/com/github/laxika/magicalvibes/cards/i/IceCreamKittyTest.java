package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.p.PartyDude;
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

@CardUsed({IceCreamKitty.class, PartyDude.class})
class IceCreamKittyTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing another creature draws a card")
    void sacrificesAnotherCreatureToDraw() {
        Permanent kitty = harness.addToBattlefieldAndReturn(player1, new IceCreamKitty());
        Permanent otherKitty = harness.addToBattlefieldAndReturn(player1, new IceCreamKitty());
        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, battlefieldIndex(kitty), 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(kitty).doesNotContain(otherKitty);
        harness.assertInGraveyard(player1, "Ice Cream Kitty");
    }

    @Test
    @DisplayName("Sacrificing another token draws a card")
    void sacrificesAnotherTokenToDraw() {
        Permanent kitty = harness.addToBattlefieldAndReturn(player1, new IceCreamKitty());
        harness.addToBattlefield(player1, new IceCreamKitty());
        harness.setHand(player1, List.of(new PartyDude()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castEnchantment(player1, 0);
        resolveAllTriggers();

        Permanent food = findPermanent(player1, "Food");
        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, battlefieldIndex(kitty), 0, null, null);
        harness.handlePermanentChosen(player1, food.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(kitty);
        assertThat(findPermanents(player1, "Food")).isEmpty();
    }

    @Test
    @DisplayName("Tapping and sacrificing itself gains 3 life")
    void sacrificesItselfToGainLife() {
        Permanent kitty = addCreatureReady(player1, new IceCreamKitty());
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, battlefieldIndex(kitty), 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(23);
        harness.assertNotOnBattlefield(player1, "Ice Cream Kitty");
        harness.assertInGraveyard(player1, "Ice Cream Kitty");
    }

    @Test
    @DisplayName("The draw ability can only be activated at sorcery speed")
    void drawAbilityIsSorcerySpeedOnly() {
        Permanent kitty = harness.addToBattlefieldAndReturn(player1, new IceCreamKitty());
        harness.addToBattlefield(player1, new IceCreamKitty());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(kitty), 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The draw ability cannot sacrifice its own source")
    void cannotSacrificeItselfToDraw() {
        Permanent kitty = harness.addToBattlefieldAndReturn(player1, new IceCreamKitty());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(kitty), 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(kitty);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An opponent's creature cannot pay the draw ability's sacrifice cost")
    void cannotSacrificeOpponentsCreature() {
        Permanent kitty = harness.addToBattlefieldAndReturn(player1, new IceCreamKitty());
        Permanent opponentKitty = harness.addToBattlefieldAndReturn(player2, new IceCreamKitty());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(kitty), 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opponentKitty);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A tapped, summoning-sick Kitty may activate its draw ability")
    void drawAbilityDoesNotRequireTapping() {
        Permanent kitty = harness.addToBattlefieldAndReturn(player1, new IceCreamKitty());
        Permanent otherKitty = harness.addToBattlefieldAndReturn(player1, new IceCreamKitty());
        kitty.setSummoningSick(true);
        kitty.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, battlefieldIndex(kitty), 0, null, null);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(kitty).doesNotContain(otherKitty);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    @DisplayName("The draw ability cannot be activated during the opponent's main phase")
    void drawAbilityCannotActivateOnOpponentsTurn() {
        Permanent kitty = harness.addToBattlefieldAndReturn(player1, new IceCreamKitty());
        harness.addToBattlefield(player1, new IceCreamKitty());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(kitty), 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The draw ability cannot be activated while an ability is on the stack")
    void drawAbilityRequiresEmptyStack() {
        Permanent kitty = harness.addToBattlefieldAndReturn(player1, new IceCreamKitty());
        harness.addToBattlefield(player1, new IceCreamKitty());
        Permanent foodKitty = addCreatureReady(player1, new IceCreamKitty());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateAbility(player1, battlefieldIndex(foodKitty), 1, null, null);
        assertThat(gd.stack).hasSize(1);

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(kitty), 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.passBothPriorities();
    }

    @Test
    @DisplayName("A summoning-sick Kitty cannot activate its life-gain ability")
    void lifeAbilityRequiresNoSummoningSickness() {
        Permanent kitty = harness.addToBattlefieldAndReturn(player1, new IceCreamKitty());
        kitty.setSummoningSick(true);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(kitty), 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(kitty);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A tapped Kitty cannot activate its life-gain ability")
    void lifeAbilityRequiresUntappedSource() {
        Permanent kitty = addCreatureReady(player1, new IceCreamKitty());
        kitty.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(kitty), 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(kitty);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The life-gain ability can be activated during combat")
    void lifeAbilityCanActivateOutsideMainPhase() {
        Permanent kitty = addCreatureReady(player1, new IceCreamKitty());
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, battlefieldIndex(kitty), 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(23);
        harness.assertInGraveyard(player1, "Ice Cream Kitty");
    }

    @Test
    @DisplayName("A nontoken noncreature cannot pay the draw ability's sacrifice cost")
    void cannotSacrificeNontokenNoncreature() {
        Permanent kitty = harness.addToBattlefieldAndReturn(player1, new IceCreamKitty());
        Permanent partyDude = harness.addToBattlefieldAndReturn(player1, new PartyDude());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(kitty), 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(kitty, partyDude);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Self-sacrifice is paid before life gain resolves")
    void lifeAbilitySacrificesSourceAsCost() {
        Permanent kitty = addCreatureReady(player1, new IceCreamKitty());
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, battlefieldIndex(kitty), 1, null, null);

        harness.assertNotOnBattlefield(player1, "Ice Cream Kitty");
        harness.assertInGraveyard(player1, "Ice Cream Kitty");
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.getLife(player1.getId())).isEqualTo(23);
    }

    @Test
    @DisplayName("Both abilities require two mana before a sacrifice can be paid")
    void abilitiesRequireTwoMana() {
        Permanent kitty = addCreatureReady(player1, new IceCreamKitty());
        Permanent otherKitty = harness.addToBattlefieldAndReturn(player1, new IceCreamKitty());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(kitty), 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(kitty), 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(kitty, otherKitty);
        assertThat(kitty.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
