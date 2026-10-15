package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RhizomeLurcher.class, GrizzlyBears.class, Shock.class})
class RhizomeLurcherTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with a +1/+1 counter for each creature card in its controller's graveyard")
    void entersWithCountersPerCreatureCard() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new Shock()));
        harness.setGraveyard(player2, List.of(new GrizzlyBears()));

        castLurcher();

        Permanent lurcher = findLurcher();
        assertThat(lurcher).isNotNull();
        assertThat(lurcher.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(lurcher.getEffectivePower()).isEqualTo(4);
        assertThat(lurcher.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("With no creature cards in its controller's graveyard it enters as a 2/2")
    void entersAsTwoTwoWithNoCreatureCardsInGraveyard() {
        gd.playerGraveyards.get(player1.getId()).add(new Shock());

        castLurcher();

        Permanent lurcher = findLurcher();
        assertThat(lurcher).isNotNull();
        assertThat(lurcher.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.getEffectivePower(gd, lurcher)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, lurcher)).isEqualTo(2);
    }

    @Test
    @DisplayName("Counts creature cards present when the spell resolves, not when it is cast")
    void countsGraveyardAtEntry() {
        prepareLurcher();
        harness.castCreature(player1, 0);
        harness.setGraveyard(player1, List.of(new RhizomeLurcher(), new RhizomeLurcher()));

        harness.passBothPriorities();

        assertThat(findLurcher().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Creature cards removed before entry do not contribute counters")
    void ignoresCreaturesRemovedBeforeEntry() {
        harness.setGraveyard(player1, List.of(new RhizomeLurcher()));
        prepareLurcher();
        harness.castCreature(player1, 0);
        harness.setGraveyard(player1, List.of());

        harness.passBothPriorities();

        assertThat(findLurcher().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Counters remain fixed after the creature enters")
    void countersDoNotTrackLaterGraveyardChanges() {
        harness.setGraveyard(player1, List.of(new RhizomeLurcher()));
        castLurcher();
        Permanent lurcher = findLurcher();

        harness.setGraveyard(player1, List.of());
        assertThat(lurcher.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, lurcher)).isEqualTo(3);

        harness.setGraveyard(player1, List.of(new RhizomeLurcher(), new RhizomeLurcher()));
        assertThat(lurcher.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, lurcher)).isEqualTo(3);
    }

    private void castLurcher() {
        prepareLurcher();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }

    private void prepareLurcher() {
        harness.setHand(player1, List.of(new RhizomeLurcher()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }

    private Permanent findLurcher() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Rhizome Lurcher"))
                .findFirst().orElse(null);
    }
}
