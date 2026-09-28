package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.c.ContactOtherPlane;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SorcerersStrongbox;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PokeyTheScallywagg.class, SorcerersStrongbox.class, ContactOtherPlane.class,
        GrizzlyBears.class})
class PokeyTheScallywaggTest extends BaseCardTest {

    @Test
    void coinFlipUsesAD20() {
        harness.addToBattlefield(player1, new PokeyTheScallywagg());
        harness.addToBattlefield(player1, new SorcerersStrongbox());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 1, null, null);
        resolveAllTriggers();

        assertThat(gameLogContains("Pokey, the Scallywagg rolled a d20:")).isTrue();
    }

    @Test
    void d20UsesACoin() {
        harness.addToBattlefield(player1, new PokeyTheScallywagg());
        harness.setLibrary(player1, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new ContactOtherPlane()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gameLogContains("flips a coin for Contact Other Plane:")).isTrue();
    }
}
