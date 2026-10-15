package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MightOfOaks;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RunescaleStormbrood.class, GrizzlyBears.class, MightOfOaks.class, Shock.class})
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

        harness.castFromHand(player1, new RunescaleStormbrood(), "{3}{R}");
        harness.passBothPriorities();

        assertThat(stormbrood.getEffectivePower()).isEqualTo(4);
    }

    @Test
    @DisplayName("Runescale Stormbrood does not trigger for a non-Dragon creature spell")
    void nonDragonCreatureDoesNotBoostStormbrood() {
        Permanent stormbrood = castStormbrood();

        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(stormbrood.getEffectivePower()).isEqualTo(2);
    }

    @Test
    @DisplayName("Omen counters a spell with mana value 2 or less and shuffles into its owner's library")
    void omenCountersSmallSpellAndShuffles() {
        Shock spell = new Shock();
        RunescaleStormbrood card = new RunescaleStormbrood();
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(card));
        harness.setHand(player2, List.of(spell));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, target.getId());
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
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(card));
        harness.setHand(player2, List.of(spell));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.GREEN, 4);

        harness.castInstant(player2, 0, target.getId());

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

    @Test
    void opponentSpellDoesNotBoostStormbrood() {
        Permanent stormbrood = castStormbrood();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, player1.getId());
        resolveAllTriggers();

        assertThat(stormbrood.getEffectivePower()).isEqualTo(2);
    }

    @Test
    void successiveSpellsGiveCumulativeBoosts() {
        Permanent stormbrood = castStormbrood();
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();
        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(stormbrood.getEffectivePower()).isEqualTo(6);
        assertThat(stormbrood.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    void omenCountersCreatureAtManaValueTwo() {
        GrizzlyBears spell = new GrizzlyBears();
        RunescaleStormbrood card = new RunescaleStormbrood();
        harness.castFromHand(player1, spell, "{1}{G}");
        harness.setHand(player2, List.of(card));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castWithAlternateCost(player2, 0, spell.getId());
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(spell);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).contains(card);
        assertThat(gd.playerGraveyards.get(player2.getId())).doesNotContain(card);
    }

    @Test
    void omenCastBoostsStormbroodOnce() {
        Permanent stormbrood = castStormbrood();
        Shock spell = new Shock();
        RunescaleStormbrood omen = new RunescaleStormbrood();
        harness.setHand(player2, List.of(spell));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.setHand(player1, List.of(omen));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player2, 0, player1.getId());
        harness.castWithAlternateCost(player1, 0, spell.getId());
        resolveAllTriggers();

        assertThat(stormbrood.getEffectivePower()).isEqualTo(4);
        assertThat(stormbrood.getEffectiveToughness()).isEqualTo(4);
        assertThat(gd.playerDecks.get(player1.getId())).contains(omen);
    }

    @Test
    void omenCanCounterAnotherOmenUsingItsOwnManaValue() {
        Shock spell = new Shock();
        RunescaleStormbrood firstOmen = new RunescaleStormbrood();
        RunescaleStormbrood secondOmen = new RunescaleStormbrood();
        harness.setHand(player1, List.of(spell, secondOmen));
        harness.setHand(player2, List.of(firstOmen));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.castWithAlternateCost(player2, 0, spell.getId());
        harness.castWithAlternateCost(player1, 0, firstOmen.getId());
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player2.getId())).contains(firstOmen);
        assertThat(gd.playerDecks.get(player2.getId())).doesNotContain(firstOmen);
        assertThat(gd.playerDecks.get(player1.getId())).contains(secondOmen);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(spell);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    void omenWithMissingTargetGoesToGraveyardInsteadOfLibrary() {
        Shock spell = new Shock();
        RunescaleStormbrood firstOmen = new RunescaleStormbrood();
        RunescaleStormbrood secondOmen = new RunescaleStormbrood();
        harness.setHand(player1, List.of(spell, secondOmen));
        harness.setHand(player2, List.of(firstOmen));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.castWithAlternateCost(player2, 0, spell.getId());
        harness.castWithAlternateCost(player1, 0, spell.getId());
        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(spell);
        assertThat(gd.playerDecks.get(player1.getId())).contains(secondOmen);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(firstOmen);
        assertThat(gd.playerDecks.get(player2.getId())).doesNotContain(firstOmen);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }
    private Permanent castStormbrood() {
        harness.castFromHand(player1, new RunescaleStormbrood(), "{3}{R}");
        harness.passBothPriorities();
        return findPermanent(player1, "Runescale Stormbrood");
    }
}
