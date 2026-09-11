package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MsMarvelKamalaKhan.class, GiantGrowth.class, GrizzlyBears.class, Shock.class})
class MsMarvelKamalaKhanTest extends BaseCardTest {

    @Test
    void drawsAndTracksControllerHandSizeUntilEndOfTurn() {
        Permanent msMarvel = harness.addToBattlefieldAndReturn(player1, new MsMarvelKamalaKhan());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, new ArrayList<>(List.of(
                new GiantGrowth(), new Shock(), new Shock())));
        harness.addMana(player1, ManaColor.GREEN, 1);

        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
        assertThat(gqs.getEffectivePower(gd, msMarvel)).isEqualTo(1);

        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gqs.getEffectivePower(gd, msMarvel)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, msMarvel)).isEqualTo(4);

        harness.setHand(player1, new ArrayList<>(List.of(
                new Shock(), new Shock(), new Shock(), new Shock())));
        assertThat(gqs.getEffectivePower(gd, msMarvel)).isEqualTo(4);

        harness.setHand(player1, List.of());
        assertThat(gqs.getEffectivePower(gd, msMarvel)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, msMarvel)).isEqualTo(4);
    }

    @Test
    void doesNotTriggerForSpellTargetingOpponentCreature() {
        Permanent msMarvel = harness.addToBattlefieldAndReturn(player1, new MsMarvelKamalaKhan());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, msMarvel)).isEqualTo(1);
    }

    @Test
    void doesNotTriggerForSpellTargetingPlayer() {
        Permanent msMarvel = harness.addToBattlefieldAndReturn(player1, new MsMarvelKamalaKhan());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, msMarvel)).isEqualTo(1);
    }

    @Test
    void hasNoMaximumHandSize() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.addToBattlefield(player1, new MsMarvelKamalaKhan());
        harness.setHand(player1, new ArrayList<>(List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears())));

        harness.getGameService().advanceStep(gd);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(9);
    }
}
