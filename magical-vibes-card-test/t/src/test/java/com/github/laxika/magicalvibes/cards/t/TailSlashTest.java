package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TailSlash.class, AirElemental.class, GrizzlyBears.class})
class TailSlashTest extends BaseCardTest {

    @Test
    void controlledCreatureDealsDamageEqualToItsPower() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.setHand(player1, List.of(new TailSlash()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0, List.of(source.getId(), target.getId()));

        assertThat(target.getMarkedDamage()).isEqualTo(2);
        assertThat(source.getMarkedDamage()).isZero();
    }

    @Test
    void sourceMustBeACreatureYouControl() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.setHand(player1, List.of(new TailSlash()));
        addMana();

        assertThatThrownBy(() -> harness.castInstant(
                player1, 0, List.of(opponentCreature.getId(), target.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature you control");
    }

    @Test
    void targetMustBeACreatureAnOpponentControls() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        harness.addToBattlefield(player2, new AirElemental());
        harness.setHand(player1, List.of(new TailSlash()));
        addMana();

        assertThatThrownBy(() -> harness.castInstant(
                player1, 0, List.of(source.getId(), ownCreature.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature an opponent controls");
    }

    @Test
    void usesPowerAtResolution() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.setHand(player1, List.of(new TailSlash()));
        addMana();

        harness.castInstant(player1, 0, List.of(source.getId(), target.getId()));
        source.setPowerModifier(1);
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(3);
        assertThat(source.getMarkedDamage()).isZero();
    }

    @Test
    void negativePowerDealsNoDamage() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        source.setPowerModifier(-3);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.setHand(player1, List.of(new TailSlash()));
        addMana();

        harness.castAndResolveInstant(player1, 0, List.of(source.getId(), target.getId()));

        assertThat(target.getMarkedDamage()).isZero();
    }

    @Test
    void sourceLeavingBeforeResolutionPreventsDamage() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.setHand(player1, List.of(new TailSlash()));
        addMana();

        harness.castInstant(player1, 0, List.of(source.getId(), target.getId()));
        source.setToughnessModifier(-2);
        harness.runStateBasedActions();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(target.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player1, "Tail Slash");
    }

    @Test
    void sourceGainingShroudBeforeResolutionPreventsDamage() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.setHand(player1, List.of(new TailSlash()));
        addMana();

        harness.castInstant(player1, 0, List.of(source.getId(), target.getId()));
        source.getGrantedKeywords().add(Keyword.SHROUD);
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Tail Slash");
    }

    @Test
    void victimGainingShroudBeforeResolutionPreventsDamage() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.setHand(player1, List.of(new TailSlash()));
        addMana();

        harness.castInstant(player1, 0, List.of(source.getId(), target.getId()));
        target.getGrantedKeywords().add(Keyword.SHROUD);
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isZero();
        assertThat(source.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player1, "Tail Slash");
    }

    @Test
    void lethalDamageKillsOnlyTheVictim() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new TailSlash()));
        addMana();

        harness.castAndResolveInstant(player1, 0, List.of(source.getId(), target.getId()));

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Air Elemental");
        assertThat(source.getMarkedDamage()).isZero();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
