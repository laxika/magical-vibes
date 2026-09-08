package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.j.JaceCunningCastaway;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ViviensCrocodileTest extends BaseCardTest {

    @Test
    void getsBonusWhileYouControlVivienPlaneswalker() {
        harness.addToBattlefield(player1, new ViviensCrocodile());
        harness.addToBattlefield(player1, new VivienNaturesAvenger());

        Permanent crocodile = findPermanent(player1, "Vivien's Crocodile");

        assertThat(gqs.getEffectivePower(gd, crocodile)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, crocodile)).isEqualTo(4);
    }

    @Test
    void doesNotGetBonusWithoutVivienPlaneswalker() {
        harness.addToBattlefield(player1, new ViviensCrocodile());

        Permanent crocodile = findPermanent(player1, "Vivien's Crocodile");

        assertThat(gqs.getEffectivePower(gd, crocodile)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, crocodile)).isEqualTo(3);
    }

    @Test
    void doesNotGetBonusForAnotherPlaneswalker() {
        harness.addToBattlefield(player1, new ViviensCrocodile());
        harness.addToBattlefield(player1, new JaceCunningCastaway());

        Permanent crocodile = findPermanent(player1, "Vivien's Crocodile");

        assertThat(gqs.getEffectivePower(gd, crocodile)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, crocodile)).isEqualTo(3);
    }

    @Test
    void bonusIsRemovedWhenVivienLeavesTheBattlefield() {
        harness.addToBattlefield(player1, new ViviensCrocodile());
        harness.addToBattlefield(player1, new VivienNaturesAvenger());

        Permanent crocodile = findPermanent(player1, "Vivien's Crocodile");
        assertThat(gqs.getEffectivePower(gd, crocodile)).isEqualTo(4);

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Vivien, Nature's Avenger"));

        assertThat(gqs.getEffectivePower(gd, crocodile)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, crocodile)).isEqualTo(3);
    }
}
