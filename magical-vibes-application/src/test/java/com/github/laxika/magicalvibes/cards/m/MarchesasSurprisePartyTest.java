package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MarchesasSurpriseParty.class, GrizzlyBears.class})
class MarchesasSurprisePartyTest extends BaseCardTest {

    @Test
    void rewardsAfterControllerCastsThreeSpellsAndCompletesMission() {
        Card mission = new MarchesasSurpriseParty();
        gd.playerCommandZones.get(player1.getId()).add(mission);
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        gd.recordSpellCast(player1.getId(), new GrizzlyBears());
        gd.recordSpellCast(player1.getId(), new GrizzlyBears());
        gd.recordSpellCast(player1.getId(), new GrizzlyBears());

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        harness.passUntil(TurnStep.END_STEP);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
        assertThat(gd.faceDownCommandZoneCards).contains(mission.getId());
    }

    @Test
    void doesNotTriggerWhenNoMissionConditionIsMet() {
        Card mission = new MarchesasSurpriseParty();
        gd.playerCommandZones.put(player1.getId(), new ArrayList<>(List.of(mission)));

        harness.passUntil(TurnStep.END_STEP);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }
}
