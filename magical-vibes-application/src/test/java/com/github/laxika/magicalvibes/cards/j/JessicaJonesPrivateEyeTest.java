package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({JessicaJonesPrivateEye.class, Forest.class})
class JessicaJonesPrivateEyeTest extends BaseCardTest {

    @Test
    void exilesCardsEqualToPowerAndGrantsPlayPermission() {
        Forest topCard = new Forest();
        Forest secondCard = new Forest();
        Card thirdCard = new Forest();
        harness.setLibrary(player1, List.of(topCard, secondCard, thirdCard));
        Permanent jessica = addReadyJessica();
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(jessica.isTapped()).isTrue();
        assertThat(jessica.getCounterCount(CounterType.STUN)).isEqualTo(1);

        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getId)
                .containsExactly(topCard.getId(), secondCard.getId());
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(thirdCard);
        assertThat(gd.exilePlayPermissions)
                .containsEntry(topCard.getId(), player1.getId())
                .containsEntry(secondCard.getId(), player1.getId());
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn)
                .contains(topCard.getId(), secondCard.getId());
    }

    @Test
    void activatesWithoutMana() {
        Forest topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));
        Permanent jessica = addReadyJessica();

        harness.activateAbility(player1, 0, null, null);

        assertThat(jessica.isTapped()).isTrue();
        assertThat(jessica.getCounterCount(CounterType.STUN)).isEqualTo(1);
        harness.passBothPriorities();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(topCard);
    }

    @Test
    void usesPowerAtResolution() {
        Forest first = new Forest();
        Forest second = new Forest();
        Forest third = new Forest();
        Forest fourth = new Forest();
        harness.setLibrary(player1, List.of(first, second, third, fourth));
        Permanent jessica = addReadyJessica();
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        jessica.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(first, second, third);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(fourth);
    }

    @Test
    void exilesOnlyAvailableCardsAndAllowsPlayingALand() {
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(forest));
        addReadyJessica();
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.exilePlayWithoutPayingManaCost).doesNotContain(forest.getId());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromExile(player1, forest.getId());

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(p -> p.getCard().getId()).contains(forest.getId());
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    void stunCounterReplacesTheNextUntap() {
        harness.setLibrary(player1, List.of(new Forest()));
        Permanent jessica = addReadyJessica();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.performUntapStep(player1);

        assertThat(jessica.isTapped()).isTrue();
        assertThat(jessica.getCounterCount(CounterType.STUN)).isZero();

        harness.performUntapStep(player1);

        assertThat(jessica.isTapped()).isFalse();
    }

    @Test
    void unusedPlayPermissionExpiresAtEndOfTurn() {
        Forest first = new Forest();
        Forest second = new Forest();
        harness.setLibrary(player1, List.of(first, second, new Forest()));
        harness.setLibrary(player2, List.of(new Forest(), new Forest()));
        addReadyJessica();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(first, second);
        assertThat(gd.exilePlayPermissions).doesNotContainKeys(first.getId(), second.getId());
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn).doesNotContain(first.getId(), second.getId());
    }

    private Permanent addReadyJessica() {
        Permanent jessica = harness.addToBattlefieldAndReturn(player1, new JessicaJonesPrivateEye());
        jessica.setSummoningSick(false);
        return jessica;
    }
}
