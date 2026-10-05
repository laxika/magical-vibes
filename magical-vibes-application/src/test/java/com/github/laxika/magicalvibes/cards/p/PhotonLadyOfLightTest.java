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

    @Test
    @DisplayName("Flickering a stolen creature returns it to its owner")
    void stolenCreatureReturnsToOwner() {
        addCreatureReady(player1, new PhotonLadyOfLight());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        bear.getCard().setOwnerId(player2.getId());
        gd.stolenCreatures.put(bear.getId(), player2.getId());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, bear.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        Permanent returned = findPermanent(player2, "Grizzly Bears");
        assertThat(returned.getId()).isNotEqualTo(bear.getId());
        assertThat(returned.getCard().getOwnerId()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Flickering another attacker returns it untapped and outside combat")
    void flickeredAttackerLeavesCombat() {
        addCreatureReady(player1, new PhotonLadyOfLight());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0, 1));
        harness.handlePermanentChosen(player1, bear.getId());
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Grizzly Bears");
        assertThat(returned.getId()).isNotEqualTo(bear.getId());
        assertThat(returned.isTapped()).isFalse();
        assertThat(returned.isAttacking()).isFalse();
        assertThat(returned.isSummoningSick()).isTrue();
    }

    @Test
    @DisplayName("A target that changes controller before resolution is not flickered")
    void targetChangingControllerIsIllegal() {
        addCreatureReady(player1, new PhotonLadyOfLight());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, bear.getId());
        gd.playerBattlefields.get(player1.getId()).remove(bear);
        gd.playerBattlefields.get(player2.getId()).add(bear);
        gd.stolenCreatures.put(bear.getId(), player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(bear);
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }
}
