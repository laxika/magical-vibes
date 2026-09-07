package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BladedBattleFan.class, GrizzlyBears.class})
class BladedBattleFanTest extends BaseCardTest {

    @Test
    @DisplayName("Entering Bladed Battle-Fan attaches it and grants indestructible until end of turn")
    void enteringAttachesAndGrantsIndestructible() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent fan = castBattleFan();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(fan.getAttachedTo()).isEqualTo(bears.getId());
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("Bladed Battle-Fan's enter-the-battlefield indestructible grant expires at end of turn")
    void indestructibleExpiresAtEndOfTurn() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castBattleFan();

        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.INDESTRUCTIBLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
    }

    @Test
    @DisplayName("Equip {1} moves Bladed Battle-Fan and its bonus to another creature")
    void equipMovesFanAndBonus() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent fan = addBattleFanReady(player1);
        fan.setAttachedTo(first.getId());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 1, null, second.getId());
        harness.passBothPriorities();

        assertThat(fan.getAttachedTo()).isEqualTo(second.getId());
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(3);
    }

    private Permanent castBattleFan() {
        harness.setHand(player1, List.of(new BladedBattleFan()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        return findPermanent(player1, "Bladed Battle-Fan");
    }

    private Permanent addBattleFanReady(Player player) {
        Permanent fan = new Permanent(new BladedBattleFan());
        fan.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(fan);
        return fan;
    }
}
