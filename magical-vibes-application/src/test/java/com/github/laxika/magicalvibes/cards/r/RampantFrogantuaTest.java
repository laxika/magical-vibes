package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RampantFrogantua.class, Forest.class, GrizzlyBears.class})
class RampantFrogantuaTest extends BaseCardTest {

    @Test
    @DisplayName("gets +10/+10 for each player who has lost the game")
    void getsBonusForPlayersWhoLostTheGame() {
        Permanent frogantua = harness.addToBattlefieldAndReturn(player1, new RampantFrogantua());

        assertThat(gqs.getEffectivePower(gd, frogantua)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, frogantua)).isEqualTo(3);

        gd.playersWhoLostGameThisMatch.add(player1.getId());
        gd.playersWhoLostGameThisMatch.add(player2.getId());

        assertThat(gqs.getEffectivePower(gd, frogantua)).isEqualTo(23);
        assertThat(gqs.getEffectiveToughness(gd, frogantua)).isEqualTo(23);
    }

    @Test
    @DisplayName("may mill combat damage and put any number of milled lands onto the battlefield tapped")
    void millsCombatDamageAndPutsChosenMilledLandsTapped() {
        Permanent frogantua = addAttackingFrogantua();
        Card firstLand = new Forest();
        Card nonland = new GrizzlyBears();
        Card secondLand = new Forest();
        harness.setLibrary(player1, List.of(firstLand, nonland, secondLand));

        resolveCombat();
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);
        PendingInteraction.MultiGraveyardChoice choice = gd.interaction
                .activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(firstLand.getId(), secondLand.getId());
        harness.handleMultipleCardsChosen(player1, List.of(firstLand.getId()));
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Forest")).singleElement().satisfies(land ->
                assertThat(land.isTapped()).isTrue());
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactlyInAnyOrder(nonland, secondLand);
    }

    @Test
    @DisplayName("may decline to mill")
    void mayDeclineToMill() {
        addAttackingFrogantua();
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));

        resolveCombat();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    private Permanent addAttackingFrogantua() {
        Permanent frogantua = harness.addToBattlefieldAndReturn(player1, new RampantFrogantua());
        frogantua.setSummoningSick(false);
        frogantua.setAttacking(true);
        return frogantua;
    }
}
