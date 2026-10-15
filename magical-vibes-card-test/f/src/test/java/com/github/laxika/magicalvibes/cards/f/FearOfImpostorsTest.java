package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.c.CarnageTyrant;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.ParanormalAnalyst;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FearOfImpostors.class, CarnageTyrant.class, Forest.class, GrizzlyBears.class,
        ParanormalAnalyst.class})
class FearOfImpostorsTest extends BaseCardTest {

    @Test
    void countersSpellAndItsControllerManifestsDread() {
        GrizzlyBears spell = new GrizzlyBears();
        Card manifestedCard = new GrizzlyBears();
        Card graveyardCard = new Forest();
        prepareFearAndSpell(spell, List.of(manifestedCard, graveyardCard));

        harness.castCreature(player2, 0);
        harness.passPriority(player2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.handlePermanentChosen(player1, spell.getId());
        harness.passBothPriorities();

        assertThat(harness.getGameData().interaction.activeInteraction(
                PendingInteraction.LibraryRevealChoice.class).playerId()).isEqualTo(player2.getId());
        harness.handleMultipleCardsChosen(player2, List.of(manifestedCard.getId()));

        assertThat(harness.getGameData().playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.isManifested()
                        && permanent.getCard().getId().equals(manifestedCard.getId()));
        assertThat(harness.getGameData().playerGraveyards.get(player2.getId())).contains(graveyardCard);
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Fear of Impostors");
    }

    @Test
    void uncounterableSpellStillHasItsControllerManifestDread() {
        CarnageTyrant spell = new CarnageTyrant();
        Card manifestedCard = new GrizzlyBears();
        Card graveyardCard = new Forest();
        prepareFearAndSpell(spell, List.of(manifestedCard, graveyardCard));
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.castCreature(player2, 0);
        harness.passPriority(player2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.handlePermanentChosen(player1, spell.getId());
        harness.passBothPriorities();

        assertThat(harness.getGameData().interaction.activeInteraction(
                PendingInteraction.LibraryRevealChoice.class).playerId()).isEqualTo(player2.getId());
        harness.handleMultipleCardsChosen(player2, List.of(manifestedCard.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Carnage Tyrant");
        assertThat(harness.getGameData().playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.isManifested()
                        && permanent.getCard().getId().equals(manifestedCard.getId()));
        assertThat(harness.getGameData().playerGraveyards.get(player2.getId())).contains(graveyardCard);
    }

    @Test
    void manifestsTheOnlyCardInTheTargetControllersLibrary() {
        FearOfImpostors spell = new FearOfImpostors();
        Card manifestedCard = new Forest();
        prepareFearAndSpell(spell, List.of(manifestedCard));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castCreature(player2, 0);
        harness.passPriority(player2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, spell.getId());
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player2, List.of(manifestedCard.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .singleElement().satisfies(permanent -> {
                    assertThat(permanent.isManifested()).isTrue();
                    assertThat(permanent.getCard().getId()).isEqualTo(manifestedCard.getId());
                });
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(spell);
    }

    @Test
    void emptyLibraryStillTriggersManifestDreadAbilities() {
        FearOfImpostors spell = new FearOfImpostors();
        prepareFearAndSpell(spell, List.of());
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addToBattlefield(player2, new ParanormalAnalyst());

        harness.castCreature(player2, 0);
        harness.passPriority(player2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, spell.getId());
        harness.withAutoStop(gd.currentStep, harness::passBothPriorities);

        harness.assertInGraveyard(player2, "Fear of Impostors");
        assertThat(gd.stack).singleElement().satisfies(entry -> {
            assertThat(entry.getCard()).isInstanceOf(ParanormalAnalyst.class);
            assertThat(entry.getControllerId()).isEqualTo(player2.getId());
        });
    }

    @Test
    void entersWithoutManifestingWhenThereIsNoSpellToTarget() {
        harness.setHand(player1, List.of(new FearOfImpostors()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 2);
        Card libraryCard = new Forest();
        harness.setLibrary(player1, List.of(libraryCard));

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Fear of Impostors");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
    }

    private void prepareFearAndSpell(Card spell, List<Card> library) {
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(spell));
        harness.setLibrary(player2, library);
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.setHand(player1, List.of(new FearOfImpostors()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 2);
    }
}
