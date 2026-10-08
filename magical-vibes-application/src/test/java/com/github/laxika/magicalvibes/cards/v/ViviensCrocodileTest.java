package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.j.JaceCunningCastaway;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ViviensCrocodile.class, VivienNaturesAvenger.class, JaceCunningCastaway.class,
        VivienArkbowRanger.class})
class ViviensCrocodileTest extends BaseCardTest {

    @Test
    void getsBonusWhileYouControlVivienPlaneswalker() {
        Permanent crocodile = harness.addToBattlefieldAndReturn(player1, new ViviensCrocodile());
        harness.addToBattlefield(player1, new VivienNaturesAvenger());

        assertThat(gqs.getEffectivePower(gd, crocodile)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, crocodile)).isEqualTo(4);
    }

    @Test
    void doesNotGetBonusWithoutVivienPlaneswalker() {
        Permanent crocodile = harness.addToBattlefieldAndReturn(player1, new ViviensCrocodile());

        assertThat(gqs.getEffectivePower(gd, crocodile)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, crocodile)).isEqualTo(3);
    }

    @Test
    void doesNotGetBonusForAnotherPlaneswalker() {
        Permanent crocodile = harness.addToBattlefieldAndReturn(player1, new ViviensCrocodile());
        harness.addToBattlefield(player1, new JaceCunningCastaway());

        assertThat(gqs.getEffectivePower(gd, crocodile)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, crocodile)).isEqualTo(3);
    }

    @Test
    void bonusIsRemovedWhenVivienLeavesTheBattlefield() {
        Permanent crocodile = harness.addToBattlefieldAndReturn(player1, new ViviensCrocodile());
        harness.addToBattlefield(player1, new VivienNaturesAvenger());
        assertThat(gqs.getEffectivePower(gd, crocodile)).isEqualTo(4);

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Vivien, Nature's Avenger"));

        assertThat(gqs.getEffectivePower(gd, crocodile)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, crocodile)).isEqualTo(3);
    }

    @Test
    void opponentsVivienDoesNotGrantBonus() {
        Permanent crocodile = harness.addToBattlefieldAndReturn(player1, new ViviensCrocodile());
        harness.addToBattlefield(player2, new VivienNaturesAvenger());

        assertThat(gqs.getEffectivePower(gd, crocodile)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, crocodile)).isEqualTo(3);
    }

    @Test
    void bonusBeginsWhenVivienEntersTheBattlefield() {
        Permanent crocodile = harness.addToBattlefieldAndReturn(player1, new ViviensCrocodile());
        assertThat(gqs.getEffectivePower(gd, crocodile)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, crocodile)).isEqualTo(3);

        harness.enterBattlefieldAndReturn(player1, new VivienNaturesAvenger());

        assertThat(gqs.getEffectivePower(gd, crocodile)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, crocodile)).isEqualTo(4);
    }

    @Test
    void multipleViviensGrantOnlyOneBonusAndOneRemainingKeepsItActive() {
        Permanent crocodile = harness.addToBattlefieldAndReturn(player1, new ViviensCrocodile());
        harness.addToBattlefield(player1, new VivienNaturesAvenger());
        harness.addToBattlefield(player1, new VivienArkbowRanger());

        assertThat(gqs.getEffectivePower(gd, crocodile)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, crocodile)).isEqualTo(4);

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Vivien, Nature's Avenger"));

        assertThat(gqs.getEffectivePower(gd, crocodile)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, crocodile)).isEqualTo(4);
    }
}
