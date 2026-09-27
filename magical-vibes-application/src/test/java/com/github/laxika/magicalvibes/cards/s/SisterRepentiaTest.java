package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SisterRepentia.class, LightningBolt.class})
class SisterRepentiaTest extends BaseCardTest {

    @Test
    @DisplayName("When Sister Repentia dies, its controller gains 2 life and draws two cards")
    void diesGainsLifeAndDrawsTwoCards() {
        Permanent sister = harness.addToBattlefieldAndReturn(player1, new SisterRepentia());
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);

        int lifeBefore = gd.getLife(player1.getId());
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.castInstant(player2, 0, sister.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 2);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 2);
        harness.assertInGraveyard(player1, "Sister Repentia");
    }

    @Test
    @DisplayName("Drawing Sister Repentia as the first card of the turn offers its miracle cost")
    void firstDrawOffersMiracleCast() {
        SisterRepentia sister = new SisterRepentia();
        harness.setLibrary(player1, List.of(sister));

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        harness.inMutationScope(() -> harness.getPlayerInputService().processNextMayAbility(gd));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(sister.getId()));
    }
}
