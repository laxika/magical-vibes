package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Glimmerlight.class, GrizzlyBears.class})
class GlimmerlightTest extends BaseCardTest {

    @Test
    void enteringCreatesAGlimmerEnchantmentCreatureToken() {
        harness.setHand(player1, List.of(new Glimmerlight()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        resolveAllTriggers();

        Permanent glimmer = findPermanent(player1, "Glimmer");
        assertThat(glimmer.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(glimmer.getCard().getSubtypes()).containsExactly(CardSubtype.GLIMMER);
        assertThat(glimmer.getCard().hasType(CardType.CREATURE)).isTrue();
        assertThat(glimmer.getCard().hasType(CardType.ENCHANTMENT)).isTrue();
        assertThat(glimmer.getEffectivePower()).isEqualTo(1);
        assertThat(glimmer.getEffectiveToughness()).isEqualTo(1);
        assertThat(glimmer.getCard().isToken()).isTrue();
    }

    @Test
    void equippedCreatureGetsPlusOnePlusOne() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent glimmerlight = addGlimmerlightReady(player1);
        glimmerlight.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }

    @Test
    void equipAbilityAttachesGlimmerlightToAControlledCreature() {
        Permanent glimmerlight = addGlimmerlightReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(glimmerlight.getAttachedTo()).isEqualTo(creature.getId());
    }

    private Permanent addGlimmerlightReady(Player player) {
        Permanent permanent = new Permanent(new Glimmerlight());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
