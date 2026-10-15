package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.e.ExplosiveDerailment;
import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({SeraphicSteed.class, ExplosiveDerailment.class})
class SeraphicSteedTest extends BaseCardTest {

    @Test
    @DisplayName("Saddle 4 taps other creatures and saddles Seraphic Steed")
    void saddleTapsOtherCreatures() {
        Permanent steed = addCreatureReady(player1, new SeraphicSteed());
        Permanent firstHelper = addCreatureReady(player1, new SeraphicSteed());
        Permanent secondHelper = addCreatureReady(player1, new SeraphicSteed());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(steed.isSaddled()).isTrue();
        assertThat(firstHelper.isTapped()).isTrue();
        assertThat(secondHelper.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Attacking while saddled creates a 3/3 flying Angel token")
    void attacksWhileSaddledCreatesAngel() {
        Permanent steed = addCreatureReady(player1, new SeraphicSteed());
        steed.setSaddled(true);

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Angel")).singleElement()
                .satisfies(angel -> {
                    assertThat(gqs.getEffectivePower(gd, angel)).isEqualTo(3);
                    assertThat(gqs.getEffectiveToughness(gd, angel)).isEqualTo(3);
                    assertThat(gqs.hasKeyword(gd, angel, Keyword.FLYING)).isTrue();
                });
    }

    @Test
    @DisplayName("Attacking while not saddled does not create an Angel token")
    void doesNotCreateAngelWhenNotSaddled() {
        addCreatureReady(player1, new SeraphicSteed());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Angel")).isEmpty();
    }

    @Test
    @DisplayName("The attack trigger checks saddled when attackers are declared")
    void checksSaddledAtDeclaration() {
        Permanent steed = addCreatureReady(player1, new SeraphicSteed());

        declareAttackers(player1, List.of(0));
        steed.setSaddled(true);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Angel")).isEmpty();
    }

    @Test
    @DisplayName("Saddle cannot count the Mount itself or opposing creatures")
    void saddleRequiresFourPowerOfOtherControlledCreatures() {
        Permanent steed = addCreatureReady(player1, new SeraphicSteed());
        Permanent helper = addCreatureReady(player1, new SeraphicSteed());
        addCreatureReady(player2, new SeraphicSteed());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(steed.isSaddled()).isFalse();
        assertThat(steed.isTapped()).isFalse();
        assertThat(helper.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Summoning-sick creatures can pay the saddle cost")
    void summoningSickCreaturesCanSaddle() {
        Permanent steed = harness.addToBattlefieldAndReturn(player1, new SeraphicSteed());
        Permanent firstHelper = harness.addToBattlefieldAndReturn(player1, new SeraphicSteed());
        Permanent secondHelper = harness.addToBattlefieldAndReturn(player1, new SeraphicSteed());

        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();

        assertThat(steed.isSaddled()).isTrue();
        assertThat(steed.isTapped()).isFalse();
        assertThat(firstHelper.isTapped()).isTrue();
        assertThat(secondHelper.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Saddle cannot be activated during combat")
    void saddleIsSorcerySpeed() {
        addCreatureReady(player1, new SeraphicSteed());
        addCreatureReady(player1, new SeraphicSteed());
        addCreatureReady(player1, new SeraphicSteed());
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Saddling enables the attack trigger and expires at end of turn")
    void saddleEnablesAttackTriggerUntilEndOfTurn() {
        Permanent steed = addCreatureReady(player1, new SeraphicSteed());
        addCreatureReady(player1, new SeraphicSteed());
        addCreatureReady(player1, new SeraphicSteed());

        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();
        assertThat(steed.isSaddled()).isTrue();

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();
        assertThat(findPermanents(player1, "Angel")).hasSize(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(steed.isSaddled()).isFalse();
        assertThat(findPermanents(player1, "Angel")).hasSize(1);
    }

    @Test
    @DisplayName("The Angel is created even if the attacking Steed dies before resolution")
    void attackTriggerSurvivesSourceRemoval() {
        Permanent steed = addCreatureReady(player1, new SeraphicSteed());
        steed.setSaddled(true);
        harness.setHand(player2, List.of(new ExplosiveDerailment()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        declareAttackers(player1, List.of(0));
        harness.castModalInstantWithModes(player2, 0, 1, 2,
                new int[]{0}, List.of(steed.getId()));
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Seraphic Steed");
        assertThat(findPermanents(player1, "Angel")).singleElement()
                .satisfies(angel -> {
                    assertThat(angel.isAttacking()).isFalse();
                    assertThat(angel.isTapped()).isFalse();
                    assertThat(gqs.getEffectivePower(gd, angel)).isEqualTo(3);
                    assertThat(gqs.getEffectiveToughness(gd, angel)).isEqualTo(3);
                    assertThat(gqs.hasKeyword(gd, angel, Keyword.FLYING)).isTrue();
                });
        assertThat(findPermanents(player2, "Angel")).isEmpty();
    }
}
