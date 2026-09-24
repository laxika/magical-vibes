package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.q.QasaliAmbusher;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.GameActionAvailabilityService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ConquerorsFlail.class, AirElemental.class, GrizzlyBears.class, QasaliAmbusher.class, Shock.class})
class ConquerorsFlailTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +1/+1 for each distinct color among permanents you control")
    void boostCountsDistinctControlledPermanentColors() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new AirElemental());
        addCreatureReady(player1, new QasaliAmbusher());
        Permanent flail = addFlailReady(player1);
        flail.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(5);
    }

    @Test
    @DisplayName("Attached Conqueror's Flail stops opponents from casting during its controller's turn")
    void attachedFlailRestrictsOpponentsDuringControllersTurn() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent flail = addFlailReady(player1);
        flail.setAttachedTo(creature.getId());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passPriority(player1);

        GameActionAvailabilityService availability = harness.getGameActionAvailabilityService();
        assertThat(availability.getPlayableCardIndices(gd, player2.getId())).isEmpty();
        assertThatThrownBy(() -> harness.castInstant(player2, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The Flail's restriction disappears when it is unattached")
    void unattachedFlailDoesNotRestrictOpponents() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent flail = addFlailReady(player1);
        flail.setAttachedTo(creature.getId());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passPriority(player1);

        flail.setAttachedTo(null);

        assertThat(harness.getGameActionAvailabilityService()
                .getPlayableCardIndices(gd, player2.getId())).contains(0);
    }

    @Test
    @DisplayName("Opponents can cast spells during their own turn")
    void opponentCanCastDuringOwnTurn() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent flail = addFlailReady(player1);
        flail.setAttachedTo(creature.getId());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThat(harness.getGameActionAvailabilityService()
                .getPlayableCardIndices(gd, player2.getId())).contains(0);
    }

    private Permanent addFlailReady(Player player) {
        Permanent permanent = new Permanent(new ConquerorsFlail());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
