package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.c.ColossalDreadmaw;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WicksPatrol.class, AirElemental.class, ColossalDreadmaw.class, Forest.class})
class WicksPatrolTest extends BaseCardTest {

    @Test
    @DisplayName("Milling three cards creates a reflexive targeted -X/-X ability")
    void millsThreeThenDebuffsAnOpponentsCreature() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new ColossalDreadmaw());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new ColossalDreadmaw());
        harness.setGraveyard(player1, List.of(new AirElemental()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));

        castAndResolveWicks();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, opponentCreature.getId());
        harness.passBothPriorities();

        assertThat(ownCreature.getEffectivePower()).isEqualTo(6);
        assertThat(ownCreature.getEffectiveToughness()).isEqualTo(6);
        assertThat(opponentCreature.getEffectivePower()).isEqualTo(1);
        assertThat(opponentCreature.getEffectiveToughness()).isEqualTo(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(4);
    }

    @Test
    @DisplayName("A library with fewer than three cards does not create the reflexive ability")
    void doesNotDebuffWhenThreeCardsCannotBeMilled() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new ColossalDreadmaw());
        harness.setGraveyard(player1, List.of(new AirElemental()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));

        castAndResolveWicks();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(opponentCreature.getEffectivePower()).isEqualTo(6);
        assertThat(opponentCreature.getEffectiveToughness()).isEqualTo(6);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
    }

    private void castAndResolveWicks() {
        harness.setHand(player1, List.of(new WicksPatrol()));
        harness.addMana(player1, ManaColor.BLACK, 6);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.passBothPriorities();
    }
}
