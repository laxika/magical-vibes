package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Shock;
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

@CardUsed({ElementalTeachings.class, Forest.class, Island.class, Mountain.class, Plains.class, Shock.class})
class ElementalTeachingsTest extends BaseCardTest {

    private void castElementalTeachings(List<Card> library) {
        harness.setLibrary(player1, library);
        harness.setHand(player1, List.of(new ElementalTeachings()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();
    }

    private List<String> offeredNames() {
        return gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().cards().stream().map(Card::getName).toList();
    }

    private void pickFromLibrary(String name) {
        int index = offeredNames().indexOf(name);
        assertThat(index).isGreaterThanOrEqualTo(0);
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.LibraryCardChosen(index));
    }

    @Test
    void opponentChoosesTwoForGraveyardAndRestEnterTapped() {
        Card island = new Island();
        Card forest = new Forest();
        Card mountain = new Mountain();
        Card plains = new Plains();
        Card shock = new Shock();
        castElementalTeachings(List.of(island, forest, mountain, plains, shock));

        assertThat(offeredNames()).containsExactlyInAnyOrder("Island", "Forest", "Mountain", "Plains");
        pickFromLibrary("Island");
        pickFromLibrary("Forest");
        pickFromLibrary("Mountain");
        pickFromLibrary("Plains");

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.minCount()).isEqualTo(2);
        assertThat(choice.maxCount()).isEqualTo(2);

        harness.handleMultipleCardsChosen(player2, List.of(forest.getId(), plains.getId()));

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(forest, plains);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == island && permanent.isTapped())
                .anyMatch(permanent -> permanent.getCard() == mountain && permanent.isTapped());
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() == forest || permanent.getCard() == plains);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(shock);
        harness.assertInGraveyard(player1, "Elemental Teachings");
    }

    @Test
    void findingOneLandPutsItIntoTheGraveyard() {
        Card island = new Island();
        castElementalTeachings(List.of(island, new Shock()));

        pickFromLibrary("Island");

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.minCount()).isEqualTo(1);
        assertThat(choice.maxCount()).isEqualTo(1);
        harness.handleMultipleCardsChosen(player2, List.of(island.getId()));

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(island);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() == island);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
