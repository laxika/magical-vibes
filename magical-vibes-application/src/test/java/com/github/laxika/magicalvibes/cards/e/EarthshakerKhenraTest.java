package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class EarthshakerKhenraTest extends BaseCardTest {

    @Test
    @DisplayName("ETB makes a target creature with power <= Khenra's power unable to block")
    void etbMakesLowPowerTargetUnableToBlock() {
        Permanent blocker = addReady(player2, new GrizzlyBears()); // power 2 == Khenra's power 2
        harness.setHand(player1, List.of(new EarthshakerKhenra()));
        harness.addMana(player1, ManaColor.RED, 2);

        UUID targetId = blocker.getId();
        harness.castCreature(player1, 0, 0, targetId);

        // Resolve creature spell → ETB on stack, then resolve ETB.
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(blocker.isCantBlockThisTurn()).isTrue();
    }

    @Test
    @DisplayName("A creature with power greater than Khenra's is not a legal target — ETB never triggers")
    void cannotTargetHigherPowerCreature() {
        Permanent hillGiant = addReady(player2, new HillGiant()); // power 3 > Khenra's power 2
        harness.setHand(player1, List.of(new EarthshakerKhenra()));
        harness.addMana(player1, ManaColor.RED, 2);

        // No legal target exists, so the creature is cast without one.
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(hillGiant.isCantBlockThisTurn()).isFalse();
    }

    @Test
    @DisplayName("Eternalize creates a 4/4 black Zombie token copy with no mana cost")
    void eternalizeCreatesFourFourBlackZombieToken() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(new EarthshakerKhenra()));
        harness.addMana(player1, ManaColor.RED, 6); // {4}{R}{R}

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Earthshaker Khenra") && p.getCard().isToken())
                .findFirst().orElseThrow();

        assertThat(token.getEffectivePower()).isEqualTo(4);
        assertThat(token.getEffectiveToughness()).isEqualTo(4);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.BLACK);
        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.ZOMBIE);
        assertThat(token.getCard().getManaCost()).isEmpty();
    }

    @Test
    @DisplayName("Eternalize exiles the source card from the graveyard as a cost")
    void eternalizeExilesSourceAsCost() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(new EarthshakerKhenra()));
        harness.addMana(player1, ManaColor.RED, 6);

        harness.activateGraveyardAbility(player1, 0);

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Earthshaker Khenra"));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Earthshaker Khenra"));
    }

    private Permanent addReady(Player player, Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
