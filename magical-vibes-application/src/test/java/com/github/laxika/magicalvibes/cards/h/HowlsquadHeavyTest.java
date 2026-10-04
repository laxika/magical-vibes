package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.FullThrottle;
import com.github.laxika.magicalvibes.cards.g.GreasewrenchGoblin;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HowlsquadHeavy.class, GreasewrenchGoblin.class, HowlersHeavy.class, FullThrottle.class})
class HowlsquadHeavyTest extends BaseCardTest {

    @Test
    void hasteExcludesItselfOpposingGoblinsAndOtherCreatureTypes() {
        Permanent heavy = harness.addToBattlefieldAndReturn(player1, new HowlsquadHeavy());
        Permanent opposingGoblin = harness.addToBattlefieldAndReturn(player2, new GreasewrenchGoblin());
        Permanent nongoblin = harness.addToBattlefieldAndReturn(player1, new HowlersHeavy());

        assertThat(gqs.hasKeyword(gd, heavy, Keyword.HASTE)).isFalse();
        assertThat(gqs.hasKeyword(gd, opposingGoblin, Keyword.HASTE)).isFalse();
        assertThat(gqs.hasKeyword(gd, nongoblin, Keyword.HASTE)).isFalse();
    }

    @Test
    void enteringStartsSpeedAtOne() {
        harness.enterBattlefieldAndReturn(player1, new HowlsquadHeavy());

        assertThat(gd.playerSpeeds.get(player1.getId())).isEqualTo(1);
    }

    @Test
    void doesNotCreateTokenOnOpponentsTurn() {
        harness.addToBattlefield(player1, new HowlsquadHeavy());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(player2, TurnStep.BEGINNING_OF_COMBAT);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    void tokenAttackRequirementDoesNotCreateAnAdditionalTriggeredAbility() {
        harness.addToBattlefield(player1, new HowlsquadHeavy());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.BEGINNING_OF_COMBAT);
        assertThat(gd.stack).hasSize(1);

        harness.withAutoStop(TurnStep.BEGINNING_OF_COMBAT, harness::passBothPriorities);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken()).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void tokenFromEarlierCombatIsNotRequiredToAttackInAdditionalCombat() {
        harness.addToBattlefield(player1, new HowlsquadHeavy());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.DECLARE_ATTACKERS);
        Permanent firstToken = gd.playerBattlefields.get(player1.getId()).get(1);
        declareAttackers(List.of(1));
        harness.passUntil(player1, TurnStep.POSTCOMBAT_MAIN);

        harness.castFromHand(player1, new FullThrottle(), "{4}{R}{R}");
        harness.passBothPriorities();
        harness.passUntil(player1, TurnStep.DECLARE_ATTACKERS);

        assertThat(firstToken.isTapped()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken()).hasSize(2);
        declareAttackers(List.of(2));
        assertThat(firstToken.isTapped()).isFalse();
    }

    @Test
    void manaCountsTokensButExcludesOpposingGoblinsAndOtherCreatureTypes() {
        Permanent heavy = addCreatureReady(player1, new HowlsquadHeavy());
        harness.addToBattlefield(player1, new HowlersHeavy());
        harness.addToBattlefield(player2, new GreasewrenchGoblin());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.BEGINNING_OF_COMBAT);
        harness.withAutoStop(TurnStep.BEGINNING_OF_COMBAT, this::resolveAllTriggers);
        gd.playerSpeeds.put(player1.getId(), 4);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(2);
        assertThat(heavy.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void speedThreeIsNotMaxSpeed() {
        Permanent heavy = addCreatureReady(player1, new HowlsquadHeavy());
        gd.playerSpeeds.put(player1.getId(), 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("max speed");
        assertThat(heavy.isTapped()).isFalse();
    }

    @Test
    void maxSpeedDoesNotLetSummoningSickHeavyTapForMana() {
        Permanent heavy = harness.addToBattlefieldAndReturn(player1, new HowlsquadHeavy());
        gd.playerSpeeds.put(player1.getId(), 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(heavy.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }

    @Test
    void otherGoblinsYouControlHaveHaste() {
        harness.addToBattlefield(player1, new HowlsquadHeavy());
        Permanent goblin = harness.addToBattlefieldAndReturn(player1, new GreasewrenchGoblin());

        assertThat(gqs.hasKeyword(gd, goblin, Keyword.HASTE)).isTrue();
    }

    @Test
    void createsTokenThatMustAttackThisCombat() {
        harness.addToBattlefield(player1, new HowlsquadHeavy());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        resolveAllTriggers();

        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(tokens).hasSize(1);
        assertThat(gqs.hasKeyword(gd, tokens.getFirst(), Keyword.HASTE)).isTrue();

        assertThatThrownBy(() -> declareAttackers(List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    void maxSpeedAbilityAddsRedManaForEachGoblinYouControl() {
        Permanent heavy = addCreatureReady(player1, new HowlsquadHeavy());
        harness.addToBattlefield(player1, new GreasewrenchGoblin());
        harness.addToBattlefield(player1, new GreasewrenchGoblin());
        gd.playerSpeeds.put(player1.getId(), 4);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(3);
        assertThat(heavy.isTapped()).isTrue();
    }

    @Test
    void maxSpeedAbilityRequiresMaxSpeed() {
        Permanent heavy = addCreatureReady(player1, new HowlsquadHeavy());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("max speed");
        assertThat(heavy.isTapped()).isFalse();
    }
}
