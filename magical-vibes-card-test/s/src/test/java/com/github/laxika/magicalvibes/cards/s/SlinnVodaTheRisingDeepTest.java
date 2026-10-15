package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BalothGorger;
import com.github.laxika.magicalvibes.cards.b.BlinkOfAnEye;
import com.github.laxika.magicalvibes.cards.i.IcyManipulator;
import com.github.laxika.magicalvibes.cards.v.VodalianArcanist;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SlinnVodaTheRisingDeep.class, BalothGorger.class, SerraAngel.class,
        VodalianArcanist.class, BlinkOfAnEye.class, IcyManipulator.class})
class SlinnVodaTheRisingDeepTest extends BaseCardTest {

    @Test
    @DisplayName("Kicked trigger leaves noncreature permanents on the battlefield")
    void noncreaturePermanentsStay() {
        harness.addToBattlefield(player1, new IcyManipulator());
        harness.addToBattlefield(player2, new IcyManipulator());
        harness.addToBattlefield(player2, new BalothGorger());
        harness.setHand(player1, List.of(new SlinnVodaTheRisingDeep()));
        harness.addMana(player1, ManaColor.BLUE, 10);

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Icy Manipulator");
        harness.assertOnBattlefield(player2, "Icy Manipulator");
        harness.assertInHand(player2, "Baloth Gorger");
    }

    @Test
    @DisplayName("Kicked trigger still returns creatures after Slinn Voda leaves")
    void kickedTriggerResolvesAfterSourceLeaves() {
        harness.addToBattlefield(player2, new BalothGorger());
        harness.addToBattlefield(player2, new VodalianArcanist());
        harness.setHand(player1, List.of(new SlinnVodaTheRisingDeep(), new BlinkOfAnEye()));
        harness.addMana(player1, ManaColor.BLUE, 12);

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player2, "Baloth Gorger");
        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Slinn Voda, the Rising Deep"));
        harness.passBothPriorities();
        harness.assertInHand(player1, "Slinn Voda, the Rising Deep");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Baloth Gorger");
        harness.assertInHand(player2, "Baloth Gorger");
        harness.assertOnBattlefield(player2, "Vodalian Arcanist");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cast without kicker â€” no ETB trigger, all creatures remain")
    void castWithoutKickerNoBounce() {
        harness.addToBattlefield(player1, new BalothGorger());
        harness.addToBattlefield(player2, new SerraAngel());
        harness.setHand(player1, List.of(new SlinnVodaTheRisingDeep()));
        harness.addMana(player1, ManaColor.BLUE, 8);

        harness.castCreature(player1, 0);
        // Resolve creature spell
        harness.passBothPriorities();

        // No triggered ability on stack (kicker condition not met)
        assertThat(gd.stack).isEmpty();

        // All creatures remain on battlefield
        harness.assertOnBattlefield(player1, "Baloth Gorger");
        harness.assertOnBattlefield(player1, "Slinn Voda, the Rising Deep");
        harness.assertOnBattlefield(player2, "Serra Angel");
    }

    @Test
    @DisplayName("Cast with kicker â€” bounces non-exempt creatures from both players")
    void castWithKickerBouncesNonExemptCreatures() {
        harness.addToBattlefield(player1, new BalothGorger());
        harness.addToBattlefield(player2, new SerraAngel());
        harness.setHand(player1, List.of(new SlinnVodaTheRisingDeep()));
        // {6}{U}{U} + kicker {1}{U} = 10 total (7 generic + 3 blue)
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.addMana(player1, ManaColor.WHITE, 7);

        harness.castKickedCreature(player1, 0);
        // Resolve creature spell â†’ enters battlefield, kicked ETB triggers
        harness.passBothPriorities();

        // ETB triggered ability should be on stack
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);

        // Resolve ETB trigger
        harness.passBothPriorities();

        // Non-exempt creatures should be bounced
        harness.assertNotOnBattlefield(player1, "Baloth Gorger");
        harness.assertNotOnBattlefield(player2, "Serra Angel");

        // Bounced creatures go to their owners' hands
        harness.assertInHand(player1, "Baloth Gorger");
        harness.assertInHand(player2, "Serra Angel");
    }

    @Test
    @DisplayName("Cast with kicker â€” Slinn Voda itself stays (Leviathan is exempt)")
    void castWithKickerSlinnVodaStays() {
        harness.setHand(player1, List.of(new SlinnVodaTheRisingDeep()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.addMana(player1, ManaColor.WHITE, 7);

        harness.castKickedCreature(player1, 0);
        // Resolve creature spell
        harness.passBothPriorities();
        // Resolve ETB trigger
        harness.passBothPriorities();

        // Slinn Voda is a Leviathan so it stays
        harness.assertOnBattlefield(player1, "Slinn Voda, the Rising Deep");
    }

    @Test
    @DisplayName("Cast with kicker â€” Merfolk creatures are exempt and stay")
    void castWithKickerMerfolkStays() {
        harness.addToBattlefield(player2, new VodalianArcanist());

        harness.addToBattlefield(player2, new BalothGorger());

        harness.setHand(player1, List.of(new SlinnVodaTheRisingDeep()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.addMana(player1, ManaColor.WHITE, 7);

        harness.castKickedCreature(player1, 0);
        // Resolve creature spell
        harness.passBothPriorities();
        // Resolve ETB trigger
        harness.passBothPriorities();

        // Vodalian Arcanist should stay on battlefield
        harness.assertOnBattlefield(player2, "Vodalian Arcanist");

        // Baloth Gorger should be bounced
        harness.assertNotOnBattlefield(player2, "Baloth Gorger");
        harness.assertInHand(player2, "Baloth Gorger");
    }

    @Test
    @DisplayName("Cast with kicker â€” empty battlefields do not cause errors")
    void castWithKickerEmptyBattlefield() {
        harness.setHand(player1, List.of(new SlinnVodaTheRisingDeep()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.addMana(player1, ManaColor.WHITE, 7);

        harness.castKickedCreature(player1, 0);
        // Resolve creature spell
        harness.passBothPriorities();
        // Resolve ETB trigger
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        // Only Slinn Voda should be on battlefield
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .hasSize(1)
                .allMatch(p -> p.getCard().getName().equals("Slinn Voda, the Rising Deep"));
    }
}
