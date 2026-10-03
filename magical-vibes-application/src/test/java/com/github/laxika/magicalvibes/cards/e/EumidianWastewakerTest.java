package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EumidianWastewaker.class, Forest.class, GrizzlyBears.class})
class EumidianWastewakerTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking counts lands discarded or sacrificed and draws that many cards")
    void attackCountsLandsPutIntoGraveyards() {
        Permanent wastewaker = harness.addToBattlefieldAndReturn(player1, new EumidianWastewaker());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new Forest()));
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        harness.handleListChoice(player1,
                ChoiceContext.EachPlayerSacrificeOrDiscardChoice.DISCARD);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Grizzly Bears", "Grizzly Bears");
        harness.assertInGraveyard(player1, "Forest");
        harness.assertInGraveyard(player2, "Forest");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(wastewaker);
    }

    @Test
    @DisplayName("Encore creates a hasty attacking token and sacrifices it at the next end step")
    void encoreCreatesAndSacrificesToken() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(new EumidianWastewaker()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        Permanent token = findPermanent(player1, "Eumidian Wastewaker");
        assertThat(token.getCard().isToken()).isTrue();
        assertThat(gqs.hasKeyword(gd, token, com.github.laxika.magicalvibes.model.Keyword.HASTE)).isTrue();
        assertThat(token.isTapped()).isTrue();
        assertThat(token.isAttacking()).isTrue();
        assertThat(token.getAttackTarget()).isEqualTo(player2.getId());

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Eumidian Wastewaker")).isEmpty();
    }
}
