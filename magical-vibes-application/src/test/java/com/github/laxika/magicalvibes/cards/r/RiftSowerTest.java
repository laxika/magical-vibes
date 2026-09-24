package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RiftSower.class})
class RiftSowerTest extends BaseCardTest {

    @Test
    void tappingAddsTheChosenColor() {
        Permanent riftSower = harness.addToBattlefieldAndReturn(player1, new RiftSower());
        riftSower.setSummoningSick(false);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
    }

    @Test
    void suspendExilesWithTwoTimeCountersAndFreeCastsWithHaste() {
        RiftSower card = new RiftSower();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.activateHandAbility(player1, 0, null);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 2);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        var permanent = findPermanent(player1, "Rift Sower");
        assertThat(gqs.hasKeyword(gd, permanent, Keyword.HASTE)).isTrue();
    }
}
