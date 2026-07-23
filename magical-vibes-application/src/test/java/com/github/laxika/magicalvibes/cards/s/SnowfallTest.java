package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.TestCards;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SnowfallTest extends BaseCardTest {

    private void advanceToUpkeep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    private Permanent snowIsland() {
        Permanent land = new Permanent(new Island());
        TestCards.mutableCard(land).setSupertypes(EnumSet.of(CardSupertype.BASIC, CardSupertype.SNOW));
        return land;
    }

    @Test
    @DisplayName("Tapping an Island adds one cumulative-upkeep-only blue")
    void islandAddsRestrictedBlue() {
        harness.addToBattlefield(player1, new Snowfall());
        harness.addToBattlefield(player1, new Island());

        harness.tapPermanent(player1, 1);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getCumulativeUpkeepOnlyColored(ManaColor.BLUE))
                .isEqualTo(1);
    }

    @Test
    @DisplayName("Tapping a snow Island adds two cumulative-upkeep-only blue")
    void snowIslandAddsTwoRestrictedBlue() {
        harness.addToBattlefield(player1, new Snowfall());
        gd.playerBattlefields.get(player1.getId()).add(snowIsland());

        harness.tapPermanent(player1, 1);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getCumulativeUpkeepOnlyColored(ManaColor.BLUE))
                .isEqualTo(2);
    }

    @Test
    @DisplayName("Non-Island land does not trigger Snowfall")
    void nonIslandDoesNotTrigger() {
        harness.addToBattlefield(player1, new Snowfall());
        harness.addToBattlefield(player1, new Mountain());

        harness.tapPermanent(player1, 1);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getCumulativeUpkeepOnlyColored(ManaColor.BLUE))
                .isZero();
    }

    @Test
    @DisplayName("Effect is symmetric — opponent's Island also gets the restricted mana")
    void opponentIslandAlsoBenefits() {
        harness.addToBattlefield(player1, new Snowfall());
        harness.addToBattlefield(player2, new Island());

        harness.tapPermanent(player2, 0);

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player2.getId()).getCumulativeUpkeepOnlyColored(ManaColor.BLUE))
                .isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getCumulativeUpkeepOnlyColored(ManaColor.BLUE))
                .isZero();
    }

    @Test
    @DisplayName("Restricted mana cannot pay a normal spell")
    void cannotPayNormalSpell() {
        harness.addToBattlefield(player1, new Snowfall());
        harness.addToBattlefield(player1, new Island());
        harness.tapPermanent(player1, 1);

        // Drain Island's unrestricted blue — leave only CU-restricted mana.
        var pool = gd.playerManaPools.get(player1.getId());
        while (pool.get(ManaColor.BLUE) > 0) {
            pool.remove(ManaColor.BLUE);
        }

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.setHand(player1, List.of(new GrizzlyBears()));

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerManaPools.get(player1.getId()).getCumulativeUpkeepOnlyColored(ManaColor.BLUE))
                .isEqualTo(1);
    }

    @Test
    @DisplayName("Paying cumulative upkeep with Snowfall mana keeps Snowfall")
    void paysCumulativeUpkeepWithSnowfallMana() {
        Permanent snowfall = harness.addToBattlefieldAndReturn(player1, new Snowfall());

        advanceToUpkeep();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(snowfall.getCounterCount(CounterType.AGE)).isEqualTo(1);

        // Mana empties between steps — inject CU-only blue as Snowfall's Island trigger would.
        gd.playerManaPools.get(player1.getId()).addCumulativeUpkeepOnlyColored(ManaColor.BLUE, 1);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(snowfall);
        assertThat(gd.playerManaPools.get(player1.getId()).getCumulativeUpkeepOnlyColored(ManaColor.BLUE))
                .isZero();
    }

    @Test
    @DisplayName("Declining cumulative upkeep sacrifices Snowfall")
    void declineSacrifices() {
        Permanent snowfall = harness.addToBattlefieldAndReturn(player1, new Snowfall());

        advanceToUpkeep();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(snowfall);
        assertThat(gd.playerGraveyards.get(player1.getId())).anyMatch(c -> c.getName().equals("Snowfall"));
    }
}
