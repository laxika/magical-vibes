package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.b.BloodMoon;
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

@CardUsed({WrathOfMaritLage.class, BloodMoon.class, GrizzlyBears.class, HillGiant.class})
class WrathOfMaritLageTest extends BaseCardTest {

    @Test
    @DisplayName("Entering taps all red creatures but leaves non-red creatures untapped")
    void etbTapsRedCreatures() {
        Permanent redGiant = addCreatureReady(player1, new HillGiant());   // Red 3/3
        Permanent greenBears = addCreatureReady(player2, new GrizzlyBears()); // Green 2/2

        harness.setHand(player1, List.of(new WrathOfMaritLage()));
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.castEnchantment(player1, 0);

        harness.passBothPriorities(); // enchantment resolves → ETB trigger on stack
        harness.passBothPriorities(); // ETB trigger resolves

        assertThat(redGiant.isTapped()).isTrue();
        assertThat(greenBears.isTapped()).isFalse();
    }

    @Test
    @DisplayName("ETB taps red creatures that enter before the trigger resolves")
    void etbChecksRedCreaturesAtResolution() {
        harness.setHand(player1, List.of(new WrathOfMaritLage()));
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.castEnchantment(player1, 0);

        harness.passBothPriorities(); // enchantment resolves, leaving its ETB trigger on the stack

        Permanent redGiant = addCreatureReady(player2, new HillGiant());
        harness.passBothPriorities();

        assertThat(redGiant.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Red noncreatures are unaffected by both abilities")
    void redNoncreaturesAreUnaffected() {
        Permanent bloodMoon = harness.addToBattlefieldAndReturn(player2, new BloodMoon());

        harness.setHand(player1, List.of(new WrathOfMaritLage()));
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.castEnchantment(player1, 0);

        harness.passBothPriorities(); // enchantment resolves -> ETB trigger on stack
        harness.passBothPriorities(); // ETB trigger resolves

        assertThat(bloodMoon.isTapped()).isFalse();

        bloodMoon.tap();
        advanceToUpkeep(player2);

        assertThat(bloodMoon.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Tapped red creature does not untap while Wrath of Marit Lage is out")
    void redCreatureStaysTapped() {
        harness.addToBattlefield(player1, new WrathOfMaritLage());
        Permanent redGiant = addCreatureReady(player1, new HillGiant());
        redGiant.tap();

        advanceToUpkeep(player1);

        assertThat(redGiant.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Non-red creature untaps normally")
    void nonRedCreatureUntaps() {
        harness.addToBattlefield(player1, new WrathOfMaritLage());
        Permanent greenBears = addCreatureReady(player1, new GrizzlyBears());
        greenBears.tap();

        advanceToUpkeep(player1);

        assertThat(greenBears.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Affects opponents' red creatures during their untap step")
    void affectsOpponentRedCreatures() {
        harness.addToBattlefield(player1, new WrathOfMaritLage());
        Permanent opponentGiant = addCreatureReady(player2, new HillGiant());
        opponentGiant.tap();

        advanceToUpkeep(player2);

        assertThat(opponentGiant.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Once Wrath of Marit Lage leaves, red creatures untap again")
    void untapsAfterEnchantmentLeaves() {
        Permanent enchantment = harness.addToBattlefieldAndReturn(player1, new WrathOfMaritLage());
        Permanent redGiant = addCreatureReady(player1, new HillGiant());
        redGiant.tap();

        gd.playerBattlefields.get(player1.getId()).remove(enchantment);

        advanceToUpkeep(player1);

        assertThat(redGiant.isTapped()).isFalse();
    }
}
