package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.b.BarbedLightning;
import com.github.laxika.magicalvibes.cards.d.DarksteelGargoyle;
import com.github.laxika.magicalvibes.cards.d.DarksteelIngot;
import com.github.laxika.magicalvibes.cards.d.DarksteelPendant;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LichsTomb.class, DarksteelGargoyle.class, DarksteelIngot.class,
        DarksteelPendant.class, BarbedLightning.class})
class LichsTombTest extends BaseCardTest {

    @Test
    @DisplayName("Controller doesn't lose the game at 0 or less life")
    void controllerDoesNotLoseAtZeroLife() {
        Permanent tomb = harness.addToBattlefieldAndReturn(player1, new LichsTomb());
        harness.setLife(player1, 0);

        harness.runStateBasedActions();

        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(tomb);
    }

    @Test
    @DisplayName("Losing life triggers a sacrifice for each life lost")
    void losingLifeTriggersSacrificeForEachLifeLost() {
        Permanent tomb = harness.addToBattlefieldAndReturn(player1, new LichsTomb());
        Permanent gargoyle = harness.addToBattlefieldAndReturn(player1, new DarksteelGargoyle());
        Permanent ingot = harness.addToBattlefieldAndReturn(player1, new DarksteelIngot());
        harness.setLife(player1, 20);

        loseLife(2);

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.maxCount()).isEqualTo(2);
        assertThat(choice.validIds()).containsExactly(tomb.getId(), gargoyle.getId(), ingot.getId());

        harness.handleMultiplePermanentsChosen(player1, List.of(gargoyle.getId(), ingot.getId()));

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        harness.assertOnBattlefield(player1, "Lich's Tomb");
        harness.assertNotOnBattlefield(player1, "Darksteel Gargoyle");
        harness.assertNotOnBattlefield(player1, "Darksteel Ingot");
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    @Test
    @DisplayName("Lich's Tomb itself is an eligible sacrifice")
    void itselfIsEligibleSacrifice() {
        Permanent tomb = harness.addToBattlefieldAndReturn(player1, new LichsTomb());
        Permanent gargoyle = harness.addToBattlefieldAndReturn(player1, new DarksteelGargoyle());
        harness.setLife(player1, 20);

        loseLife(1);

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactly(tomb.getId(), gargoyle.getId());

        harness.handleMultiplePermanentsChosen(player1, List.of(tomb.getId()));

        harness.assertNotOnBattlefield(player1, "Lich's Tomb");
        harness.assertOnBattlefield(player1, "Darksteel Gargoyle");
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    @Test
    @DisplayName("Damage causes the life-loss sacrifice trigger")
    void damageCausesLifeLossSacrificeTrigger() {
        Permanent tomb = harness.addToBattlefieldAndReturn(player1, new LichsTomb());
        Permanent gargoyle = harness.addToBattlefieldAndReturn(player1, new DarksteelGargoyle());
        Permanent ingot = harness.addToBattlefieldAndReturn(player1, new DarksteelIngot());
        Permanent pendant = harness.addToBattlefieldAndReturn(player1, new DarksteelPendant());
        harness.setLife(player1, 20);

        harness.setHand(player1, List.of(new BarbedLightning()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castModalInstantWithModes(player1, 0, 1, 2, new int[]{1}, List.of(player1.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(17);

        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.maxCount()).isEqualTo(3);
        assertThat(choice.validIds()).containsExactly(
                tomb.getId(), gargoyle.getId(), ingot.getId(), pendant.getId());

        harness.handleMultiplePermanentsChosen(player1,
                List.of(gargoyle.getId(), ingot.getId(), pendant.getId()));

        harness.assertOnBattlefield(player1, "Lich's Tomb");
        harness.assertNotOnBattlefield(player1, "Darksteel Gargoyle");
        harness.assertNotOnBattlefield(player1, "Darksteel Ingot");
        harness.assertNotOnBattlefield(player1, "Darksteel Pendant");
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    private void loseLife(int amount) {
        harness.inMutationScope(() -> harness.getLifeSupport().applyLifeLoss(gd, player1.getId(), amount, "test"));
        assertThat(gd.stack).isNotEmpty();
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getEventValue()).isEqualTo(amount);
        harness.passBothPriorities();
    }
}
