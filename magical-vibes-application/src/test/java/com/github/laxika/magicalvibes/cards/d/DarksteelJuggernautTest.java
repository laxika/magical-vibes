package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.MustAttackEffect;
import com.github.laxika.magicalvibes.model.effect.PowerToughnessEqualToControlledPermanentCountEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DarksteelJuggernautTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Has P/T equal to controlled artifacts and must attack static effects")
    void hasCorrectEffects() {
        DarksteelJuggernaut card = new DarksteelJuggernaut();

        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(2);
        assertThat(card.getEffects(EffectSlot.STATIC).get(0))
                .isInstanceOf(PowerToughnessEqualToControlledPermanentCountEffect.class);
        assertThat(card.getEffects(EffectSlot.STATIC).get(1))
                .isInstanceOf(MustAttackEffect.class);
    }

    // ===== P/T = number of artifacts you control =====

    @Test
    @DisplayName("P/T is 1/1 when only itself is on the battlefield (it is an artifact)")
    void ptIsOneOneWhenOnlyItself() {
        Permanent juggernaut = addReadyJuggernaut(player1);

        assertThat(gqs.getEffectivePower(gd, juggernaut)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, juggernaut)).isEqualTo(1);
    }

    @Test
    @DisplayName("P/T increases with additional artifacts")
    void ptIncreasesWithArtifacts() {
        Permanent juggernaut = addReadyJuggernaut(player1);
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new Spellbook());

        assertThat(gqs.getEffectivePower(gd, juggernaut)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, juggernaut)).isEqualTo(3);
    }

    @Test
    @DisplayName("P/T only counts controller's artifacts, not opponent's")
    void ptOnlyCountsControllerArtifacts() {
        Permanent juggernaut = addReadyJuggernaut(player1);
        harness.addToBattlefield(player2, new Spellbook());
        harness.addToBattlefield(player2, new Spellbook());

        assertThat(gqs.getEffectivePower(gd, juggernaut)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, juggernaut)).isEqualTo(1);
    }

    @Test
    @DisplayName("Non-artifact creatures do not count toward P/T")
    void nonArtifactCreaturesDoNotCount() {
        Permanent juggernaut = addReadyJuggernaut(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, juggernaut)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, juggernaut)).isEqualTo(1);
    }

    @Test
    @DisplayName("P/T updates as artifacts enter and leave")
    void ptUpdatesAsArtifactsChange() {
        Permanent juggernaut = addReadyJuggernaut(player1);

        assertThat(gqs.getEffectivePower(gd, juggernaut)).isEqualTo(1);

        harness.addToBattlefield(player1, new Spellbook());
        assertThat(gqs.getEffectivePower(gd, juggernaut)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, juggernaut)).isEqualTo(2);

        harness.addToBattlefield(player1, new Spellbook());
        assertThat(gqs.getEffectivePower(gd, juggernaut)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, juggernaut)).isEqualTo(3);

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Spellbook"));
        assertThat(gqs.getEffectivePower(gd, juggernaut)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, juggernaut)).isEqualTo(1);
    }

    // ===== Helpers =====

    private Permanent addReadyJuggernaut(Player player) {
        DarksteelJuggernaut card = new DarksteelJuggernaut();
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
