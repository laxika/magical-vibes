package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TymaretTheMurderKingTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices another creature and deals 2 damage to a target player")
    void sacrificesAnotherCreatureAndDamagesTargetPlayer() {
        Permanent tymaret = harness.addToBattlefieldAndReturn(player1, new TymaretTheMurderKing());
        Permanent otherCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(tymaret);
        assertThat(gd.playerGraveyards.get(player1.getId())).anyMatch(card -> card == otherCreature.getCard());
    }

    @Test
    @DisplayName("Cannot target a creature with the damage ability")
    void cannotTargetCreature() {
        harness.addToBattlefield(player1, new TymaretTheMurderKing());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);
    }

    @Test
    @DisplayName("Returns itself from the graveyard to its owner's hand")
    void returnsFromGraveyardToHand() {
        TymaretTheMurderKing tymaret = new TymaretTheMurderKing();
        harness.setGraveyard(player1, List.of(tymaret));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(tymaret);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(tymaret);
    }
}
