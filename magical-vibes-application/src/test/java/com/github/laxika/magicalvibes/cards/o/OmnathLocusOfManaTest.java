package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.t.TurnToFrog;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OmnathLocusOfMana.class, TurnToFrog.class})
class OmnathLocusOfManaTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +1/+1 for each unspent green mana its controller has")
    void getsBoostFromUnspentGreenMana() {
        Permanent omnath = addCreatureReady(player1, new OmnathLocusOfMana());
        int basePower = gqs.getEffectivePower(gd, omnath);
        int baseToughness = gqs.getEffectiveToughness(gd, omnath);

        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.addMana(player1, ManaColor.RED, 2);

        assertThat(gqs.getEffectivePower(gd, omnath)).isEqualTo(basePower + 3);
        assertThat(gqs.getEffectiveToughness(gd, omnath)).isEqualTo(baseToughness + 3);
    }

    @Test
    @DisplayName("Preserves only its controller's green mana across a step boundary")
    void preservesOnlyControllersGreenMana() {
        Permanent omnath = addCreatureReady(player1, new OmnathLocusOfMana());
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player2, ManaColor.GREEN, 4);

        advanceToUpkeep(player1);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(3);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(gqs.getEffectivePower(gd, omnath)).isEqualTo(4);
    }

    @Test
    @DisplayName("Counts only green mana remaining after paying its casting cost")
    void countsRemainingManaAfterCasting() {
        harness.setHand(player1, List.of(new OmnathLocusOfMana()));
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent omnath = findPermanent(player1, "Omnath, Locus of Mana");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, omnath)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, omnath)).isEqualTo(4);
    }

    @Test
    @DisplayName("Opponent's green mana does not increase its power or toughness")
    void ignoresOpponentsMana() {
        Permanent omnath = addCreatureReady(player1, new OmnathLocusOfMana());
        int basePower = gqs.getEffectivePower(gd, omnath);
        int baseToughness = gqs.getEffectiveToughness(gd, omnath);

        harness.addMana(player2, ManaColor.GREEN, 5);

        assertThat(gqs.getEffectivePower(gd, omnath)).isEqualTo(basePower);
        assertThat(gqs.getEffectiveToughness(gd, omnath)).isEqualTo(baseToughness);
    }

    @Test
    @DisplayName("Losing all abilities removes the boost and mana retention")
    void losingAbilitiesStopsManaRetention() {
        Permanent omnath = addCreatureReady(player1, new OmnathLocusOfMana());
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.setHand(player2, List.of(new TurnToFrog()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castAndResolveInstant(player2, 0, omnath.getId());

        assertThat(gqs.getEffectivePower(gd, omnath)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, omnath)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(3);

        harness.passUntil(player1, TurnStep.BEGINNING_OF_COMBAT);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }
}
