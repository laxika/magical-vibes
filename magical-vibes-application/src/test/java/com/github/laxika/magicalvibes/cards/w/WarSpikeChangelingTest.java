package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.b.BallyrushBanneret;
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

@CardUsed({WarSpikeChangeling.class, BallyrushBanneret.class})
class WarSpikeChangelingTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving {R} ability grants first strike until end of turn")
    void resolvingAbilityGrantsFirstStrike() {
        Permanent changeling = addCreatureReady(player1, new WarSpikeChangeling());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.hasKeyword(gd, changeling, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("First strike granted by ability wears off at end of turn")
    void firstStrikeWearsOffAtEndOfTurn() {
        Permanent changeling = addCreatureReady(player1, new WarSpikeChangeling());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, changeling, Keyword.FIRST_STRIKE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, changeling, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Cannot activate ability without red mana")
    void cannotActivateWithoutRedMana() {
        addCreatureReady(player1, new WarSpikeChangeling());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Activation consumes exactly one red mana")
    void activationConsumesExactlyOneRedMana() {
        addCreatureReady(player1, new WarSpikeChangeling());
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot activate ability with only colorless mana")
    void cannotActivateWithOnlyColorlessMana() {
        addCreatureReady(player1, new WarSpikeChangeling());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("A tapped, summoning-sick Changeling can activate its ability")
    void canActivateWhileTappedAndSummoningSick() {
        Permanent changeling = harness.addToBattlefieldAndReturn(player1, new WarSpikeChangeling());
        changeling.setSummoningSick(true);
        changeling.tap();
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, changeling, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(changeling.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Repeated activations grant first strike only to their source")
    void repeatedActivationsAffectOnlySource() {
        Permanent source = addCreatureReady(player1, new WarSpikeChangeling());
        Permanent other = addCreatureReady(player1, new WarSpikeChangeling());
        Permanent opponent = addCreatureReady(player2, new WarSpikeChangeling());
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, null);
        assertThat(gqs.hasKeyword(gd, source, Keyword.FIRST_STRIKE)).isFalse();
        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.hasKeyword(gd, source, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, other, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponent, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }

    @Test
    @DisplayName("Granted first strike kills an equal-size blocker before it can deal damage")
    void firstStrikeKillsBlockerBeforeRegularDamage() {
        Permanent attacker = addCreatureReady(player1, new WarSpikeChangeling());
        WarSpikeChangeling blocker = new WarSpikeChangeling();
        addCreatureReady(player2, blocker);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(attacker);
        harness.assertNotOnBattlefield(player2, "War-Spike Changeling");
        harness.assertInGraveyard(player2, "War-Spike Changeling");
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Changeling qualifies for Banneret's reduction once despite having both creature types")
    void changelingReceivesOneTribalCostReduction() {
        harness.addToBattlefield(player1, new BallyrushBanneret());
        WarSpikeChangeling changeling = new WarSpikeChangeling();
        harness.setHand(player1, List.of(changeling));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "War-Spike Changeling");
    }
}
