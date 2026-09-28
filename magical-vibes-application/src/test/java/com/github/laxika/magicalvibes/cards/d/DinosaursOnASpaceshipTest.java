package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DinosaursOnASpaceship.class, DrowsingTyrannodon.class, GrizzlyBears.class})
class DinosaursOnASpaceshipTest extends BaseCardTest {

    @Test
    void boostsOtherDinosaursAndGrantsThemVigilanceAndTrample() {
        harness.addToBattlefield(player1, new DinosaursOnASpaceship());
        Permanent dinosaur = harness.addToBattlefieldAndReturn(player1, new DrowsingTyrannodon());
        Permanent nonDinosaur = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentDinosaur = harness.addToBattlefieldAndReturn(player2, new DrowsingTyrannodon());

        assertThat(gqs.getEffectivePower(gd, dinosaur)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, dinosaur)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, dinosaur, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, dinosaur, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, nonDinosaur)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, nonDinosaur, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.getEffectivePower(gd, opponentDinosaur)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, opponentDinosaur, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    void suspendsFromHandWithFourTimeCounters() {
        DinosaursOnASpaceship card = suspendCard();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 4);
    }

    @Test
    void createsFlyingHastyMulticolorDinosaurWhenTimeCounterIsRemoved() {
        DinosaursOnASpaceship card = suspendCard();

        for (int i = 0; i < 3; i++) {
            advanceToUpkeep(player1);
            harness.passBothPriorities();
            harness.passBothPriorities();
        }

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getName().equals("Dinosaur"))
                .findFirst()
                .orElseThrow();
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(2);
        assertThat(gqs.getEffectiveColors(gd, token)).containsExactlyInAnyOrder(CardColor.RED, CardColor.WHITE);
        assertThat(gqs.effectiveCreatureSubtypes(gd, token)).contains(CardSubtype.DINOSAUR);
        assertThat(gqs.hasKeyword(gd, token, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, token, Keyword.HASTE)).isTrue();
    }

    private DinosaursOnASpaceship suspendCard() {
        DinosaursOnASpaceship card = new DinosaursOnASpaceship();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateHandAbility(player1, 0, null);
        return card;
    }
}
