package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.s.SquirrelSanctuary;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Chitterspitter.class, SquirrelSanctuary.class})
class ChitterspitterTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a Squirrel token with its activated ability")
    void createsSquirrelToken() {
        Permanent spitter = harness.addToBattlefieldAndReturn(player1, new Chitterspitter());

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Squirrel")).hasSize(1);
        assertThat(spitter.isTapped()).isTrue();
    }

    @Test
    @DisplayName("May sacrifice a token at upkeep to add an acorn counter")
    void sacrificesTokenForAcornCounter() {
        Permanent spitter = harness.addToBattlefieldAndReturn(player1, new Chitterspitter());
        createSquirrelToken(player1);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(findPermanents(player1, "Squirrel")).isEmpty();
        assertThat(spitter.getCounterCount(CounterType.ACORN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Choosing among multiple tokens still adds an acorn counter")
    void choosesTokenForAcornCounter() {
        Permanent spitter = harness.addToBattlefieldAndReturn(player1, new Chitterspitter());
        createSquirrelToken(player1);
        createSquirrelToken(player1);

        Permanent tokenToSacrifice = findPermanents(player1, "Squirrel").getFirst();
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, tokenToSacrifice.getId());

        assertThat(findPermanents(player1, "Squirrel")).hasSize(1);
        assertThat(spitter.getCounterCount(CounterType.ACORN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Acorn counters boost only Squirrels the artifact's controller controls")
    void boostsControlledSquirrels() {
        Permanent spitter = harness.addToBattlefieldAndReturn(player1, new Chitterspitter());
        spitter.setCounterCount(CounterType.ACORN, 2);
        createSquirrelToken(player1);
        createSquirrelToken(player2);

        Permanent ownSquirrel = findPermanents(player1, "Squirrel").getFirst();
        Permanent opposingSquirrel = findPermanents(player2, "Squirrel").getFirst();

        assertThat(gqs.getEffectivePower(gd, ownSquirrel)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ownSquirrel)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, opposingSquirrel)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, opposingSquirrel)).isEqualTo(1);
    }

    @Test
    @DisplayName("May decline sacrificing a token at upkeep")
    void mayDeclineSacrifice() {
        Permanent spitter = harness.addToBattlefieldAndReturn(player1, new Chitterspitter());
        createSquirrelToken(player1);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(findPermanents(player1, "Squirrel")).hasSize(1);
        assertThat(spitter.getCounterCount(CounterType.ACORN)).isZero();
    }

    @Test
    @DisplayName("No acorn counter is added without a token controlled by the upkeep player")
    void cannotSacrificeOpposingToken() {
        Permanent spitter = harness.addToBattlefieldAndReturn(player1, new Chitterspitter());
        createSquirrelToken(player2);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(spitter.getCounterCount(CounterType.ACORN)).isZero();
        assertThat(findPermanents(player2, "Squirrel")).hasSize(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Chitterspitter does not trigger during an opponent's upkeep")
    void doesNotTriggerOnOpponentsUpkeep() {
        Permanent spitter = harness.addToBattlefieldAndReturn(player1, new Chitterspitter());
        createSquirrelToken(player1);

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(spitter.getCounterCount(CounterType.ACORN)).isZero();
        assertThat(findPermanents(player1, "Squirrel")).hasSize(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Sacrificing one token immediately boosts the remaining Squirrel")
    void sacrificeUpdatesBoost() {
        Permanent spitter = harness.addToBattlefieldAndReturn(player1, new Chitterspitter());
        createSquirrelToken(player1);
        createSquirrelToken(player1);
        Permanent sacrificed = findPermanents(player1, "Squirrel").getFirst();
        Permanent survivor = findPermanents(player1, "Squirrel").getLast();

        assertThat(gqs.getEffectivePower(gd, survivor)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, survivor)).isEqualTo(1);
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, sacrificed.getId());

        assertThat(findPermanents(player1, "Squirrel")).containsExactly(survivor);
        assertThat(spitter.getCounterCount(CounterType.ACORN)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, survivor)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, survivor)).isEqualTo(2);
    }

    private void createSquirrelToken(Player player) {
        harness.forceActivePlayer(player);
        harness.castFromHand(player, new SquirrelSanctuary(), "{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
