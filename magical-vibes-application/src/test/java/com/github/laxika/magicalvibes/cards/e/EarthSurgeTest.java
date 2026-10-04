package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.a.AshayaSoulOfTheWild;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.n.NaturesRevolt;
import com.github.laxika.magicalvibes.cards.o.Opalescence;
import com.github.laxika.magicalvibes.cards.r.RumblingSlum;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EarthSurge.class, Forest.class, Mountain.class, NaturesRevolt.class, RumblingSlum.class,
        AshayaSoulOfTheWild.class, Opalescence.class})
class EarthSurgeTest extends BaseCardTest {

    @Test
    @DisplayName("Creature lands get +2/+2")
    void boostsCreatureLands() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent mountain = harness.addToBattlefieldAndReturn(player2, new Mountain());
        harness.addToBattlefield(player1, new NaturesRevolt());
        harness.addToBattlefield(player1, new EarthSurge());

        assertThat(gqs.isCreature(gd, forest)).isTrue();
        assertThat(gqs.getEffectivePower(gd, forest)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, forest)).isEqualTo(4);
        assertThat(gqs.isLand(gd, forest)).isTrue();

        assertThat(gqs.isCreature(gd, mountain)).isTrue();
        assertThat(gqs.getEffectivePower(gd, mountain)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, mountain)).isEqualTo(4);
    }

    @Test
    @DisplayName("Noncreature lands are not boosted")
    void doesNotBoostNoncreatureLands() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addToBattlefield(player1, new EarthSurge());

        assertThat(gqs.isCreature(gd, forest)).isFalse();
        assertThat(gqs.getEffectivePower(gd, forest)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, forest)).isEqualTo(0);
    }

    @Test
    @DisplayName("The boost disappears when the creature-making effect leaves")
    void boostDisappearsWhenLandStopsBeingCreature() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addToBattlefield(player1, new NaturesRevolt());
        harness.addToBattlefield(player1, new EarthSurge());

        assertThat(gqs.getEffectivePower(gd, forest)).isEqualTo(4);

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Nature's Revolt"));

        assertThat(gqs.isCreature(gd, forest)).isFalse();
        assertThat(gqs.getEffectivePower(gd, forest)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, forest)).isEqualTo(0);
    }

    @Test
    @DisplayName("Does not boost nonland creatures")
    void doesNotBoostNonlandCreatures() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new RumblingSlum());
        harness.addToBattlefield(player1, new EarthSurge());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(5);
    }

    @Test
    @DisplayName("Earth Surge boosts lands animated after it enters")
    void boostsLandsAnimatedLater() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addToBattlefield(player1, new EarthSurge());

        assertThat(gqs.isCreature(gd, forest)).isFalse();

        harness.addToBattlefield(player2, new NaturesRevolt());

        assertThat(gqs.getEffectivePower(gd, forest)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, forest)).isEqualTo(4);
    }

    @Test
    @DisplayName("Creature lands entering later receive the boost")
    void boostsLandsEnteringLater() {
        harness.addToBattlefield(player1, new EarthSurge());
        harness.addToBattlefield(player1, new NaturesRevolt());

        Permanent mountain = harness.enterBattlefieldAndReturn(player2, new Mountain());

        assertThat(gqs.getEffectivePower(gd, mountain)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, mountain)).isEqualTo(4);
    }

    @Test
    @DisplayName("Earth Surges on either battlefield stack and stop boosting when removed")
    void multipleCopiesStackAndRemovalUpdatesBoost() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent mountain = harness.addToBattlefieldAndReturn(player2, new Mountain());
        harness.addToBattlefield(player1, new NaturesRevolt());
        Permanent firstSurge = harness.addToBattlefieldAndReturn(player1, new EarthSurge());
        Permanent secondSurge = harness.addToBattlefieldAndReturn(player2, new EarthSurge());

        assertThat(gqs.getEffectivePower(gd, forest)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, forest)).isEqualTo(6);
        assertThat(gqs.getEffectivePower(gd, mountain)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, mountain)).isEqualTo(6);

        gd.playerBattlefields.get(player1.getId()).remove(firstSurge);

        assertThat(gqs.getEffectivePower(gd, forest)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, mountain)).isEqualTo(4);

        gd.playerBattlefields.get(player2.getId()).remove(secondSurge);

        assertThat(gqs.isCreature(gd, forest)).isTrue();
        assertThat(gqs.getEffectivePower(gd, forest)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, forest)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, mountain)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, mountain)).isEqualTo(2);
    }

    @Test
    @CardUsed({EarthSurge.class, AshayaSoulOfTheWild.class, Opalescence.class})
    @DisplayName("Earth Surge boosts itself when it becomes a creature land")
    void boostsItselfWhenItBecomesCreatureLand() {
        Permanent surge = harness.addToBattlefieldAndReturn(player1, new EarthSurge());
        harness.addToBattlefield(player1, new Opalescence());
        harness.addToBattlefield(player1, new AshayaSoulOfTheWild());

        assertThat(gqs.isCreature(gd, surge)).isTrue();
        assertThat(gqs.isLand(gd, surge)).isTrue();
        assertThat(gqs.getEffectivePower(gd, surge)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, surge)).isEqualTo(6);
    }
}
