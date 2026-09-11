package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.t.TakeAGlance;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BilboBagginsBurglar.class, TakeAGlance.class})
class BilboBagginsBurglarTest extends BaseCardTest {

    @Test
    void adventureScriesTwoAndExilesTheCard() {
        BilboBagginsBurglar card = new BilboBagginsBurglar();
        Card first = new BilboBagginsBurglar();
        Card second = new TakeAGlance();
        harness.setHand(player1, List.of(card));
        harness.setLibrary(player1, List.of(first, second));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAdventure(player1, 0, List.of());
        harness.passBothPriorities();

        PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry).isNotNull();
        assertThat(scry.cards()).containsExactly(first, second);

        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(1), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second, first);
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
        assertThat(gd.exilePlayPermissions.get(card.getId())).isEqualTo(player1.getId());
    }

    @Test
    void creatureFaceDrawsACardWhenItEnters() {
        BilboBagginsBurglar card = new BilboBagginsBurglar();
        Card drawn = new TakeAGlance();
        harness.setHand(player1, List.of(card));
        harness.setLibrary(player1, List.of(drawn));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInHand(player1, "Take a Glance");
        harness.assertOnBattlefield(player1, "Bilbo Baggins, Burglar");
    }

    @Test
    void creatureFaceCanBeCastFromExileAfterAdventure() {
        BilboBagginsBurglar card = new BilboBagginsBurglar();
        harness.setHand(player1, List.of(card));
        harness.setLibrary(player1, List.of(new TakeAGlance()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAdventure(player1, 0, List.of());
        harness.passBothPriorities();
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castFromExile(player1, card.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Bilbo Baggins, Burglar");
        harness.assertInHand(player1, "Take a Glance");
        assertThat(gd.findExiledCard(card.getId())).isNull();
    }
}
