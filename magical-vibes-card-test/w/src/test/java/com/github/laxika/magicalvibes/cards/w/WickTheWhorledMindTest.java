package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.c.Conspiracy;
import com.github.laxika.magicalvibes.cards.m.MuckRats;
import com.github.laxika.magicalvibes.cards.p.PersistentMarshstalker;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WickTheWhorledMind.class, MuckRats.class, PersistentMarshstalker.class, Conspiracy.class})
class WickTheWhorledMindTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a black Snail token when Wick enters and no Snail is controlled")
    void createsSnailWhenNoneIsControlled() {
        castWick();

        Permanent snail = findSnail();
        assertThat(snail.getEffectivePower()).isEqualTo(1);
        assertThat(snail.getEffectiveToughness()).isEqualTo(1);
        assertThat(snail.getCard().getColor()).isEqualTo(CardColor.BLACK);
    }

    @Test
    @DisplayName("Puts a +1/+1 counter on a controlled Snail when another Rat enters")
    void addsCounterToControlledSnail() {
        castWick();
        Permanent snail = findSnail();

        harness.castFromHand(player1, new MuckRats(), "{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(snail.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Sacrificing a Snail damages each opponent and draws cards equal to its power")
    void sacrificesSnailForDamageAndCards() {
        castWick();
        int handBefore = gd.playerHands.get(player1.getId()).size();
        int opponentLifeBefore = gd.getLife(player2.getId());

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(opponentLifeBefore - 1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.SNAIL))
                .isEmpty();
    }

    private void castWick() {
        harness.castFromHand(player1, new WickTheWhorledMind(), "{3}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    @Test
    void stackedRatTriggersCreateOneSnailThenAddACounter() {
        harness.enterBattlefieldAndReturn(player1, new WickTheWhorledMind());
        harness.enterBattlefieldAndReturn(player1, new PersistentMarshstalker());

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.SNAIL))
                .hasSize(1);
        assertThat(findSnail().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void createsReplacementSnailIfExistingSnailIsSacrificedBeforeRatTriggerResolves() {
        castWick();
        Permanent originalSnail = findSnail();
        harness.enterBattlefieldAndReturn(player1, new PersistentMarshstalker());
        addActivationMana();
        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(originalSnail);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findSnail().getId()).isNotEqualTo(originalSnail.getId());
        assertThat(findSnail().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertLife(player2, 19);
    }

    @Test
    void usesCounterBoostedSnailPowerForDamageAndDrawing() {
        castWick();
        harness.enterBattlefieldAndReturn(player1, new PersistentMarshstalker());
        harness.passBothPriorities();
        int handBefore = gd.playerHands.get(player1.getId()).size();

        addActivationMana();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        harness.assertLife(player1, 20);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 2);
    }

    @Test
    void opposingRatEntryDoesNotTriggerWick() {
        castWick();
        Permanent snail = findSnail();

        harness.enterBattlefieldAndReturn(player2, new PersistentMarshstalker());

        assertThat(gd.stack).isEmpty();
        assertThat(snail.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void cannotActivateWithoutASnail() {
        harness.addToBattlefield(player1, new WickTheWhorledMind());
        harness.addToBattlefield(player1, new PersistentMarshstalker());
        addActivationMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Persistent Marshstalker");
    }

    @Test
    void selfEntryStillTriggersWhenConspiracyReplacesRatType() {
        harness.castFromHand(player1, new Conspiracy(), "{3}{B}{B}");
        harness.passBothPriorities();
        harness.handleListChoice(player1, CardSubtype.GOBLIN.name());

        harness.castFromHand(player1, new WickTheWhorledMind(), "{3}{B}");
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Snail");
    }

    @Test
    void maySacrificeWickItselfWhenItIsASnail() {
        Permanent conspiracy = harness.addToBattlefieldAndReturn(player1, new Conspiracy());
        conspiracy.setChosenSubtype(CardSubtype.SNAIL);
        harness.addToBattlefield(player1, new WickTheWhorledMind());
        int handBefore = gd.playerHands.get(player1.getId()).size();

        addActivationMana();
        harness.activateAbility(player1, 1, null, null);
        harness.assertNotOnBattlefield(player1, "Wick, the Whorled Mind");
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 2);
    }

    @Test
    void nonRatCreatureEntryDoesNotTriggerWick() {
        castWick();
        Permanent conspiracy = harness.addToBattlefieldAndReturn(player1, new Conspiracy());
        conspiracy.setChosenSubtype(CardSubtype.GOBLIN);

        harness.enterBattlefieldAndReturn(player1, new PersistentMarshstalker());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().getName().equals("Snail"))
                .hasSize(1);
    }

    private void addActivationMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
    }

    private Permanent findSnail() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.SNAIL))
                .findFirst()
                .orElseThrow();
    }
}
