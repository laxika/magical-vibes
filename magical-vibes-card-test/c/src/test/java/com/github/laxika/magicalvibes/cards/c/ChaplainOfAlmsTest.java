package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
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

@CardUsed({ChaplainOfAlms.class, ChapelShieldgeist.class, GrizzlyBears.class, Shock.class,
        ProdigalPyromancer.class, Cancel.class})
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

        harness.assertInGraveyard(player1, "Chaplain of Alms");
    }

    @Test
    @DisplayName("Disturb enters transformed as Chapel Shieldgeist")
    void disturbEntersTransformed() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(new ChaplainOfAlms()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveFlashback(player1, 0, null);

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
        harness.castAndResolveFlashback(player1, 0, null);
        return gd.playerBattlefields.get(player1.getId()).getFirst();
    }

    @Test
    void wardCountersOpponentActivatedAbility() {
        Permanent chaplain = harness.addToBattlefieldAndReturn(player1, new ChaplainOfAlms());
        addCreatureReady(player2, new ProdigalPyromancer());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player2, 0, null, chaplain.getId());
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Chaplain of Alms");
        harness.assertOnBattlefield(player2, "Prodigal Pyromancer");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void transformedFaceGrantsWardToItself() {
        Permanent geist = putTransformedGeistOnBattlefield();
        castShockAt(geist, 1);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Chapel Shieldgeist");
        harness.assertInGraveyard(player2, "Shock");
    }

    @Test
    void ownSpellDoesNotTriggerWard() {
        Permanent chaplain = harness.addToBattlefieldAndReturn(player1, new ChaplainOfAlms());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, chaplain.getId());
        harness.assertInGraveyard(player1, "Chaplain of Alms");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void transformedFaceDoesNotGrantWardToOpponentsCreatures() {
        putTransformedGeistOnBattlefield();
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, bears.getId());
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void innateAndGrantedWardRequireSeparatePayments() {
        putTransformedGeistOnBattlefield();
        Permanent chaplain = harness.addToBattlefieldAndReturn(player1, new ChaplainOfAlms());
        castShockAt(chaplain, 2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Chaplain of Alms");
        harness.assertInGraveyard(player2, "Shock");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void payingGrantedWardAllowsDamageAndExilesShieldgeist() {
        Permanent geist = putTransformedGeistOnBattlefield();
        castShockAt(geist, 2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Chapel Shieldgeist");
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.exiledCards.stream().map(e -> e.card().getId()))
                .contains(geist.getOriginalCard().getId());
    }

    @Test
    void counteredDisturbSpellIsExiled() {
        ChaplainOfAlms chaplain = new ChaplainOfAlms();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(chaplain));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castFlashback(player1, 0);
        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0, chaplain.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.exiledCards.stream().map(e -> e.card().getId())).contains(chaplain.getId());
        harness.assertInGraveyard(player2, "Cancel");
    }

    @Test
    void grantedWardCountersOpponentActivatedAbility() {
        Permanent geist = putTransformedGeistOnBattlefield();
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        addCreatureReady(player2, new ProdigalPyromancer());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player2, 0, null, bears.getId());
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(bears.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player1, "Chapel Shieldgeist");
        assertThat(geist.isTransformed()).isTrue();
        assertThat(gd.stack).isEmpty();
    }
}
