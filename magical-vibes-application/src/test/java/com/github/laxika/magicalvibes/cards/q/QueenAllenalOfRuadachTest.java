package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.b.BladeSplicer;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WilyGoblin;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({QueenAllenalOfRuadach.class, BladeSplicer.class, GrizzlyBears.class, WilyGoblin.class})
class QueenAllenalOfRuadachTest extends BaseCardTest {

    @Test
    @DisplayName("Power and toughness equal the number of creatures you control")
    void powerAndToughnessEqualControlledCreatures() {
        Permanent queen = addQueenReady(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, queen)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, queen)).isEqualTo(2);
    }

    @Test
    @DisplayName("Adds one Soldier to a creature-token creation event")
    void addsSoldierToCreatureTokenCreation() {
        harness.addToBattlefield(player1, new QueenAllenalOfRuadach());
        harness.setHand(player1, List.of(new BladeSplicer()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Phyrexian Golem")).hasSize(1);
        assertThat(findPermanents(player1, "Soldier")).hasSize(1);
    }

    @Test
    @DisplayName("Does not add a Soldier to a noncreature-token creation event")
    void doesNotAddSoldierToNoncreatureTokenCreation() {
        harness.addToBattlefield(player1, new QueenAllenalOfRuadach());
        harness.setHand(player1, List.of(new WilyGoblin()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
        assertThat(findPermanents(player1, "Soldier")).isEmpty();
    }

    private Permanent addQueenReady(Player player) {
        return addCreatureReady(player, new QueenAllenalOfRuadach());
    }
}
