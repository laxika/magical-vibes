package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.OrnithopterOfParadise;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Moderation.class, GrizzlyBears.class, OrnithopterOfParadise.class})
class ModerationTest extends BaseCardTest {

    @Test
    @DisplayName("Controller draws a card whenever they cast a spell")
    void controllerDrawsWhenCastingSpell() {
        harness.addToBattlefield(player1, new Moderation());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Controller cannot cast more than one spell each turn")
    void controllerCannotCastSecondSpell() {
        harness.addToBattlefield(player1, new Moderation());
        harness.setHand(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Grizzly Bears");

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Moderation does not restrict an opponent's spells")
    void doesNotRestrictOpponent() {
        harness.addToBattlefield(player1, new Moderation());
        harness.setHand(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 4);
        harness.forceActivePlayer(player2);
        harness.clearPriorityPassed();

        harness.castCreature(player2, 0);
        harness.passBothPriorities();
        harness.castCreature(player2, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Casting Moderation does not draw a card and uses the turn's spell allowance")
    void castingModerationUsesSpellAllowanceWithoutDrawing() {
        harness.setLibrary(player1, List.of(new OrnithopterOfParadise()));
        harness.setHand(player1, List.of(new Moderation(), new OrnithopterOfParadise()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castEnchantment(player1, 0);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Moderation");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("An opponent's spell does not trigger Moderation")
    void opponentsSpellDoesNotDraw() {
        harness.addToBattlefield(player1, new Moderation());
        harness.setLibrary(player1, List.of(new OrnithopterOfParadise()));
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new OrnithopterOfParadise(), "{2}");

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        harness.assertOnBattlefield(player2, "Ornithopter of Paradise");
    }

    @Test
    @DisplayName("Each Moderation draws separately before the spell resolves")
    void multipleCopiesDrawBeforeSpellResolves() {
        harness.addToBattlefield(player1, new Moderation());
        harness.addToBattlefield(player1, new Moderation());
        harness.setLibrary(player1, List.of(new OrnithopterOfParadise(), new OrnithopterOfParadise()));
        harness.castFromHand(player1, new OrnithopterOfParadise(), "{2}");

        assertThat(gd.stack).hasSize(3);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);
        harness.assertNotOnBattlefield(player1, "Ornithopter of Paradise");
    }
}
