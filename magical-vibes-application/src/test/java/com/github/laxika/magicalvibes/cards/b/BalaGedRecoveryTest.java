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
}
