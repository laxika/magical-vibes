package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.b.BlackKnight;
import com.github.laxika.magicalvibes.cards.e.ElaborateFirecannon;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AshenmoorLiege.class, AshenmoorGouger.class, BlackKnight.class, HillGiant.class,
        GrizzlyBears.class, Shock.class, ElaborateFirecannon.class})
class AshenmoorLiegeTest extends BaseCardTest {

    @Test
    @DisplayName("Buffs other black creatures you control")
    void buffsOtherBlack() {
        harness.addToBattlefield(player1, new AshenmoorLiege());
        harness.addToBattlefield(player1, new BlackKnight());

        Permanent black = findPermanent(player1, "Black Knight");

        // 2/2 base + 1/1 = 3/3
        assertThat(gqs.getEffectivePower(gd, black)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, black)).isEqualTo(3);
    }

    @Test
    @DisplayName("Buffs other red creatures you control")
    void buffsOtherRed() {
        harness.addToBattlefield(player1, new AshenmoorLiege());
        harness.addToBattlefield(player1, new HillGiant());

        Permanent red = findPermanent(player1, "Hill Giant");

        // 3/3 base + 1/1 = 4/4
        assertThat(gqs.getEffectivePower(gd, red)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, red)).isEqualTo(4);
    }

    @Test
    @DisplayName("Does not buff itself or off-color creatures")
    void doesNotBuffItselfOrOffColor() {
        harness.addToBattlefield(player1, new AshenmoorLiege());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent liege = findPermanent(player1, "Ashenmoor Liege");
        Permanent green = findPermanent(player1, "Grizzly Bears");

        // Base 4/1, unaffected by its own "other" boosts.
        assertThat(gqs.getEffectivePower(gd, liege)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, liege)).isEqualTo(1);
        // Green creature is neither black nor red.
        assertThat(gqs.getEffectivePower(gd, green)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, green)).isEqualTo(2);
    }

    @Test
    @DisplayName("Opponent loses 4 life when their spell targets this creature")
    void opponentLosesLifeOnSpell() {
        harness.addToBattlefield(player1, new AshenmoorLiege());
        UUID liegeId = harness.getPermanentId(player1, "Ashenmoor Liege");
        int lifeBefore = gd.getLife(player2.getId());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, liegeId);

        // Shock + Ashenmoor Liege's triggered ability on the stack.
        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack.getLast().getCard().getName()).isEqualTo("Ashenmoor Liege");

        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 4);
    }

    @Test
    @DisplayName("Opponent loses 4 life when their activated ability targets this creature")
    void opponentLosesLifeOnAbility() {
        harness.addToBattlefield(player1, new AshenmoorLiege());
        UUID liegeId = harness.getPermanentId(player1, "Ashenmoor Liege");
        int lifeBefore = gd.getLife(player2.getId());

        harness.addToBattlefield(player2, new ElaborateFirecannon());

        harness.addMana(player2, ManaColor.COLORLESS, 4);
        harness.activateAbility(player2, 0, null, liegeId);

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack.getLast().getCard().getName()).isEqualTo("Ashenmoor Liege");

        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 4);
    }

    @Test
    @DisplayName("Does NOT trigger when the controller's own spell targets it")
    void doesNotTriggerOnOwnSpell() {
        harness.addToBattlefield(player1, new AshenmoorLiege());
        UUID liegeId = harness.getPermanentId(player1, "Ashenmoor Liege");
        int lifeBefore = gd.getLife(player1.getId());

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, liegeId);

        // Only Shock is on the stack — no becomes-target trigger from the controller's own spell.
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Shock");

        harness.passBothPriorities();
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Black and red creatures receive both boosts")
    void multicoloredCreatureReceivesBothBoosts() {
        harness.addToBattlefield(player1, new AshenmoorLiege());
        Permanent gouger = harness.addToBattlefieldAndReturn(player1, new AshenmoorGouger());

        assertThat(gqs.getEffectivePower(gd, gouger)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, gouger)).isEqualTo(6);
    }

    @Test
    @DisplayName("Opponent's black and red creatures receive neither boost")
    void doesNotBoostOpponentCreatures() {
        harness.addToBattlefield(player1, new AshenmoorLiege());
        Permanent gouger = harness.addToBattlefieldAndReturn(player2, new AshenmoorGouger());

        assertThat(gqs.getEffectivePower(gd, gouger)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, gouger)).isEqualTo(4);
    }

    @Test
    @DisplayName("Two Lieges boost each other and their boosts stack")
    void multipleLiegesStack() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new AshenmoorLiege());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new AshenmoorLiege());
        Permanent gouger = harness.addToBattlefieldAndReturn(player1, new AshenmoorGouger());

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, gouger)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, gouger)).isEqualTo(8);
    }

    @Test
    @DisplayName("Targeting another creature does not trigger the Liege")
    void targetingAnotherCreatureDoesNotTrigger() {
        harness.addToBattlefield(player1, new AshenmoorLiege());
        Permanent gouger = harness.addToBattlefieldAndReturn(player1, new AshenmoorGouger());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, gouger.getId());

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Controller's own activated ability does not trigger the Liege")
    void ownAbilityDoesNotTrigger() {
        harness.addToBattlefield(player1, new AshenmoorLiege());
        harness.addToBattlefield(player1, new ElaborateFirecannon());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 1, null,
                harness.getPermanentId(player1, "Ashenmoor Liege"));

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }
}
