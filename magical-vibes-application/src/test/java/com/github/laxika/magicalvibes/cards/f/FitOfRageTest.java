package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FitOfRage.class, GrizzlyBears.class, FountainOfYouth.class})
class FitOfRageTest extends BaseCardTest {

    @Test
    @DisplayName("Grants +3/+3 and first strike to target creature")
    void grantsBoostAndFirstStrike() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new FitOfRage()));
        harness.addMana(player1, ManaColor.RED, 2);

        UUID bearId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castAndResolveSorcery(player1, 0, bearId);

        Permanent bear = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        assertThat(bear.getPowerModifier()).isEqualTo(3);
        assertThat(bear.getToughnessModifier()).isEqualTo(3);
        assertThat(bear.getGrantedKeywords()).contains(Keyword.FIRST_STRIKE);
    }

    @Test
    @DisplayName("Boost and first strike wear off at cleanup step")
    void boostWearsOffAtCleanup() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new FitOfRage()));
        harness.addMana(player1, ManaColor.RED, 2);

        UUID bearId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castAndResolveSorcery(player1, 0, bearId);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent bear = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        assertThat(bear.getPowerModifier()).isZero();
        assertThat(bear.getToughnessModifier()).isZero();
        assertThat(bear.getGrantedKeywords()).doesNotContain(Keyword.FIRST_STRIKE);
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
        GrizzlyBears attackerCard = new GrizzlyBears();
        attackerCard.setPower(1);
        attackerCard.setToughness(1);
        Permanent attacker = addCreatureReady(player1, attackerCard);

        GrizzlyBears blockerCard = new GrizzlyBears();
        blockerCard.setPower(4);
        blockerCard.setToughness(4);
        addCreatureReady(player2, blockerCard);

        harness.setHand(player1, List.of(new FitOfRage()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castAndResolveSorcery(player1, 0, attacker.getId());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new com.github.laxika.magicalvibes.cards.f.FountainOfYouth());
        harness.setHand(player1, List.of(new FitOfRage()));
        harness.addMana(player1, ManaColor.RED, 2);

        UUID targetId = harness.getPermanentId(player1, "Fountain of Youth");
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
