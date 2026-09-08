package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.BalduvianBarbarians;
import com.github.laxika.magicalvibes.cards.b.BalduvianBears;
import com.github.laxika.magicalvibes.cards.s.SnowCoveredMountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ChaosMoon.class, BalduvianBarbarians.class, BalduvianBears.class, SnowCoveredMountain.class})
class ChaosMoonTest extends BaseCardTest {

    @Test
    @DisplayName("Odd permanent count gives red creatures +1/+1 and leaves other colors alone")
    void oddCountBoostsRedCreatures() {
        harness.addToBattlefield(player1, new ChaosMoon());
        harness.addToBattlefield(player1, new BalduvianBarbarians());
        harness.addToBattlefield(player1, new BalduvianBears());

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Balduvian Barbarians").getEffectivePower()).isEqualTo(4);
        assertThat(findPermanent(player1, "Balduvian Barbarians").getEffectiveToughness()).isEqualTo(3);
        assertThat(findPermanent(player1, "Balduvian Bears").getEffectivePower()).isEqualTo(2);
    }

    @Test
    @DisplayName("Even permanent count gives red creatures -1/-1")
    void evenCountShrinksRedCreatures() {
        harness.addToBattlefield(player1, new ChaosMoon());
        harness.addToBattlefield(player1, new BalduvianBarbarians());

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Balduvian Barbarians").getEffectivePower()).isEqualTo(2);
        assertThat(findPermanent(player1, "Balduvian Barbarians").getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("The upkeep ability counts permanents when it resolves")
    void parityIsEvaluatedAtResolution() {
        Permanent mountain = harness.addToBattlefieldAndReturn(player1, new SnowCoveredMountain());
        harness.addToBattlefield(player1, new ChaosMoon());
        harness.addToBattlefield(player1, new BalduvianBarbarians());

        advanceToUpkeep(player1);
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, mountain));
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Balduvian Barbarians").getEffectivePower()).isEqualTo(2);
        assertThat(findPermanent(player1, "Balduvian Barbarians").getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Odd permanent count boosts red creatures on both battlefields")
    void oddCountBoostsOpponentsRedCreatures() {
        harness.addToBattlefield(player1, new ChaosMoon());
        harness.addToBattlefield(player1, new BalduvianBarbarians());
        harness.addToBattlefield(player2, new BalduvianBarbarians());

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Balduvian Barbarians").getEffectivePower()).isEqualTo(4);
        assertThat(findPermanent(player2, "Balduvian Barbarians").getEffectivePower()).isEqualTo(4);
    }

    @Test
    @DisplayName("The ability triggers during the opponent's upkeep")
    void triggersDuringOpponentsUpkeep() {
        harness.addToBattlefield(player1, new ChaosMoon());
        harness.addToBattlefield(player1, new BalduvianBarbarians());
        harness.addToBattlefield(player2, new SnowCoveredMountain());

        advanceToUpkeep(player2);
        resolveAllTriggers();
        harness.tapPermanent(player2, 0);

        assertThat(findPermanent(player1, "Balduvian Barbarians").getEffectivePower()).isEqualTo(4);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.RED)).isEqualTo(2);
    }

    @Test
    @DisplayName("Multiple Chaos Moons each add mana from a Mountain")
    void multipleChaosMoonsStackTheirExtraMana() {
        harness.addToBattlefield(player1, new ChaosMoon());
        harness.addToBattlefield(player1, new ChaosMoon());
        harness.addToBattlefield(player1, new SnowCoveredMountain());

        advanceToUpkeep(player1);
        resolveAllTriggers();
        harness.tapPermanent(player1, 2);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(3);
    }

    @Test
    @DisplayName("Odd permanent count: tapping a Mountain for mana adds an additional {R}")
    void oddCountAddsExtraRedFromMountain() {
        harness.addToBattlefield(player1, new ChaosMoon());
        harness.addToBattlefield(player1, new SnowCoveredMountain());
        harness.addToBattlefield(player1, new BalduvianBarbarians());

        advanceToUpkeep(player1);
        resolveAllTriggers();
        harness.tapPermanent(player1, 1);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(2);
    }

    @Test
    @DisplayName("The additional {R} is symmetric — an opponent tapping a Mountain gets it too")
    void extraRedIsSymmetric() {
        harness.addToBattlefield(player1, new ChaosMoon());
        harness.addToBattlefield(player1, new BalduvianBarbarians());
        harness.addToBattlefield(player2, new SnowCoveredMountain());

        advanceToUpkeep(player1);
        resolveAllTriggers();
        harness.tapPermanent(player2, 0);

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.RED)).isEqualTo(2);
    }

    @Test
    @DisplayName("Even permanent count: a Mountain produces colorless mana instead of red")
    void evenCountMakesMountainsProduceColorless() {
        harness.addToBattlefield(player1, new ChaosMoon());
        harness.addToBattlefield(player1, new SnowCoveredMountain());

        advanceToUpkeep(player1);
        resolveAllTriggers();
        harness.tapPermanent(player1, 1);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }

    @Test
    @DisplayName("The mana clauses last only until end of turn")
    void manaClauseWearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new ChaosMoon());
        harness.addToBattlefield(player1, new SnowCoveredMountain());

        advanceToUpkeep(player1);
        resolveAllTriggers();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent mountain = findPermanent(player1, "Snow-Covered Mountain");
        mountain.untap();
        harness.tapPermanent(player1, 1);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }
}
