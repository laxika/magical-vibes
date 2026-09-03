package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.AncientBrontodon;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FlowstoneEmbrace.class, AncientBrontodon.class, Mountain.class})
class FlowstoneEmbraceTest extends BaseCardTest {

    @Test
    void activatedAbilityBoostsEnchantedCreature() {
        Permanent giant = addCreatureReady(player1, new AncientBrontodon());
        Permanent aura = new Permanent(new FlowstoneEmbrace());
        aura.setAttachedTo(giant.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, giant)).isEqualTo(11);
        assertThat(gqs.getEffectiveToughness(gd, giant)).isEqualTo(7);
    }

    @Test
    void activatedAbilityBoostWearsOffAtEndOfTurn() {
        Permanent giant = addCreatureReady(player1, new AncientBrontodon());
        Permanent aura = new Permanent(new FlowstoneEmbrace());
        aura.setAttachedTo(giant.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, giant)).isEqualTo(9);
        assertThat(gqs.getEffectiveToughness(gd, giant)).isEqualTo(9);
    }

    @Test
    void cannotEnchantALand() {
        harness.addToBattlefield(player1, new Mountain());
        harness.setHand(player1, List.of(new FlowstoneEmbrace()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        Permanent mountain = findPermanent(player1, "Mountain");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, mountain.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
