package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.MaskwoodNexus;
import com.github.laxika.magicalvibes.cards.m.MomentOfCraving;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TendershootDryad.class, Forest.class, MomentOfCraving.class, MaskwoodNexus.class})
class TendershootDryadTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a Saproling during each upkeep")
    void createsSaprolingDuringEachUpkeep() {
        harness.addToBattlefield(player1, new TendershootDryad());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        advanceToUpkeep(player2);
        harness.passBothPriorities();

        List<Permanent> saprolings = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();

        assertThat(saprolings).hasSize(2);
    }

    @Test
    @DisplayName("The city's blessing gives Saprolings +2/+2")
    void blessingBoostsSaprolings() {
        harness.addToBattlefield(player1, new TendershootDryad());
        gd.playersWithCityBlessing.add(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        Permanent saproling = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();

        assertThat(gqs.getEffectivePower(gd, saproling)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, saproling)).isEqualTo(3);
    }

    @Test
    @DisplayName("Gains the city's blessing when it enters as the tenth permanent")
    void gainsBlessingAsTenthPermanent() {
        for (int i = 0; i < 9; i++) {
            harness.addToBattlefield(player1, new Forest());
        }
        harness.castFromHand(player1, new TendershootDryad(), "{4}{G}");
        harness.passBothPriorities();

        assertThat(gd.playersWithCityBlessing).contains(player1.getId());
    }

    @Test
    void saprolingIsUnboostedWithoutBlessing() {
        harness.addToBattlefield(player1, new TendershootDryad());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        Permanent saproling = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()).findFirst().orElseThrow();
        assertThat(gqs.getEffectivePower(gd, saproling)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, saproling)).isEqualTo(1);
        assertThat(gd.playersWithCityBlessing).doesNotContain(player1.getId());
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    void upkeepTokenGrantsBlessingAsTenthPermanent() {
        harness.addToBattlefield(player1, new TendershootDryad());
        for (int i = 0; i < 8; i++) {
            harness.addToBattlefield(player1, new Forest());
        }

        advanceToUpkeep(player1);
        assertThat(gd.playersWithCityBlessing).doesNotContain(player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playersWithCityBlessing).contains(player1.getId());
        Permanent saproling = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()).findFirst().orElseThrow();
        assertThat(gqs.getEffectivePower(gd, saproling)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, saproling)).isEqualTo(3);
    }

    @Test
    void opponentsPermanentsDoNotCountTowardsAscend() {
        for (int i = 0; i < 8; i++) {
            harness.addToBattlefield(player1, new Forest());
        }
        for (int i = 0; i < 10; i++) {
            harness.addToBattlefield(player2, new Forest());
        }

        harness.castFromHand(player1, new TendershootDryad(), "{4}{G}");
        harness.passBothPriorities();

        assertThat(gd.playersWithCityBlessing).doesNotContain(player1.getId(), player2.getId());
    }

    @Test
    void multipleDryadsStackTheirBoosts() {
        harness.addToBattlefield(player1, new TendershootDryad());
        harness.addToBattlefield(player1, new TendershootDryad());
        gd.playersWithCityBlessing.add(player1.getId());

        advanceToUpkeep(player2);
        harness.passBothPriorities();
        harness.passBothPriorities();

        List<Permanent> saprolings = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()).toList();
        assertThat(saprolings).hasSize(2).allSatisfy(saproling -> {
            assertThat(gqs.getEffectivePower(gd, saproling)).isEqualTo(5);
            assertThat(gqs.getEffectiveToughness(gd, saproling)).isEqualTo(5);
        });
    }

    @Test
    void blessingDoesNotBoostOpposingSaprolingsOrTheDryad() {
        Permanent dryad = harness.addToBattlefieldAndReturn(player1, new TendershootDryad());
        harness.addToBattlefield(player2, new TendershootDryad());
        gd.playersWithCityBlessing.add(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent opposingSaproling = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()).findFirst().orElseThrow();
        assertThat(gqs.getEffectivePower(gd, opposingSaproling)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, opposingSaproling)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, dryad)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, dryad)).isEqualTo(2);
    }

    @Test
    void upkeepTriggerResolvesAfterDryadDiesAndBlessingPersists() {
        Permanent dryad = harness.addToBattlefieldAndReturn(player1, new TendershootDryad());
        gd.playersWithCityBlessing.add(player1.getId());
        harness.setHand(player1, List.of(new MomentOfCraving()));

        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, dryad.getId());
        harness.assertInGraveyard(player1, "Tendershoot Dryad");
        harness.passBothPriorities();

        Permanent saproling = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()).findFirst().orElseThrow();
        assertThat(gd.playersWithCityBlessing).contains(player1.getId());
        assertThat(gqs.getEffectivePower(gd, saproling)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, saproling)).isEqualTo(1);

        harness.enterBattlefieldAndReturn(player1, new TendershootDryad());
        assertThat(gqs.getEffectivePower(gd, saproling)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, saproling)).isEqualTo(3);
    }

    @Test
    void dryadBoostsItselfWhenItIsASaproling() {
        Permanent dryad = harness.addToBattlefieldAndReturn(player1, new TendershootDryad());
        harness.addToBattlefield(player1, new MaskwoodNexus());
        gd.playersWithCityBlessing.add(player1.getId());

        assertThat(gqs.getEffectivePower(gd, dryad)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, dryad)).isEqualTo(4);
    }
}
