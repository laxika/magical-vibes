package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({BattleCryGoblin.class, GrizzlyBears.class})
class BattleCryGoblinTest extends BaseCardTest {

    @Test
    @DisplayName("The activated ability boosts and grants haste to all controlled Goblins")
    void activatedAbilityAffectsAllControlledGoblins() {
        Permanent source = addCreatureReady(player1, new BattleCryGoblin());
        Permanent otherGoblin = addCreatureReady(player1, new BattleCryGoblin());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();

        assertThat(source.getPowerModifier()).isEqualTo(1);
        assertThat(otherGoblin.getPowerModifier()).isEqualTo(1);
        assertThat(bear.getPowerModifier()).isZero();
        assertThat(gqs.hasKeyword(gd, source, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, otherGoblin, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, bear, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Pack tactics creates a tapped and attacking Goblin at total power six")
    void packTacticsCreatesTokenAtThreshold() {
        addCreatureReady(player1, new BattleCryGoblin());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0, 1, 2));
        resolveAllTriggers();

        Permanent token = findPermanent(player1, "Goblin");
        assertThat(token.isTapped()).isTrue();
        assertThat(token.isAttackedThisTurn()).isTrue();
    }

    @Test
    @DisplayName("Pack tactics does not trigger when the Battle Cry Goblin does not attack")
    void packTacticsRequiresSourceToAttack() {
        addCreatureReady(player1, new BattleCryGoblin());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(1, 2, 3));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Goblin")).isEmpty();
    }

    @Test
    @DisplayName("Pack tactics uses the attacking power when the trigger is created")
    void packTacticsDoesNotRecheckPowerOnResolution() {
        addCreatureReady(player1, new BattleCryGoblin());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0, 1, 2));
        bear.setPowerModifier(-2);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Goblin")).hasSize(1);
    }
}
