package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.p.PithingNeedle;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RiftSower.class, PithingNeedle.class})
class RiftSowerTest extends BaseCardTest {

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"WHITE", "BLUE", "BLACK", "RED", "GREEN"})
    void tappingAddsTheChosenColor(ManaColor color) {
        Permanent riftSower = addCreatureReady(player1, new RiftSower());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, color.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isEqualTo(1);
        assertThat(riftSower.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void normallyEnteredCreatureCannotTapForManaWhileSummoningSick() {
        harness.addToBattlefield(player1, new RiftSower());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void pithingNeedleDoesNotPreventTheSuspendSpecialAction() {
        harness.castFromHand(player1, new PithingNeedle(), "{1}");
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Rift Sower");
        RiftSower card = new RiftSower();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateHandAbility(player1, 0, null);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 2);
        assertThat(gd.stack).isEmpty();
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
        assertThat(gd.stack).isEmpty();

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 1);
        harness.assertNotOnBattlefield(player1, "Rift Sower");
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        var permanent = findPermanent(player1, "Rift Sower");
        assertThat(gqs.hasKeyword(gd, permanent, Keyword.HASTE)).isTrue();
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(card);
        assertThat(gd.exiledCardTimeCounters).doesNotContainKey(card.getId());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "GREEN");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(permanent.isTapped()).isTrue();
    }

    @Test
    void decliningSuspendCastLeavesCardExiledWithoutAnotherUpkeepOffer() {
        RiftSower card = new RiftSower();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.activateHandAbility(player1, 0, null);

        advanceToUpkeep(player2);
        harness.passBothPriorities();
        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 2);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
        assertThat(gd.exiledCardTimeCounters).doesNotContainKey(card.getId());
        harness.assertNotOnBattlefield(player1, "Rift Sower");

        advanceToUpkeep(player1);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
    }
}
