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

    private Permanent addRingReady(Player player) {
        Permanent ring = new Permanent(new LotusRing());
        ring.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(ring);
        return ring;
    }
}
