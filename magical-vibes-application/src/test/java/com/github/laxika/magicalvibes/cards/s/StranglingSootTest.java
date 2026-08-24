package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StranglingSoot.class, GrizzlyBears.class, HillGiant.class, AirElemental.class})
class StranglingSootTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a creature with toughness 3 or less")
    void destroysCreatureWithToughnessAtMostThree() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new StranglingSoot()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Hill Giant");
    }

    @Test
    @DisplayName("Cannot target a creature with toughness greater than 3")
    void cannotTargetCreatureWithGreaterToughness() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.setHand(player1, List.of(new StranglingSoot()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("toughness 3 or less");
    }

    @Test
    @DisplayName("Flashback destroys the target and exiles Strangling Soot")
    void flashbackDestroysAndExiles() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new StranglingSoot()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castFlashback(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Strangling Soot");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Strangling Soot"));
    }
}
