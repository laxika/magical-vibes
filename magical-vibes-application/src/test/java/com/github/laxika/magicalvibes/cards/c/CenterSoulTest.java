package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.action.ReboundAtNextUpkeep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CenterSoul.class, GrizzlyBears.class})
class CenterSoulTest extends BaseCardTest {

    @Test
    void protectsTargetCreatureAndExilesForRebound() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        CenterSoul card = new CenterSoul();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castInstant(player1, 0, bear.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "RED");

        assertThat(bear.getProtectionFromColorsUntilEndOfTurn()).contains(CardColor.RED);
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
        assertThat(gd.delayedActions).anyMatch(action -> action instanceof ReboundAtNextUpkeep);
    }

    @Test
    void reboundOffersAFreeCastAtNextUpkeep() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        CenterSoul card = new CenterSoul();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castInstant(player1, 0, bear.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "RED");
        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.ExileCastSpellTarget.class);
        harness.handlePermanentChosen(player1, bear.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.findExiledCard(card.getId())).isNull();
        assertThat(bear.getProtectionFromColorsUntilEndOfTurn()).contains(CardColor.BLUE);
        harness.assertInGraveyard(player1, "Center Soul");
        assertThat(gd.delayedActions).noneMatch(action -> action instanceof ReboundAtNextUpkeep);
    }

    @Test
    void cannotTargetAnOpponentsCreature() {
        Permanent opponentBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new CenterSoul()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, opponentBear.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
