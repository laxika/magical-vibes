package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyStrength;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;

import java.util.List;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FaceOfDivinity.class, GrizzlyBears.class, HolyStrength.class})
class FaceOfDivinityTest extends BaseCardTest {

    @Test
    @DisplayName("Gives +2/+2 without the bonus keywords when it is the only Aura")
    void onlyAuraProvidesBoost() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent face = harness.addToBattlefieldAndReturn(player1, new FaceOfDivinity());
        face.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Grants first strike and lifelink while another Aura is attached")
    void anotherAuraProvidesKeywords() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent face = harness.addToBattlefieldAndReturn(player1, new FaceOfDivinity());
        face.setAttachedTo(creature.getId());
        Permanent otherAura = harness.addToBattlefieldAndReturn(player1, new HolyStrength());
        otherAura.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(6);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.LIFELINK)).isTrue();
    }

    @Test
    @DisplayName("Loses the bonus keywords when the other Aura leaves")
    void keywordsDisappearWhenOtherAuraLeaves() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent face = harness.addToBattlefieldAndReturn(player1, new FaceOfDivinity());
        face.setAttachedTo(creature.getId());
        Permanent otherAura = harness.addToBattlefieldAndReturn(player1, new HolyStrength());
        otherAura.setAttachedTo(creature.getId());

        gd.playerBattlefields.get(player1.getId()).remove(otherAura);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.LIFELINK)).isFalse();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
    }

    @Test
    @DisplayName("Resolves attached to an opponent's creature and boosts it")
    void resolvesOnOpponentsCreature() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new FaceOfDivinity()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        Permanent face = findPermanent(player1, "Face of Divinity");
        assertThat(face.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("An Aura controlled by the opponent satisfies the condition")
    void opponentsAuraProvidesKeywords() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent face = harness.addToBattlefieldAndReturn(player1, new FaceOfDivinity());
        face.setAttachedTo(creature.getId());
        Permanent otherAura = harness.addToBattlefieldAndReturn(player2, new HolyStrength());
        otherAura.setAttachedTo(creature.getId());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.LIFELINK)).isTrue();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(6);
    }

    @Test
    @DisplayName("An Aura on another creature does not satisfy the condition")
    void auraOnDifferentCreatureDoesNotProvideKeywords() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent otherCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent face = harness.addToBattlefieldAndReturn(player1, new FaceOfDivinity());
        face.setAttachedTo(creature.getId());
        Permanent otherAura = harness.addToBattlefieldAndReturn(player1, new HolyStrength());
        otherAura.setAttachedTo(otherCreature.getId());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.LIFELINK)).isFalse();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, otherCreature, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, otherCreature, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Two Faces of Divinity satisfy each other's condition")
    void secondCopyProvidesKeywords() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new FaceOfDivinity());
        first.setAttachedTo(creature.getId());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new FaceOfDivinity());
        second.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(6);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.LIFELINK)).isTrue();
    }
}