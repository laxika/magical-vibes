package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ShriekwoodDevourer.class, Forest.class, GrizzlyBears.class})
class ShriekwoodDevourerTest extends BaseCardTest {

    @Test
    @DisplayName("Untaps up to the greatest power among creatures that attacked")
    void untapsLandsUpToGreatestAttackingPower() {
        addCreatureReady(player1, new ShriekwoodDevourer());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent firstLand = addTappedLand(player1);
        Permanent secondLand = addTappedLand(player1);
        Permanent thirdLand = addTappedLand(player1);
        Permanent fourthLand = addTappedLand(player2);

        declareAttackers(List.of(1));
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.maxCount()).isEqualTo(2);
        assertThat(choice.validIds()).containsExactlyInAnyOrder(
                firstLand.getId(), secondLand.getId(), thirdLand.getId(), fourthLand.getId());
        assertThat(choice.validIds()).doesNotContain(attacker.getId());

        harness.handleMultiplePermanentsChosen(player1, List.of(firstLand.getId(), fourthLand.getId()));

        assertThat(firstLand.isTapped()).isFalse();
        assertThat(fourthLand.isTapped()).isFalse();
        assertThat(secondLand.isTapped()).isTrue();
        assertThat(thirdLand.isTapped()).isTrue();
    }

    private Permanent addTappedLand(com.github.laxika.magicalvibes.model.Player player) {
        Permanent land = harness.addToBattlefieldAndReturn(player, new Forest());
        land.tap();
        return land;
    }
}
