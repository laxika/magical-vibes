package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DispersalShield.class, GiantSpider.class, GrizzlyBears.class, SerraAngel.class})
class DispersalShieldTest extends BaseCardTest {

    @Test
    @DisplayName("Counters a spell whose mana value equals the greatest mana value among your permanents")
    void countersSpellAtGreatestControlledPermanentManaValue() {
        harness.addToBattlefield(player2, new GiantSpider());

        GrizzlyBears spell = new GrizzlyBears();
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new DispersalShield()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, spell.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Dispersal Shield");
    }

    @Test
    @DisplayName("May target a spell above the greatest mana value but does not counter it")
    void doesNotCounterSpellAboveGreatestControlledPermanentManaValue() {
        harness.addToBattlefield(player2, new GiantSpider());

        SerraAngel spell = new SerraAngel();
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.setHand(player2, List.of(new DispersalShield()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, spell.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Serra Angel");
        harness.assertInGraveyard(player2, "Dispersal Shield");
    }

    @Test
    @DisplayName("Checks the greatest controlled permanent mana value when it resolves")
    void checksGreatestControlledPermanentManaValueOnResolution() {
        GrizzlyBears spell = new GrizzlyBears();
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new DispersalShield()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, spell.getId());
        harness.addToBattlefield(player2, new GiantSpider());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(harness.getGameData().stack).isEmpty();
    }
}
