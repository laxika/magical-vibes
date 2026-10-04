package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({FallToEarth.class, GrizzlyBears.class, Plains.class})
class FallToEarthTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles target creature and each player gains 3 life")
    void exilesCreatureAndEachPlayerGainsLife() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castFallToEarth(target);

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Fall to Earth");
        harness.assertLife(player1, 23);
        harness.assertLife(player2, 23);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Plains());
        harness.setHand(player1, List.of(new FallToEarth()));
        addSpellMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    @Test
    @DisplayName("Basic landcycling discards the card and offers only basic lands")
    void basicLandcyclingSearchesForBasicLand() {
        harness.setHand(player1, List.of(new FallToEarth()));
        harness.setLibrary(player1, List.of(new Plains(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Fall to Earth");
        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).extracting(Card::getName).containsExactly("Plains");

        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Plains");
    }

    @Test
    @DisplayName("Neither player gains life when the only target leaves before resolution")
    void illegalTargetPreventsLifeGain() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castFallToEarth(target);
        gd.playerBattlefields.get(player2.getId()).remove(target);

        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Fall to Earth");
    }

    @Test
    @DisplayName("Can exile its controller's own creature")
    void canExileOwnCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castFallToEarth(target);

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.exiledCards).extracting(entry -> entry.card()).contains(target.getCard());
        harness.assertLife(player1, 23);
        harness.assertLife(player2, 23);
    }

    @Test
    @DisplayName("Basic landcycling pays discard immediately and does not grant life")
    void basicLandcyclingCanFailToFind() {
        harness.setHand(player1, List.of(new FallToEarth()));
        harness.setLibrary(player1, List.of(new Plains()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);

        harness.assertInGraveyard(player1, "Fall to Earth");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getName).containsExactly("Plains");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Basic landcycling cannot be activated with less than two mana")
    void basicLandcyclingRequiresTwoMana() {
        harness.setHand(player1, List.of(new FallToEarth()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Fall to Earth");
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    private void castFallToEarth(Permanent target) {
        harness.setHand(player1, List.of(new FallToEarth()));
        addSpellMana();
        harness.castInstant(player1, 0, target.getId());
    }

    private void addSpellMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }
}
