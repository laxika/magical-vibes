package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.h.HearthKami;
import com.github.laxika.magicalvibes.cards.l.LanternKami;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class KodamaOfTheCenterTreeTest extends BaseCardTest {

    @Test
    @DisplayName("Power and toughness equal the number of Spirits controlled")
    void powerAndToughnessTrackControlledSpirits() {
        Permanent kodama = harness.addToBattlefieldAndReturn(player1, new KodamaOfTheCenterTree());
        assertThat(gqs.getEffectivePower(gd, kodama)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, kodama)).isEqualTo(1);

        harness.addToBattlefield(player1, new LanternKami());

        assertThat(gqs.getEffectivePower(gd, kodama)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, kodama)).isEqualTo(2);
    }

    @Test
    @DisplayName("Soulshift X includes Kodama and returns a Spirit within the value fixed at death")
    void soulshiftUsesSpiritCountIncludingKodama() {
        Permanent kodama = harness.addToBattlefieldAndReturn(player1, new KodamaOfTheCenterTree());
        harness.addToBattlefield(player1, new LanternKami());
        Card eligibleSpirit = new HearthKami();
        harness.setGraveyard(player1, List.of(eligibleSpirit));

        harness.setHand(player1, List.of(new Murder()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castInstant(player1, 0, kodama.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).contains(eligibleSpirit.getId());

        harness.handleMultipleCardsChosen(player1, List.of(eligibleSpirit.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Hearth Kami");
    }
}
