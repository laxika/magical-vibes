package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GoldenEgg;
import com.github.laxika.magicalvibes.cards.m.MaraleafPixie;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OkoThiefOfCrowns.class, GoldenEgg.class, MaraleafPixie.class})
class OkoThiefOfCrownsTest extends BaseCardTest {

    @Test
    void createsFoodToken() {
        Permanent oko = addReadyOko(player1, 3);

        harness.activateAbility(player1, battlefieldIndex(player1, oko), 0, null, null);
        harness.passBothPriorities();

        Permanent food = findPermanent(player1, "Food");
        assertThat(gqs.isArtifact(gd, food)).isTrue();
        assertThat(food.getCard().getSubtypes()).contains(CardSubtype.FOOD);
        assertThat(oko.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
    }

    @Test
    void transformsArtifactIntoGreenElk() {
        Permanent oko = addReadyOko(player1, 3);
        Permanent egg = harness.addToBattlefieldAndReturn(player2, new GoldenEgg());

        harness.activateAbility(player1, battlefieldIndex(player1, oko), 1, null, egg.getId());
        harness.passBothPriorities();

        assertThat(gqs.isArtifact(gd, egg)).isFalse();
        assertThat(gqs.isCreature(gd, egg)).isTrue();
        assertThat(gqs.hasColor(gd, egg, CardColor.GREEN)).isTrue();
        assertThat(gqs.effectiveCreatureSubtypes(gd, egg)).containsExactly(CardSubtype.ELK);
        assertThat(gqs.getEffectivePower(gd, egg)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, egg)).isEqualTo(3);
        assertThat(gqs.hasLostAllAbilities(gd, egg)).isTrue();
        assertThat(oko.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
    }

    @Test
    void exchangesControlledArtifactForSmallOpponentCreature() {
        Permanent oko = addReadyOko(player1, 5);
        Permanent egg = harness.addToBattlefieldAndReturn(player1, new GoldenEgg());
        Permanent pixie = harness.addToBattlefieldAndReturn(player2, new MaraleafPixie());

        harness.activateAbilityWithMultiTargets(player1, battlefieldIndex(player1, oko), 2,
                List.of(egg.getId(), pixie.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(pixie);
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(egg);
        assertThat(oko.getCounterCount(CounterType.LOYALTY)).isEqualTo(0);
    }

    @Test
    void foodCanBeSacrificedForThreeLife() {
        Permanent oko = addReadyOko(player1, 4);
        harness.activateAbility(player1, battlefieldIndex(player1, oko), 0, null, null);
        harness.passBothPriorities();
        Permanent food = findPermanent(player1, "Food");
        harness.setLife(player1, 10);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, battlefieldIndex(player1, food), 0, null, null);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(food);
        harness.passBothPriorities();

        harness.assertLife(player1, 13);
    }

    @Test
    void elkTransformationRemovesFlyingAndBlueButPreservesCounters() {
        Permanent oko = addReadyOko(player1, 4);
        Permanent pixie = harness.addToBattlefieldAndReturn(player2, new MaraleafPixie());
        pixie.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        harness.activateAbility(player1, battlefieldIndex(player1, oko), 1, null, pixie.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, pixie, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasColor(gd, pixie, CardColor.BLUE)).isFalse();
        assertThat(gqs.hasColor(gd, pixie, CardColor.GREEN)).isTrue();
        assertThat(gqs.effectiveCreatureSubtypes(gd, pixie)).containsExactly(CardSubtype.ELK);
        assertThat(gqs.getEffectivePower(gd, pixie)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, pixie)).isEqualTo(5);
    }

    @Test
    void exchangeDoesNothingWhenOpponentCreatureGrowsAboveThreeBeforeResolution() {
        Permanent oko = addReadyOko(player1, 6);
        Permanent egg = harness.addToBattlefieldAndReturn(player1, new GoldenEgg());
        Permanent pixie = harness.addToBattlefieldAndReturn(player2, new MaraleafPixie());

        harness.activateAbilityWithMultiTargets(player1, battlefieldIndex(player1, oko), 2,
                List.of(egg.getId(), pixie.getId()));
        pixie.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(egg).doesNotContain(pixie);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(pixie).doesNotContain(egg);
        assertThat(oko.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
    }

    @Test
    void exchangeDoesNothingWhenControlledArtifactIsSacrificedInResponse() {
        Permanent oko = addReadyOko(player1, 6);
        Permanent egg = harness.addToBattlefieldAndReturn(player1, new GoldenEgg());
        Permanent pixie = harness.addToBattlefieldAndReturn(player2, new MaraleafPixie());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbilityWithMultiTargets(player1, battlefieldIndex(player1, oko), 2,
                List.of(egg.getId(), pixie.getId()));
        harness.activateAbility(player1, battlefieldIndex(player1, egg), 1, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(egg, pixie);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(pixie).doesNotContain(egg);
    }

    @Test
    void exchangedCreatureStaysControlledAfterItsPowerIncreases() {
        Permanent oko = addReadyOko(player1, 6);
        Permanent egg = harness.addToBattlefieldAndReturn(player1, new GoldenEgg());
        Permanent pixie = harness.addToBattlefieldAndReturn(player2, new MaraleafPixie());

        harness.activateAbilityWithMultiTargets(player1, battlefieldIndex(player1, oko), 2,
                List.of(egg.getId(), pixie.getId()));
        harness.passBothPriorities();
        pixie.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.runStateBasedActions();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(pixie).doesNotContain(egg);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(egg).doesNotContain(pixie);
    }

    @Test
    void elkTransformationPersistsAfterOkoLeavesBattlefield() {
        Permanent oko = addReadyOko(player1, 4);
        Permanent egg = harness.addToBattlefieldAndReturn(player2, new GoldenEgg());

        harness.activateAbility(player1, battlefieldIndex(player1, oko), 1, null, egg.getId());
        harness.passBothPriorities();
        oko.setCounterCount(CounterType.LOYALTY, 0);
        harness.runStateBasedActions();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(oko);
        assertThat(gqs.isCreature(gd, egg)).isTrue();
        assertThat(gqs.isArtifact(gd, egg)).isFalse();
        assertThat(gqs.hasColor(gd, egg, CardColor.GREEN)).isTrue();
        assertThat(gqs.effectiveCreatureSubtypes(gd, egg)).containsExactly(CardSubtype.ELK);
        assertThat(gqs.hasLostAllAbilities(gd, egg)).isTrue();
        assertThat(gqs.getEffectivePower(gd, egg)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, egg)).isEqualTo(3);
    }
    private Permanent addReadyOko(Player player, int loyalty) {
        Permanent oko = harness.addToBattlefieldAndReturn(player, new OkoThiefOfCrowns());
        oko.setCounterCount(CounterType.LOYALTY, loyalty);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return oko;
    }

    private int battlefieldIndex(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
