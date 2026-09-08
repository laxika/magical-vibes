package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GiftedAetherborn;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class MidnightEntourageTest extends BaseCardTest {

    @Test
    @DisplayName("Other Aetherborn you control get +1/+1")
    void buffsOtherAetherbornYouControl() {
        harness.addToBattlefield(player1, new GiftedAetherborn());
        Permanent aetherborn = findPermanent(player1, "Gifted Aetherborn");
        int basePower = gqs.getEffectivePower(gd, aetherborn);
        int baseToughness = gqs.getEffectiveToughness(gd, aetherborn);

        harness.addToBattlefield(player1, new MidnightEntourage());

        assertThat(gqs.getEffectivePower(gd, aetherborn)).isEqualTo(basePower + 1);
        assertThat(gqs.getEffectiveToughness(gd, aetherborn)).isEqualTo(baseToughness + 1);
    }

    @Test
    @DisplayName("Midnight Entourage does not buff itself")
    void doesNotBuffItself() {
        MidnightEntourage card = new MidnightEntourage();
        card.setPower(10);
        card.setToughness(10);
        harness.addToBattlefield(player1, card);

        Permanent entourage = findPermanent(player1, "Midnight Entourage");

        assertThat(gqs.getEffectivePower(gd, entourage)).isEqualTo(10);
        assertThat(gqs.getEffectiveToughness(gd, entourage)).isEqualTo(10);
    }

    @Test
    @DisplayName("Non-Aetherborn and opposing Aetherborn are not buffed")
    void doesNotBuffNonAetherbornOrOpposingAetherborn() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GiftedAetherborn());
        Permanent bears = findPermanent(player1, "Grizzly Bears");
        Permanent opposingAetherborn = findPermanent(player2, "Gifted Aetherborn");
        int bearsPower = gqs.getEffectivePower(gd, bears);
        int bearsToughness = gqs.getEffectiveToughness(gd, bears);
        int opposingPower = gqs.getEffectivePower(gd, opposingAetherborn);
        int opposingToughness = gqs.getEffectiveToughness(gd, opposingAetherborn);

        harness.addToBattlefield(player1, new MidnightEntourage());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(bearsPower);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(bearsToughness);
        assertThat(gqs.getEffectivePower(gd, opposingAetherborn)).isEqualTo(opposingPower);
        assertThat(gqs.getEffectiveToughness(gd, opposingAetherborn)).isEqualTo(opposingToughness);
    }

    @Test
    @DisplayName("Another Aetherborn dying draws a card and costs 1 life")
    void anotherAetherbornDeathDrawsAndLosesLife() {
        harness.addToBattlefield(player1, new MidnightEntourage());
        harness.addToBattlefield(player1, new GiftedAetherborn());
        int lifeBefore = gd.getLife(player1.getId());

        killCreature(player1, "Gifted Aetherborn");

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore - 1);
    }

    @Test
    @DisplayName("Midnight Entourage dying triggers its own ability")
    void ownDeathDrawsAndLosesLife() {
        harness.addToBattlefield(player1, new MidnightEntourage());
        int lifeBefore = gd.getLife(player1.getId());

        killCreature(player1, "Midnight Entourage");

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore - 1);
    }

    @Test
    @DisplayName("A non-Aetherborn or opposing Aetherborn dying does not trigger")
    void unrelatedDeathsDoNotTrigger() {
        harness.addToBattlefield(player1, new MidnightEntourage());
        harness.addToBattlefield(player1, new GrizzlyBears());
        int lifeBefore = gd.getLife(player1.getId());

        killCreature(player1, "Grizzly Bears");

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("An opponent's Aetherborn dying does not trigger")
    void opposingAetherbornDeathDoesNotTrigger() {
        harness.addToBattlefield(player1, new MidnightEntourage());
        harness.addToBattlefield(player2, new GiftedAetherborn());
        int lifeBefore = gd.getLife(player1.getId());

        killCreature(player2, "Gifted Aetherborn");

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
    }

    private void killCreature(com.github.laxika.magicalvibes.model.Player owner, String name) {
        harness.setHand(player1, List.of(new Murder()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        UUID targetId = harness.getPermanentId(owner, name);
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
