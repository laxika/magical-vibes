package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Stormsplitter.class, Shock.class, Divination.class, GrizzlyBears.class})
class StormsplitterTest extends BaseCardTest {

    @Test
    @DisplayName("Casting an instant creates a token copy")
    void instantCreatesTokenCopy() {
        harness.addToBattlefield(player1, new Stormsplitter());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(countTokens()).isEqualTo(1);
    }

    @Test
    @DisplayName("Casting a sorcery creates a token copy")
    void sorceryCreatesTokenCopy() {
        harness.addToBattlefield(player1, new Stormsplitter());
        harness.setHand(player1, List.of(new Divination()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(countTokens()).isEqualTo(1);
    }

    @Test
    @DisplayName("Casting a creature does not create a token copy")
    void creatureDoesNotCreateTokenCopy() {
        harness.addToBattlefield(player1, new Stormsplitter());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(countTokens()).isZero();
    }

    @Test
    @DisplayName("The token copy is exiled at the next end step")
    void tokenCopyIsExiledAtNextEndStep() {
        harness.addToBattlefield(player1, new Stormsplitter());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(countTokens()).isEqualTo(1);

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(countTokens()).isZero();
    }

    private long countTokens() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .count();
    }
}
