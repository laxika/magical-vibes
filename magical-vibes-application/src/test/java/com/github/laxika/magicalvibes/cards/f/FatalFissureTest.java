package com.github.laxika.magicalvibes.cards.f;

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

@CardUsed({FatalFissure.class, Forest.class, GrizzlyBears.class})
class FatalFissureTest extends BaseCardTest {

    @Test
    @DisplayName("Earthbends a land controlled by Fatal Fissure's caster when the target dies")
    void earthbendsCasterLandWhenTargetDies() {
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent casterLand = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent opponentLand = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new FatalFissure()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, victim.getId());
        harness.passBothPriorities();

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, victim));
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validPermanentIds())
                .contains(casterLand.getId())
                .doesNotContain(opponentLand.getId());

        harness.handlePermanentChosen(player1, casterLand.getId());
        harness.passBothPriorities();

        assertThat(gqs.isLand(gd, casterLand)).isTrue();
        assertThat(gqs.isCreature(gd, casterLand)).isTrue();
        assertThat(gqs.getEffectivePower(gd, casterLand)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, casterLand)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, casterLand, Keyword.HASTE)).isTrue();
        assertThat(casterLand.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }
}
