package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HexgoldSlash;
import com.github.laxika.magicalvibes.cards.s.SerumSnare;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BarbedBatterfist.class, GrizzlyBears.class, HexgoldSlash.class, SerumSnare.class})
class BarbedBatterfistTest extends BaseCardTest {

    @Test
    @DisplayName("Entering Barbed Batterfist creates and equips a 2/2 Rebel token")
    void enteringCreatesAndEquipsRebel() {
        harness.setHand(player1, List.of(new BarbedBatterfist()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castArtifact(player1, 0);
        resolveAllTriggers();

        Permanent batterfist = findPermanent(player1, "Barbed Batterfist");
        Permanent rebel = findPermanent(player1, "Rebel");

        assertThat(rebel.getCard().getPower()).isEqualTo(2);
        assertThat(rebel.getCard().getToughness()).isEqualTo(2);
        assertThat(rebel.getCard().getSubtypes()).contains(CardSubtype.REBEL);
        assertThat(batterfist.getAttachedTo()).isEqualTo(rebel.getId());
        assertThat(gqs.getEffectivePower(gd, rebel)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, rebel)).isEqualTo(1);
    }

    @Test
    @DisplayName("Equip moves Barbed Batterfist and its bonus to another creature")
    void equipMovesBatterfistAndBonus() {
        harness.setHand(player1, List.of(new BarbedBatterfist()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castArtifact(player1, 0);
        resolveAllTriggers();

        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 1);

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        Permanent batterfist = findPermanent(player1, "Barbed Batterfist");
        Permanent rebel = findPermanent(player1, "Rebel");

        assertThat(batterfist.getAttachedTo()).isEqualTo(bears.getId());
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, rebel)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, rebel)).isEqualTo(2);
    }

    @Test
    @DisplayName("For Mirrodin creates its Rebel even if the Equipment leaves before resolution")
    void createsRebelAfterEquipmentLeaves() {
        harness.setHand(player1, List.of(new BarbedBatterfist()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        Permanent batterfist = findPermanent(player1, "Barbed Batterfist");
        harness.setHand(player2, List.of(new SerumSnare()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player2, 0, batterfist.getId());
        harness.assertNotOnBattlefield(player1, "Barbed Batterfist");
        harness.assertInHand(player1, "Barbed Batterfist");
        resolveAllTriggers();

        Permanent rebel = findPermanent(player1, "Rebel");
        assertThat(gqs.getEffectivePower(gd, rebel)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, rebel)).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("The Equipment remains unattached when its Rebel dies")
    void equipmentRemainsAfterRebelDies() {
        harness.setHand(player1, List.of(new BarbedBatterfist()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castArtifact(player1, 0);
        resolveAllTriggers();

        Permanent batterfist = findPermanent(player1, "Barbed Batterfist");
        Permanent rebel = findPermanent(player1, "Rebel");
        harness.setHand(player2, List.of(new HexgoldSlash()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, rebel.getId());

        harness.assertNotOnBattlefield(player1, "Rebel");
        harness.assertOnBattlefield(player1, "Barbed Batterfist");
        assertThat(batterfist.getAttachedTo()).isNull();
    }
}
