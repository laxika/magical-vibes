package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.b.BackupAgent;
import com.github.laxika.magicalvibes.cards.e.ElspethResplendent;
import com.github.laxika.magicalvibes.cards.e.ExhibitionMagician;
import com.github.laxika.magicalvibes.cards.g.GlamorousOutlaw;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LightEmUp.class, AirElemental.class, GrizzlyBears.class, Island.class,
        BackupAgent.class, ElspethResplendent.class, ExhibitionMagician.class, GlamorousOutlaw.class})
class LightEmUpTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 2 damage to a creature")
    void dealsDamageToCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.setHand(player1, List.of(new LightEmUp()));
        addMana();

        harness.castAndResolveSorcery(player1, 0, target.getId());

        assertThat(target.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Casualty copies the damage spell")
    void casualtyCopiesSpell() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        Permanent casualtyCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new LightEmUp()));
        addMana();

        harness.castSorceryWithSacrifice(player1, 0, target.getId(), casualtyCreature.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(4);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(casualtyCreature.getId()));
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        harness.setHand(player1, List.of(new LightEmUp()));
        addMana();
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Island());

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature or planeswalker");
    }

    @Test
    void damagesPlaneswalkerWithoutDamagingItsController() {
        Permanent target = harness.enterBattlefieldAndReturn(player2, new ElspethResplendent());
        harness.setHand(player1, List.of(new LightEmUp()));
        addMana();
        int loyaltyBefore = target.getCounterCount(CounterType.LOYALTY);
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.castAndResolveSorcery(player1, 0, target.getId());

        assertThat(target.getCounterCount(CounterType.LOYALTY)).isEqualTo(loyaltyBefore - 2);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore);
    }

    @Test
    void casualtyCopyCanTargetPlaneswalkerWhileOriginalTargetsCreature() {
        Permanent originalTarget = harness.addToBattlefieldAndReturn(player2, new GlamorousOutlaw());
        Permanent copyTarget = harness.enterBattlefieldAndReturn(player2, new ElspethResplendent());
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new ExhibitionMagician());
        harness.setHand(player1, List.of(new LightEmUp()));
        addMana();
        int loyaltyBefore = copyTarget.getCounterCount(CounterType.LOYALTY);

        harness.castSorceryWithSacrifice(player1, 0, originalTarget.getId(), sacrifice.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, copyTarget.getId());
        harness.passBothPriorities();

        assertThat(copyTarget.getCounterCount(CounterType.LOYALTY)).isEqualTo(loyaltyBefore - 2);
        assertThat(originalTarget.getMarkedDamage()).isZero();

        harness.passBothPriorities();

        assertThat(originalTarget.getMarkedDamage()).isEqualTo(2);
        assertThat(copyTarget.getCounterCount(CounterType.LOYALTY)).isEqualTo(loyaltyBefore - 2);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(sacrifice.getCard());
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotPayCasualtyWithPowerBelowTwo() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GlamorousOutlaw());
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new BackupAgent());
        LightEmUp spell = new LightEmUp();
        harness.setHand(player1, List.of(spell));
        addMana();

        assertThatThrownBy(() -> harness.castSorceryWithSacrifice(
                player1, 0, target.getId(), sacrifice.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("power 2 or greater");

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(sacrifice);
        assertThat(gd.playerHands.get(player1.getId())).contains(spell);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotPayCasualtyWithOpponentsCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GlamorousOutlaw());
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player2, new ExhibitionMagician());
        harness.setHand(player1, List.of(new LightEmUp()));
        addMana();

        assertThatThrownBy(() -> harness.castSorceryWithSacrifice(
                player1, 0, target.getId(), sacrifice.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("you control");

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(sacrifice);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotTargetPlayer() {
        harness.setHand(player1, List.of(new LightEmUp()));
        addMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
