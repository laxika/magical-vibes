package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.c.CentaurNurturer;
import com.github.laxika.magicalvibes.cards.e.Excruciator;
import com.github.laxika.magicalvibes.cards.g.GoblinAssailant;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JayaVeneratedFiremage;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.p.PaladinEnVec;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BandTogether.class, AirElemental.class, GrizzlyBears.class, LlanowarElves.class,
        CentaurNurturer.class, GoblinAssailant.class, JayaVeneratedFiremage.class, Excruciator.class,
        PaladinEnVec.class})
class BandTogetherTest extends BaseCardTest {

    @Test
    @DisplayName("Each of two chosen creatures deals damage equal to its power")
    void eachChosenCreatureDealsItsPower() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.addToBattlefield(player1, new AirElemental());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.setHand(player1, List.of(new BandTogether()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        List<Permanent> sources = gd.playerBattlefields.get(player1.getId());
        harness.castAndResolveInstant(player1, 0, List.of(target.getId(), sources.get(0).getId(), sources.get(1).getId()));

        assertThat(target.getMarkedDamage()).isEqualTo(3);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("The source group is optional")
    void allowsOneSource() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.setHand(player1, List.of(new BandTogether()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castAndResolveInstant(player1, 0, List.of(target.getId(), source.getId()));

        assertThat(target.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Only creatures you control can be chosen as sources")
    void sourceMustBeControlled() {
        Permanent ownTarget = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentSource = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new BandTogether()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        assertThatThrownBy(() -> harness.castInstant(
                player1, 0, List.of(ownTarget.getId(), opponentSource.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("you control");
    }

    @Test
    @DisplayName("The damage target must be different from every source target")
    void targetMustBeAnotherCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new BandTogether()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        assertThatThrownBy(() -> harness.castInstant(
                player1, 0, List.of(creature.getId(), creature.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("different");
    }

    @Test
    @DisplayName("With no source chosen nothing is dealt")
    void allowsZeroSources() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CentaurNurturer());
        harness.setHand(player1, List.of(new BandTogether()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castAndResolveInstant(player1, 0, List.of(target.getId()));

        assertThat(target.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player1, "Band Together");
    }

    @Test
    @DisplayName("A source that left deals nothing and the other still deals its damage")
    void remainingSourceDealsDamageWhenFirstSourceLeaves() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GoblinAssailant());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GoblinAssailant());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CentaurNurturer());
        harness.setHand(player1, List.of(new BandTogether()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castInstant(player1, 0, List.of(target.getId(), first.getId(), second.getId()));
        gd.playerBattlefields.get(player1.getId()).remove(first);
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(2);
        assertThat(second.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("The damage target may be another creature you control")
    void canDamageAnotherCreatureYouControl() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new GoblinAssailant());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new CentaurNurturer());
        harness.setHand(player1, List.of(new BandTogether()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castAndResolveInstant(player1, 0, List.of(target.getId(), source.getId()));

        assertThat(target.getMarkedDamage()).isEqualTo(2);
        assertThat(source.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("A source you no longer control deals no damage")
    void sourceThatChangesControllerDoesNotDealDamage() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GoblinAssailant());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GoblinAssailant());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CentaurNurturer());
        harness.setHand(player1, List.of(new BandTogether()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castInstant(player1, 0, List.of(target.getId(), first.getId(), second.getId()));
        gd.playerBattlefields.get(player1.getId()).remove(first);
        gd.playerBattlefields.get(player2.getId()).add(first);
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("With the damage target gone the sources deal no damage")
    void missingVictimDoesNotCauseSourcesToDamageEachOther() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GoblinAssailant());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GoblinAssailant());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CentaurNurturer());
        harness.setHand(player1, List.of(new BandTogether()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castInstant(player1, 0, List.of(target.getId(), first.getId(), second.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(target);
        harness.passBothPriorities();

        assertThat(first.getMarkedDamage()).isZero();
        assertThat(second.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(first, second);
        harness.assertInGraveyard(player1, "Band Together");
    }

    @Test
    @DisplayName("A red source's damage gets Jaya's bonus, the creature being the source")
    void redCreatureDamageReceivesJayasBonus() {
        harness.addToBattlefieldAndReturn(player1, new JayaVeneratedFiremage())
                .setCounterCount(com.github.laxika.magicalvibes.model.CounterType.LOYALTY, 5);
        Permanent source = harness.addToBattlefieldAndReturn(player1, new GoblinAssailant());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CentaurNurturer());
        harness.setHand(player1, List.of(new BandTogether()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castAndResolveInstant(player1, 0, List.of(target.getId(), source.getId()));

        assertThat(target.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("Protection from the source's colour prevents a source's damage")
    void protectionFromSourceColourPreventsDamage() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new GoblinAssailant());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new PaladinEnVec());
        harness.setHand(player1, List.of(new BandTogether()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castAndResolveInstant(player1, 0, List.of(target.getId(), source.getId()));

        assertThat(target.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
    }

    @Test
    @DisplayName("Damage from a source whose damage can't be prevented ignores the target's protection")
    void unpreventableSourceIgnoresProtection() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new Excruciator());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new PaladinEnVec());
        harness.setHand(player1, List.of(new BandTogether()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castAndResolveInstant(player1, 0, List.of(target.getId(), source.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        harness.assertInGraveyard(player2, "Paladin en-Vec");
    }
}
