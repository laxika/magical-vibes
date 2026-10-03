package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Endling.class, LightningBolt.class})
class EndlingTest extends BaseCardTest {

    @Test
    void gainsMenaceDeathtouchAndUndyingUntilEndOfTurn() {
        Permanent endling = harness.addToBattlefieldAndReturn(player1, new Endling());
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, endling, Keyword.MENACE)).isTrue();
        assertThat(gqs.hasKeyword(gd, endling, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.hasKeyword(gd, endling, Keyword.UNDYING)).isTrue();
    }

    @Test
    void undyingReturnsItWithACounterWhenItDies() {
        Permanent endling = harness.addToBattlefieldAndReturn(player1, new Endling());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, endling.getId());
        harness.passBothPriorities();

        Permanent returnedEndling = findPermanent(player1, "Endling");
        assertThat(returnedEndling.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertNotInGraveyard(player1, "Endling");
    }

    @Test
    void firstPowerToughnessModeAppliesUntilEndOfTurn() {
        Permanent endling = harness.addToBattlefieldAndReturn(player1, new Endling());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 3, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "This creature gets +1/-1 until end of turn");

        assertThat(endling.getPowerModifier()).isEqualTo(1);
        assertThat(endling.getToughnessModifier()).isEqualTo(-1);
    }

    @Test
    void secondPowerToughnessModeAppliesUntilEndOfTurn() {
        Permanent endling = harness.addToBattlefieldAndReturn(player1, new Endling());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 3, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "This creature gets -1/+1 until end of turn");

        assertThat(endling.getPowerModifier()).isEqualTo(-1);
        assertThat(endling.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    void grantedKeywordsExpireDuringCleanup() {
        Permanent endling = harness.addToBattlefieldAndReturn(player1, new Endling());
        harness.addMana(player1, ManaColor.BLACK, 3);
        for (int ability = 0; ability < 3; ability++) {
            harness.activateAbility(player1, 0, ability, null, null);
            harness.passBothPriorities();
        }

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.hasKeyword(gd, endling, Keyword.MENACE)).isFalse();
        assertThat(gqs.hasKeyword(gd, endling, Keyword.DEATHTOUCH)).isFalse();
        assertThat(gqs.hasKeyword(gd, endling, Keyword.UNDYING)).isFalse();
    }

    @Test
    void undyingDoesNotTriggerWithAPlusOneCounter() {
        Permanent endling = harness.addToBattlefieldAndReturn(player1, new Endling());
        endling.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        harness.setHand(player2, List.of(new LightningBolt(), new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castAndResolveInstant(player2, 0, endling.getId());
        harness.castAndResolveInstant(player2, 0, endling.getId());

        harness.assertNotOnBattlefield(player1, "Endling");
        harness.assertInGraveyard(player1, "Endling");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void repeatedUndyingGrantsTriggerSeparately() {
        harness.addToBattlefield(player1, new Endling());
        harness.addMana(player1, ManaColor.BLACK, 2);
        for (int activation = 0; activation < 2; activation++) {
            harness.activateAbility(player1, 0, 2, null, null);
            harness.passBothPriorities();
        }
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, harness.getPermanentId(player1, "Endling"));

        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(findPermanent(player1, "Endling").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
    }

    @Test
    void stolenEndlingsUndyingTriggerBelongsToItsControllerButReturnsToItsOwner() {
        Endling card = new Endling();
        card.setOwnerId(player2.getId());
        Permanent endling = harness.addToBattlefieldAndReturn(player1, card);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, endling.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getControllerId()).isEqualTo(player1.getId());
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Endling");
        assertThat(findPermanent(player2, "Endling").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(1);
    }

    @Test
    void powerToughnessChangeExpiresDuringCleanup() {
        Permanent endling = harness.addToBattlefieldAndReturn(player1, new Endling());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 3, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "This creature gets -1/+1 until end of turn");
        assertThat(endling.getPowerModifier()).isEqualTo(-1);
        assertThat(endling.getToughnessModifier()).isEqualTo(1);

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(endling.getPowerModifier()).isZero();
        assertThat(endling.getToughnessModifier()).isZero();
    }

    @Test
    void returningThroughUndyingLosesPreviouslyGrantedKeywords() {
        Permanent endling = harness.addToBattlefieldAndReturn(player1, new Endling());
        harness.addMana(player1, ManaColor.BLACK, 3);
        for (int ability = 0; ability < 3; ability++) {
            harness.activateAbility(player1, 0, ability, null, null);
            harness.passBothPriorities();
        }
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, endling.getId());
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Endling");
        assertThat(returned.getId()).isNotEqualTo(endling.getId());
        assertThat(gqs.hasKeyword(gd, returned, Keyword.MENACE)).isFalse();
        assertThat(gqs.hasKeyword(gd, returned, Keyword.DEATHTOUCH)).isFalse();
        assertThat(gqs.hasKeyword(gd, returned, Keyword.UNDYING)).isFalse();
    }
}
