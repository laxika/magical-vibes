package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.ArmoredSkaab;
import com.github.laxika.magicalvibes.cards.d.Dissipate;
import com.github.laxika.magicalvibes.cards.t.ThinkTwice;
import com.github.laxika.magicalvibes.cards.w.WalkingCorpse;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SkaabGoliath.class, ArmoredSkaab.class, WalkingCorpse.class, ThinkTwice.class, Dissipate.class})
class SkaabGoliathTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Skaab Goliath exiles two creature cards from graveyard")
    void castingExilesTwoCreatureCards() {
        ArmoredSkaab skaab1 = new ArmoredSkaab();
        ArmoredSkaab skaab2 = new ArmoredSkaab();
        WalkingCorpse corpse = new WalkingCorpse();
        harness.setGraveyard(player1, List.of(skaab1, skaab2, corpse));

        harness.setHand(player1, List.of(new SkaabGoliath()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castCreatureWithMultipleGraveyardExile(player1, 0, List.of(0, 1));

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(entry.getCard()).isInstanceOf(SkaabGoliath.class);

        // Two creature cards exiled, one creature remains in graveyard
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Skaab Goliath resolves and enters the battlefield")
    void resolvesOntoBattlefield() {
        ArmoredSkaab skaab1 = new ArmoredSkaab();
        ArmoredSkaab skaab2 = new ArmoredSkaab();
        harness.setGraveyard(player1, List.of(skaab1, skaab2));

        harness.setHand(player1, List.of(new SkaabGoliath()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castCreatureWithMultipleGraveyardExile(player1, 0, List.of(0, 1));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Skaab Goliath");
    }

    @Test
    @DisplayName("Cannot cast Skaab Goliath with only 1 creature card in graveyard")
    void cannotCastWithOnlyOneCreatureCard() {
        ArmoredSkaab skaab = new ArmoredSkaab();
        harness.setGraveyard(player1, List.of(skaab));

        harness.setHand(player1, List.of(new SkaabGoliath()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        // Only 1 creature card — card should not be playable
        assertThatThrownBy(() -> harness.castCreatureWithMultipleGraveyardExile(player1, 0, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot cast Skaab Goliath exiling non-creature cards")
    void cannotExileNonCreatureCards() {
        ArmoredSkaab skaab1 = new ArmoredSkaab();
        ArmoredSkaab skaab2 = new ArmoredSkaab();
        ThinkTwice instant = new ThinkTwice();
        harness.setGraveyard(player1, List.of(skaab1, skaab2, instant));

        harness.setHand(player1, List.of(new SkaabGoliath()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        // Index 2 is an instant, not a creature — must exile creature cards only
        assertThatThrownBy(() -> harness.castCreatureWithMultipleGraveyardExile(player1, 0, List.of(0, 2)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    @Test
    @DisplayName("Cannot cast Skaab Goliath with empty graveyard")
    void cannotCastWithEmptyGraveyard() {
        harness.setHand(player1, List.of(new SkaabGoliath()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        // No creatures in graveyard — card should not be playable
        assertThatThrownBy(() -> harness.castCreatureWithMultipleGraveyardExile(player1, 0, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Exile cost is paid even if Skaab Goliath is countered")
    void exileCostPaidEvenIfCountered() {
        ArmoredSkaab skaab1 = new ArmoredSkaab();
        ArmoredSkaab skaab2 = new ArmoredSkaab();
        harness.setGraveyard(player1, List.of(skaab1, skaab2));

        harness.setHand(player1, List.of(new SkaabGoliath()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castCreatureWithMultipleGraveyardExile(player1, 0, List.of(0, 1));

        harness.setHand(player2, List.of(new Dissipate()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0, gd.stack.getFirst().getCard().getId());

        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player1, "Skaab Goliath");
        harness.assertInGraveyard(player2, "Dissipate");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .contains(skaab1, skaab2)
                .anyMatch(card -> card instanceof SkaabGoliath);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Can cast with exactly 2 creature cards in graveyard (no surplus needed)")
    void canCastWithExactlyTwoCreatures() {
        ArmoredSkaab skaab1 = new ArmoredSkaab();
        WalkingCorpse corpse = new WalkingCorpse();
        harness.setGraveyard(player1, List.of(skaab1, corpse));

        harness.setHand(player1, List.of(new SkaabGoliath()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castCreatureWithMultipleGraveyardExile(player1, 0, List.of(0, 1));

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(2);
    }

    @Test
    void cannotPayWithDuplicateCards() {
        ArmoredSkaab first = new ArmoredSkaab();
        WalkingCorpse second = new WalkingCorpse();
        WalkingCorpse third = new WalkingCorpse();
        harness.setGraveyard(player1, List.of(first, second, third));
        harness.setHand(player1, List.of(new SkaabGoliath()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.castCreatureWithMultipleGraveyardExile(player1, 0, List.of(0, 0)))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(first, second, third);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    void cannotExileMoreThanTwoCards() {
        ArmoredSkaab first = new ArmoredSkaab();
        WalkingCorpse second = new WalkingCorpse();
        WalkingCorpse third = new WalkingCorpse();
        harness.setGraveyard(player1, List.of(first, second, third));
        harness.setHand(player1, List.of(new SkaabGoliath()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.castCreatureWithMultipleGraveyardExile(player1, 0, List.of(0, 1, 2)))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(first, second, third);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    void cannotUnderpayWithEnoughCreaturesAvailable() {
        ArmoredSkaab first = new ArmoredSkaab();
        WalkingCorpse second = new WalkingCorpse();
        WalkingCorpse third = new WalkingCorpse();
        harness.setGraveyard(player1, List.of(first, second, third));
        harness.setHand(player1, List.of(new SkaabGoliath()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.castCreatureWithMultipleGraveyardExile(player1, 0, List.of(0)))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(first, second, third);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    void cannotPayUsingOpponentsGraveyard() {
        ArmoredSkaab ownCreature = new ArmoredSkaab();
        WalkingCorpse opposingCreature = new WalkingCorpse();
        harness.setGraveyard(player1, List.of(ownCreature));
        harness.setGraveyard(player2, List.of(opposingCreature));
        harness.setHand(player1, List.of(new SkaabGoliath()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.castCreatureWithMultipleGraveyardExile(player1, 0, List.of(0, 1)))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(ownCreature);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opposingCreature);
    }

    @Test
    void exilesChosenCreaturesAroundNonCreatureCards() {
        ThinkTwice instant = new ThinkTwice();
        ArmoredSkaab first = new ArmoredSkaab();
        WalkingCorpse unchosen = new WalkingCorpse();
        WalkingCorpse second = new WalkingCorpse();
        harness.setGraveyard(player1, List.of(instant, first, unchosen, second));
        harness.setHand(player1, List.of(new SkaabGoliath()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castCreatureWithMultipleGraveyardExile(player1, 0, List.of(3, 1));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Skaab Goliath");
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(instant, unchosen);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyInAnyOrder(first, second);
    }

}
