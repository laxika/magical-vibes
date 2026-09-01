package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.n.NaturesRevolt;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EarthSurge.class, Forest.class, Mountain.class, NaturesRevolt.class})
class EarthSurgeTest extends BaseCardTest {

    @Test
    @DisplayName("Creature lands get +2/+2")
    void boostsCreatureLands() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Mountain());
        harness.addToBattlefield(player1, new NaturesRevolt());
        harness.addToBattlefield(player1, new EarthSurge());

        Permanent forest = findPermanent(player1, "Forest");
        Permanent mountain = findPermanent(player2, "Mountain");

        assertThat(gqs.isCreature(gd, forest)).isTrue();
        assertThat(gqs.getEffectivePower(gd, forest)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, forest)).isEqualTo(4);
        assertThat(forest.getCard().hasType(CardType.LAND)).isTrue();

        assertThat(gqs.isCreature(gd, mountain)).isTrue();
        assertThat(gqs.getEffectivePower(gd, mountain)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, mountain)).isEqualTo(4);
    }

    @Test
    @DisplayName("Noncreature lands are not boosted")
    void doesNotBoostNoncreatureLands() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new EarthSurge());

        Permanent forest = findPermanent(player1, "Forest");

        assertThat(gqs.isCreature(gd, forest)).isFalse();
        assertThat(gqs.getEffectivePower(gd, forest)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, forest)).isEqualTo(0);
    }

    @Test
    @DisplayName("The boost disappears when the creature-making effect leaves")
    void boostDisappearsWhenLandStopsBeingCreature() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new NaturesRevolt());
        harness.addToBattlefield(player1, new EarthSurge());

        Permanent forest = findPermanent(player1, "Forest");
        assertThat(gqs.getEffectivePower(gd, forest)).isEqualTo(4);

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Nature's Revolt"));

        assertThat(gqs.isCreature(gd, forest)).isFalse();
        assertThat(gqs.getEffectivePower(gd, forest)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, forest)).isEqualTo(0);
    }
}
