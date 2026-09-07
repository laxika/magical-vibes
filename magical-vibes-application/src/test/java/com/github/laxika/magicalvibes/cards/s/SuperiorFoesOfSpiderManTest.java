package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SuperiorFoesOfSpiderMan.class, Forest.class, HillGiant.class, GrizzlyBears.class})
class SuperiorFoesOfSpiderManTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a spell with mana value 4 or greater may exile the top card and play it")
    void qualifyingSpellExilesTopCardWithPlayPermission() {
        Forest topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));
        harness.addToBattlefield(player1, new SuperiorFoesOfSpiderMan());
        harness.setHand(player1, List.of(new HillGiant()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castCreature(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(topCard);
        assertThat(gd.exilePlayPermissions).containsEntry(topCard.getId(), player1.getId());
    }

    @Test
    @DisplayName("Declining the optional exile leaves the top card in the library")
    void decliningExileLeavesTopCardInLibrary() {
        Forest topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));
        harness.addToBattlefield(player1, new SuperiorFoesOfSpiderMan());
        harness.setHand(player1, List.of(new HillGiant()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castCreature(player1, 0);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(topCard);
    }

    @Test
    @DisplayName("Casting a spell with mana value less than 4 does not trigger the ability")
    void lowManaValueSpellDoesNotTrigger() {
        Forest topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));
        harness.addToBattlefield(player1, new SuperiorFoesOfSpiderMan());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castCreature(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(topCard);
    }
}
