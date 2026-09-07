package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(TunnelRats.class)
class TunnelRatsTest extends BaseCardTest {

    @Test
    @DisplayName("Graveyard ability returns Tunnel Rats to the battlefield tapped")
    void returnsFromGraveyardTapped() {
        TunnelRats rats = new TunnelRats();
        harness.setGraveyard(player1, List.of(rats));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        Permanent permanent = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(candidate -> candidate.getCard().getId().equals(rats.getId()))
                .findFirst()
                .orElseThrow();
        assertThat(permanent.getCard().getId()).isEqualTo(rats.getId());
        assertThat(permanent.isTapped()).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(rats.getId()));
    }

    @Test
    @DisplayName("Graveyard ability pays its mana cost")
    void graveyardAbilityPaysManaCost() {
        harness.setGraveyard(player1, List.of(new TunnelRats()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateGraveyardAbility(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    @DisplayName("Cannot activate the graveyard ability without enough mana")
    void cannotActivateWithoutEnoughMana() {
        harness.setGraveyard(player1, List.of(new TunnelRats()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }
}
