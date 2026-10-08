package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.SandAugury;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({YoungBlueDragon.class, SandAugury.class, Island.class})
class YoungBlueDragonTest extends BaseCardTest {

    @Test
    void adventureScriesDrawsAndExilesTheCard() {
        YoungBlueDragon card = new YoungBlueDragon();
        Island topCard = new Island();
        Island nextCard = new Island();
        harness.setHand(player1, List.of(card));
        harness.setLibrary(player1, List.of(topCard, nextCard));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAdventure(player1, 0, List.of());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0), List.of()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(topCard);
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
        assertThat(gd.exilePlayPermissions.get(card.getId())).isEqualTo(player1.getId());
    }

    @Test
    void adventureDrawsTheNextCardAfterBottomingTheScryCard() {
        YoungBlueDragon card = new YoungBlueDragon();
        Island topCard = new Island();
        Island nextCard = new Island();
        harness.setHand(player1, List.of(card));
        harness.setLibrary(player1, List.of(topCard, nextCard));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAdventure(player1, 0, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.findExiledCard(card.getId())).isNull();
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0)));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(nextCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
        assertThat(gd.exilePlayPermissions.get(card.getId())).isEqualTo(player1.getId());
    }

    @Test
    void creatureCanBeCastFromHandWithoutResolvingTheAdventure() {
        YoungBlueDragon card = new YoungBlueDragon();
        Island topCard = new Island();
        harness.setHand(player1, List.of(card));
        harness.setLibrary(player1, List.of(topCard));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Young Blue Dragon");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
        assertThat(gd.findExiledCard(card.getId())).isNull();
    }

    @Test
    void creatureFaceCanBeCastFromExileAfterAdventure() {
        YoungBlueDragon card = new YoungBlueDragon();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAdventure(player1, 0, List.of());
        harness.passBothPriorities();
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0)));
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castFromExile(player1, card.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getCard)
                .contains(card);
        assertThat(gd.findExiledCard(card.getId())).isNull();
    }
}
