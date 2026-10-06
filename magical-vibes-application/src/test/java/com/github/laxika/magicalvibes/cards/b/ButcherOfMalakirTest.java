package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.c.CruelEdict;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ButcherOfMalakir.class, CruelEdict.class, GiantSpider.class, GrizzlyBears.class})
class ButcherOfMalakirTest extends BaseCardTest {

    @Test
    @DisplayName("When another creature you control dies, each opponent sacrifices a creature")
    void anotherCreatureDies() {
        harness.addToBattlefield(player1, new ButcherOfMalakir());
        Permanent victim = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GiantSpider());

        castCruelEdictAtPlayer1();
        harness.handlePermanentChosen(player1, victim.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Butcher of Malakir");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Giant Spider");
    }

    @Test
    @DisplayName("When Butcher of Malakir dies, each opponent sacrifices a creature")
    void thisCreatureDies() {
        harness.addToBattlefield(player1, new ButcherOfMalakir());
        harness.addToBattlefield(player2, new GiantSpider());

        castCruelEdictAtPlayer1();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Butcher of Malakir");
        harness.assertInGraveyard(player2, "Giant Spider");
    }

    @Test
    @DisplayName("The opponent chooses which creature to sacrifice")
    void opponentChoosesCreature() {
        Permanent butcher = harness.addToBattlefieldAndReturn(player1, new ButcherOfMalakir());
        harness.addToBattlefield(player2, new GiantSpider());
        Permanent chosen = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        butcher.setMarkedDamage(4);
        harness.runStateBasedActions();
        resolveAllTriggers();
        harness.handleMultiplePermanentsChosen(player2, List.of(chosen.getId()));
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Giant Spider");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Butcher sees each other controlled creature dying simultaneously with it")
    void simultaneousDeathsTriggerSeparately() {
        Permanent butcher = harness.addToBattlefieldAndReturn(player1, new ButcherOfMalakir());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent spider = harness.addToBattlefieldAndReturn(player1, new GiantSpider());
        for (int i = 0; i < 3; i++) {
            harness.addToBattlefield(player2, new GrizzlyBears());
        }

        butcher.setMarkedDamage(4);
        bears.setMarkedDamage(2);
        spider.setMarkedDamage(4);
        harness.runStateBasedActions();
        assertThat(gd.stack).hasSize(3);

        for (int i = 0; i < 3; i++) {
            resolveAllTriggers();
            if (gd.interaction.isAwaitingInput()) {
                harness.handleMultiplePermanentsChosen(player2,
                        List.of(findPermanent(player2, "Grizzly Bears").getId()));
            }
        }

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(3);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player1, "Butcher of Malakir");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Giant Spider");
    }

    @Test
    @DisplayName("An opponent's creature dying does not trigger Butcher")
    void opponentCreatureDeathDoesNotTrigger() {
        harness.addToBattlefield(player1, new ButcherOfMalakir());
        Permanent dying = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GiantSpider());

        dying.setMarkedDamage(2);
        harness.runStateBasedActions();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Giant Spider");
    }

    @Test
    @DisplayName("An opponent without creatures sacrifices nothing")
    void opponentWithoutCreatures() {
        harness.addToBattlefield(player1, new ButcherOfMalakir());
        Permanent dying = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GiantSpider());

        dying.setMarkedDamage(2);
        harness.runStateBasedActions();
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Butcher of Malakir");
        harness.assertOnBattlefield(player1, "Giant Spider");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private void castCruelEdictAtPlayer1() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new CruelEdict()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.castAndResolveSorcery(player2, 0, player1.getId());
    }
}
