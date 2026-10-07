package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.d.DarksteelRelic;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.k.KarnLiberated;
import com.github.laxika.magicalvibes.cards.m.MyrSuperion;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UnholyHeat.class, DarksteelRelic.class, GrizzlyBears.class, HillGiant.class, Pacifism.class,
        Shock.class, KarnLiberated.class, MyrSuperion.class})
class UnholyHeatTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 2 damage without delirium")
    void dealsTwoDamageWithoutDelirium() {
        Permanent target = addHillGiant();

        cast(target);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
    }

    @Test
    @DisplayName("Deals 6 damage with delirium")
    void dealsSixDamageWithDelirium() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new Shock(), new DarksteelRelic(),
                new Pacifism()));
        Permanent target = addHillGiant();

        cast(target);

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
    }

    @Test
    @DisplayName("Cannot target a player")
    void cannotTargetPlayer() {
        harness.setHand(player1, List.of(new UnholyHeat()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Marks exactly 2 damage without delirium")
    void marksExactlyTwoDamage() {
        Permanent target = addHillGiant();

        cast(target);

        assertThat(target.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Removes exactly 2 loyalty without delirium")
    void damagesPlaneswalkerWithoutDelirium() {
        Permanent target = harness.enterBattlefieldAndReturn(player2, new KarnLiberated());

        cast(target);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(target.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
    }

    @Test
    @DisplayName("Removes exactly 6 loyalty with delirium")
    void damagesPlaneswalkerWithDelirium() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new Shock(), new DarksteelRelic(),
                new Pacifism()));
        Permanent target = harness.enterBattlefieldAndReturn(player2, new KarnLiberated());
        target.setCounterCount(CounterType.LOYALTY, 10);

        cast(target);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(target.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
    }

    @Test
    @DisplayName("Four cards with only three types do not enable delirium")
    void duplicateTypesDoNotEnableDelirium() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new HillGiant(), new DarksteelRelic(),
                new Pacifism()));
        Permanent target = addHillGiant();

        cast(target);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(target.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Unholy Heat on the stack does not supply the fourth graveyard type")
    void doesNotCountItselfBeforeFinishingResolution() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new DarksteelRelic(), new Pacifism()));
        Permanent target = addHillGiant();

        cast(target);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(target.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Opponent's graveyard does not enable delirium")
    void ignoresOpponentsGraveyard() {
        harness.setGraveyard(player2, List.of(new GrizzlyBears(), new Shock(), new DarksteelRelic(),
                new Pacifism()));
        Permanent target = addHillGiant();

        cast(target);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(target.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Delirium gained after casting upgrades the damage")
    void checksDeliriumGainedBeforeResolution() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new DarksteelRelic(), new Pacifism()));
        Permanent target = addHillGiant();
        harness.setHand(player1, List.of(new UnholyHeat()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, target.getId());
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new Shock(), new DarksteelRelic(),
                new Pacifism()));

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
    }

    @Test
    @DisplayName("Delirium lost after casting reduces the damage")
    void checksDeliriumLostBeforeResolution() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new Shock(), new DarksteelRelic(),
                new Pacifism()));
        Permanent target = addHillGiant();
        harness.setHand(player1, List.of(new UnholyHeat()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, target.getId());
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new DarksteelRelic(), new Pacifism()));

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(target.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot target a noncreature artifact")
    void cannotTargetNoncreatureArtifact() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DarksteelRelic());
        harness.setHand(player1, List.of(new UnholyHeat()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("An artifact creature supplies two types for delirium")
    void countsAllTypesOnEachGraveyardCard() {
        harness.setGraveyard(player1, List.of(new MyrSuperion(), new Shock(), new Pacifism()));
        Permanent target = addHillGiant();

        cast(target);

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
    }

    private Permanent addHillGiant() {
        return harness.addToBattlefieldAndReturn(player2, new HillGiant());
    }

    private void cast(Permanent target) {
        harness.setHand(player1, List.of(new UnholyHeat()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, target.getId());
    }
}
