package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AuroraGriffin;
import com.github.laxika.magicalvibes.cards.r.RushingRiver;
import com.github.laxika.magicalvibes.cards.z.RoostOfDrakes;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SunscapeBattlemage.class, AuroraGriffin.class, RushingRiver.class, RoostOfDrakes.class})
class SunscapeBattlemageTest extends BaseCardTest {

    @Test
    @DisplayName("Without kicker, neither ability resolves")
    void noKicker() {
        harness.setHand(player1, List.of(new SunscapeBattlemage()));
        addMana(3, ManaColor.COLORLESS, ManaColor.WHITE);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Sunscape Battlemage");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Green kicker destroys a target creature with flying")
    void greenKicker() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AuroraGriffin());
        harness.setHand(player1, List.of(new SunscapeBattlemage()));
        addMana(4, ManaColor.COLORLESS, ManaColor.WHITE, ManaColor.GREEN);

        harness.castKickedCreature(player1, 0, target.getId());
        resolveAllTriggers();

        assertThat(countPermanents(player2, "Aurora Griffin")).isZero();
    }

    @Test
    @DisplayName("Green kicker can be paid when no creature with flying exists")
    void greenKickerWithoutFlyingCreature() {
        harness.addToBattlefield(player2, new SunscapeBattlemage());
        harness.setHand(player1, List.of(new SunscapeBattlemage()));
        addMana(4, ManaColor.COLORLESS, ManaColor.WHITE, ManaColor.GREEN);

        harness.castKickedCreature(player1, 0);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Sunscape Battlemage");
        harness.assertOnBattlefield(player2, "Sunscape Battlemage");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Blue kicker draws two cards")
    void blueKicker() {
        harness.setHand(player1, List.of(new SunscapeBattlemage()));
        addMana(5, ManaColor.COLORLESS, ManaColor.WHITE, ManaColor.BLUE);

        castWithAdditionalCosts(List.of("{2}{U}"));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Both kicker costs resolve their independent abilities")
    void bothKickers() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AuroraGriffin());
        harness.setHand(player1, List.of(new SunscapeBattlemage()));
        addMana(6, ManaColor.COLORLESS, ManaColor.WHITE, ManaColor.GREEN, ManaColor.BLUE);

        castWithAdditionalCosts(List.of("{2}{U}"), target.getId(), true);
        resolveAllTriggers();

        assertThat(countPermanents(player2, "Aurora Griffin")).isZero();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Paying both kickers creates two independent enter triggers")
    void bothKickersCreateSeparateTriggers() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AuroraGriffin());
        harness.setHand(player1, List.of(new SunscapeBattlemage()));
        addMana(6, ManaColor.COLORLESS, ManaColor.WHITE, ManaColor.GREEN, ManaColor.BLUE);

        castWithAdditionalCosts(List.of("{2}{U}"), target.getId(), true);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Sunscape Battlemage");
        assertThat(gd.stack).hasSize(2);
    }

    @Test
    @CardUsed(RushingRiver.class)
    @DisplayName("Losing the flying target does not stop the separate draw trigger")
    void bothKickersStillDrawWhenTargetLeaves() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AuroraGriffin());
        harness.setHand(player1, List.of(new SunscapeBattlemage(), new RushingRiver()));
        addMana(9, ManaColor.COLORLESS, ManaColor.WHITE, ManaColor.GREEN, ManaColor.BLUE, ManaColor.BLUE);

        castWithAdditionalCosts(List.of("{2}{U}"), target.getId(), true);
        harness.passBothPriorities();
        harness.castInstant(player1, 0, target.getId());
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Sunscape Battlemage");
        harness.assertInHand(player2, "Aurora Griffin");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    @CardUsed(RoostOfDrakes.class)
    @DisplayName("Paying only the blue kicker triggers abilities for casting a kicked spell")
    void blueKickerCountsAsKickedSpell() {
        harness.addToBattlefield(player1, new RoostOfDrakes());
        harness.setHand(player1, List.of(new SunscapeBattlemage()));
        addMana(5, ManaColor.COLORLESS, ManaColor.WHITE, ManaColor.BLUE);

        castWithAdditionalCosts(List.of("{2}{U}"));
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Drake")).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("The blue kicker cannot be paid more than once")
    void blueKickerCannotBeRepeated() {
        harness.setHand(player1, List.of(new SunscapeBattlemage()));
        addMana(8, ManaColor.COLORLESS, ManaColor.WHITE, ManaColor.BLUE, ManaColor.BLUE);

        assertThatThrownBy(() -> castWithAdditionalCosts(List.of("{2}{U}", "{2}{U}")))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addMana(int colorless, ManaColor... colored) {
        harness.addMana(player1, ManaColor.COLORLESS, colorless);
        for (ManaColor color : colored) {
            harness.addMana(player1, color, 1);
        }
    }

    private void castWithAdditionalCosts(List<String> payments) {
        castWithAdditionalCosts(payments, null, false);
    }

    private void castWithAdditionalCosts(List<String> payments, java.util.UUID targetId, boolean kicked) {
        gs.playCard(gd, player1, 0, 0, targetId, null, List.of(), List.of(), false,
                null, null, null, null, null, kicked, null, null, null, null,
                payments, false);
    }
}
