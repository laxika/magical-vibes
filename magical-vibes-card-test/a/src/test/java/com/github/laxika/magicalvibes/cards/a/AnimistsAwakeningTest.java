package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.FieryImpulse;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.l.LeafGilder;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.v.ValakutTheMoltenPinnacle;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AnimistsAwakening.class, Forest.class, Mountain.class, LeafGilder.class, FieryImpulse.class,
        ValakutTheMoltenPinnacle.class})
class AnimistsAwakeningTest extends BaseCardTest {

    @Test
    @DisplayName("Revealed lands enter tapped and the rest go to the bottom of the library")
    void landsEnterTappedRestOnBottom() {
        Card forest = new Forest();
        Card nonland = new LeafGilder();
        Card mountain = new Mountain();

        harness.setLibrary(player1, List.of(forest, nonland, mountain));

        harness.setHand(player1, List.of(new AnimistsAwakening()));
        harness.addMana(player1, ManaColor.GREEN, 4); // {3}{G} with X=3

        harness.castSorcery(player1, 0, 3);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Forest");
        harness.assertOnBattlefield(player1, "Mountain");
        assertThat(findPermanent(player1, "Forest").isTapped()).isTrue();
        assertThat(findPermanent(player1, "Mountain").isTapped()).isTrue();

        // The non-land goes to the bottom of the library, not the graveyard.
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Leaf Gilder");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getName)
                .doesNotContain("Leaf Gilder");
    }

    @Test
    @DisplayName("Spell mastery untaps the lands put onto the battlefield")
    void spellMasteryUntapsLands() {
        Card forest = new Forest();

        harness.setLibrary(player1, List.of(forest));

        harness.setGraveyard(player1, List.of(new AnimistsAwakening(), new AnimistsAwakening()));
        harness.setHand(player1, List.of(new AnimistsAwakening()));
        harness.addMana(player1, ManaColor.GREEN, 3); // X=2

        harness.castSorcery(player1, 0, 2);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Forest");
        assertThat(findPermanent(player1, "Forest").isTapped()).isFalse();
    }

    @Test
    @DisplayName("One instant or sorcery in the graveyard is not enough for spell mastery")
    void oneInstantOrSorceryDoesNotEnableSpellMastery() {
        Card forest = new Forest();

        harness.setLibrary(player1, List.of(forest));

        harness.setGraveyard(player1, List.of(new AnimistsAwakening()));
        harness.setHand(player1, List.of(new AnimistsAwakening()));
        harness.addMana(player1, ManaColor.GREEN, 3); // X=2

        harness.castSorcery(player1, 0, 2);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Forest").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Casting with X=0 reveals nothing and puts no lands onto the battlefield")
    void xZeroDoesNothing() {
        harness.setLibrary(player1, List.of(new Forest()));

        harness.setHand(player1, List.of(new AnimistsAwakening()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    void mixedInstantAndSorceryUntapOnlyNewLands() {
        Permanent existing = harness.addToBattlefieldAndReturn(player1, new Forest());
        existing.tap();
        harness.setLibrary(player1, List.of(new Mountain(), new Forest()));
        harness.setGraveyard(player1, List.of(new FieryImpulse(), new AnimistsAwakening()));
        harness.setHand(player1, List.of(new AnimistsAwakening()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castSorcery(player1, 0, 2);
        harness.passBothPriorities();

        assertThat(existing.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent != existing)
                .hasSize(2)
                .allMatch(permanent -> !permanent.isTapped());
    }

    @Test
    void creaturesAndLandsInGraveyardDoNotCountForSpellMastery() {
        harness.setLibrary(player1, List.of(new Mountain()));
        harness.setGraveyard(player1, List.of(new FieryImpulse(), new LeafGilder(), new Forest()));
        harness.setHand(player1, List.of(new AnimistsAwakening()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castSorcery(player1, 0, 1);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Mountain").isTapped()).isTrue();
    }

    @Test
    void unrevealedCardsStayAboveRevealedNonlands() {
        Card first = new LeafGilder();
        Card second = new FieryImpulse();
        Card unrevealed = new Forest();
        harness.setLibrary(player1, List.of(first, second, unrevealed));
        harness.setHand(player1, List.of(new AnimistsAwakening()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castSorcery(player1, 0, 2);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(unrevealed);
        assertThat(gd.playerDecks.get(player1.getId()).subList(1, 3))
                .containsExactlyInAnyOrder(first, second);
    }

    @Test
    void emptyLibraryRevealsNothing() {
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new AnimistsAwakening()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castSorcery(player1, 0, 3);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Animist's Awakening");
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void enteringMountainTriggersValakut() {
        harness.addToBattlefield(player1, new ValakutTheMoltenPinnacle());
        for (int i = 0; i < 5; i++) {
            harness.addToBattlefield(player1, new Mountain());
        }
        harness.setLibrary(player1, List.of(new Mountain()));
        harness.setHand(player1, List.of(new AnimistsAwakening()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castSorcery(player1, 0, 1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.assertLife(player2, 17);
    }

    @Test
    void simultaneouslyEnteringMountainsBothMeetValakutThreshold() {
        harness.addToBattlefield(player1, new ValakutTheMoltenPinnacle());
        for (int i = 0; i < 4; i++) {
            harness.addToBattlefield(player1, new Mountain());
        }
        harness.setLibrary(player1, List.of(new Mountain(), new Mountain()));
        harness.setHand(player1, List.of(new AnimistsAwakening()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castSorcery(player1, 0, 2);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.assertLife(player2, 14);
    }
}
