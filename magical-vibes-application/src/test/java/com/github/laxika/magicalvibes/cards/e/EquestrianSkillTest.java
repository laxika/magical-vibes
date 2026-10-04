package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HonorGuard;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EquestrianSkill.class, GrizzlyBears.class, HonorGuard.class, FountainOfYouth.class})
class EquestrianSkillTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Equestrian Skill attaches it to the target creature")
    void resolvingAttachesToTarget() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new EquestrianSkill()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.isAttached()
                        && permanent.getAttachedTo().equals(creature.getId()));
    }

    @Test
    @DisplayName("Equestrian Skill gives the enchanted creature +3/+3")
    void enchantedCreatureGetsBoost() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        attachTo(creature);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(5);
    }

    @Test
    @DisplayName("Equestrian Skill gives a Human enchanted creature trample")
    void humanEnchantedCreatureGetsTrample() {
        Permanent creature = addCreatureReady(player1, new HonorGuard());
        attachTo(creature);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Equestrian Skill does not give a non-Human enchanted creature trample")
    void nonHumanEnchantedCreatureDoesNotGetTrample() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        attachTo(creature);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Equestrian Skill cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new EquestrianSkill()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        Permanent artifact = findPermanent(player1, "Fountain of Youth");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Equestrian Skill can enchant an opponent's Human creature")
    void canEnchantOpponentsHuman() {
        Permanent creature = addCreatureReady(player2, new HonorGuard());
        harness.setHand(player1, List.of(new EquestrianSkill()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Equestrian Skill").getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Only the enchanted creature receives Equestrian Skill's benefits")
    void doesNotAffectOtherHumans() {
        Permanent enchanted = addCreatureReady(player1, new GrizzlyBears());
        Permanent otherHuman = addCreatureReady(player1, new HonorGuard());
        attachTo(enchanted);

        assertThat(gqs.getEffectivePower(gd, enchanted)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, enchanted, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.getEffectivePower(gd, otherHuman)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, otherHuman)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, otherHuman, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Equestrian Skill's boost and granted trample end when the Aura leaves")
    void benefitsEndWhenAuraLeaves() {
        Permanent creature = addCreatureReady(player1, new HonorGuard());
        Permanent aura = attachTo(creature);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(aura);
        gd.playerGraveyards.get(player1.getId()).add(aura.getCard());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isFalse();
    }

    private Permanent attachTo(Permanent creature) {
        Permanent aura = new Permanent(new EquestrianSkill());
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);
        return aura;
    }

}
