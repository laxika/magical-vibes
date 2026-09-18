package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.e.Enrage;
import com.github.laxika.magicalvibes.cards.n.NobleTemplar;
import com.github.laxika.magicalvibes.cards.s.ScornfulEgotist;
import com.github.laxika.magicalvibes.cards.u.UnspeakableSymbol;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DaruSpiritualist.class, Enrage.class, NobleTemplar.class, ScornfulEgotist.class,
        UnspeakableSymbol.class})
class DaruSpiritualistTest extends BaseCardTest {

    @Test
    @DisplayName("A Cleric you control gets +0/+2 when targeted by a spell")
    void clericGetsToughnessFromSpellTargeting() {
        harness.addToBattlefield(player1, new DaruSpiritualist());
        harness.addToBattlefield(player1, new NobleTemplar());
        Permanent cleric = findPermanent(player1, "Noble Templar");
        int powerBefore = gqs.getEffectivePower(gd, cleric);
        int toughnessBefore = gqs.getEffectiveToughness(gd, cleric);

        harness.setHand(player1, List.of(new Enrage()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castInstant(player1, 0, 3, cleric.getId());

        assertThat(gd.stack).hasSize(2);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, cleric)).isEqualTo(powerBefore + 3);
        assertThat(gqs.getEffectiveToughness(gd, cleric)).isEqualTo(toughnessBefore + 2);
    }

    @Test
    @DisplayName("A Cleric you control gets +0/+2 when targeted by an ability")
    void clericGetsToughnessFromAbilityTargeting() {
        harness.addToBattlefield(player1, new DaruSpiritualist());
        Permanent spiritualist = findPermanent(player1, "Daru Spiritualist");
        harness.addToBattlefield(player2, new UnspeakableSymbol());

        int powerBefore = gqs.getEffectivePower(gd, spiritualist);
        int toughnessBefore = gqs.getEffectiveToughness(gd, spiritualist);
        harness.activateAbility(player2, 0, null, spiritualist.getId());

        assertThat(gd.stack).hasSize(2);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, spiritualist)).isEqualTo(powerBefore + 1);
        assertThat(gqs.getEffectiveToughness(gd, spiritualist)).isEqualTo(toughnessBefore + 3);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Non-Clerics do not get the triggered toughness boost")
    void nonClericDoesNotGetBoost() {
        harness.addToBattlefield(player1, new DaruSpiritualist());
        harness.addToBattlefield(player1, new ScornfulEgotist());
        Permanent egotist = findPermanent(player1, "Scornful Egotist");
        int powerBefore = gqs.getEffectivePower(gd, egotist);
        int toughnessBefore = gqs.getEffectiveToughness(gd, egotist);

        harness.setHand(player1, List.of(new Enrage()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castInstant(player1, 0, 3, egotist.getId());

        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, egotist)).isEqualTo(powerBefore + 3);
        assertThat(gqs.getEffectiveToughness(gd, egotist)).isEqualTo(toughnessBefore);
    }

    @Test
    @DisplayName("A Cleric controlled by an opponent does not get the triggered toughness boost")
    void opponentClericDoesNotGetBoost() {
        harness.addToBattlefield(player1, new DaruSpiritualist());
        harness.addToBattlefield(player2, new NobleTemplar());
        Permanent opponentCleric = findPermanent(player2, "Noble Templar");
        int powerBefore = gqs.getEffectivePower(gd, opponentCleric);
        int toughnessBefore = gqs.getEffectiveToughness(gd, opponentCleric);

        harness.setHand(player1, List.of(new Enrage()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castInstant(player1, 0, 3, opponentCleric.getId());

        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, opponentCleric)).isEqualTo(powerBefore + 3);
        assertThat(gqs.getEffectiveToughness(gd, opponentCleric)).isEqualTo(toughnessBefore);
    }

    @Test
    @DisplayName("The triggered toughness boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new DaruSpiritualist());
        Permanent spiritualist = findPermanent(player1, "Daru Spiritualist");

        harness.setHand(player1, List.of(new Enrage()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castInstant(player1, 0, 3, spiritualist.getId());
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, spiritualist)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, spiritualist)).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, spiritualist)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, spiritualist)).isEqualTo(1);
    }
}
