package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LandbindRitual.class, Plains.class, Island.class, GrizzlyBears.class})
class LandbindRitualTest extends BaseCardTest {

    @Test
    @DisplayName("Gains 2 life for each Plains controlled by the caster")
    void gainsTwoLifePerControlledPlains() {
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player2, new Plains());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setLife(player1, 10);
        prepareLandbindRitual();
        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(14);
    }

    @Test
    @DisplayName("Gains no life when the caster controls no Plains")
    void gainsNoLifeWithoutControlledPlains() {
        harness.setLife(player1, 10);
        prepareLandbindRitual();
        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(10);
    }

    @Test
    @DisplayName("Tapped Plains still count toward life gained")
    void countsTappedPlains() {
        harness.addToBattlefieldAndReturn(player1, new Plains()).tap();
        harness.addToBattlefieldAndReturn(player1, new Plains()).tap();
        harness.setLife(player1, 10);
        prepareLandbindRitual();

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertLife(player1, 14);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Plains cards outside the battlefield do not count")
    void ignoresPlainsOutsideBattlefield() {
        harness.addToBattlefield(player1, new Plains());
        harness.setGraveyard(player1, List.of(new Plains()));
        harness.setExile(player1, List.of(new Plains()));
        harness.setLibrary(player1, List.of(new Plains()));
        harness.setLife(player1, 10);
        harness.setHand(player1, List.of(new LandbindRitual(), new Plains()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertLife(player1, 12);
    }

    @Test
    @DisplayName("Counts Plains at resolution rather than when cast")
    void countsPlainsAtResolution() {
        harness.addToBattlefield(player1, new Plains());
        harness.setLife(player1, 10);
        prepareLandbindRitual();
        harness.castSorcery(player1, 0, 0);
        harness.addToBattlefield(player1, new Plains());

        harness.passBothPriorities();

        harness.assertLife(player1, 14);
    }

    private void prepareLandbindRitual() {
        harness.setHand(player1, List.of(new LandbindRitual()));
        harness.addMana(player1, ManaColor.WHITE, 5);
    }
}
