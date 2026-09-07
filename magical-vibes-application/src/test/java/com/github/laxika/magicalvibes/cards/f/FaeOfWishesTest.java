package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.Granted;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FaeOfWishes.class, Granted.class, GrizzlyBears.class, Island.class, Mountain.class})
class FaeOfWishesTest extends BaseCardTest {

    @Test
    void adventureRevealsAndPutsNoncreatureSideboardCardIntoHand() {
        Card chosen = new Island();
        Card ineligible = new GrizzlyBears();
        gd.playerSideboards.put(player1.getId(), new ArrayList<>(List.of(chosen, ineligible)));
        FaeOfWishes fae = new FaeOfWishes();
        harness.setHand(player1, List.of(fae));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAdventure(player1, 0, List.of());
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().reveals()).isTrue();
        assertThat(search.params().cards()).containsExactly(chosen);

        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(chosen);
        assertThat(gd.playerSideboards.get(player1.getId())).containsExactly(ineligible);
        assertThat(gd.findExiledCard(fae.getId())).isNotNull();
        assertThat(gd.exilePlayPermissions.get(fae.getId())).isEqualTo(player1.getId());
    }

    @Test
    void activatedAbilityDiscardsTwoCardsAndReturnsFaeToHand() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        FaeOfWishes fae = new FaeOfWishes();
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, fae);
        harness.setHand(player1, List.of(new GrizzlyBears(), new Mountain()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(permanent);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(fae);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
    }
}
