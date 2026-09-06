package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LordOfAtlantis;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TextReplacement;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MagicalHack.class, GrizzlyBears.class, LordOfAtlantis.class,
        MerfolkOfThePearlTrident.class, Swamp.class})
class MagicalHackTest extends BaseCardTest {

    private Permanent grizzly(UUID ownerId) {
        return gd.playerBattlefields.get(ownerId).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();
    }

    @Test
    @DisplayName("Changes a basic land type on a target permanent")
    void changesLandTypeOnTargetPermanent() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new MagicalHack()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.handleListChoice(player1, "SWAMP");
        harness.handleListChoice(player1, "PLAINS");

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(grizzly(player2.getId()).getTextReplacements())
                .containsExactly(new TextReplacement("Swamp", "Plains"));
    }

    @Test
    @DisplayName("Only basic land types may be chosen — a color word is rejected")
    void onlyOffersLandTypes() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new MagicalHack()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castAndResolveInstant(player1, 0, targetId);

        assertThatThrownBy(() -> harness.handleListChoice(player1, "BLACK"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("A land-type change to a target spell carries onto the permanent it becomes (CR 112.4)")
    void changesLandTypeOnTargetSpellCarriesToPermanent() {
        harness.setHand(player1, List.of(new MagicalHack(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        // Grizzly Bears creature spell goes on the stack (index 1; Magical Hack stays at index 0).
        harness.castCreature(player1, 1);
        UUID grizzlySpellId = gd.stack.getFirst().getCard().getId();

        harness.castAndResolveInstant(player1, 0, grizzlySpellId); // resolve Magical Hack — begins the land-type choice

        harness.handleListChoice(player1, "SWAMP");
        harness.handleListChoice(player1, "PLAINS");

        harness.passBothPriorities(); // resolve the Grizzly Bears spell

        assertThat(grizzly(player1.getId()).getTextReplacements())
                .containsExactly(new TextReplacement("Swamp", "Plains"));
    }

    @Test
    @DisplayName("Changes a basic land permanent's effective type and mana color")
    void changesBasicLandTypeOnTargetPermanent() {
        Permanent swamp = harness.addToBattlefieldAndReturn(player2, new Swamp());
        harness.setHand(player1, List.of(new MagicalHack()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0, swamp.getId());

        harness.handleListChoice(player1, "SWAMP");
        harness.handleListChoice(player1, "PLAINS");

        assertThat(gqs.effectiveBasicLandTypes(gd, swamp))
                .containsExactly(CardSubtype.PLAINS);

        harness.tapPermanent(player2, 0);

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.BLACK)).isZero();
    }

    @Test
    @DisplayName("A land-type change to a permanent spell rewrites its landwalk ability")
    void changesLandTypeOnTargetSpellChangesItsAbility() {
        harness.setHand(player1, List.of(new MagicalHack(), new LordOfAtlantis()));
        Permanent merfolk = harness.addToBattlefieldAndReturn(player1, new MerfolkOfThePearlTrident());
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castCreature(player1, 1);
        UUID lordSpellId = gd.stack.getFirst().getCard().getId();

        harness.castAndResolveInstant(player1, 0, lordSpellId);

        harness.handleListChoice(player1, "ISLAND");
        harness.handleListChoice(player1, "SWAMP");
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, merfolk, Keyword.SWAMPWALK)).isTrue();
        assertThat(gqs.hasKeyword(gd, merfolk, Keyword.ISLANDWALK)).isFalse();
    }

    @Test
    @DisplayName("A basic land type change lasts beyond the current turn")
    void landTypeChangeDoesNotExpireAtEndOfTurn() {
        Permanent swamp = harness.addToBattlefieldAndReturn(player2, new Swamp());
        harness.setHand(player1, List.of(new MagicalHack()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0, swamp.getId());

        harness.handleListChoice(player1, "SWAMP");
        harness.handleListChoice(player1, "PLAINS");
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.effectiveBasicLandTypes(gd, swamp))
                .containsExactly(CardSubtype.PLAINS);
    }

    @Test
    @DisplayName("Rejects choosing the same basic land type as the replacement")
    void cannotReplaceLandTypeWithItself() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new MagicalHack()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castAndResolveInstant(player1, 0, targetId);
        harness.handleListChoice(player1, "SWAMP");

        assertThatThrownBy(() -> harness.handleListChoice(player1, "SWAMP"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
