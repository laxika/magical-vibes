package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.d.Disperse;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ThunderclapWyvern.class, SuntailHawk.class, GrizzlyBears.class, Disperse.class})
class ThunderclapWyvernTest extends BaseCardTest {

    @Test
    @DisplayName("Boosts another flier you control")
    void boostsOtherOwnFlier() {
        addCreatureReady(player1, new ThunderclapWyvern());
        Permanent hawk = addCreatureReady(player1, new SuntailHawk());

        assertThat(gqs.getEffectivePower(gd, hawk)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, hawk)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not boost itself")
    void doesNotBoostItself() {
        Permanent wyvern = addCreatureReady(player1, new ThunderclapWyvern());

        assertThat(gqs.getEffectivePower(gd, wyvern)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, wyvern)).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not boost a ground creature you control")
    void doesNotBoostGroundCreature() {
        addCreatureReady(player1, new ThunderclapWyvern());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not boost an opponent's flier")
    void doesNotBoostOpponentFlier() {
        addCreatureReady(player1, new ThunderclapWyvern());
        Permanent opponentHawk = addCreatureReady(player2, new SuntailHawk());

        assertThat(gqs.getEffectivePower(gd, opponentHawk)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, opponentHawk)).isEqualTo(1);
    }

    @Test
    @DisplayName("Two Wyverns boost each other")
    void twoWyvernsBoostEachOther() {
        Permanent first = addCreatureReady(player1, new ThunderclapWyvern());
        Permanent second = addCreatureReady(player1, new ThunderclapWyvern());

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(3);
    }

    @Test
    @DisplayName("Flash permits casting during the opponent's upkeep and the bonus begins on resolution")
    void flashDuringOpponentsUpkeep() {
        Permanent first = addCreatureReady(player1, new ThunderclapWyvern());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);

        harness.castFromHand(player1, new ThunderclapWyvern(), "{2}{W}{U}");

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(3);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Thunderclap Wyvern")).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(4);
    }

    @Test
    @DisplayName("Stacked bonuses end independently when a Wyvern leaves the battlefield")
    void bonusesEndWhenSourceLeaves() {
        Permanent first = addCreatureReady(player1, new ThunderclapWyvern());
        Permanent second = addCreatureReady(player1, new ThunderclapWyvern());
        Permanent third = addCreatureReady(player1, new ThunderclapWyvern());

        assertThat(gqs.getEffectivePower(gd, third)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, third)).isEqualTo(5);

        harness.setHand(player1, List.of(new Disperse(), new Disperse()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castInstant(player1, 0, first.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, third)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, third)).isEqualTo(4);

        harness.castInstant(player1, 0, second.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, third)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, third)).isEqualTo(3);
        assertThat(countPermanents(player1, "Thunderclap Wyvern")).isEqualTo(1);
    }
}
