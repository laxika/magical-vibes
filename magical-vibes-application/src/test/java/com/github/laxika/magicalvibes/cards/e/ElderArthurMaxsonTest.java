package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GaryClone;
import com.github.laxika.magicalvibes.cards.z.ZurgoThundersDecree;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ElderArthurMaxson.class, GrizzlyBears.class, ZurgoThundersDecree.class, GaryClone.class})
class ElderArthurMaxsonTest extends BaseCardTest {

    @Test
    @DisplayName("Elder Arthur Maxson gives training to creature tokens you control")
    void givesTrainingToCreatureTokens() {
        addReadyArthur();
        Permanent zurgo = addCreatureReady(player1, new ZurgoThundersDecree());

        declareAttackers(java.util.List.of(
                gd.playerBattlefields.get(player1.getId()).indexOf(zurgo)));
        harness.passBothPriorities();

        Permanent warrior = findPermanent(player1, "Warrior");

        assertThat(gqs.hasKeyword(gd, warrior, Keyword.TRAINING)).isTrue();
    }

    @Test
    @DisplayName("Sacrificing another creature grants indestructible until end of turn")
    void sacrificesAnotherCreatureAndGainsIndestructible() {
        Permanent arthur = addReadyArthur();
        addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, arthur, Keyword.INDESTRUCTIBLE)).isTrue();
        harness.assertInGraveyard(player1, "Grizzly Bears");

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, arthur, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("The ability cannot be activated without another creature")
    void cannotActivateWithoutAnotherCreature() {
        addReadyArthur();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyArthur() {
        return addCreatureReady(player1, new ElderArthurMaxson());
    }

    @Test
    void tokenCopyOfArthurHasTrainingItself() {
        ElderArthurMaxson tokenCard = new ElderArthurMaxson();
        tokenCard.setToken(true);
        Permanent arthur = addCreatureReady(player1, tokenCard);

        assertThat(gqs.hasKeyword(gd, arthur, Keyword.TRAINING)).isTrue();
    }

    @Test
    void trainingOnlyAppliesToOwnCreatureTokens() {
        Permanent arthur = addReadyArthur();
        Permanent nontoken = addCreatureReady(player1, new GaryClone());
        GaryClone tokenCard = new GaryClone();
        tokenCard.setToken(true);
        Permanent opposingToken = addCreatureReady(player2, tokenCard);

        assertThat(gqs.hasKeyword(gd, arthur, Keyword.TRAINING)).isFalse();
        assertThat(gqs.hasKeyword(gd, nontoken, Keyword.TRAINING)).isFalse();
        assertThat(gqs.hasKeyword(gd, opposingToken, Keyword.TRAINING)).isFalse();
    }

    @Test
    void tokenTrainsOnceWhenAttackingWithMultipleStrongerCreatures() {
        Permanent arthur = addReadyArthur();
        GaryClone tokenCard = new GaryClone();
        tokenCard.setToken(true);
        Permanent token = addCreatureReady(player1, tokenCard);
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(java.util.List.of(0, 1, 2));
        resolveAllTriggers();

        assertThat(token.getPlusOnePlusOneCounters()).isEqualTo(1);
        assertThat(arthur.getPlusOnePlusOneCounters()).isZero();
    }

    @Test
    void tokenDoesNotTrainWithOnlyAnEqualPowerAttacker() {
        addReadyArthur();
        GaryClone tokenCard = new GaryClone();
        tokenCard.setToken(true);
        Permanent token = addCreatureReady(player1, tokenCard);
        addCreatureReady(player1, new GaryClone());

        declareAttackers(java.util.List.of(1, 2));
        resolveAllTriggers();

        assertThat(token.getPlusOnePlusOneCounters()).isZero();
    }

    @Test
    void tokenDoesNotTrainWhenStrongerCreatureDoesNotAttack() {
        addReadyArthur();
        GaryClone tokenCard = new GaryClone();
        tokenCard.setToken(true);
        Permanent token = addCreatureReady(player1, tokenCard);

        declareAttackers(java.util.List.of(1));
        resolveAllTriggers();

        assertThat(token.getPlusOnePlusOneCounters()).isZero();
    }

    @Test
    void cannotSacrificeAnOpponentsCreature() {
        addReadyArthur();
        addCreatureReady(player2, new GaryClone());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void sacrificeIsPaidBeforeResolutionAndDoesNotRequireHaste() {
        Permanent arthur = harness.addToBattlefieldAndReturn(player1, new ElderArthurMaxson());
        harness.addToBattlefield(player1, new GaryClone());

        harness.activateAbility(player1, 0, null, null);

        harness.assertInGraveyard(player1, "Gary Clone");
        assertThat(gqs.hasKeyword(gd, arthur, Keyword.INDESTRUCTIBLE)).isFalse();

        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, arthur, Keyword.INDESTRUCTIBLE)).isTrue();
    }
}
