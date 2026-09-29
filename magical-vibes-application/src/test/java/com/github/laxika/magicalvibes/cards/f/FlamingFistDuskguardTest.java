package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FlamingFistDuskguard.class, GrizzlyBears.class})
class FlamingFistDuskguardTest extends BaseCardTest {

    @Test
    void perpetuallyBoostsTheNextCreatureSpell() {
        FlamingFistDuskguard duskguard = new FlamingFistDuskguard();
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(duskguard, bears));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        Permanent bearPermanent = findPermanent(player1, "Grizzly Bears");
        assertThat(gqs.getEffectivePower(gd, bearPermanent)).isEqualTo(3);
    }

    @Test
    void onlyBoostsOneCreatureSpell() {
        FlamingFistDuskguard duskguard = new FlamingFistDuskguard();
        GrizzlyBears firstBears = new GrizzlyBears();
        GrizzlyBears secondBears = new GrizzlyBears();
        harness.setHand(player1, List.of(duskguard, firstBears, secondBears));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        Permanent firstPermanent = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(firstBears.getId()))
                .findFirst()
                .orElseThrow();
        Permanent secondPermanent = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(secondBears.getId()))
                .findFirst()
                .orElseThrow();
        assertThat(gqs.getEffectivePower(gd, firstPermanent)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, secondPermanent)).isEqualTo(2);
    }
}
