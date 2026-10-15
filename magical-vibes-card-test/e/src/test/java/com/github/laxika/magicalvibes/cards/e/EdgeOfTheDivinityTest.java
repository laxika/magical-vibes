package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.cards.d.DeathbringerLiege;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.z.ZombieGoliath;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EdgeOfTheDivinity.class, EliteVanguard.class, ZombieGoliath.class,
        DeathbringerLiege.class, GrizzlyBears.class, FountainOfYouth.class})
class EdgeOfTheDivinityTest extends BaseCardTest {

    private Permanent attach(Permanent creature) {
        Permanent edge = harness.addToBattlefieldAndReturn(player1, new EdgeOfTheDivinity());
        edge.setAttachedTo(creature.getId());
        return edge;
    }

    @Test
    @DisplayName("White enchanted creature gets +1/+2 only")
    void whiteCreatureGetsPlusOnePlusTwo() {
        Permanent white = harness.addToBattlefieldAndReturn(player1, new EliteVanguard());
        int basePower = gqs.getEffectivePower(gd, white);
        int baseToughness = gqs.getEffectiveToughness(gd, white);

        attach(white);

        assertThat(gqs.getEffectivePower(gd, white)).isEqualTo(basePower + 1);
        assertThat(gqs.getEffectiveToughness(gd, white)).isEqualTo(baseToughness + 2);
    }

    @Test
    @DisplayName("Black enchanted creature gets +2/+1 only")
    void blackCreatureGetsPlusTwoPlusOne() {
        Permanent black = harness.addToBattlefieldAndReturn(player1, new ZombieGoliath());
        int basePower = gqs.getEffectivePower(gd, black);
        int baseToughness = gqs.getEffectiveToughness(gd, black);

        attach(black);

        assertThat(gqs.getEffectivePower(gd, black)).isEqualTo(basePower + 2);
        assertThat(gqs.getEffectiveToughness(gd, black)).isEqualTo(baseToughness + 1);
    }

    @Test
    @DisplayName("White-black enchanted creature gets both boosts (+3/+3)")
    void whiteBlackCreatureGetsBothBoosts() {
        Permanent gold = harness.addToBattlefieldAndReturn(player1, new DeathbringerLiege());
        int basePower = gqs.getEffectivePower(gd, gold);
        int baseToughness = gqs.getEffectiveToughness(gd, gold);

        attach(gold);

        assertThat(gqs.getEffectivePower(gd, gold)).isEqualTo(basePower + 3);
        assertThat(gqs.getEffectiveToughness(gd, gold)).isEqualTo(baseToughness + 3);
    }

    @Test
    @DisplayName("Non-white, non-black enchanted creature gets no boost")
    void otherColorCreatureUnaffected() {
        Permanent green = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        int basePower = gqs.getEffectivePower(gd, green);
        int baseToughness = gqs.getEffectiveToughness(gd, green);

        attach(green);

        assertThat(gqs.getEffectivePower(gd, green)).isEqualTo(basePower);
        assertThat(gqs.getEffectiveToughness(gd, green)).isEqualTo(baseToughness);
    }

    @Test
    @DisplayName("Boost wears off when Edge of the Divinity is removed")
    void boostRemovedWhenAuraLeaves() {
        Permanent white = harness.addToBattlefieldAndReturn(player1, new EliteVanguard());
        int basePower = gqs.getEffectivePower(gd, white);

        Permanent edge = attach(white);
        assertThat(gqs.getEffectivePower(gd, white)).isEqualTo(basePower + 1);

        gd.playerBattlefields.get(player1.getId()).remove(edge);

        assertThat(gqs.getEffectivePower(gd, white)).isEqualTo(basePower);
    }

    @Test
    @DisplayName("Resolving Edge of the Divinity attaches it to target creature")
    void resolvingAttachesToTarget() {
        Permanent white = harness.addToBattlefieldAndReturn(player2, new EliteVanguard());

        harness.setHand(player1, List.of(new EdgeOfTheDivinity()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castEnchantment(player1, 0, white.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Edge of the Divinity")
                        && p.isAttached()
                        && p.getAttachedTo().equals(white.getId()));
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent with Edge of the Divinity")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new EdgeOfTheDivinity()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        Permanent artifact = findPermanent(player1, "Fountain of Youth");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Multiple copies stack their bonuses only on the enchanted creature")
    void multipleAurasStackOnlyOnEnchantedCreature() {
        Permanent enchanted = harness.addToBattlefieldAndReturn(player1, new DeathbringerLiege());
        Permanent other = harness.addToBattlefieldAndReturn(player2, new DeathbringerLiege());
        int basePower = gqs.getEffectivePower(gd, enchanted);
        int baseToughness = gqs.getEffectiveToughness(gd, enchanted);
        int otherPower = gqs.getEffectivePower(gd, other);
        int otherToughness = gqs.getEffectiveToughness(gd, other);

        attach(enchanted);
        attach(enchanted);

        assertThat(gqs.getEffectivePower(gd, enchanted)).isEqualTo(basePower + 6);
        assertThat(gqs.getEffectiveToughness(gd, enchanted)).isEqualTo(baseToughness + 6);
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(otherPower);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(otherToughness);
    }

    @Test
    @DisplayName("Aura goes to the graveyard if its target leaves before resolution")
    void targetLeavesBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new EliteVanguard());
        harness.setHand(player1, List.of(new EdgeOfTheDivinity()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castEnchantment(player1, 0, target.getId());

        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerGraveyards.get(player2.getId()).add(target.getCard());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player1, "Edge of the Divinity");
        harness.assertInGraveyard(player1, "Edge of the Divinity");
    }
}
