package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.b.BenalishInfantry;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BenalishInfantry.class, FitOfRage.class, FireElemental.class, GrizzlyBears.class, MindStone.class, Unsummon.class})
class FitOfRageTest extends BaseCardTest {

    @Test
    @DisplayName("Grants +3/+3 and first strike to target creature")
    void grantsBoostAndFirstStrike() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new BenalishInfantry());
        harness.setHand(player1, List.of(new FitOfRage()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveSorcery(player1, 0, creature.getId());

        assertThat(creature.getPowerModifier()).isEqualTo(3);
        assertThat(creature.getToughnessModifier()).isEqualTo(3);
        assertThat(creature.getGrantedKeywords()).contains(Keyword.FIRST_STRIKE);
    }

    @Test
    @DisplayName("Boost and first strike wear off at cleanup step")
    void boostWearsOffAtCleanup() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new BenalishInfantry());
        harness.setHand(player1, List.of(new FitOfRage()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveSorcery(player1, 0, creature.getId());

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(creature.getPowerModifier()).isZero();
        assertThat(creature.getToughnessModifier()).isZero();
        assertThat(creature.getGrantedKeywords()).doesNotContain(Keyword.FIRST_STRIKE);
    }

    @Test
    @DisplayName("Only the targeted creature gets the boost and first strike")
    void onlyTargetedCreatureIsAffected() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        Permanent other = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new FitOfRage()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveSorcery(player1, 0, target.getId());

        assertThat(target.getPowerModifier()).isEqualTo(3);
        assertThat(target.getToughnessModifier()).isEqualTo(3);
        assertThat(target.getGrantedKeywords()).contains(Keyword.FIRST_STRIKE);
        assertThat(other.getPowerModifier()).isZero();
        assertThat(other.getToughnessModifier()).isZero();
        assertThat(other.getGrantedKeywords()).doesNotContain(Keyword.FIRST_STRIKE);
    }

    @Test
    @DisplayName("Can target a creature controlled by an opponent")
    void canTargetOpposingCreature() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new FitOfRage()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveSorcery(player1, 0, target.getId());

        assertThat(target.getPowerModifier()).isEqualTo(3);
        assertThat(target.getToughnessModifier()).isEqualTo(3);
        assertThat(target.getGrantedKeywords()).contains(Keyword.FIRST_STRIKE);
    }

    @Test
    @DisplayName("Granted first strike deals combat damage before a blocker")
    void grantedFirstStrikeDealsCombatDamageFirst() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new FireElemental());

        harness.setHand(player1, List.of(new FitOfRage()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castAndResolveSorcery(player1, 0, attacker.getId());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Fire Elemental");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new MindStone());
        harness.setHand(player1, List.of(new FitOfRage()));
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Can target a creature controlled by an opponent")
    void canTargetOpponentsCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new BenalishInfantry());
        harness.setHand(player1, List.of(new FitOfRage()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveSorcery(player1, 0, creature.getId());

        assertThat(creature.getPowerModifier()).isEqualTo(3);
        assertThat(creature.getToughnessModifier()).isEqualTo(3);
        assertThat(creature.getGrantedKeywords()).contains(Keyword.FIRST_STRIKE);
    }

    @Test
    @DisplayName("Does not affect another creature when its target leaves before resolution")
    void targetLeavesBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new FitOfRage(), new Unsummon()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castSorcery(player1, 0, target.getId());
        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Fit of Rage");
        assertThat(other.getPowerModifier()).isZero();
        assertThat(other.getToughnessModifier()).isZero();
        assertThat(other.getGrantedKeywords()).doesNotContain(Keyword.FIRST_STRIKE);
    }
}
