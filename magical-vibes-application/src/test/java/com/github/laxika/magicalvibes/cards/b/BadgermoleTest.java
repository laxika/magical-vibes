package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Badgermole.class, Forest.class, GrizzlyBears.class})
class BadgermoleTest extends BaseCardTest {

    @Test
    @DisplayName("Enters by earthbending a land you control")
    void earthbendsLandOnEntry() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        castBadgermole(land.getId());

        assertThat(gqs.isLand(gd, land)).isTrue();
        assertThat(gqs.isCreature(gd, land)).isTrue();
        assertThat(gqs.getEffectivePower(gd, land)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, land)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, land, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, land, Keyword.TRAMPLE)).isTrue();
        assertThat(land.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Creatures you control with +1/+1 counters have trample")
    void counteredControlledCreaturesHaveTrample() {
        harness.addToBattlefield(player1, new Badgermole());
        Permanent counteredCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent uncounteredCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        counteredCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        assertThat(gqs.hasKeyword(gd, counteredCreature, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, uncounteredCreature, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Cannot earthbend a land controlled by an opponent")
    void cannotTargetOpponentsLand() {
        Permanent ownLand = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent opponentLand = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new Badgermole()));
        addMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(ownLand.getId())
                .doesNotContain(opponentLand.getId());

        harness.handlePermanentChosen(player1, ownLand.getId());
        harness.passBothPriorities();
    }

    private void castBadgermole(java.util.UUID targetId) {
        harness.setHand(player1, List.of(new Badgermole()));
        addMana();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, targetId);
        harness.passBothPriorities();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }
}
