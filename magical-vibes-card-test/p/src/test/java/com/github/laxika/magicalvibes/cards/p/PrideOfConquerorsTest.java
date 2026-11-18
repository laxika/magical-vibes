package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.SunSentinel;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PrideOfConquerors.class, SunSentinel.class, Forest.class})
class PrideOfConquerorsTest extends BaseCardTest {

    @Test
    @DisplayName("Without the city's blessing, creatures you control get +1/+1")
    void boostsOwnCreaturesWithoutBlessing() {
        Permanent creature = addCreatureAndForests(8);
        harness.setHand(player1, List.of(new PrideOfConquerors()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }

    @Test
    @DisplayName("With ten permanents, ascend gives the city's blessing before the +2/+2 boost")
    void ascendsAndBoostsByTwo() {
        Permanent creature = addCreatureAndForests(9);
        harness.setHand(player1, List.of(new PrideOfConquerors()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        assertThat(gd.playersWithCityBlessing).contains(player1.getId());
    }

    @Test
    @DisplayName("The city's blessing remains after the controller has fewer than ten permanents")
    void blessingPersists() {
        addCreatureAndForests(9);
        harness.setHand(player1, List.of(new PrideOfConquerors()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0);

        gd.playerBattlefields.get(player1.getId()).removeLast();
        gd.playerBattlefields.get(player1.getId()).removeLast();
        Permanent newCreature = harness.addToBattlefieldAndReturn(player1, new SunSentinel());

        harness.setHand(player1, List.of(new PrideOfConquerors()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0);

        assertThat(gqs.getEffectivePower(gd, newCreature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, newCreature)).isEqualTo(4);
    }

    @Test
    @DisplayName("Only creatures controlled at resolution receive the boost")
    void boostsOnlyCurrentOwnCreatures() {
        Permanent first = addCreatureAndForests(0);
        Permanent second = harness.addToBattlefieldAndReturn(player1, new SunSentinel());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new SunSentinel());
        harness.setHand(player1, List.of(new PrideOfConquerors()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveInstant(player1, 0);
        Permanent later = harness.addToBattlefieldAndReturn(player1, new SunSentinel());

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, opponent)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponent)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, later)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, later)).isEqualTo(2);
        assertThat(gd.playersWithCityBlessing).doesNotContain(player1.getId());
    }

    @Test
    @DisplayName("The enhanced boost expires at end of turn but the blessing remains")
    void boostExpiresAtEndOfTurn() {
        Permanent creature = addCreatureAndForests(9);
        harness.setHand(player1, List.of(new PrideOfConquerors()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castAndResolveInstant(player1, 0);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gd.playersWithCityBlessing).contains(player1.getId());
    }

    @Test
    @DisplayName("Ascend counts permanents at resolution rather than when cast")
    void gainsTenthPermanentBeforeResolution() {
        Permanent creature = addCreatureAndForests(8);
        harness.setHand(player1, List.of(new PrideOfConquerors()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castInstant(player1, 0);

        assertThat(gd.playersWithCityBlessing).doesNotContain(player1.getId());
        harness.addToBattlefield(player1, new Forest());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        assertThat(gd.playersWithCityBlessing).contains(player1.getId());
    }

    @Test
    @DisplayName("Ten permanents at casting do not grant the blessing if one leaves before resolution")
    void losesTenthPermanentBeforeResolution() {
        Permanent creature = addCreatureAndForests(9);
        harness.setHand(player1, List.of(new PrideOfConquerors()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castInstant(player1, 0);

        assertThat(gd.playersWithCityBlessing).doesNotContain(player1.getId());
        gd.playerBattlefields.get(player1.getId()).removeLast();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
        assertThat(gd.playersWithCityBlessing).doesNotContain(player1.getId());
    }

    private Permanent addCreatureAndForests(int forestCount) {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new SunSentinel());
        for (int i = 0; i < forestCount; i++) {
            harness.addToBattlefield(player1, new Forest());
        }
        return creature;
    }
}
