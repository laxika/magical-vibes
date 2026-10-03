package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.AncientBrontodon;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BituminousBlast.class, AncientBrontodon.class, GrizzlyBears.class, HillGiant.class, Mountain.class})
class BituminousBlastTest extends BaseCardTest {


    @Test
    @DisplayName("Deals 4 damage to target creature, killing a 3/3")
    void deals4DamageKillingTargetCreature() {
        prepareCaster();
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new HillGiant()); // 3/3

        harness.castInstant(player1, 0, giant.getId());
        harness.passBothPriorities(); // resolve cascade (empty library, no-op)
        harness.passBothPriorities(); // resolve the spell

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(giant.getId()));
        harness.assertInGraveyard(player2, "Hill Giant");
    }

    @Test
    @DisplayName("Marks 4 damage on a creature large enough to survive")
    void marks4DamageOnSurvivingCreature() {
        prepareCaster();
        Permanent brontodon = harness.addToBattlefieldAndReturn(player2, new AncientBrontodon()); // 9/9

        harness.castInstant(player1, 0, brontodon.getId());
        harness.passBothPriorities(); // resolve cascade (empty library, no-op)
        harness.passBothPriorities(); // resolve the spell

        Permanent surviving = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getId().equals(brontodon.getId()))
                .findFirst().orElseThrow();
        assertThat(surviving.getMarkedDamage()).isEqualTo(4);
    }


    @Test
    @DisplayName("Cascade digs past a land to the first lesser-mana-value nonland")
    void cascadeOffersLesserManaValueNonland() {
        prepareCaster();
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new HillGiant());

        // Bituminous Blast is {3}{B}{R} = mana value 5. Dig skips the Mountain and stops at
        // Grizzly Bears (MV 2 < 5).
        harness.setLibrary(player1, List.of(new Mountain(), new GrizzlyBears()));

        harness.castInstant(player1, 0, giant.getId());
        harness.passBothPriorities(); // resolve the cascade trigger

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        List<String> castable = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().cards().stream().map(Card::getName).toList();
        assertThat(castable).containsExactly("Grizzly Bears");
    }

    @Test
    @DisplayName("Cascade exiles the skipped cards and hit while offering the free cast")
    void cascadeCardsEnterExile() {
        prepareCaster();
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        Mountain land = new Mountain();
        GrizzlyBears hit = new GrizzlyBears();
        harness.setLibrary(player1, List.of(land, hit));

        harness.castInstant(player1, 0, giant.getId());
        harness.passBothPriorities();

        assertThat(gd.exiledCards).extracting(entry -> entry.card().getId())
                .containsExactlyInAnyOrder(land.getId(), hit.getId());
        assertThat(gd.cardsExiledThisTurn).isEqualTo(2);
    }

    @Test
    @DisplayName("Cascade skips equal and greater mana values and stops at the first lesser card")
    void cascadeSkipsEqualAndGreaterManaValues() {
        prepareCaster();
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        BituminousBlast equal = new BituminousBlast();
        AncientBrontodon greater = new AncientBrontodon();
        GrizzlyBears hit = new GrizzlyBears();
        Mountain below = new Mountain();
        harness.setLibrary(player1, List.of(equal, greater, hit, below));

        harness.castInstant(player1, 0, giant.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().cards()).containsExactly(hit);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(below);
        harness.handleCardChosen(player1, -1);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(below);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(below, equal, greater, hit);
        assertThat(gd.exiledCards).isEmpty();
        harness.passBothPriorities();
        harness.assertInGraveyard(player2, "Hill Giant");
    }

    @Test
    @DisplayName("Cascade casts its hit for free and that spell resolves before Bituminous Blast")
    void cascadeCastsHitBeforeBlastResolves() {
        prepareCaster();
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        Mountain land = new Mountain();
        GrizzlyBears hit = new GrizzlyBears();
        Mountain below = new Mountain();
        harness.setLibrary(player1, List.of(land, hit, below));

        harness.castInstant(player1, 0, giant.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack.getLast().getCard().getId()).isEqualTo(hit.getId());
        assertThat(gd.playersWhoPlayedCardFromExileThisTurn).contains(player1.getId());
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(below, land);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Hill Giant");
        harness.passBothPriorities();
        harness.assertInGraveyard(player2, "Hill Giant");
        harness.assertInGraveyard(player1, "Bituminous Blast");
    }

    @Test
    @DisplayName("Cascade returns all cards to the library when there is no qualifying hit")
    void cascadeWithNoHitReturnsAllCards() {
        prepareCaster();
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        Mountain land = new Mountain();
        BituminousBlast equal = new BituminousBlast();
        AncientBrontodon greater = new AncientBrontodon();
        harness.setLibrary(player1, List.of(land, equal, greater));

        harness.castInstant(player1, 0, giant.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(land, equal, greater);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertInGraveyard(player2, "Hill Giant");
    }

    @Test
    @DisplayName("Cannot target a player")
    void cannotTargetPlayer() {
        prepareCaster();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }


    private void prepareCaster() {
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player1);
        // Empty library by default so the cascade trigger is a no-op unless a test stocks it.
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new BituminousBlast()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
    }
}
