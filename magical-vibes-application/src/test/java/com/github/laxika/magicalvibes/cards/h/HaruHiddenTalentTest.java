package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.e.EarthenAlly;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HaruHiddenTalent.class, EarthenAlly.class, Forest.class, GrizzlyBears.class})
class HaruHiddenTalentTest extends BaseCardTest {

    @Test
    void anotherAllyEnteringEarthbendsLandYouControl() {
        harness.addToBattlefield(player1, new HaruHiddenTalent());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());

        harness.enterBattlefieldAndReturn(player1, new EarthenAlly());
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(land.getId());
        harness.handlePermanentChosen(player1, land.getId());
        harness.passBothPriorities();

        assertThat(gqs.isLand(gd, land)).isTrue();
        assertThat(gqs.isCreature(gd, land)).isTrue();
        assertThat(gqs.getEffectivePower(gd, land)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, land)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, land, Keyword.HASTE)).isTrue();
        assertThat(land.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void nonAllyEnteringDoesNotTrigger() {
        harness.addToBattlefield(player1, new HaruHiddenTalent());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());

        harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gqs.isCreature(gd, land)).isFalse();
    }

    @Test
    void triggerCannotTargetOpponentLand() {
        harness.addToBattlefield(player1, new HaruHiddenTalent());
        Permanent ownLand = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent opponentLand = harness.addToBattlefieldAndReturn(player2, new Forest());

        harness.enterBattlefieldAndReturn(player1, new EarthenAlly());
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(ownLand.getId());
        assertThat(choice.validIds()).doesNotContain(opponentLand.getId());
    }
}
