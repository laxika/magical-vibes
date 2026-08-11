package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.FireElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ChandraAwakenedInfernoTest extends BaseCardTest {

    @Test
    @DisplayName("The planeswalker spell cannot be countered")
    void cannotBeCountered() {
        ChandraAwakenedInferno chandra = new ChandraAwakenedInferno();
        harness.setHand(player1, List.of(chandra));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castPlaneswalker(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, chandra.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Chandra, Awakened Inferno");
        harness.assertInGraveyard(player2, "Cancel");
    }

    @Test
    @DisplayName("+2 gives the opponent an upkeep damage emblem")
    void plusTwoCreatesEmblem() {
        Permanent chandra = addReadyChandra(player1, 4);
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(chandra.getCounterCount(CounterType.LOYALTY)).isEqualTo(6);
        assertThat(gd.emblems).hasSize(1);
        assertThat(gd.emblems.getFirst().controllerId()).isEqualTo(player2.getId());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 1);
    }

    @Test
    @DisplayName("-3 damages non-Elemental creatures only")
    void minusThreeSkipsElementals() {
        Permanent chandra = addReadyChandra(player1, 4);
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new FireElemental());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(chandra.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Fire Elemental");
    }

    @Test
    @DisplayName("-X exiles a creature that would die from the damage")
    void minusXExilesLethalCreatureDamage() {
        Permanent chandra = addReadyChandra(player1, 4);
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent bear = findPermanent(player2, "Grizzly Bears");

        harness.activateAbility(player1, 0, 2, 3, bear.getId());
        harness.passBothPriorities();

        assertThat(chandra.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.playerGraveyards.get(player2.getId())).extracting(Card::getName)
                .doesNotContain("Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId())).extracting(Card::getName)
                .contains("Grizzly Bears");
    }

    @Test
    @DisplayName("-X cannot target a player")
    void minusXCannotTargetPlayer() {
        addReadyChandra(player1, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, 3, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyChandra(Player player, int loyalty) {
        Permanent perm = new Permanent(new ChandraAwakenedInferno());
        perm.setCounterCount(CounterType.LOYALTY, loyalty);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }
}
