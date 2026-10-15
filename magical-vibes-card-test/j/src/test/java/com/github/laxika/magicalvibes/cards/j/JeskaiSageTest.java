package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({JeskaiSage.class, GrizzlyBears.class, Shock.class, WrathOfGod.class})
class JeskaiSageTest extends BaseCardTest {

    private Permanent addSage() {
        harness.addToBattlefield(player1, new JeskaiSage());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return gd.playerBattlefields.get(player1.getId()).getFirst();
    }

    @Test
    @DisplayName("Casting a noncreature spell gives Jeskai Sage +1/+1 until end of turn")
    void noncreatureSpellPumps() {
        Permanent sage = addSage();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, sage)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, sage)).isEqualTo(2);
    }

    @Test
    @DisplayName("When Jeskai Sage dies, its controller draws a card")
    void diesDrawsCard() {
        harness.addToBattlefield(player1, new JeskaiSage());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Jeskai Sage");
        harness.assertInGraveyard(player1, "Jeskai Sage");

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBefore);
    }

    @Test
    void prowessResolvesBeforeSpellAndExpiresAtEndOfTurn() {
        Permanent sage = addSage();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        assertThat(gqs.getEffectivePower(gd, sage)).isEqualTo(1);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, sage)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, sage)).isEqualTo(2);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        harness.passBothPriorities();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);

        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);
        assertThat(gqs.getEffectivePower(gd, sage)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, sage)).isEqualTo(1);
    }

    @Test
    void eachNoncreatureSpellAddsAnotherProwessBoost() {
        Permanent sage = addSage();
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        for (int i = 0; i < 2; i++) {
            harness.castInstant(player1, 0, player2.getId());
            harness.passBothPriorities();
            harness.passBothPriorities();
        }

        assertThat(gqs.getEffectivePower(gd, sage)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, sage)).isEqualTo(3);
    }

    @Test
    void creatureSpellDoesNotTriggerProwess() {
        Permanent sage = addSage();
        harness.setHand(player1, List.of(new JeskaiSage()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, sage)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, sage)).isEqualTo(1);
    }

    @Test
    void opponentSpellDoesNotTriggerProwess() {
        Permanent sage = addSage();
        harness.forceActivePlayer(player2);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, player1.getId());
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, sage)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, sage)).isEqualTo(1);
    }

    @Test
    void opponentKillingSageDrawsOnlyForSagesController() {
        Permanent sage = addSage();
        harness.forceActivePlayer(player2);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new Shock()));
        JeskaiSage drawnCard = new JeskaiSage();
        harness.setLibrary(player1, List.of(drawnCard));

        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, sage.getId());
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Jeskai Sage");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();

        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }
}
