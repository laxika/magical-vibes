package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PhotonLadyOfLight.class, GrizzlyBears.class})
class PhotonLadyOfLightTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking flickers another creature you control")
    void attackingFlickersAnotherCreatureYouControl() {
        Permanent photon = addCreatureReady(player1, new PhotonLadyOfLight());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposingBear = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(0));

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validPermanentIds())
                .containsExactly(bear.getId())
                .doesNotContain(photon.getId(), opposingBear.getId());

        harness.handlePermanentChosen(player1, bear.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(bear.getId()));
        assertThat(findPermanent(player1, "Grizzly Bears").getId()).isNotEqualTo(bear.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(photon);
    }

    @Test
    @DisplayName("The attack trigger may choose no target")
    void mayChooseNoTarget() {
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new PhotonLadyOfLight());

        declareAttackers(List.of(1));
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(bear);
    }
}
