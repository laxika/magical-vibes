package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DigUpTheBody.class, Forest.class, GrizzlyBears.class})
class DigUpTheBodyTest extends BaseCardTest {

    @Test
    @DisplayName("Mills two cards, then may return any creature card from the graveyard")
    void millsThenReturnsCreatureFromGraveyard() {
        Card creatureAlreadyInGraveyard = new GrizzlyBears();
        Card milledOne = new Forest();
        Card milledTwo = new Forest();
        Card spell = new DigUpTheBody();
        harness.setGraveyard(player1, List.of(creatureAlreadyInGraveyard));
        harness.setLibrary(player1, List.of(milledOne, milledTwo));
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(
                creatureAlreadyInGraveyard, milledOne, milledTwo);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.GraveyardChoice.class);
        harness.handleGraveyardCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(creatureAlreadyInGraveyard);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(milledOne, milledTwo, spell);
    }

    @Test
    @DisplayName("Declining the return still mills two cards")
    void decliningReturnStillMills() {
        Card milledOne = new Forest();
        Card milledTwo = new Forest();
        Card spell = new DigUpTheBody();
        harness.setLibrary(player1, List.of(milledOne, milledTwo));
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(milledOne, milledTwo, spell);
    }

    @Test
    @DisplayName("Casualty copies the spell and sacrifices the chosen creature")
    void casualtyCopiesSpell() {
        Permanent casualtyCreature = addCreatureReady(player1, new GrizzlyBears());
        Card milledOne = new Forest();
        Card milledTwo = new Forest();
        Card milledThree = new Forest();
        Card milledFour = new Forest();
        Card spell = new DigUpTheBody();
        harness.setLibrary(player1, List.of(milledOne, milledTwo, milledThree, milledFour));
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castInstantWithSacrifice(player1, 0, null, casualtyCreature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(
                casualtyCreature.getCard(), milledOne, milledTwo, milledThree, milledFour, spell);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(casualtyCreature.getId()));
    }
}
