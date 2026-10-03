package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.MachineOverMatter;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CombatThresher.class, Forest.class, MachineOverMatter.class})
class CombatThresherTest extends BaseCardTest {

    @Test
    void enteringTheBattlefieldDrawsACard() {
        harness.setHand(player1, List.of(new CombatThresher()));
        harness.addMana(player1, ManaColor.COLORLESS, 7);
        harness.setLibrary(player1, List.of(new Forest()));

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInHand(player1, "Forest");
    }

    @Test
    void prototypeCastUsesAlternateCharacteristicsAndStillDraws() {
        harness.setHand(player1, List.of(new CombatThresher()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setLibrary(player1, List.of(new Forest()));

        harness.castCreatureWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();

        Permanent thresher = findPermanent(player1, "Combat Thresher");
        assertThat(gqs.getEffectivePower(gd, thresher)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, thresher)).isEqualTo(1);
        assertThat(gqs.getEffectiveColors(gd, thresher)).containsExactly(CardColor.WHITE);

        harness.passBothPriorities();

        harness.assertInHand(player1, "Forest");
    }

    @Test
    void normalCastDealsDamageInBothCombatDamageSteps() {
        harness.setHand(player1, List.of(new CombatThresher()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 7);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        findPermanent(player1, "Combat Thresher").setSummoningSick(false);
        harness.setLife(player2, 20);

        declareAttackers(List.of(0));
        resolveCombat();

        harness.assertLife(player2, 14);
    }

    @Test
    void prototypeRetainsDoubleStrikeInCombat() {
        harness.setHand(player1, List.of(new CombatThresher()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreatureWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();
        harness.passBothPriorities();
        findPermanent(player1, "Combat Thresher").setSummoningSick(false);
        harness.setLife(player2, 20);

        declareAttackers(List.of(0));
        resolveCombat();

        harness.assertLife(player2, 18);
    }

    @Test
    void bouncedPrototypeCanBeRecastWithNormalCharacteristics() {
        harness.setHand(player1, List.of(new CombatThresher(), new MachineOverMatter()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreatureWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();
        harness.passBothPriorities();
        Permanent prototype = findPermanent(player1, "Combat Thresher");
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0, prototype.getId());
        harness.assertNotOnBattlefield(player1, "Combat Thresher");
        harness.assertInHand(player1, "Combat Thresher");
        harness.addMana(player1, ManaColor.COLORLESS, 7);
        int cardIndex = java.util.stream.IntStream.range(0, gd.playerHands.get(player1.getId()).size())
                .filter(i -> gd.playerHands.get(player1.getId()).get(i).getName().equals("Combat Thresher"))
                .findFirst().orElseThrow();
        harness.castCreature(player1, cardIndex);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent recast = findPermanent(player1, "Combat Thresher");
        assertThat(gqs.getEffectivePower(gd, recast)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, recast)).isEqualTo(3);
        assertThat(gqs.getEffectiveColors(gd, recast)).isEmpty();
    }
}
