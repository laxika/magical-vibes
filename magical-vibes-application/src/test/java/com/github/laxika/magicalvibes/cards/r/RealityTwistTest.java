package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.cards.v.VolcanicIsland;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(RealityTwist.class)
class RealityTwistTest extends BaseCardTest {

    @Test
    @CardUsed({Plains.class, Swamp.class, Mountain.class, Forest.class, Island.class})
    @DisplayName("Remaps each basic land type's mana, leaving Islands unchanged")
    void remapsBasicLandTypes() {
        harness.addToBattlefield(player1, new RealityTwist());
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Island());

        harness.tapPermanent(player1, 1);
        harness.tapPermanent(player1, 2);
        harness.tapPermanent(player1, 3);
        harness.tapPermanent(player1, 4);
        harness.tapPermanent(player1, 5);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
    }

    @Test
    @CardUsed(Forest.class)
    @DisplayName("Applies to lands controlled by another player")
    void appliesGlobally() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new RealityTwist());

        harness.tapPermanent(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    @CardUsed(VolcanicIsland.class)
    @DisplayName("A land with multiple basic types can choose either applicable mana color")
    void multiTypeLandCanChooseEitherApplicableColor() {
        harness.addToBattlefield(player1, new VolcanicIsland());
        harness.addToBattlefield(player2, new RealityTwist());

        harness.activateAbility(player1, 0, 0, null, null);

        PendingInteraction.ColorChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.options()).containsExactlyInAnyOrder("BLUE", "WHITE");

        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
    }

    @Test
    @DisplayName("Cumulative upkeep is paid once per age counter")
    void cumulativeUpkeepIsPaidPerAgeCounter() {
        Permanent realityTwist = harness.addToBattlefieldAndReturn(player1, new RealityTwist());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(realityTwist.getCounterCount(CounterType.AGE)).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(realityTwist);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(realityTwist.getCounterCount(CounterType.AGE)).isEqualTo(2);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(realityTwist);
    }

    @Test
    @DisplayName("Declining cumulative upkeep sacrifices Reality Twist")
    void decliningCumulativeUpkeepSacrifices() {
        Permanent realityTwist = harness.addToBattlefieldAndReturn(player1, new RealityTwist());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(realityTwist);
        harness.assertInGraveyard(player1, "Reality Twist");
    }
}
