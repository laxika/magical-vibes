package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BloodForBonesTest extends BaseCardTest {

    @Test
    void sacrificesACreatureAndReturnsAnotherCreatureToBattlefieldAndHand() {
        Permanent sacrifice = new Permanent(new LlanowarElves());
        gd.playerBattlefields.get(player1.getId()).add(sacrifice);
        Card battlefieldCard = new GrizzlyBears();
        Card handCard = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(battlefieldCard, handCard, new Shock()));
        harness.setHand(player1, List.of(new BloodForBones()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorceryWithSacrifice(player1, 0, sacrifice.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(sacrifice.getCard());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class)).isNotNull();

        harness.handleGraveyardCardChosen(player1, 0);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class)).isNotNull();
        harness.handleGraveyardCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(battlefieldCard.getId()));
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(handCard.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(battlefieldCard.getId())
                        || card.getId().equals(handCard.getId()));
    }

    @Test
    void cannotCastWithoutAcreatureToSacrifice() {
        harness.setHand(player1, List.of(new BloodForBones()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        assertThatThrownBy(() -> harness.castSorceryWithSacrifice(player1, 0, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sacrifice");
    }
}
