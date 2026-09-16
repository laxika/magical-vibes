package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.c.CloudSprite;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EnslavedHorror.class, CloudSprite.class, Island.class})
class EnslavedHorrorTest extends BaseCardTest {

    @Test
    void eachOtherPlayerMayReturnCreatureFromTheirGraveyard() {
        Card creature = new CloudSprite();
        harness.setGraveyard(player1, List.of(new CloudSprite()));
        harness.setGraveyard(player2, List.of(creature, new Island()));
        castEnslavedHorror();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class))
                .isNotNull();
        harness.handleGraveyardCardChosen(player2, 0);

        Permanent returned = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(creature.getId()))
                .findFirst()
                .orElseThrow();
        assertThat(returned.getCard()).isSameAs(creature);
        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(
                permanent -> permanent.getCard().getId().equals(creature.getId()));
        harness.assertInGraveyard(player1, "Cloud Sprite");
        harness.assertInGraveyard(player2, "Island");
    }

    @Test
    void returnsAtMostOneCreature() {
        Card first = new CloudSprite();
        Card second = new CloudSprite();
        harness.setGraveyard(player2, List.of(first, second));
        castEnslavedHorror();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class))
                .isNotNull();
        harness.handleGraveyardCardChosen(player2, 1);

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(Permanent::getCard)
                .containsExactly(second);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(first);
    }

    @Test
    void decliningLeavesOpponentCreatureInGraveyard() {
        Card creature = new CloudSprite();
        harness.setGraveyard(player2, List.of(creature));
        castEnslavedHorror();

        harness.handleGraveyardCardChosen(player2, -1);

        harness.assertInGraveyard(player2, "Cloud Sprite");
        harness.assertNotOnBattlefield(player2, "Cloud Sprite");
    }

    @Test
    void doesNotOfferControllerOrNoncreatureCards() {
        harness.setGraveyard(player1, List.of(new CloudSprite()));
        harness.setGraveyard(player2, List.of(new Island()));
        castEnslavedHorror();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class))
                .isNull();
        harness.assertInGraveyard(player1, "Cloud Sprite");
        harness.assertInGraveyard(player2, "Island");
    }

    private void castEnslavedHorror() {
        harness.setHand(player1, List.of(new EnslavedHorror()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
