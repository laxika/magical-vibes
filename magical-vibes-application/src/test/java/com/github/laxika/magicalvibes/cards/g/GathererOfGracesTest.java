package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.AngelicChorus;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyStrength;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GathererOfGraces.class, AngelicChorus.class, GrizzlyBears.class, HolyStrength.class})
class GathererOfGracesTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +1/+1 for each Aura attached to it")
    void getsBoostForEachAttachedAura() {
        Permanent gatherer = harness.addToBattlefieldAndReturn(player1, new GathererOfGraces());
        Permanent otherCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        Permanent firstAura = new Permanent(new HolyStrength());
        firstAura.setAttachedTo(gatherer.getId());
        Permanent secondAura = new Permanent(new HolyStrength());
        secondAura.setAttachedTo(gatherer.getId());
        Permanent auraAttachedElsewhere = new Permanent(new HolyStrength());
        auraAttachedElsewhere.setAttachedTo(otherCreature.getId());
        gd.playerBattlefields.get(player1.getId()).add(firstAura);
        gd.playerBattlefields.get(player1.getId()).add(secondAura);
        gd.playerBattlefields.get(player1.getId()).add(auraAttachedElsewhere);

        assertThat(gqs.getEffectivePower(gd, gatherer)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, gatherer)).isEqualTo(8);
    }

    @Test
    @DisplayName("Sacrificing an Aura regenerates Gatherer of Graces")
    void sacrificingAuraRegeneratesGatherer() {
        Permanent gatherer = harness.addToBattlefieldAndReturn(player1, new GathererOfGraces());
        Permanent aura = new Permanent(new HolyStrength());
        aura.setAttachedTo(gatherer.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gatherer.getRegenerationShield()).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, gatherer)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, gatherer)).isEqualTo(2);
        harness.assertInGraveyard(player1, "Holy Strength");
    }

    @Test
    @DisplayName("A non-Aura enchantment cannot pay the regeneration cost")
    void nonAuraCannotPayRegenerationCost() {
        harness.addToBattlefield(player1, new GathererOfGraces());
        harness.addToBattlefield(player1, new AngelicChorus());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
