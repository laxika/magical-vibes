package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SaberclawGolem.class})
class SaberclawGolemTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Saberclaw Golem puts it on the stack")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new SaberclawGolem()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castArtifact(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ARTIFACT_SPELL);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Saberclaw Golem");
    }

    @Test
    @DisplayName("Resolving puts Saberclaw Golem onto the battlefield")
    void resolvingPutsOnBattlefield() {
        harness.setHand(player1, List.of(new SaberclawGolem()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Saberclaw Golem");
    }

    @Test
    @DisplayName("Activating ability puts GrantKeywordToSelf on the stack")
    void activatingAbilityPutsOnStack() {
        Permanent golem = addCreatureReady(player1, new SaberclawGolem());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getCard().getName()).isEqualTo("Saberclaw Golem");
        assertThat(entry.getTargetId()).isEqualTo(golem.getId());
    }

    @Test
    @DisplayName("Resolving ability grants first strike until end of turn")
    void resolvingAbilityGrantsFirstStrike() {
        Permanent golem = addCreatureReady(player1, new SaberclawGolem());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.hasKeyword(gd, golem, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("First strike resets at end of turn cleanup")
    void firstStrikeResetsAtEndOfTurn() {
        Permanent golem = addCreatureReady(player1, new SaberclawGolem());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, golem, Keyword.FIRST_STRIKE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, golem, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Activating ability does not tap Saberclaw Golem")
    void activatingAbilityDoesNotTap() {
        Permanent golem = addCreatureReady(player1, new SaberclawGolem());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(golem.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Can activate ability when tapped")
    void canActivateWhenTapped() {
        Permanent golem = addCreatureReady(player1, new SaberclawGolem());
        golem.tap();
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Can activate ability with summoning sickness")
    void canActivateWithSummoningSickness() {
        harness.addToBattlefield(player1, new SaberclawGolem());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Requires red mana to activate")
    void requiresRedMana() {
        addCreatureReady(player1, new SaberclawGolem());
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Can activate same ability multiple times")
    void canActivateMultipleTimes() {
        Permanent golem = addCreatureReady(player1, new SaberclawGolem());
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, golem, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Ability resolves without granting first strike if its source has left")
    void abilityDoesNothingIfSourceRemoved() {
        addCreatureReady(player1, new SaberclawGolem());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        gd.playerBattlefields.get(player1.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Activated first strike kills a blocker before it can deal damage")
    void firstStrikeKillsBlockerBeforeRegularDamage() {
        addCreatureReady(player1, new SaberclawGolem());
        Permanent blocker = addCreatureReady(player2, new SaberclawGolem());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, blocker, Keyword.FIRST_STRIKE)).isFalse();
        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertOnBattlefield(player1, "Saberclaw Golem");
        harness.assertInGraveyard(player2, "Saberclaw Golem");
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("An ability from a departed Golem does not affect another Golem")
    void departedSourceDoesNotGrantKeywordToReplacement() {
        addCreatureReady(player1, new SaberclawGolem());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 0, 0, null, null);

        gd.playerBattlefields.get(player1.getId()).clear();
        Permanent replacement = harness.addToBattlefieldAndReturn(player1, new SaberclawGolem());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.hasKeyword(gd, replacement, Keyword.FIRST_STRIKE)).isFalse();
    }
}
