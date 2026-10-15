package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BalaGedRecovery.class, BalaGedSanctuary.class, HolyDay.class})
class BalaGedRecoveryTest extends BaseCardTest {

    @Test
    void recoveryReturnsTargetCardFromGraveyardToHand() {
        Card target = new HolyDay();
        BalaGedRecovery recovery = new BalaGedRecovery();
        harness.setGraveyard(player1, List.of(target));
        harness.setHand(player1, List.of(recovery));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castModalSorcery(player1, 0, 0, List.of(target.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(target.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(target.getId()))
                .anyMatch(card -> card.getId().equals(recovery.getId()));
    }

    @Test
    void sanctuaryEntersTappedAndProducesGreenMana() {
        harness.setHand(player1, List.of(new BalaGedRecovery()));

        gs.playCard(gd, player1, 0, 1, null, null);

        Permanent land = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(land.getCard()).isInstanceOf(BalaGedSanctuary.class);
        assertThat(land.isTapped()).isTrue();

        land.untap();
        harness.activateAbility(player1, 0, 0, null, null);

        ManaPool mana = gd.playerManaPools.get(player1.getId());
        assertThat(mana.get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    void recoveryCannotTargetOpponentsGraveyard() {
        Card target = new BalaGedRecovery();
        harness.setGraveyard(player2, List.of(target));
        harness.setHand(player1, List.of(new BalaGedRecovery()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        assertThatThrownBy(() -> harness.castModalSorcery(player1, 0, 0, List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(target);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void recoveryRequiresATargetEvenWhenGraveyardContainsACard() {
        harness.setGraveyard(player1, List.of(new BalaGedRecovery()));
        harness.setHand(player1, List.of(new BalaGedRecovery()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        assertThatThrownBy(() -> harness.castModalSorcery(player1, 0, 0, List.of()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void recoveryDoesNotReturnAnotherCardWhenTargetLeavesGraveyard() {
        Card target = new BalaGedRecovery();
        Card other = new BalaGedRecovery();
        Card recovery = new BalaGedRecovery();
        harness.setGraveyard(player1, List.of(target, other));
        harness.setHand(player1, List.of(recovery));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castModalSorcery(player1, 0, 0, List.of(target.getId()));
        harness.setGraveyard(player1, List.of(other));
        harness.setExile(player1, List.of(target));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(Card::getId)
                .containsExactlyInAnyOrder(other.getId(), recovery.getId());
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void sanctuaryUsesALandPlayAndCannotTapWhileTapped() {
        harness.setHand(player1, List.of(new BalaGedRecovery(), new BalaGedRecovery()));

        gs.playCard(gd, player1, 0, 1, null, null);

        assertThat(gd.stack).isEmpty();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
    }
}
