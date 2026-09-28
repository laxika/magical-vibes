package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.b.BottleGnomes;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IllustriousWanderglyph.class, BottleGnomes.class, GrizzlyBears.class})
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
        harness.setHand(player1, List.of(new IllustriousWanderglyph()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playersWithCityBlessing).contains(player1.getId());
    }
}
