package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({YouFindSomePrisoners.class, FountainOfYouth.class, LlanowarElves.class, Forest.class})
class YouFindSomePrisonersTest extends BaseCardTest {

    @Test
    void breakTheirChainsDestroysAnArtifact() {
        FountainOfYouth artifact = new FountainOfYouth();
        var permanent = harness.addToBattlefieldAndReturn(player2, artifact);
        harness.setHand(player1, List.of(new YouFindSomePrisoners()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castModalInstant(player1, 0, 0, List.of(permanent.getId()));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Fountain of Youth");
        harness.assertInGraveyard(player2, "Fountain of Youth");
    }

    @Test
    void interrogateThemExilesThreeCardsAndLetsTheChosenCardBeCastWithAnyColor() {
        LlanowarElves chosen = new LlanowarElves();
        Card second = new Forest();
        Card third = new Forest();
        harness.setLibrary(player2, List.of(chosen, second, third));
        harness.setHand(player1, List.of(new YouFindSomePrisoners()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castModalInstant(player1, 0, 1, List.of(player2.getId()));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ExiledCardMayPlayChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(chosen, second, third);
        assertThat(gd.exilePlayPermissions).containsEntry(chosen.getId(), player1.getId());
        assertThat(gd.exilePlayPermissionsExpireAtTurnEnd).containsKey(chosen.getId());
        assertThat(gd.exilePlayAnyManaType).contains(chosen.getId());

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castFromExile(player1, chosen.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Llanowar Elves");
    }

    @Test
    void breakTheirChainsRequiresAnArtifactTarget() {
        LlanowarElves creature = new LlanowarElves();
        harness.addToBattlefield(player2, creature);
        harness.setHand(player1, List.of(new YouFindSomePrisoners()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castModalInstant(player1, 0, 0, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void interrogateThemRequiresAnOpponentTarget() {
        harness.setHand(player1, List.of(new YouFindSomePrisoners()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castModalInstant(player1, 0, 1, List.of(player1.getId())))
                .isInstanceOf(IllegalStateException.class);
    }
}
