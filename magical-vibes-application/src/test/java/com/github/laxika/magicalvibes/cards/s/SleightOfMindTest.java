package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BlueElementalBlast;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.w.WhiteKnight;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TextReplacement;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SleightOfMind.class, WhiteKnight.class, HillGiant.class, BlueElementalBlast.class})
class SleightOfMindTest extends BaseCardTest {

    @Test
    @DisplayName("Changes a color word on a target permanent")
    void changesColorWordOnTargetPermanent() {
        harness.addToBattlefield(player2, new WhiteKnight());
        harness.setHand(player1, List.of(new SleightOfMind()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        UUID targetId = harness.getPermanentId(player2, "White Knight");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        harness.handleListChoice(player1, "BLACK");
        harness.handleListChoice(player1, "GREEN");

        assertThat(gd.interaction.activeInteraction()).isNull();
        Permanent target = findPermanent(player2, "White Knight");
        assertThat(target.getTextReplacements())
                .containsExactly(new TextReplacement("black", "green"));
        assertThat(gqs.hasProtectionFrom(gd, target, CardColor.BLACK)).isFalse();
        assertThat(gqs.hasProtectionFrom(gd, target, CardColor.GREEN)).isTrue();
    }

    @Test
    @DisplayName("Only color words may be chosen — a basic land type is rejected")
    void onlyOffersColorWords() {
        harness.addToBattlefield(player2, new WhiteKnight());
        harness.setHand(player1, List.of(new SleightOfMind()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        UUID targetId = harness.getPermanentId(player2, "White Knight");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleListChoice(player1, "SWAMP"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("A text change to a target spell carries onto the permanent it becomes (CR 400.7a)")
    void changesColorWordOnTargetSpellCarriesToPermanent() {
        harness.setHand(player1, List.of(new SleightOfMind(), new WhiteKnight()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        // White Knight creature spell goes on the stack (index 1; Sleight of Mind stays at index 0).
        harness.castCreature(player1, 1);
        UUID whiteKnightSpellId = gd.stack.getFirst().getCard().getId();

        harness.castInstant(player1, 0, whiteKnightSpellId);
        harness.passBothPriorities(); // resolve Sleight of Mind — begins the color choice

        harness.handleListChoice(player1, "BLACK");
        harness.handleListChoice(player1, "GREEN");

        harness.passBothPriorities(); // resolve the White Knight spell

        Permanent whiteKnight = findPermanent(player1, "White Knight");
        assertThat(whiteKnight.getTextReplacements())
                .containsExactly(new TextReplacement("black", "green"));
        assertThat(gqs.hasProtectionFrom(gd, whiteKnight, CardColor.BLACK)).isFalse();
        assertThat(gqs.hasProtectionFrom(gd, whiteKnight, CardColor.GREEN)).isTrue();
    }

    @Test
    @DisplayName("Changes the text of an instant spell before it resolves")
    void changesColorWordOnInstantSpell() {
        HillGiant giant = new HillGiant();
        BlueElementalBlast blueElementalBlast = new BlueElementalBlast();
        harness.setHand(player1, List.of(giant, new SleightOfMind()));
        harness.setHand(player2, List.of(blueElementalBlast));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, 0, giant.getId());
        harness.castInstant(player1, 0, blueElementalBlast.getId());
        harness.passBothPriorities();

        harness.handleListChoice(player1, "RED");
        harness.handleListChoice(player1, "GREEN");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Hill Giant");
        harness.assertInGraveyard(player2, "Blue Elemental Blast");
    }

    @Test
    @DisplayName("Cannot replace a color word with itself")
    void cannotReplaceColorWordWithItself() {
        harness.addToBattlefield(player2, new WhiteKnight());
        harness.setHand(player1, List.of(new SleightOfMind()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        UUID targetId = harness.getPermanentId(player2, "White Knight");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        harness.handleListChoice(player1, "BLACK");

        assertThatThrownBy(() -> harness.handleListChoice(player1, "BLACK"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Fizzles if the target permanent leaves before resolution")
    void fizzlesIfTargetRemoved() {
        harness.addToBattlefield(player2, new WhiteKnight());
        harness.setHand(player1, List.of(new SleightOfMind()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        UUID targetId = harness.getPermanentId(player2, "White Knight");
        harness.castInstant(player1, 0, targetId);
        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(l -> l.contains("fizzles"));
    }
}
