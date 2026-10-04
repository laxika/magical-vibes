package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.StoneworkPuma;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HakodaSelflessCommander.class, StoneworkPuma.class, GrizzlyBears.class})
class HakodaSelflessCommanderTest extends BaseCardTest {

    @Test
    @DisplayName("Can cast an Ally spell from the top of the library")
    void castsAllySpellFromLibraryTop() {
        harness.addToBattlefield(player1, new HakodaSelflessCommander());
        StoneworkPuma puma = new StoneworkPuma();
        harness.setLibrary(player1, List.of(puma));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castFromLibraryTop(player1);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Stonework Puma");
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(puma);
    }

    @Test
    @DisplayName("Cannot cast a non-Ally spell from the top of the library")
    void cannotCastNonAllySpellFromLibraryTop() {
        harness.addToBattlefield(player1, new HakodaSelflessCommander());
        GrizzlyBears bears = new GrizzlyBears();
        harness.setLibrary(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(bears);
    }

    @Test
    @DisplayName("Sacrificing Hakoda boosts your creatures and grants indestructible")
    void sacrificeBoostsOwnCreaturesAndGrantsIndestructible() {
        harness.addToBattlefield(player1, new HakodaSelflessCommander());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Hakoda, Selfless Commander");
        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(7);
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, opponentCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentCreature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Hakoda's temporary effects wear off at end of turn")
    void effectsWearOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new HakodaSelflessCommander());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(7);
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.INDESTRUCTIBLE)).isTrue();

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Only Hakoda's controller may see the top card, even when it is not an Ally")
    void privatelyLooksAtNonAllyLibraryTop() {
        harness.addToBattlefield(player1, new HakodaSelflessCommander());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.clearMessages();

        harness.publishState();

        assertThat(harness.getConn1().getSentMessages())
                .anyMatch(message -> message.contains("\"revealedLibraryTopCards\":[[{")
                        && message.contains("Grizzly Bears"));
        assertThat(harness.getConn2().getSentMessages())
                .noneMatch(message -> message.contains("Grizzly Bears"));
    }

    @Test
    @DisplayName("Casting an Ally from the library still requires paying its mana cost")
    void cannotCastLibraryAllyWithoutMana() {
        harness.addToBattlefield(player1, new HakodaSelflessCommander());
        StoneworkPuma puma = new StoneworkPuma();
        harness.setLibrary(player1, List.of(puma));

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(puma);
    }

    @Test
    @DisplayName("Hakoda does not grant flash to Ally spells cast from the library")
    void cannotCastLibraryAllyDuringCombat() {
        harness.addToBattlefield(player1, new HakodaSelflessCommander());
        StoneworkPuma puma = new StoneworkPuma();
        harness.setLibrary(player1, List.of(puma));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(puma);
    }

    @Test
    @DisplayName("Sacrifice is paid immediately and creatures are chosen when the ability resolves")
    void sacrificeIsCostAndBoostUsesCreaturesPresentAtResolution() {
        harness.addToBattlefield(player1, new HakodaSelflessCommander());
        Permanent existing = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);

        harness.assertInGraveyard(player1, "Hakoda, Selfless Commander");
        harness.assertNotOnBattlefield(player1, "Hakoda, Selfless Commander");
        assertThat(gqs.getEffectiveToughness(gd, existing)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, existing, Keyword.INDESTRUCTIBLE)).isFalse();
        Permanent beforeResolution = harness.addToBattlefieldAndReturn(player1, new StoneworkPuma());
        harness.passBothPriorities();
        Permanent afterResolution = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.getEffectiveToughness(gd, beforeResolution)).isEqualTo(7);
        assertThat(gqs.hasKeyword(gd, beforeResolution, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.getEffectiveToughness(gd, afterResolution)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, afterResolution, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Sacrificing Hakoda removes its library casting permission immediately")
    void sacrificeRemovesLibraryCastingPermission() {
        harness.addToBattlefield(player1, new HakodaSelflessCommander());
        StoneworkPuma puma = new StoneworkPuma();
        harness.setLibrary(player1, List.of(puma));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(puma);
    }

    @Test
    @DisplayName("Hakoda's look ability does not function while Hakoda is in the library")
    void hakodaDoesNotRevealItselfFromLibrary() {
        harness.setLibrary(player1, List.of(new HakodaSelflessCommander()));
        harness.clearMessages();

        harness.publishState();

        assertThat(harness.getConn1().getSentMessages())
                .noneMatch(message -> message.contains("Hakoda, Selfless Commander"));
        assertThat(harness.getConn2().getSentMessages())
                .noneMatch(message -> message.contains("Hakoda, Selfless Commander"));
    }
}
