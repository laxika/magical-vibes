package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(RunebladeRaiser.class)
class RunebladeRaiserTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped and returns to its owner's battlefield when it dies")
    void entersTappedAndReturnsToOwnersBattlefield() {
        harness.setHand(player1, List.of(new RunebladeRaiser()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        Permanent raiser = findPermanent(player1, "Runeblade Raiser");
        Card card = raiser.getOriginalCard();

        assertThat(raiser.isTapped()).isTrue();

        kill(raiser);

        Permanent returned = findPermanent(card);
        assertThat(returned).isNotNull();
        assertThat(returned.isTapped()).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(graveyardCard -> graveyardCard.getId().equals(card.getId()));
    }

    @Test
    @DisplayName("Perpetually loses the death-return ability after returning")
    void losesDeathReturnAbilityAfterReturning() {
        Permanent raiser = harness.addToBattlefieldAndReturn(player1, new RunebladeRaiser());
        Card card = raiser.getOriginalCard();

        kill(raiser);
        Permanent returned = findPermanent(card);
        assertThat(returned).isNotNull();

        kill(returned);

        assertThat(findPermanent(card)).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(graveyardCard -> graveyardCard.getId().equals(card.getId()));
    }

    @Test
    @DisplayName("Returns under its owner's control when controlled by another player")
    void returnsUnderOwnersControl() {
        Card card = new RunebladeRaiser();
        card.setOwnerId(player2.getId());
        Permanent raiser = harness.addToBattlefieldAndReturn(player1, card);

        kill(raiser);

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(card.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(card.getId()));
    }

    private void kill(Permanent permanent) {
        permanent.setMarkedDamage(2);
        harness.runStateBasedActions();
        harness.passBothPriorities();
    }

    private Permanent findPermanent(Card card) {
        return gd.playerBattlefields.values().stream()
                .flatMap(java.util.Collection::stream)
                .filter(permanent -> permanent.getCard().getId().equals(card.getId()))
                .findFirst()
                .orElse(null);
    }
}
