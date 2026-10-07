package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TunnelRats.class})
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

        Permanent permanent = findPermanent(player1, "Tunnel Rats");
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

    @Test
    @DisplayName("Only the activated copy returns, even with other copies in both graveyards")
    void returnsOnlyActivatedCopy() {
        TunnelRats source = new TunnelRats();
        TunnelRats other = new TunnelRats();
        TunnelRats opponentsCopy = new TunnelRats();
        harness.setGraveyard(player1, List.of(other, source));
        harness.setGraveyard(player2, List.of(opponentsCopy));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.activateGraveyardAbility(player1, 1);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Tunnel Rats").getCard().getId()).isEqualTo(source.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(other);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opponentsCopy);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("The ability can be activated during an opponent's end step")
    void activatesDuringOpponentsTurn() {
        harness.setGraveyard(player1, List.of(new TunnelRats()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Tunnel Rats").isTapped()).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("An older activation cannot return Tunnel Rats after it returns and dies again")
    void olderActivationDoesNotReturnNewGraveyardObject() {
        TunnelRats rats = new TunnelRats();
        harness.setGraveyard(player1, List.of(rats));
        harness.addMana(player1, ManaColor.BLACK, 10);
        harness.activateGraveyardAbility(player1, 0);
        harness.activateGraveyardAbility(player1, 0);

        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);
        Permanent returned = findPermanent(player1, "Tunnel Rats");
        harness.getPermanentRemovalService().sacrificePermanentToGraveyard(gd, returned);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(rats);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Five colorless mana cannot pay the required black mana")
    void requiresBlackMana() {
        harness.setGraveyard(player1, List.of(new TunnelRats()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }
}
