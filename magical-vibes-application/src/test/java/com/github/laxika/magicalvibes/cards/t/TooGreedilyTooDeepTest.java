package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TooGreedilyTooDeep.class, AirElemental.class, GrizzlyBears.class})
class TooGreedilyTooDeepTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a creature and it damages each other creature")
    void returnsCreatureAndDamagesEachOtherCreature() {
        GrizzlyBears returnedCard = new GrizzlyBears();
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        prepareCast(returnedCard);

        harness.castSorcery(player1, 0, returnedCard.getId());
        harness.passBothPriorities();

        Permanent returnedPermanent = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(returnedCard.getId()))
                .findFirst()
                .orElseThrow();
        assertThat(returnedPermanent.getMarkedDamage()).isZero();
        assertThat(ownCreature.getMarkedDamage()).isEqualTo(2);
        assertThat(opposingCreature.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Only accepts a creature card as the graveyard target")
    void onlyAcceptsCreatureCardTarget() {
        TooGreedilyTooDeep nonCreatureTarget = new TooGreedilyTooDeep();
        harness.setGraveyard(player1, List.of(nonCreatureTarget));
        harness.setHand(player1, List.of(new TooGreedilyTooDeep()));
        addMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, nonCreatureTarget.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void prepareCast(GrizzlyBears returnedCard) {
        harness.setGraveyard(player1, List.of(returnedCard));
        harness.setHand(player1, List.of(new TooGreedilyTooDeep()));
        addMana();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
    }
}
