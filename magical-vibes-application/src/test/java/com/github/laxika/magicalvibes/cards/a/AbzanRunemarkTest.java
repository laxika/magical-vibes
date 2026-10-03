package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.ScatheZombies;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AbzanRunemark.class, GrizzlyBears.class, ScatheZombies.class, FountainOfYouth.class})
class AbzanRunemarkTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature gets +2/+2")
    void enchantedCreatureGetsBoost() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        attach(player1, creature);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
    }

    @Test
    @DisplayName("Enchanted creature has vigilance while Aura controller controls a green permanent")
    void vigilanceWithGreenPermanent() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        attach(player1, creature);
        addCreatureReady(player1, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("Enchanted creature has vigilance while Aura controller controls a black permanent")
    void vigilanceWithBlackPermanent() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        attach(player1, creature);
        addCreatureReady(player1, new ScatheZombies());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("Vigilance is absent when only the enchanted creature controller has a qualifying permanent")
    void vigilanceUsesAuraController() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        attach(player1, creature);
        addCreatureReady(player2, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Bonuses disappear when Abzan Runemark leaves the battlefield")
    void bonusesDisappearWhenAuraLeaves() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent aura = attach(player1, creature);
        addCreatureReady(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Abzan Runemark cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new AbzanRunemark()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        Permanent artifact = findPermanent(player1, "Fountain of Youth");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Casting Abzan Runemark attaches it to an opposing creature and grants its bonuses")
    void castingOnOpposingCreature() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player1, new ScatheZombies());
        harness.setHand(player1, List.of(new AbzanRunemark()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        Permanent aura = findPermanent(player1, "Abzan Runemark");
        assertThat(aura.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("Vigilance updates as green and black permanents enter and leave, while the boost remains")
    void vigilanceUpdatesWithQualifyingPermanents() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        attach(player1, creature);
        harness.addToBattlefield(player1, new FountainOfYouth());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isFalse();

        Permanent green = addCreatureReady(player1, new GrizzlyBears());
        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isTrue();
        Permanent black = addCreatureReady(player1, new ScatheZombies());
        gd.playerBattlefields.get(player1.getId()).remove(green);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(black);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
    }

    @Test
    @DisplayName("The enchanted green creature itself satisfies the vigilance condition when controlled by the Aura controller")
    void enchantedCreatureItselfQualifies() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        attach(player1, creature);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isTrue();
        declareAttackers(List.of(0));
        assertThat(creature.isTapped()).isFalse();
    }

    private Permanent attach(Player controller, Permanent creature) {
        Permanent aura = harness.addToBattlefieldAndReturn(controller, new AbzanRunemark());
        aura.setAttachedTo(creature.getId());
        return aura;
    }
}
