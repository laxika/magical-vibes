package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.k.KikiJikiMirrorBreaker;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentAction;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentActionKind;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TempestraDameOfGames.class, GrizzlyBears.class, KikiJikiMirrorBreaker.class, Ornithopter.class})
class TempestraDameOfGamesTest extends BaseCardTest {

    @Test
    void createsHastyNonlegendaryCopyAndSacrificesArtifactAsCost() {
        Permanent tempestra = addCreatureReady(player1, new TempestraDameOfGames());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new Ornithopter());
        addManaForAbility();

        harness.activateAbility(player1, battlefieldIndex(tempestra), null, target.getId());
        harness.passBothPriorities();

        assertThat(tempestra.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Ornithopter");

        Permanent token = findPermanents(player1, "Grizzly Bears").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(gqs.hasKeyword(gd, token, Keyword.HASTE)).isTrue();
        assertThat(token.getEffectivePower()).isEqualTo(target.getEffectivePower());
        assertThat(token.getEffectiveToughness()).isEqualTo(target.getEffectiveToughness());
        assertThat(gd.getDelayedActions(DelayedPermanentAction.class))
                .contains(new DelayedPermanentAction(token.getId(), DelayedPermanentActionKind.SACRIFICE_AT_END_STEP,
                        false, null, null, player1.getId()));
    }

    @Test
    void sacrificesCopyAtTheNextEndStep() {
        Permanent tempestra = addCreatureReady(player1, new TempestraDameOfGames());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new Ornithopter());
        addManaForAbility();

        harness.activateAbility(player1, battlefieldIndex(tempestra), null, target.getId());
        harness.passBothPriorities();
        assertThat(findPermanents(player1, "Grizzly Bears")).hasSize(2);

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Grizzly Bears")).hasSize(1);
    }

    @Test
    void copyOfLegendaryCreatureIsNotLegendary() {
        Permanent tempestra = addCreatureReady(player1, new TempestraDameOfGames());
        Permanent target = addCreatureReady(player1, new KikiJikiMirrorBreaker());
        harness.addToBattlefield(player1, new Ornithopter());
        addManaForAbility();

        harness.activateAbility(player1, battlefieldIndex(tempestra), null, target.getId());
        harness.passBothPriorities();

        Permanent token = findPermanents(player1, "Kiki-Jiki, Mirror Breaker").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(token.getCard().getSupertypes()).doesNotContain(CardSupertype.LEGENDARY);
    }

    @Test
    void cannotTargetTempestraItself() {
        Permanent tempestra = addCreatureReady(player1, new TempestraDameOfGames());
        harness.addToBattlefield(player1, new Ornithopter());
        addManaForAbility();

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(tempestra), null, tempestra.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("another creature you control");
    }

    @Test
    void cannotTargetOpponentsCreature() {
        Permanent tempestra = addCreatureReady(player1, new TempestraDameOfGames());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new Ornithopter());
        addManaForAbility();

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(tempestra), null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(tempestra.isTapped()).isFalse();
        harness.assertOnBattlefield(player1, "Ornithopter");
    }

    @Test
    void cannotActivateWithoutAnArtifactToSacrifice() {
        Permanent tempestra = addCreatureReady(player1, new TempestraDameOfGames());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new Ornithopter());
        addManaForAbility();

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(tempestra), null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(tempestra.isTapped()).isFalse();
        assertThat(findPermanents(player1, "Grizzly Bears")).hasSize(1);
    }

    @Test
    void cannotActivateWithoutRedMana() {
        Permanent tempestra = addCreatureReady(player1, new TempestraDameOfGames());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new Ornithopter());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(tempestra), null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Ornithopter");
        assertThat(tempestra.isTapped()).isFalse();
    }

    @Test
    void cannotActivateWhileSummoningSick() {
        Permanent tempestra = harness.addToBattlefieldAndReturn(player1, new TempestraDameOfGames());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new Ornithopter());
        addManaForAbility();

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(tempestra), null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Ornithopter");
    }

    @Test
    void sacrificingTargetArtifactCreatureMakesAbilityFailToResolve() {
        Permanent tempestra = addCreatureReady(player1, new TempestraDameOfGames());
        Permanent target = addCreatureReady(player1, new Ornithopter());
        addManaForAbility();

        harness.activateAbility(player1, battlefieldIndex(tempestra), null, target.getId());
        harness.assertInGraveyard(player1, "Ornithopter");
        assertThat(tempestra.isTapped()).isTrue();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Ornithopter");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void copyDoesNotCopyCountersOnTarget() {
        Permanent tempestra = addCreatureReady(player1, new TempestraDameOfGames());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        target.getCounters().put(CounterType.PLUS_ONE_PLUS_ONE, 3);
        harness.addToBattlefield(player1, new Ornithopter());
        addManaForAbility();

        harness.activateAbility(player1, battlefieldIndex(tempestra), null, target.getId());
        harness.passBothPriorities();

        Permanent token = findPermanents(player1, "Grizzly Bears").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst().orElseThrow();
        assertThat(token.getCounters()).isEmpty();
        assertThat(token.getEffectivePower()).isEqualTo(target.getEffectivePower() - 3);
        assertThat(token.getEffectiveToughness()).isEqualTo(target.getEffectiveToughness() - 3);
    }

    @Test
    void targetMustStillBeControlledByAbilityControllerOnResolution() {
        Permanent tempestra = addCreatureReady(player1, new TempestraDameOfGames());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new Ornithopter());
        addManaForAbility();

        harness.activateAbility(player1, battlefieldIndex(tempestra), null, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(target);
        gd.playerBattlefields.get(player2.getId()).add(target);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(findPermanents(player2, "Grizzly Bears")).containsExactly(target);
        harness.assertInGraveyard(player1, "Ornithopter");
    }

    @Test
    void copyCreatedDuringEndStepSurvivesUntilFollowingEndStep() {
        Permanent tempestra = addCreatureReady(player1, new TempestraDameOfGames());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new Ornithopter());
        addManaForAbility();
        harness.forceStep(TurnStep.END_STEP);

        harness.activateAbility(player1, battlefieldIndex(tempestra), null, target.getId());
        harness.passBothPriorities();
        assertThat(findPermanents(player1, "Grizzly Bears")).hasSize(2);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Grizzly Bears")).containsExactly(target);
    }

    private void addManaForAbility() {
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
