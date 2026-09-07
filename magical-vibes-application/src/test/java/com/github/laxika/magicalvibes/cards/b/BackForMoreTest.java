package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BackForMore.class, GrizzlyBears.class, LlanowarElves.class})
class BackForMoreTest extends BaseCardTest {

    @Test
    void returnsCreatureThenFightsChosenOpponentCreature() {
        GrizzlyBears returnedCard = new GrizzlyBears();
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());
        prepareCast(returnedCard);

        harness.castInstant(player1, 0, returnedCard.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, opposingCreature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(returnedCard.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(opposingCreature);
    }

    @Test
    void mayChooseNoCreatureForTheReflexiveFight() {
        GrizzlyBears returnedCard = new GrizzlyBears();
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());
        prepareCast(returnedCard);

        harness.castInstant(player1, 0, returnedCard.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(returnedCard.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opposingCreature);
    }

    @Test
    void reflexiveFightCannotTargetYourOwnCreature() {
        GrizzlyBears returnedCard = new GrizzlyBears();
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new LlanowarElves());
        prepareCast(returnedCard);

        harness.castInstant(player1, 0, returnedCard.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .doesNotContain(ownCreature.getId());
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, ownCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void prepareCast(GrizzlyBears returnedCard) {
        harness.setGraveyard(player1, List.of(returnedCard));
        harness.setHand(player1, List.of(new BackForMore()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }
}
