package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MarchesasSurpriseParty.class, GrizzlyBears.class})
class MarchesasSurprisePartyTest extends BaseCardTest {

    @Test
    void castingThreeSpellsDoesNotAutomaticallyTriggerAtBeginningOfEndStep() {
        Card mission = new MarchesasSurpriseParty();
        gd.playerCommandZones.get(player1.getId()).add(mission);
        for (int i = 0; i < 3; i++) {
            harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
            harness.passBothPriorities();
        }

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        harness.passUntil(TurnStep.END_STEP);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.faceDownCommandZoneCards).doesNotContain(mission.getId());
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
    }

    @Test
    void doesNotTriggerWhenNoMissionConditionIsMet() {
        Card mission = new MarchesasSurpriseParty();
        gd.playerCommandZones.get(player1.getId()).add(mission);

        harness.passUntil(TurnStep.END_STEP);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    void nineGraveyardCardsDoNotAutomaticallyTriggerAtBeginningOfEndStep() {
        Card mission = new MarchesasSurpriseParty();
        gd.playerCommandZones.get(player1.getId()).add(mission);
        harness.setGraveyard(player1, IntStream.range(0, 9)
                .mapToObj(i -> (Card) new GrizzlyBears()).toList());

        harness.passUntil(TurnStep.END_STEP);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.faceDownCommandZoneCards).doesNotContain(mission.getId());
    }

    @Test
    void completedMissionDoesNotTriggerAgain() {
        Card mission = new MarchesasSurpriseParty();
        gd.playerCommandZones.get(player1.getId()).add(mission);
        gd.faceDownCommandZoneCards.add(mission.getId());
        harness.setGraveyard(player1, IntStream.range(0, 9)
                .mapToObj(i -> (Card) new GrizzlyBears()).toList());

        harness.passUntil(TurnStep.END_STEP);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.faceDownCommandZoneCards).contains(mission.getId());
    }
}
