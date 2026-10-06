package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.z.ZodiacRabbit;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ScarwoodHag.class, Forest.class, GrizzlyBears.class, ZodiacRabbit.class})
class ScarwoodHagTest extends BaseCardTest {

    @Test
    @DisplayName("Four green mana and tapping grants forestwalk to a target creature")
    void grantsForestwalkUntilEndOfTurn() {
        addCreatureReady(player1, new ScarwoodHag());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.activateAbility(player1, 0, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.FORESTWALK)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.FORESTWALK)).isFalse();
    }

    @Test
    @DisplayName("Tapping removes forestwalk from a target creature until end of turn")
    void removesForestwalkUntilEndOfTurn() {
        addCreatureReady(player1, new ScarwoodHag());
        Permanent rabbit = addCreatureReady(player2, new ZodiacRabbit());

        assertThat(gqs.hasKeyword(gd, rabbit, Keyword.FORESTWALK)).isTrue();

        harness.activateAbility(player1, 0, 1, null, rabbit.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, rabbit, Keyword.FORESTWALK)).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, rabbit, Keyword.FORESTWALK)).isTrue();
    }

    @Test
    @DisplayName("Both abilities can target only creatures")
    void rejectsNonCreatureTarget() {
        addCreatureReady(player1, new ScarwoodHag());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addMana(player1, ManaColor.GREEN, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, forest.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A later forestwalk grant overrides an earlier removal even on a creature without forestwalk")
    void laterGrantOverridesRemoval() {
        addCreatureReady(player1, new ScarwoodHag());
        Permanent grantSource = addCreatureReady(player1, new ScarwoodHag());
        Permanent target = addCreatureReady(player2, new ScarwoodHag());

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, target, Keyword.FORESTWALK)).isFalse();

        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.activateAbility(player1, 1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(grantSource.isTapped()).isTrue();
        assertThat(gqs.hasKeyword(gd, target, Keyword.FORESTWALK)).isTrue();
    }

    @Test
    @DisplayName("A later removal overrides granted forestwalk, and the grant can target its source")
    void laterRemovalOverridesSelfTargetedGrant() {
        Permanent target = addCreatureReady(player1, new ScarwoodHag());
        Permanent removalSource = addCreatureReady(player1, new ScarwoodHag());
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();
        assertThat(target.isTapped()).isTrue();
        assertThat(gqs.hasKeyword(gd, target, Keyword.FORESTWALK)).isTrue();

        harness.activateAbility(player1, 1, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(removalSource.isTapped()).isTrue();
        assertThat(gqs.hasKeyword(gd, target, Keyword.FORESTWALK)).isFalse();
    }

    @Test
    @DisplayName("Granting forestwalk requires all four green mana")
    void rejectsInsufficientGreenMana() {
        Permanent hag = addCreatureReady(player1, new ScarwoodHag());
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, hag.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(hag.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Summoning sickness prevents both tap abilities")
    void summoningSicknessPreventsActivation() {
        Permanent hag = harness.addToBattlefieldAndReturn(player1, new ScarwoodHag());
        harness.addMana(player1, ManaColor.GREEN, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, hag.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, hag.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(hag.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    @DisplayName("Granted forestwalk prevents blocking against a Forest unless subsequently removed")
    void forestwalkAffectsBlocking(boolean removeForestwalk) {
        addCreatureReady(player1, new ScarwoodHag());
        Permanent attacker = addCreatureReady(player1, new ScarwoodHag());
        addCreatureReady(player1, new ScarwoodHag());
        harness.addToBattlefield(player2, new Forest());
        Permanent blocker = addCreatureReady(player2, new ScarwoodHag());
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.activateAbility(player1, 0, 0, null, attacker.getId());
        harness.passBothPriorities();
        if (removeForestwalk) {
            harness.activateAbility(player1, 2, 1, null, attacker.getId());
            harness.passBothPriorities();
        }

        declareAttackersAndPrepareBlockers(List.of(1));

        if (removeForestwalk) {
            gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(1, 1)));
            assertThat(blocker.isBlocking()).isTrue();
        } else {
            assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                    List.of(new BlockerAssignment(1, 1))))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("can't be blocked");
        }
    }
}
