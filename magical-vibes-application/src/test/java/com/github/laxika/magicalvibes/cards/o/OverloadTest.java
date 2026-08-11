package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.d.DarksteelJuggernaut;
import com.github.laxika.magicalvibes.cards.d.DarksteelIngot;
import com.github.laxika.magicalvibes.cards.f.FireDiamond;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OverloadTest extends BaseCardTest {

    @Test
    void destroysArtifactWithManaValueTwoOrLess() {
        harness.forceActivePlayer(player1);
        harness.addToBattlefield(player2, new FireDiamond());
        harness.setHand(player1, List.of(new Overload()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, harness.getPermanentId(player2, "Fire Diamond"));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Fire Diamond");
        harness.assertInGraveyard(player2, "Fire Diamond");
    }

    @Test
    void cannotTargetArtifactWithManaValueAboveTwoWithoutKicker() {
        harness.forceActivePlayer(player1);
        harness.addToBattlefield(player2, new DarksteelIngot());
        harness.setHand(player1, List.of(new Overload()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                harness.getPermanentId(player2, "Darksteel Ingot")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("mana value 2 or less");
    }

    @Test
    void kickedOverloadCanTargetArtifactWithManaValueFive() {
        harness.forceActivePlayer(player1);
        harness.addToBattlefield(player2, new DarksteelJuggernaut());
        harness.setHand(player1, List.of(new Overload()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castKickedInstant(player1, 0, harness.getPermanentId(player2, "Darksteel Juggernaut"));

        harness.passBothPriorities();
        harness.assertOnBattlefield(player2, "Darksteel Juggernaut");
    }
}
