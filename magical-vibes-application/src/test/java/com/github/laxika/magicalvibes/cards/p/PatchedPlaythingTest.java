package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.j.JinGitaxiasProgressTyrant;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PatchedPlaything.class, JinGitaxiasProgressTyrant.class})
class PatchedPlaythingTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with two -1/-1 counters when cast from hand")
    void entersWithCountersWhenCastFromHand() {
        harness.setHand(player1, List.of(new PatchedPlaything()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent plaything = findPermanent(player1, "Patched Plaything");
        assertThat(plaything.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Enters without counters when put onto the battlefield")
    void entersWithoutCountersWhenNotCast() {
        Permanent plaything = harness.enterBattlefieldAndReturn(player1, new PatchedPlaything());

        assertThat(plaything.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Cast-from-hand counters are present immediately and do not use the stack")
    void countersAreAppliedAsItEnters() {
        harness.setHand(player1, List.of(new PatchedPlaything()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent plaything = findPermanent(player1, "Patched Plaything");
        assertThat(plaything.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A plaything cast from hand deals two damage in each combat damage step")
    void castFromHandDealsReducedDoubleStrikeDamage() {
        harness.setHand(player1, List.of(new PatchedPlaything()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        findPermanent(player1, "Patched Plaything").setSummoningSick(false);

        declareAttackers(List.of(0));
        resolveCombat();

        harness.assertLife(player2, 16);
    }

    @Test
    @DisplayName("A plaything put onto the battlefield deals four damage in each combat damage step")
    void notCastDealsFullDoubleStrikeDamage() {
        Permanent plaything = harness.enterBattlefieldAndReturn(player1, new PatchedPlaything());
        plaything.setSummoningSick(false);

        declareAttackers(List.of(0));
        resolveCombat();

        harness.assertLife(player2, 12);
    }

    @Test
    @DisplayName("A copy of the permanent spell enters without cast-from-hand counters")
    void copiedSpellEntersWithoutCounters() {
        harness.addToBattlefield(player1, new JinGitaxiasProgressTyrant());
        harness.setHand(player1, List.of(new PatchedPlaything()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        List<Permanent> playthings = findPermanents(player1, "Patched Plaything");
        assertThat(playthings).hasSize(2);
        assertThat(playthings).filteredOn(permanent -> permanent.getCard().isToken())
                .singleElement().satisfies(permanent ->
                        assertThat(permanent.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero());
        assertThat(playthings).filteredOn(permanent -> !permanent.getCard().isToken())
                .singleElement().satisfies(permanent ->
                        assertThat(permanent.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2));
    }
}
