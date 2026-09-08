package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RiftmarkedKnight.class})
class RiftmarkedKnightTest extends BaseCardTest {

    @Test
    void suspendExilesRiftmarkedKnightWithThreeTimeCounters() {
        RiftmarkedKnight card = suspendCard();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 3);
    }

    @Test
    void knightHasProtectionFromBlack() {
        Permanent knight = harness.addToBattlefieldAndReturn(player1, new RiftmarkedKnight());

        assertThat(gqs.hasProtectionFrom(gd, knight, CardColor.BLACK)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, knight, CardColor.WHITE)).isFalse();
    }

    @Test
    void lastTimeCounterCreatesHastyFlankingKnightWithProtectionFromWhite() {
        suspendCard();

        for (int i = 0; i < 2; i++) {
            advanceToUpkeep(player1);
            harness.passBothPriorities();
        }
        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getName().equals("Knight"))
                .findFirst()
                .orElseThrow();
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, token, Keyword.FLANKING)).isTrue();
        assertThat(gqs.hasKeyword(gd, token, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, token, CardColor.WHITE)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, token, CardColor.BLACK)).isFalse();
    }

    private RiftmarkedKnight suspendCard() {
        RiftmarkedKnight card = new RiftmarkedKnight();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateHandAbility(player1, 0, null);
        return card;
    }
}
