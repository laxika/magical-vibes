package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HealingSalve;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MartyrOfAshesTest extends BaseCardTest {

    @Test
    @DisplayName("Reveals red cards, sacrifices itself, and damages each creature without flying")
    void revealsRedCardsAndDamagesNonFlyers() {
        LightningBolt firstRedCard = new LightningBolt();
        RagingGoblin secondRedCard = new RagingGoblin();
        harness.setHand(player1, List.of(firstRedCard, secondRedCard));
        Permanent martyr = addCreatureReady(player1, new MartyrOfAshes());
        Permanent ownNonFlyer = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposingNonFlyer = addCreatureReady(player2, new GrizzlyBears());
        Permanent opposingFlyer = addCreatureReady(player2, new AirElemental());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 2, null);

        PendingInteraction.RevealAnyNumberOfCardsFromHandChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.RevealAnyNumberOfCardsFromHandChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(firstRedCard.getId(), secondRedCard.getId());

        harness.handleMultipleCardsChosen(player1, List.of(firstRedCard.getId(), secondRedCard.getId()));
        harness.passBothPriorities();

        assertThat(ownNonFlyer.getMarkedDamage()).isEqualTo(2);
        assertThat(opposingNonFlyer.getMarkedDamage()).isEqualTo(2);
        assertThat(opposingFlyer.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(martyr);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(martyr.getCard());
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstRedCard, secondRedCard);
    }

    @Test
    @DisplayName("Cannot reveal more red cards than are in hand")
    void cannotRevealMoreRedCardsThanAreInHand() {
        harness.setHand(player1, List.of(new HealingSalve()));
        Permanent martyr = addCreatureReady(player1, new MartyrOfAshes());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(martyr);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
    }
}
