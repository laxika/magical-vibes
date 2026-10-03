package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AngelOfMercy;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SoulWarden;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DefiantBloodlord.class, AngelOfMercy.class, GrizzlyBears.class, SoulWarden.class, Unsummon.class})
class DefiantBloodlordTest extends BaseCardTest {

    @Test
    @DisplayName("Target opponent loses the amount of life gained")
    void opponentLosesLifeEqualToLifeGained() {
        harness.addToBattlefield(player1, new DefiantBloodlord());
        int opponentLifeBefore = gd.getLife(player2.getId());

        harness.castFromHand(player1, new AngelOfMercy(), "{4}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(23);
        assertThat(gd.getLife(player2.getId())).isEqualTo(opponentLifeBefore - 3);
    }

    @Test
    @DisplayName("Does not trigger when an opponent gains life")
    void doesNotTriggerOnOpponentLifeGain() {
        harness.addToBattlefield(player1, new DefiantBloodlord());
        int player1LifeBefore = gd.getLife(player1.getId());
        int player2LifeBefore = gd.getLife(player2.getId());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player2, new AngelOfMercy(), "{4}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(player1LifeBefore);
        assertThat(gd.getLife(player2.getId())).isEqualTo(player2LifeBefore + 3);
    }

    @Test
    @DisplayName("Triggers separately for separate life gain events")
    void triggersForEachLifeGainEvent() {
        harness.addToBattlefield(player1, new DefiantBloodlord());
        harness.addToBattlefield(player1, new SoulWarden());
        int opponentLifeBefore = gd.getLife(player2.getId());

        harness.setHand(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(opponentLifeBefore - 2);
    }

    @Test
    @DisplayName("Each Bloodlord triggers independently for the same life gain")
    void multipleBloodlordsEachDrainTheFullAmount() {
        harness.addToBattlefield(player1, new DefiantBloodlord());
        harness.addToBattlefield(player1, new DefiantBloodlord());

        harness.castFromHand(player1, new AngelOfMercy(), "{4}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(23);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(23);
        assertThat(gd.getLife(player2.getId())).isEqualTo(14);
    }

    @Test
    @DisplayName("Pending triggers retain the amounts of differently sized life gains")
    void separateTriggersRetainTheirOwnLifeGainAmounts() {
        harness.addToBattlefield(player1, new DefiantBloodlord());
        harness.addToBattlefield(player1, new SoulWarden());

        harness.castFromHand(player1, new AngelOfMercy(), "{4}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(24);
        assertThat(gd.getLife(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Life-loss trigger resolves after Bloodlord leaves the battlefield")
    void pendingTriggerSurvivesSourceLeavingBattlefield() {
        var bloodlord = harness.addToBattlefieldAndReturn(player1, new DefiantBloodlord());

        harness.castFromHand(player1, new AngelOfMercy(), "{4}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(23);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);

        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0, bloodlord.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(23);
        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
    }
}
