package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(BronzeWalrus.class)
class BronzeWalrusTest extends BaseCardTest {

    @Test
    @DisplayName("When Bronze Walrus enters, its controller scries 2")
    void scriesTwoOnEnter() {
        Card topCard = new BronzeWalrus();
        Card bottomCard = new BronzeWalrus();
        harness.setLibrary(player1, List.of(topCard, bottomCard));
        harness.setHand(player1, List.of(new BronzeWalrus()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry).isNotNull();
        assertThat(scry.cards()).containsExactly(topCard, bottomCard);

        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(1), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(bottomCard, topCard);
        harness.assertOnBattlefield(player1, "Bronze Walrus");
    }

    @Test
    @DisplayName("Taps for one mana of any color")
    void tapsForAnyColor() {
        Permanent walrus = addCreatureReady(player1, new BronzeWalrus());

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, ManaColor.GREEN.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(walrus.isTapped()).isTrue();
    }
}
