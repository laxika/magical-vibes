package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DragonsDesire.class, Spellbook.class, GrizzlyBears.class})
class DragonsDesireTest extends BaseCardTest {

    @Test
    @DisplayName("Adds red mana for each artifact an opponent controls")
    void addsRedManaForOpponentsArtifacts() {
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player2, new Spellbook());
        harness.addToBattlefield(player2, new Spellbook());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.castFromHand(player1, new DragonsDesire(), "{2}{R}{R}");
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(2);
    }

    @Test
    @DisplayName("Counts opponent artifacts when the spell resolves")
    void countsArtifactsAtResolution() {
        harness.addToBattlefield(player2, new Spellbook());

        harness.castFromHand(player1, new DragonsDesire(), "{2}{R}{R}");

        harness.addToBattlefield(player2, new Spellbook());
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(2);
    }

    @Test
    @DisplayName("Adds no mana when opponents control no artifacts")
    void addsNoManaWithoutOpponentArtifacts() {
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.castFromHand(player1, new DragonsDesire(), "{2}{R}{R}");
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }
}
