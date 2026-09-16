package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DaruHealer;
import com.github.laxika.magicalvibes.cards.e.ExaltedAngel;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Smother.class, DaruHealer.class, ExaltedAngel.class, Forest.class})
class SmotherTest extends BaseCardTest {

    @Test
    @DisplayName("Smother destroys a creature with mana value 3 or less")
    void destroysCreatureWithManaValueThreeOrLess() {
        harness.addToBattlefield(player2, new DaruHealer());

        harness.setHand(player1, List.of(new Smother()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castInstant(player1, 0, harness.getPermanentId(player2, "Daru Healer"));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Daru Healer");
        harness.assertInGraveyard(player2, "Daru Healer");
    }

    @Test
    @DisplayName("Smother cannot target a creature with mana value greater than 3")
    void cannotTargetCreatureWithManaValueGreaterThanThree() {
        harness.addToBattlefield(player2, new ExaltedAngel());

        harness.setHand(player1, List.of(new Smother()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                harness.getPermanentId(player2, "Exalted Angel")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("mana value 3 or less");
    }

    @Test
    @DisplayName("Smother cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        harness.addToBattlefield(player2, new Forest());

        harness.setHand(player1, List.of(new Smother()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                harness.getPermanentId(player2, "Forest")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature with mana value 3 or less");
    }

    @Test
    @DisplayName("Smother destroys a creature even when it has a regeneration shield")
    void cannotBeRegenerated() {
        Permanent healer = harness.addToBattlefieldAndReturn(player2, new DaruHealer());
        healer.setRegenerationShield(1);

        harness.setHand(player1, List.of(new Smother()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castInstant(player1, 0, healer.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Daru Healer");
        harness.assertInGraveyard(player2, "Daru Healer");
    }
}
