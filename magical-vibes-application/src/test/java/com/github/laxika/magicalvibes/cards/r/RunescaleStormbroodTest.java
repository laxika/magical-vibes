package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MightOfOaks;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RunescaleStormbroodTest extends BaseCardTest {

    @Test
    @DisplayName("Runescale Stormbrood gets +2/+0 for a noncreature spell")
    void noncreatureSpellBoostsStormbrood() {
        Permanent stormbrood = castStormbrood();

        harness.addMana(player1, ManaColor.RED, 1);
        harness.setHand(player1, List.of(new Shock()));
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(stormbrood.getEffectivePower()).isEqualTo(4);
    }

    @Test
    @DisplayName("Runescale Stormbrood gets +2/+0 for a Dragon spell")
    void dragonSpellBoostsStormbrood() {
        Permanent stormbrood = castStormbrood();

        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.setHand(player1, List.of(new RunescaleStormbrood()));
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(stormbrood.getEffectivePower()).isEqualTo(4);
    }

    @Test
    @DisplayName("Runescale Stormbrood does not trigger for a non-Dragon creature spell")
    void nonDragonCreatureDoesNotBoostStormbrood() {
        Permanent stormbrood = castStormbrood();

        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(stormbrood.getEffectivePower()).isEqualTo(2);
    }

    @Test
    @DisplayName("Omen counters a spell with mana value 2 or less and shuffles into its owner's library")
    void omenCountersSmallSpellAndShuffles() {
        Shock spell = new Shock();
        RunescaleStormbrood card = new RunescaleStormbrood();
        harness.setHand(player1, List.of(card));
        harness.setHand(player2, List.of(spell));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, player1.getId());
        harness.castWithAlternateCost(player1, 0, spell.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).contains(card);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(card);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(spell);
    }

    @Test
    @DisplayName("Omen cannot target a spell with mana value greater than 2")
    void omenRejectsLargeSpell() {
        MightOfOaks spell = new MightOfOaks();
        RunescaleStormbrood card = new RunescaleStormbrood();
        harness.setHand(player1, List.of(card));
        harness.setHand(player2, List.of(spell));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.GREEN, 4);

        harness.castInstant(player2, 0, player1.getId());

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, spell.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("mana value 2 or less");
    }

    @Test
    @DisplayName("Runescale Stormbrood's temporary boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent stormbrood = castStormbrood();

        harness.addMana(player1, ManaColor.RED, 1);
        harness.setHand(player1, List.of(new Shock()));
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        assertThat(stormbrood.getEffectivePower()).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(stormbrood.getEffectivePower()).isEqualTo(2);
    }

    private Permanent castStormbrood() {
        harness.setHand(player1, List.of(new RunescaleStormbrood()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        return findPermanent(player1, "Runescale Stormbrood");
    }
}
