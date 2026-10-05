package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.d.DressDown;
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

@CardUsed({MsMarvelKamalaKhan.class, GiantGrowth.class, GrizzlyBears.class, Shock.class, DressDown.class})
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

        harness.castAndResolveInstant(player1, 0, targetId);

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
        harness.castAndResolveInstant(player1, 0, targetId);

        assertThat(gqs.getEffectivePower(gd, msMarvel)).isEqualTo(1);
    }

    @Test
    void doesNotTriggerForSpellTargetingPlayer() {
        Permanent msMarvel = harness.addToBattlefieldAndReturn(player1, new MsMarvelKamalaKhan());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gqs.getEffectivePower(gd, msMarvel)).isEqualTo(1);
    }

    @Test
    void targetingHerselfDrawsBeforeSpellResolvesAndAddsBonusesToBasePower() {
        Permanent msMarvel = harness.addToBattlefieldAndReturn(player1, new MsMarvelKamalaKhan());
        harness.setLibrary(player1, List.of(new Shock()));
        harness.setHand(player1, List.of(new GiantGrowth(), new Shock()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, msMarvel.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gqs.getEffectivePower(gd, msMarvel)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, msMarvel)).isEqualTo(4);

        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, msMarvel)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, msMarvel)).isEqualTo(7);
    }

    @Test
    void opponentsSpellTargetingYourCreatureDoesNotTrigger() {
        Permanent msMarvel = harness.addToBattlefieldAndReturn(player1, new MsMarvelKamalaKhan());
        harness.setLibrary(player1, List.of(new Shock()));
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.setHand(player2, List.of(new GiantGrowth()));
        harness.addMana(player2, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player2, 0, msMarvel.getId());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gqs.getEffectivePower(gd, msMarvel)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, msMarvel)).isEqualTo(7);
    }

    @Test
    void grantedPowerAbilityExpiresDuringCleanup() {
        Permanent msMarvel = harness.addToBattlefieldAndReturn(player1, new MsMarvelKamalaKhan());
        harness.setLibrary(player1, List.of(new Shock()));
        harness.setHand(player1, List.of(new GiantGrowth(), new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castAndResolveInstant(player1, 0, msMarvel.getId());
        resolveAllTriggers();
        assertThat(gqs.getEffectivePower(gd, msMarvel)).isEqualTo(6);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        gs.advanceStep(gd);

        assertThat(gqs.getEffectivePower(gd, msMarvel)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, msMarvel)).isEqualTo(4);
    }

    @Test
    void losingGrantedAbilityStopsHandSizePowerEffect() {
        Permanent msMarvel = harness.addToBattlefieldAndReturn(player1, new MsMarvelKamalaKhan());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new Shock()));
        harness.setHand(player1, List.of(new GiantGrowth(), new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Grizzly Bears"));
        resolveAllTriggers();
        assertThat(gqs.getEffectivePower(gd, msMarvel)).isEqualTo(3);

        harness.setLibrary(player2, List.of(new Shock()));
        harness.setHand(player2, List.of(new DressDown()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.castEnchantment(player2, 0);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, msMarvel)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, msMarvel)).isEqualTo(4);
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
