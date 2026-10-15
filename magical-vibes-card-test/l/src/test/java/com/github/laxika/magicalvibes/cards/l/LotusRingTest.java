package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shatter;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LotusRing.class, GrizzlyBears.class, Shatter.class})
class LotusRingTest extends BaseCardTest {

    @Test
    void equippedCreatureGetsBoostVigilanceAndGrantedAbility() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent ring = addRingReady(player1);
        ring.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    void equipAbilityAttachesRingToCreature() {
        Permanent ring = addRingReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(ring.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    void equippedCreatureCanSacrificeForThreeManaOfOneColor() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent ring = addRingReady(player1);
        ring.setAttachedTo(creature.getId());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, ManaColor.BLUE.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(3);
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ring);
        assertThat(ring.getAttachedTo()).isNull();
    }

    @Test
    void ringSurvivesAffectingDestroyEffect() {
        Permanent ring = addRingReady(player1);
        harness.setHand(player1, java.util.List.of(new Shatter()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, ring.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ring);
    }

    @Test
    void tappedEquippedCreatureCannotActivateManaAbility() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent ring = addRingReady(player1);
        ring.setAttachedTo(creature.getId());
        creature.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature, ring);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    void summoningSickEquippedCreatureCannotActivateManaAbility() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent ring = addRingReady(player1);
        ring.setAttachedTo(creature.getId());
        creature.setSummoningSick(true);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature, ring);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    void reequippingMovesAllGrantedBenefitsToNewCreature() {
        Permanent ring = addRingReady(player1);
        Permanent original = addCreatureReady(player1, new GrizzlyBears());
        Permanent replacement = addCreatureReady(player1, new GrizzlyBears());
        ring.setAttachedTo(original.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, null, replacement.getId());
        harness.passBothPriorities();

        assertThat(ring.getAttachedTo()).isEqualTo(replacement.getId());
        assertThat(gqs.getEffectivePower(gd, original)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, original)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, original, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.getEffectivePower(gd, replacement)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, replacement)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, replacement, Keyword.VIGILANCE)).isTrue();
        assertThatThrownBy(() -> harness.activateAbility(player1, 1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.activateAbility(player1, 2, 0, null, null);
        harness.handleListChoice(player1, ManaColor.GREEN.name());
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(3);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(original, ring)
                .doesNotContain(replacement);
    }

    @Test
    void manaAbilityResolvesWithoutUsingStackAndSacrificesAsCost() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent ring = addRingReady(player1);
        ring.setAttachedTo(creature.getId());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(creature);
        harness.handleListChoice(player1, ManaColor.WHITE.name());
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(3);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(3);
    }

    private Permanent addRingReady(Player player) {
        return addCreatureReady(player, new LotusRing());
    }
}
