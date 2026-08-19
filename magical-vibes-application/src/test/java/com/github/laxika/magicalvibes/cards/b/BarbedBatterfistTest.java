package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BarbedBatterfistTest extends BaseCardTest {

    @Test
    @DisplayName("Entering Barbed Batterfist creates and equips a 2/2 Rebel token")
    void enteringCreatesAndEquipsRebel() {
        harness.setHand(player1, List.of(new BarbedBatterfist()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

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
        harness.passBothPriorities();
        harness.passBothPriorities();

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
}
