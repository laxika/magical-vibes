package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.cards.n.NuisanceEngine;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({IdolOfOblivion.class, NuisanceEngine.class})
class IdolOfOblivionTest extends BaseCardTest {

    @Test
    void cannotDrawBeforeCreatingToken() {
        harness.addToBattlefield(player1, new IdolOfOblivion());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("created a token this turn");
    }

    @Test
    void drawingAbilityWorksAfterCreatingToken() {
        harness.addToBattlefield(player1, new IdolOfOblivion());
        harness.addToBattlefield(player1, new NuisanceEngine());
        harness.setLibrary(player1, List.of(new IdolOfOblivion()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playersWhoCreatedTokensThisTurn).contains(player1.getId());
        int handSizeBeforeDraw = gd.playerHands.get(player1.getId()).size();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBeforeDraw + 1);
    }

    @Test
    void sacrificesItselfToCreateTenTenEldraziToken() {
        harness.addToBattlefield(player1, new IdolOfOblivion());
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() instanceof IdolOfOblivion);
        Permanent token = findPermanent(player1, "Eldrazi");
        assertThat(token.getEffectivePower()).isEqualTo(10);
        assertThat(token.getEffectiveToughness()).isEqualTo(10);
        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.ELDRAZI);
    }

    @Test
    void sacrificeIsPaidBeforeTokenAbilityResolves() {
        harness.addToBattlefield(player1, new IdolOfOblivion());
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.activateAbility(player1, 0, 1, null, null);

        harness.assertNotOnBattlefield(player1, "Idol of Oblivion");
        harness.assertInGraveyard(player1, "Idol of Oblivion");
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();

        harness.passBothPriorities();

        Permanent token = findPermanent(player1, "Eldrazi");
        assertThat(token.getCard().isToken()).isTrue();
        assertThat(token.getCard().getColors()).isEmpty();
        assertThat(token.isTapped()).isFalse();
        assertThat(gqs.isCreature(gd, token)).isTrue();
    }

    @Test
    void cannotDrawWhileTokenCreationIsStillOnStack() {
        harness.addToBattlefield(player1, new IdolOfOblivion());
        harness.addToBattlefield(player1, new IdolOfOblivion());
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.activateAbility(player1, 1, 1, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("created a token this turn");

        harness.passBothPriorities();
    }

    @Test
    void opponentsTokenDoesNotEnableDrawing() {
        harness.addToBattlefield(player1, new IdolOfOblivion());
        harness.addToBattlefield(player2, new IdolOfOblivion());
        harness.addMana(player2, ManaColor.COLORLESS, 8);

        harness.activateAbility(player2, 0, 1, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("created a token this turn");
    }

    @Test
    void canDrawAfterCreatedTokenLeavesBattlefield() {
        harness.addToBattlefield(player1, new IdolOfOblivion());
        harness.addToBattlefield(player1, new IdolOfOblivion());
        harness.setLibrary(player1, List.of(new IdolOfOblivion()));
        harness.addMana(player1, ManaColor.COLORLESS, 8);
        harness.activateAbility(player1, 1, 1, null, null);
        harness.passBothPriorities();
        Permanent token = findPermanent(player1, "Eldrazi");
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .sacrificePermanentToGraveyard(gd, token));
        harness.runStateBasedActions();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Idol of Oblivion");
        assertThat(findPermanent(player1, "Idol of Oblivion").isTapped()).isTrue();
    }

    @Test
    void tokenCreatedOnPreviousTurnDoesNotEnableDrawing() {
        harness.addToBattlefield(player1, new IdolOfOblivion());
        harness.addToBattlefield(player1, new IdolOfOblivion());
        harness.addMana(player1, ManaColor.COLORLESS, 8);
        harness.activateAbility(player1, 1, 1, null, null);
        harness.passBothPriorities();

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(findPermanent(player1, "Eldrazi").getCard().isToken()).isTrue();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("created a token this turn");
    }
}
