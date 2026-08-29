package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.b.BurstOfEnergy;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Memnarch;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TreacherousBlessing.class, GrizzlyBears.class, BurstOfEnergy.class, Memnarch.class})
class TreacherousBlessingTest extends BaseCardTest {

    @Test
    void drawsThreeCardsWhenItEnters() {
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new TreacherousBlessing()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void controllerLosesOneLifeWhenCastingASpell() {
        harness.addToBattlefield(player1, new TreacherousBlessing());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        int lifeBefore = gd.getLife(player1.getId());

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore - 1);
    }

    @Test
    void opponentCastingASpellDoesNotTriggerIt() {
        harness.addToBattlefield(player1, new TreacherousBlessing());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        int controllerLifeBefore = gd.getLife(player1.getId());

        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(controllerLifeBefore);
    }

    @Test
    void sacrificesItselfWhenTargetedByASpell() {
        Permanent blessing = harness.addToBattlefieldAndReturn(player1, new TreacherousBlessing());
        harness.setHand(player2, List.of(new BurstOfEnergy()));
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.castInstant(player2, 0, blessing.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Treacherous Blessing");
        harness.assertInGraveyard(player1, "Treacherous Blessing");
    }

    @Test
    void sacrificesItselfWhenTargetedByAnAbility() {
        Permanent blessing = harness.addToBattlefieldAndReturn(player1, new TreacherousBlessing());
        harness.addToBattlefield(player2, new Memnarch());
        Permanent memnarch = findPermanent(player2, "Memnarch");
        memnarch.setSummoningSick(false);
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.activateAbility(player2, gd.playerBattlefields.get(player2.getId()).indexOf(memnarch),
                null, blessing.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Treacherous Blessing");
        harness.assertInGraveyard(player1, "Treacherous Blessing");
    }
}
