package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GrimBaubleTest extends BaseCardTest {

    @Test
    void etbGivesOpponentCreatureMinusTwoMinusTwo() {
        Permanent elemental = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.setHand(player1, List.of(new GrimBauble()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castArtifact(player1, 0, elemental.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, elemental)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, elemental)).isEqualTo(2);
    }

    @Test
    void etbCannotTargetCreatureYouControl() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new GrimBauble()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castArtifact(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature an opponent controls");
    }

    @Test
    void activatedAbilitySurveilsTwoAndSacrificesArtifact() {
        harness.addToBattlefield(player1, new GrimBauble());
        Card topCard = new GrizzlyBears();
        Card secondCard = new AirElemental();
        gd.playerDecks.get(player1.getId()).add(0, secondCard);
        gd.playerDecks.get(player1.getId()).add(0, topCard);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(topCard, secondCard);
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0, 1)));

        harness.assertNotOnBattlefield(player1, "Grim Bauble");
        harness.assertInGraveyard(player1, "Grim Bauble");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .contains(topCard, secondCard);
    }
}
