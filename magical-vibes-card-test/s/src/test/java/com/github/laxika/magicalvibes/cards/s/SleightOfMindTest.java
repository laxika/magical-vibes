package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.w.WhiteKnight;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.t.Terror;
import com.github.laxika.magicalvibes.cards.c.CircleOfProtectionRed;
import com.github.laxika.magicalvibes.cards.h.Hydroblast;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TextReplacement;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SleightOfMind.class, CircleOfProtectionRed.class, WhiteKnight.class, HillGiant.class,
        Hydroblast.class, Terror.class})
class SleightOfMindTest extends BaseCardTest {

    @Test
    @DisplayName("Changes a color word on a target permanent")
    void changesColorWordOnTargetPermanent() {
        harness.addToBattlefield(player2, new CircleOfProtectionRed());
        harness.setHand(player1, List.of(new SleightOfMind()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        UUID targetId = harness.getPermanentId(player2, "Circle of Protection: Red");
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.handleListChoice(player1, "RED");
        harness.handleListChoice(player1, "GREEN");

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(findPermanent(player2, "Circle of Protection: Red").getTextReplacements())
                .containsExactly(new TextReplacement("red", "green"));
    }

    @Test
    @DisplayName("Only color words may be chosen — a basic land type is rejected")
    void onlyOffersColorWords() {
        harness.addToBattlefield(player2, new CircleOfProtectionRed());
        harness.setHand(player1, List.of(new SleightOfMind()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        UUID targetId = harness.getPermanentId(player2, "Circle of Protection: Red");
        harness.castAndResolveInstant(player1, 0, targetId);

        assertThatThrownBy(() -> harness.handleListChoice(player1, "SWAMP"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("A text change to a target spell carries onto the permanent it becomes (CR 400.7a)")
    void changesColorWordOnTargetSpellCarriesToPermanent() {
        harness.setHand(player1, List.of(new SleightOfMind(), new CircleOfProtectionRed()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        // Circle of Protection: Red enchantment spell goes on the stack (index 1; Sleight of Mind stays at index 0).
        harness.castEnchantment(player1, 1);
        UUID circleSpellId = gd.stack.getFirst().getCard().getId();

        harness.castInstant(player1, 0, circleSpellId);
        harness.passBothPriorities(); // resolve Sleight of Mind — begins the color choice

        harness.handleListChoice(player1, "RED");
        harness.handleListChoice(player1, "GREEN");

        harness.passBothPriorities(); // resolve the Circle of Protection: Red spell

        assertThat(findPermanent(player1, "Circle of Protection: Red").getTextReplacements())
                .containsExactly(new TextReplacement("red", "green"));
    }

    @Test
    @DisplayName("Changes the text of an instant spell before it resolves")
    void changesColorWordOnInstantSpell() {
        HillGiant giant = new HillGiant();
        Hydroblast hydroblast = new Hydroblast();
        harness.setHand(player1, List.of(giant, new SleightOfMind()));
        harness.setHand(player2, List.of(hydroblast));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, 0, giant.getId());
        harness.castInstant(player1, 0, hydroblast.getId());
        harness.passBothPriorities();

        harness.handleListChoice(player1, "RED");
        harness.handleListChoice(player1, "GREEN");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Hill Giant");
        harness.assertInGraveyard(player2, "Hydroblast");
    }

    @Test
    @DisplayName("The text change lasts indefinitely")
    void textChangeLastsIndefinitely() {
        harness.addToBattlefield(player2, new CircleOfProtectionRed());
        harness.setHand(player1, List.of(new SleightOfMind()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        UUID targetId = harness.getPermanentId(player2, "Circle of Protection: Red");
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.handleListChoice(player1, "RED");
        harness.handleListChoice(player1, "GREEN");
        advanceToUpkeep(player1);

        assertThat(findPermanent(player2, "Circle of Protection: Red").getTextReplacements())
                .containsExactly(new TextReplacement("red", "green"));
    }

    @Test
    @DisplayName("Fizzles if the target permanent leaves before resolution")
    void fizzlesIfTargetRemoved() {
        harness.addToBattlefield(player2, new CircleOfProtectionRed());
        harness.setHand(player1, List.of(new SleightOfMind()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        UUID targetId = harness.getPermanentId(player2, "Circle of Protection: Red");
        harness.castInstant(player1, 0, targetId);
        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(l -> l.contains("fizzles"));
    }

    @Test
    @DisplayName("Cannot replace a color word with itself")
    void cannotReplaceColorWordWithItself() {
        harness.addToBattlefield(player2, new WhiteKnight());
        harness.setHand(player1, List.of(new SleightOfMind()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        UUID targetId = harness.getPermanentId(player2, "White Knight");
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.handleListChoice(player1, "BLACK");

        assertThatThrownBy(() -> harness.handleListChoice(player1, "BLACK"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Changed color words in a spell's targeting restriction are checked on resolution")
    void changedTargetRestrictionMakesExistingTargetIllegal() {
        harness.addToBattlefield(player2, new HillGiant());
        UUID giantId = harness.getPermanentId(player2, "Hill Giant");
        Terror terror = new Terror();
        harness.setHand(player1, List.of(new SleightOfMind()));
        harness.setHand(player2, List.of(terror));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.passPriority(player1);
        harness.castInstant(player2, 0, giantId);
        harness.castAndResolveInstant(player1, 0, terror.getId());
        harness.handleListChoice(player1, "BLACK");
        harness.handleListChoice(player1, "RED");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Hill Giant");
        harness.assertInGraveyard(player2, "Terror");
    }

    @Test
    @DisplayName("Changing printed protection changes which spells can target the permanent")
    void changedProtectionPreventsBlueTargeting() {
        harness.addToBattlefield(player2, new WhiteKnight());
        UUID knightId = harness.getPermanentId(player2, "White Knight");
        harness.setHand(player1, List.of(new SleightOfMind(), new SleightOfMind()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveInstant(player1, 0, knightId);
        harness.handleListChoice(player1, "BLACK");
        harness.handleListChoice(player1, "BLUE");

        assertThatThrownBy(() -> harness.castInstant(player1, 0, knightId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
        harness.assertOnBattlefield(player2, "White Knight");
    }

    @Test
    @DisplayName("A color word absent from the target's text may still be chosen")
    void absentColorWordDoesNotChangeProtection() {
        harness.addToBattlefield(player2, new WhiteKnight());
        UUID knightId = harness.getPermanentId(player2, "White Knight");
        harness.setHand(player1, List.of(new SleightOfMind(), new SleightOfMind()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveInstant(player1, 0, knightId);
        harness.handleListChoice(player1, "RED");
        harness.handleListChoice(player1, "BLUE");
        harness.castAndResolveInstant(player1, 0, knightId);
        harness.handleListChoice(player1, "BLACK");
        harness.handleListChoice(player1, "GREEN");

        harness.assertInGraveyard(player1, "Sleight of Mind");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
