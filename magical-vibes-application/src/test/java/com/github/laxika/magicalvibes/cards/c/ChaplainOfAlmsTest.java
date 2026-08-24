package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ChaplainOfAlms.class, ChapelShieldgeist.class, GrizzlyBears.class, Shock.class})
class ChaplainOfAlmsTest extends BaseCardTest {

    @Test
    @DisplayName("Ward counters an opponent's spell when they do not pay")
    void wardCountersOpponentSpell() {
        Permanent chaplain = harness.addToBattlefieldAndReturn(player1, new ChaplainOfAlms());
        castShockAt(chaplain, 1);

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Chaplain of Alms");
        harness.assertInGraveyard(player2, "Shock");
    }

    @Test
    @DisplayName("Paying ward lets the targeted spell resolve")
    void payingWardLetsSpellResolve() {
        Permanent chaplain = harness.addToBattlefieldAndReturn(player1, new ChaplainOfAlms());
        castShockAt(chaplain, 2);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        assertThat(chaplain.getMarkedDamage()).isEqualTo(2);
        harness.assertOnBattlefield(player1, "Chaplain of Alms");
    }

    @Test
    @DisplayName("Disturb enters transformed as Chapel Shieldgeist")
    void disturbEntersTransformed() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(new ChaplainOfAlms()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castFlashback(player1, 0);
        harness.passBothPriorities();

        Permanent geist = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(geist.isTransformed()).isTrue();
        assertThat(geist.getCard().getName()).isEqualTo("Chapel Shieldgeist");
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Chapel Shieldgeist grants ward to each creature you control")
    void transformedFaceGrantsWardToEachCreature() {
        Permanent geist = putTransformedGeistOnBattlefield();
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castShockAt(bears, 1);

        harness.passBothPriorities();

        assertThat(geist.isTransformed()).isTrue();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Shock");
    }

    @Test
    @DisplayName("Chapel Shieldgeist is exiled instead of going to the graveyard")
    void transformedFaceIsExiledInsteadOfGraveyard() {
        Permanent geist = putTransformedGeistOnBattlefield();

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, geist));

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.exiledCards.stream().map(e -> e.card().getId()))
                .contains(geist.getOriginalCard().getId());
    }

    private void castShockAt(Permanent target, int redMana) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, redMana);
        harness.castInstant(player2, 0, target.getId());
    }

    private Permanent putTransformedGeistOnBattlefield() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(new ChaplainOfAlms()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castFlashback(player1, 0);
        harness.passBothPriorities();
        return gd.playerBattlefields.get(player1.getId()).getFirst();
    }
}
