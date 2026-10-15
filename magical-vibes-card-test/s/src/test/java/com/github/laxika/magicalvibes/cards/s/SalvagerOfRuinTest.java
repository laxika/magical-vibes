package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.Disenchant;
import com.github.laxika.magicalvibes.cards.t.TormodsCrypt;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SalvagerOfRuin.class, Disenchant.class, TormodsCrypt.class})
class SalvagerOfRuinTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices itself and returns a noncreature permanent put into the graveyard this turn")
    void returnsNoncreaturePermanentFromBattlefieldThisTurn() {
        harness.addToBattlefield(player1, new SalvagerOfRuin());
        Permanent crypt = harness.addToBattlefieldAndReturn(player1, new TormodsCrypt());
        harness.setHand(player1, List.of(new Disenchant()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, crypt.getId());

        harness.activateAbility(player1, 0, null, crypt.getCard().getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Tormod's Crypt");
        harness.assertInGraveyard(player1, "Salvager of Ruin");
        harness.assertNotInGraveyard(player1, "Tormod's Crypt");
    }

    @Test
    @DisplayName("Cannot target a permanent card that was not put into the graveyard from the battlefield this turn")
    void cannotTargetOldPermanentCard() {
        harness.addToBattlefield(player1, new SalvagerOfRuin());
        TormodsCrypt crypt = new TormodsCrypt();
        harness.setGraveyard(player1, List.of(crypt));

        assertThatThrownBy(() ->
                harness.activateAbility(player1, 0, null, crypt.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("from the battlefield this turn");
    }

    @Test
    @DisplayName("Cannot target a nonpermanent card")
    void cannotTargetNonpermanentCard() {
        harness.addToBattlefield(player1, new SalvagerOfRuin());
        Disenchant disenchant = new Disenchant();
        harness.setGraveyard(player1, List.of(disenchant));

        assertThatThrownBy(() ->
                harness.activateAbility(player1, 0, null, disenchant.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Returns another creature that died this turn and pays the sacrifice cost immediately")
    void returnsCreatureThatDiedThisTurn() {
        harness.addToBattlefield(player1, new SalvagerOfRuin());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new SalvagerOfRuin());
        harness.setHand(player1, List.of(new Disenchant()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, other.getId());

        harness.activateAbility(player1, 0, null, other.getCard().getId(), Zone.GRAVEYARD);

        harness.assertNotOnBattlefield(player1, "Salvager of Ruin");
        harness.assertInGraveyard(player1, "Salvager of Ruin");
        harness.assertNotInHand(player1, "Salvager of Ruin");
        harness.passBothPriorities();
        harness.assertInHand(player1, "Salvager of Ruin");
        harness.assertInGraveyard(player1, "Salvager of Ruin");
    }

    @Test
    @DisplayName("Cannot target itself before paying the sacrifice cost")
    void cannotReturnItselfWithItsOwnAbility() {
        Permanent salvager = harness.addToBattlefieldAndReturn(player1, new SalvagerOfRuin());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null,
                salvager.getCard().getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Salvager of Ruin");
        harness.assertNotInGraveyard(player1, "Salvager of Ruin");
    }

    @Test
    @DisplayName("Cannot target an opponent's permanent card even if it died this turn")
    void cannotTargetOpponentsGraveyard() {
        harness.addToBattlefield(player1, new SalvagerOfRuin());
        Permanent other = harness.addToBattlefieldAndReturn(player2, new SalvagerOfRuin());
        harness.setHand(player1, List.of(new Disenchant()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, other.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null,
                other.getCard().getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Salvager of Ruin");
        harness.assertInGraveyard(player2, "Salvager of Ruin");
    }

    @Test
    @DisplayName("Does not return a target exiled in response and does not refund the sacrifice")
    void targetExiledInResponse() {
        harness.addToBattlefield(player1, new SalvagerOfRuin());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new SalvagerOfRuin());
        harness.addToBattlefield(player2, new TormodsCrypt());
        harness.setHand(player1, List.of(new Disenchant()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, other.getId());
        harness.activateAbility(player1, 0, null, other.getCard().getId(), Zone.GRAVEYARD);

        harness.activateAbility(player2, 0, null, player1.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotInHand(player1, "Salvager of Ruin");
        harness.assertNotInGraveyard(player1, "Salvager of Ruin");
        harness.assertNotOnBattlefield(player1, "Salvager of Ruin");
    }
}
