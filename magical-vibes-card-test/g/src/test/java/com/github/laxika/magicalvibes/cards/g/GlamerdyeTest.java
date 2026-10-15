package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.c.CascadeBluffs;
import com.github.laxika.magicalvibes.cards.d.DoubleCleave;
import com.github.laxika.magicalvibes.cards.r.RiverfallMimic;
import com.github.laxika.magicalvibes.cards.s.SoulReap;
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

@CardUsed({Glamerdye.class, RiverfallMimic.class, DoubleCleave.class, CascadeBluffs.class, SoulReap.class})
class GlamerdyeTest extends BaseCardTest {

    @Test
    @DisplayName("Changes a color word on a target permanent")
    void changesColorWordOnTargetPermanent() {
        harness.addToBattlefield(player2, new RiverfallMimic());
        harness.setHand(player1, List.of(new Glamerdye()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID targetId = harness.getPermanentId(player2, "Riverfall Mimic");
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.handleListChoice(player1, "BLUE");
        harness.handleListChoice(player1, "WHITE");

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(findPermanent(player2, "Riverfall Mimic").getTextReplacements())
                .containsExactly(new TextReplacement("blue", "white"));
    }

    @Test
    @DisplayName("Changing a color word changes which multicolored spell triggers the target")
    void changesColorWordInTargetPermanentAbility() {
        Permanent mimic = harness.addToBattlefieldAndReturn(player1, new RiverfallMimic());
        harness.setHand(player1, List.of(new Glamerdye()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, mimic.getId());
        harness.handleListChoice(player1, "BLUE");
        harness.handleListChoice(player1, "WHITE");

        harness.setHand(player1, List.of(new DoubleCleave()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, mimic.getId());

        assertThat(gqs.getEffectivePower(gd, mimic)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, mimic)).isEqualTo(3);
    }

    @Test
    @DisplayName("Only color words may be chosen — a basic land type is rejected")
    void onlyOffersColorWords() {
        harness.addToBattlefield(player2, new RiverfallMimic());
        harness.setHand(player1, List.of(new Glamerdye()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID targetId = harness.getPermanentId(player2, "Riverfall Mimic");
        harness.castAndResolveInstant(player1, 0, targetId);

        assertThatThrownBy(() -> harness.handleListChoice(player1, "SWAMP"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("A text change to a target spell carries onto the permanent it becomes (CR 400.7a)")
    void changesColorWordOnTargetSpellCarriesToPermanent() {
        harness.setHand(player1, List.of(new Glamerdye(), new RiverfallMimic()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        // Riverfall Mimic creature spell goes on the stack (index 1; Glamerdye stays at index 0).
        harness.castCreature(player1, 1);
        UUID mimicSpellId = gd.stack.getFirst().getCard().getId();

        harness.castAndResolveInstant(player1, 0, mimicSpellId); // resolve Glamerdye — begins the color choice

        harness.handleListChoice(player1, "BLUE");
        harness.handleListChoice(player1, "WHITE");

        harness.passBothPriorities(); // resolve the Riverfall Mimic spell

        Permanent mimic = findPermanent(player1, "Riverfall Mimic");
        assertThat(mimic.getTextReplacements())
                .containsExactly(new TextReplacement("blue", "white"));

        harness.setHand(player1, List.of(new DoubleCleave()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, mimic.getId());

        assertThat(gqs.getEffectivePower(gd, mimic)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, mimic)).isEqualTo(3);
    }

    @Test
    @DisplayName("Retrace lets Glamerdye be recast from the graveyard to change a permanent's text")
    void retraceTargetsPermanent() {
        harness.addToBattlefield(player2, new RiverfallMimic());
        harness.setGraveyard(player1, List.of(new Glamerdye()));
        harness.setHand(player1, List.of(new CascadeBluffs()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID targetId = harness.getPermanentId(player2, "Riverfall Mimic");
        harness.castRetrace(player1, 0, 0, targetId);
        harness.passBothPriorities();

        harness.handleListChoice(player1, "BLUE");
        harness.handleListChoice(player1, "WHITE");

        assertThat(findPermanent(player2, "Riverfall Mimic").getTextReplacements())
                .containsExactly(new TextReplacement("blue", "white"));
        harness.assertInGraveyard(player1, "Cascade Bluffs");
        // Retrace keeps the normal graveyard disposition — not exiled, so it can be retraced again.
        harness.assertInGraveyard(player1, "Glamerdye");
    }

    @Test
    @DisplayName("Retrace requires discarding a land card")
    void retraceRequiresLandDiscard() {
        harness.addToBattlefield(player2, new RiverfallMimic());
        harness.setGraveyard(player1, List.of(new Glamerdye()));
        harness.setHand(player1, List.of(new DoubleCleave()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID targetId = harness.getPermanentId(player2, "Riverfall Mimic");
        assertThatThrownBy(() -> harness.castRetrace(player1, 0, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Fizzles if the target permanent leaves before resolution")
    void fizzlesIfTargetRemoved() {
        harness.addToBattlefield(player2, new RiverfallMimic());
        harness.setHand(player1, List.of(new Glamerdye()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID targetId = harness.getPermanentId(player2, "Riverfall Mimic");
        harness.castInstant(player1, 0, targetId);
        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(l -> l.contains("fizzles"));
    }

    @Test
    @DisplayName("Changing nongreen to nonblue makes a blue creature target illegal")
    void changesTargetRestrictionOnSpell() {
        Permanent mimic = harness.addToBattlefieldAndReturn(player2, new RiverfallMimic());
        harness.setHand(player1, List.of(new SoulReap(), new Glamerdye()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, mimic.getId());
        UUID reapSpellId = gd.stack.getFirst().getCard().getId();
        harness.castAndResolveInstant(player1, 0, reapSpellId);
        harness.handleListChoice(player1, "GREEN");
        harness.handleListChoice(player1, "BLUE");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Riverfall Mimic");
        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Soul Reap");
    }

    @Test
    @DisplayName("A permanent without color words is still a legal target")
    void canTargetPermanentWithoutColorWords() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new CascadeBluffs());
        harness.setHand(player1, List.of(new Glamerdye()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, land.getId());
        harness.handleListChoice(player1, "BLUE");
        harness.handleListChoice(player1, "WHITE");

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player2, "Cascade Bluffs");
        harness.assertInGraveyard(player1, "Glamerdye");
    }

}
