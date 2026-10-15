package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.z.RoostOfDrakes;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BushyBodyguard.class, BakersbaneDuo.class, RoostOfDrakes.class})
class BushyBodyguardTest extends BaseCardTest {

    @Test
    void offspringCreatesOneOneTokenCopyWhenPaid() {
        harness.setHand(player1, List.of(new BushyBodyguard()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castKickedCreature(player1, 0);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()))
                .hasSize(1);
    }

    @Test
    void mayForageByExilingThreeGraveyardCardsAndPutCountersOnIt() {
        harness.setGraveyard(player1, List.of(new BushyBodyguard(), new BushyBodyguard(), new BushyBodyguard()));
        Permanent bodyguard = castBodyguard();

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.exiledCards).hasSize(3);
        assertThat(bodyguard.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void mayForageBySacrificingFoodAndPutCountersOnIt() {
        Permanent food = addFoodToken();
        Permanent bodyguard = castBodyguard();

        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, food.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(food);
        assertThat(bodyguard.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void choosesBetweenExilingCardsAndSacrificingFoodWhenBothAreAvailable() {
        harness.setGraveyard(player1, List.of(new BushyBodyguard(), new BushyBodyguard(), new BushyBodyguard()));
        Permanent food = addFoodToken();
        Permanent bodyguard = castBodyguard();

        harness.handleMayAbilityChosen(player1, true);
        harness.handleListChoice(player1, "Sacrifice a Food.");
        harness.handlePermanentChosen(player1, food.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
        assertThat(bodyguard.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void decliningForageDoesNotPutCountersOnIt() {
        Permanent bodyguard = castBodyguard();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(bodyguard.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @CardUsed({BushyBodyguard.class, RoostOfDrakes.class})
    void payingOffspringDoesNotTriggerKickedSpellAbilities() {
        harness.addToBattlefield(player1, new RoostOfDrakes());
        harness.setHand(player1, List.of(new BushyBodyguard()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castKickedCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void cannotForageWithOnlyTwoCardsAndNoFood() {
        harness.setGraveyard(player1, List.of(new BushyBodyguard(), new BushyBodyguard()));
        Permanent bodyguard = castBodyguard();

        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        assertThat(gd.exiledCards).isEmpty();
        assertThat(bodyguard.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
    }

    @Test
    void offspringTokenAndOriginalCanEachForageIndependently() {
        harness.setGraveyard(player1, List.of(new BushyBodyguard(), new BushyBodyguard(),
                new BushyBodyguard(), new BushyBodyguard(), new BushyBodyguard(), new BushyBodyguard()));
        harness.setHand(player1, List.of(new BushyBodyguard()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castKickedCreature(player1, 0);
        resolveAllTriggers();

        harness.handleMayAbilityChosen(player1, true);
        harness.handleGraveyardCardChosen(player1, 0);
        harness.handleGraveyardCardChosen(player1, 0);
        harness.handleGraveyardCardChosen(player1, 0);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.exiledCards).hasSize(6);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2)
                .allSatisfy(permanent -> assertThat(
                        permanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2));
        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()).findFirst().orElseThrow();
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(3);
    }

    private Permanent castBodyguard() {
        harness.setHand(player1, List.of(new BushyBodyguard()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getOriginalCard() instanceof BushyBodyguard)
                .findFirst()
                .orElseThrow();
    }

    private Permanent addFoodToken() {
        harness.enterBattlefieldAndReturn(player1, new BakersbaneDuo());
        resolveAllTriggers();
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getSubtypes().contains(CardSubtype.FOOD))
                .findFirst().orElseThrow();
    }
}
