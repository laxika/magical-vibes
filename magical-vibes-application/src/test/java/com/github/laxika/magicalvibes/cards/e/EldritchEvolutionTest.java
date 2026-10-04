package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.b.BenalishKnight;
import com.github.laxika.magicalvibes.cards.g.GoldMyr;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.p.Plains;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EldritchEvolution.class, AirElemental.class, BenalishKnight.class, GoldMyr.class,
        GrizzlyBears.class, HillGiant.class, LlanowarElves.class, Plains.class})
class EldritchEvolutionTest extends BaseCardTest {

    @Test
    @DisplayName("Casting sacrifices the chosen creature and puts the spell on the stack")
    void castingSacrificesCreature() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new LlanowarElves());

        harness.setHand(player1, List.of(new EldritchEvolution()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorceryWithSacrifice(player1, 0, sacrifice.getId());

        assertThat(gd.stack).hasSize(1);
        harness.assertNotOnBattlefield(player1, "Llanowar Elves");
        harness.assertInGraveyard(player1, "Llanowar Elves");
    }

    @Test
    @DisplayName("Cannot cast without a creature to sacrifice")
    void cannotCastWithoutSacrifice() {
        harness.setHand(player1, List.of(new EldritchEvolution()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castSorceryWithSacrifice(player1, 0, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sacrifice");
    }

    @Test
    @DisplayName("Sacrifice MV 1: search offers creatures with MV ≤ 3")
    void sacrificeMv1OffersCreaturesUpToMv3() {
        castWithSacrifice(new LlanowarElves());
        setupLibrary();

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        // Sac MV 1 → X = 3: Gold Myr (2), Benalish Knight (3); not Hill Giant (4) or Air Elemental (5)
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards().stream()
                .map(Card::getName))
                .containsExactlyInAnyOrder("Gold Myr", "Benalish Knight");
    }

    @Test
    @DisplayName("Sacrifice MV 2: search offers creatures with MV ≤ 4")
    void sacrificeMv2OffersCreaturesUpToMv4() {
        castWithSacrifice(new GrizzlyBears());
        setupLibrary();

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards().stream()
                .map(Card::getName))
                .containsExactlyInAnyOrder("Gold Myr", "Benalish Knight", "Hill Giant");
    }

    @Test
    @DisplayName("Choosing a creature puts it onto the battlefield and exiles the spell")
    void choosingPutsCreatureOntoBattlefieldAndExilesSpell() {
        castWithSacrifice(new LlanowarElves());
        setupLibrary();

        harness.passBothPriorities();

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Gold Myr")
                        || p.getCard().getName().equals("Benalish Knight"));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Eldritch Evolution"));
        harness.assertNotInGraveyard(player1, "Eldritch Evolution");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Failing to find still exiles the spell and preserves the library cards")
    void failingToFindStillExilesSpell() {
        castWithSacrifice(new LlanowarElves());
        GoldMyr eligible = new GoldMyr();
        Plains land = new Plains();
        harness.setLibrary(player1, List.of(eligible, land));

        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(eligible, land);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Eldritch Evolution"));
        harness.assertNotInGraveyard(player1, "Eldritch Evolution");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("No eligible creature still completes resolution and exiles the spell")
    void noEligibleCreatureStillExilesSpell() {
        castWithSacrifice(new LlanowarElves());
        HillGiant tooExpensive = new HillGiant();
        Plains land = new Plains();
        harness.setLibrary(player1, List.of(tooExpensive, land));

        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(tooExpensive, land);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Eldritch Evolution"));
        harness.assertNotInGraveyard(player1, "Eldritch Evolution");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Search puts exactly the chosen creature onto the battlefield untapped")
    void chosenCreatureEntersUntappedAndLeavesLibrary() {
        castWithSacrifice(new LlanowarElves());
        GoldMyr chosen = new GoldMyr();
        BenalishKnight other = new BenalishKnight();
        harness.setLibrary(player1, List.of(chosen, other));

        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).singleElement()
                .satisfies(p -> {
                    assertThat(p.getCard()).isSameAs(chosen);
                    assertThat(p.isTapped()).isFalse();
                });
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(other);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    private void castWithSacrifice(Card sacrificeCard) {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, sacrificeCard);

        harness.setHand(player1, List.of(new EldritchEvolution()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorceryWithSacrifice(player1, 0, sacrifice.getId());
    }

    private void setupLibrary() {
        // Gold Myr MV2, Benalish Knight MV3, Hill Giant MV4, Air Elemental MV5, Plains (non-creature)
        harness.setLibrary(player1, List.of(new GoldMyr(), new BenalishKnight(), new HillGiant(), new AirElemental(), new Plains()));
    }
}
