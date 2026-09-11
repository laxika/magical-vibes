package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.c.CaptainAmericaSuperSoldier;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LokiGodOfMischief;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.cards.z.ZacamaPrimalCalamity;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({
        MjLnirHammerOfThor.class,
        CaptainAmericaSuperSoldier.class,
        LokiGodOfMischief.class,
        ZacamaPrimalCalamity.class,
        GrizzlyBears.class,
        SerraAngel.class
})
class MjLnirHammerOfThorTest extends BaseCardTest {

    @Test
    @DisplayName("When it enters, Mjolnir deals 4 damage to up to one target creature")
    void entersAndDealsDamageToTargetCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SerraAngel());
        harness.setHand(player1, List.of(new MjLnirHammerOfThor()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castArtifact(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Serra Angel");
    }

    @Test
    @DisplayName("Mjolnir can enter without choosing an ETB target")
    void entersWithoutTarget() {
        harness.setHand(player1, List.of(new MjLnirHammerOfThor()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Mjölnir, Hammer of Thor");
    }

    @Test
    @DisplayName("Equip worthy attaches to a legendary non-Villain red or white creature")
    void equipsWorthyCreature() {
        Permanent captain = harness.addToBattlefieldAndReturn(player1, new CaptainAmericaSuperSoldier());
        Permanent hammer = harness.addToBattlefieldAndReturn(player1, new MjLnirHammerOfThor());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 1, 0, null, captain.getId());
        harness.passBothPriorities();

        assertThat(hammer.getAttachedTo()).isEqualTo(captain.getId());
    }

    @Test
    @DisplayName("Equip worthy cannot attach to a Villain")
    void rejectsVillain() {
        Permanent villain = harness.addToBattlefieldAndReturn(player1, new LokiGodOfMischief());
        harness.addToBattlefieldAndReturn(player1, new MjLnirHammerOfThor());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, 0, null, villain.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("worthy");
    }

    @Test
    @DisplayName("The equipped creature deals double noncombat damage even when another player controls Mjolnir")
    void doublesDamageFromEquippedCreatureRegardlessOfEquipmentController() {
        Permanent hammer = harness.addToBattlefieldAndReturn(player1, new MjLnirHammerOfThor());
        Permanent zacama = harness.addToBattlefieldAndReturn(player2, new ZacamaPrimalCalamity());
        zacama.setSummoningSick(false);
        hammer.setAttachedTo(zacama.getId());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new SerraAngel());
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.addMana(player2, ManaColor.RED, 1);

        harness.activateAbility(player2, 0, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Serra Angel");
    }

    @Test
    @DisplayName("The hand ability discards Mjolnir and deals 2 damage to each creature")
    void handAbilityDamagesEachCreature() {
        harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new MjLnirHammerOfThor()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Mjölnir, Hammer of Thor");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }
}
