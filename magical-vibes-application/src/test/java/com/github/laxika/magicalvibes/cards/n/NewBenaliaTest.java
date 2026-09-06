package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NewBenalia.class, GrizzlyBears.class})
class NewBenaliaTest extends BaseCardTest {

    @Test
    void entersTapped() {
        harness.setHand(player1, List.of(new NewBenalia()));

        harness.playLand(player1, 0);

        Permanent newBenalia = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(newBenalia.isTapped()).isTrue();
    }

    @Test
    void scriesOneOnEntering() {
        Card topCard = new GrizzlyBears();
        Card bottomCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard, bottomCard));
        harness.setHand(player1, List.of(new NewBenalia()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry).isNotNull();
        assertThat(scry.cards()).containsExactly(topCard);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(bottomCard, topCard);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void tappingAddsWhiteMana() {
        Permanent newBenalia = harness.addToBattlefieldAndReturn(player1, new NewBenalia());
        newBenalia.setSummoningSick(false);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(newBenalia.isTapped()).isTrue();
    }
}
