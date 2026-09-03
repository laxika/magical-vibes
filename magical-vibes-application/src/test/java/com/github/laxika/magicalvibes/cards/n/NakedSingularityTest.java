package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.s.SnowCoveredForest;
import com.github.laxika.magicalvibes.cards.s.SnowCoveredIsland;
import com.github.laxika.magicalvibes.cards.s.SnowCoveredMountain;
import com.github.laxika.magicalvibes.cards.s.SnowCoveredPlains;
import com.github.laxika.magicalvibes.cards.s.SnowCoveredSwamp;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NakedSingularity.class, SnowCoveredForest.class, SnowCoveredIsland.class,
        SnowCoveredMountain.class, SnowCoveredPlains.class, SnowCoveredSwamp.class})
class NakedSingularityTest extends BaseCardTest {

    @Test
    @DisplayName("Resolves onto the battlefield")
    void resolvesOntoBattlefield() {
        harness.setHand(player1, List.of(new NakedSingularity()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Naked Singularity");
    }

    @Test
    @DisplayName("Plains produce red instead of white")
    void plainsProduceRed() {
        harness.addToBattlefield(player1, new NakedSingularity());
        harness.addToBattlefield(player1, new SnowCoveredPlains());

        harness.tapPermanent(player1, 1);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(0);
    }

    @Test
    @DisplayName("Islands produce green instead of blue")
    void islandsProduceGreen() {
        harness.addToBattlefield(player1, new NakedSingularity());
        harness.addToBattlefield(player1, new SnowCoveredIsland());

        harness.tapPermanent(player1, 1);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(0);
    }

    @Test
    @DisplayName("Swamps produce white instead of black")
    void swampsProduceWhite() {
        harness.addToBattlefield(player1, new NakedSingularity());
        harness.addToBattlefield(player1, new SnowCoveredSwamp());

        harness.tapPermanent(player1, 1);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(0);
    }

    @Test
    @DisplayName("Mountains produce blue instead of red")
    void mountainsProduceBlue() {
        harness.addToBattlefield(player1, new NakedSingularity());
        harness.addToBattlefield(player1, new SnowCoveredMountain());

        harness.tapPermanent(player1, 1);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(0);
    }

    @Test
    @DisplayName("Forests produce black instead of green")
    void forestsProduceBlack() {
        harness.addToBattlefield(player1, new NakedSingularity());
        harness.addToBattlefield(player1, new SnowCoveredForest());

        harness.tapPermanent(player1, 1);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(0);
    }

    @Test
    @DisplayName("Affects opponent lands too")
    void affectsOpponentLands() {
        harness.addToBattlefield(player1, new NakedSingularity());
        harness.addToBattlefield(player2, new SnowCoveredMountain());

        harness.forceActivePlayer(player2);
        harness.clearPriorityPassed();
        harness.tapPermanent(player2, 0);

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.RED)).isEqualTo(0);
    }

    @Test
    @DisplayName("Without Naked Singularity lands produce normally")
    void baselineWithoutSingularity() {
        harness.addToBattlefield(player1, new SnowCoveredMountain());

        harness.tapPermanent(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(0);
    }

    @Test
    @DisplayName("Color replacement preserves snow mana from a snow-covered land")
    void preservesSnowManaWhenChangingColor() {
        harness.addToBattlefield(player1, new NakedSingularity());
        harness.addToBattlefield(player1, new SnowCoveredForest());

        harness.tapPermanent(player1, 1);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getSnowMana(ManaColor.BLACK)).isEqualTo(1);
    }

    @Test
    @DisplayName("Paying cumulative upkeep keeps Naked Singularity")
    void paysCumulativeUpkeep() {
        Permanent singularity = harness.addToBattlefieldAndReturn(player1, new NakedSingularity());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(singularity.getCounterCount(CounterType.AGE)).isEqualTo(1);

        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(singularity);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(singularity.getCounterCount(CounterType.AGE)).isEqualTo(2);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(singularity);
    }

    @Test
    @DisplayName("Declining cumulative upkeep sacrifices Naked Singularity")
    void declineSacrifices() {
        Permanent singularity = harness.addToBattlefieldAndReturn(player1, new NakedSingularity());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(singularity);
        harness.assertInGraveyard(player1, "Naked Singularity");
    }
}
