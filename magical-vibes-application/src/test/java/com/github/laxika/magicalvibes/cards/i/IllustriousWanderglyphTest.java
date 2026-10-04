package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.b.BottleGnomes;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IllustriousWanderglyph.class, BottleGnomes.class, GrizzlyBears.class, Forest.class})
class IllustriousWanderglyphTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a Gnome artifact creature during each upkeep")
    void createsGnomeDuringEachUpkeep() {
        harness.addToBattlefield(player1, new IllustriousWanderglyph());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        advanceToUpkeep(player2);
        harness.passBothPriorities();

        List<Permanent> gnomes = findPermanents(player1, "Gnome").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();

        assertThat(gnomes).hasSize(2);
        assertThat(gnomes).allSatisfy(gnome -> {
            assertThat(gnome.getCard().getSubtypes()).contains(CardSubtype.GNOME);
            assertThat(gqs.isArtifact(gd, gnome)).isTrue();
            assertThat(gqs.isCreature(gd, gnome)).isTrue();
            assertThat(gqs.getEffectivePower(gd, gnome)).isEqualTo(1);
            assertThat(gqs.getEffectiveToughness(gd, gnome)).isEqualTo(1);
        });
    }

    @Test
    @DisplayName("The city's blessing gives other artifact creatures +2/+2")
    void blessingBoostsOtherArtifactCreatures() {
        Permanent wanderglyph = addCreatureReady(player1, new IllustriousWanderglyph());
        Permanent artifactCreature = addCreatureReady(player1, new BottleGnomes());
        Permanent nonArtifactCreature = addCreatureReady(player1, new GrizzlyBears());
        gd.playersWithCityBlessing.add(player1.getId());

        assertThat(gqs.getEffectivePower(gd, wanderglyph)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, wanderglyph)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, artifactCreature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, artifactCreature)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, nonArtifactCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, nonArtifactCreature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Gains the city's blessing when it enters as the tenth permanent")
    void gainsBlessingAsTenthPermanent() {
        for (int i = 0; i < 9; i++) {
            harness.addToBattlefield(player1, new Forest());
        }
        harness.castFromHand(player1, new IllustriousWanderglyph(), "{4}{W}");
        harness.passBothPriorities();

        assertThat(gd.playersWithCityBlessing).contains(player1.getId());
    }

    @Test
    void noBoostWithoutControllersBlessing() {
        harness.addToBattlefield(player1, new IllustriousWanderglyph());
        Permanent gnomes = addCreatureReady(player1, new BottleGnomes());
        gd.playersWithCityBlessing.add(player2.getId());

        assertThat(gqs.getEffectivePower(gd, gnomes)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, gnomes)).isEqualTo(3);
    }

    @Test
    void multipleWanderglyphsBoostEachOtherButNotOpponents() {
        Permanent first = addCreatureReady(player1, new IllustriousWanderglyph());
        Permanent second = addCreatureReady(player1, new IllustriousWanderglyph());
        Permanent ownGnomes = addCreatureReady(player1, new BottleGnomes());
        Permanent opposingGnomes = addCreatureReady(player2, new BottleGnomes());
        gd.playersWithCityBlessing.add(player1.getId());

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, ownGnomes)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, ownGnomes)).isEqualTo(7);
        assertThat(gqs.getEffectivePower(gd, opposingGnomes)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, opposingGnomes)).isEqualTo(3);
    }

    @Test
    void upkeepTokenGrantsBlessingAsTenthPermanent() {
        harness.addToBattlefield(player1, new IllustriousWanderglyph());
        for (int i = 0; i < 8; i++) {
            harness.addToBattlefield(player1, new Forest());
        }

        advanceToUpkeep(player2);
        assertThat(gd.playersWithCityBlessing).doesNotContain(player1.getId());
        assertThat(countPermanents(player1, "Gnome")).isZero();
        harness.passBothPriorities();

        assertThat(gd.playersWithCityBlessing).contains(player1.getId());
        Permanent token = findPermanent(player1, "Gnome");
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(3);
        assertThat(countPermanents(player2, "Gnome")).isZero();

        gd.playerBattlefields.get(player1.getId()).removeIf(permanent -> permanent.getCard() instanceof Forest);
        assertThat(gd.playersWithCityBlessing).contains(player1.getId());
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(3);
    }

    @Test
    void upkeepTriggerStillCreatesTokenAfterSourceLeaves() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new IllustriousWanderglyph());
        advanceToUpkeep(player2);
        gd.playerBattlefields.get(player1.getId()).remove(source);
        gd.playerGraveyards.get(player1.getId()).add(source.getCard());
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Gnome")).isEqualTo(1);
        assertThat(countPermanents(player2, "Gnome")).isZero();
        Permanent token = findPermanent(player1, "Gnome");
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(1);
    }
}
