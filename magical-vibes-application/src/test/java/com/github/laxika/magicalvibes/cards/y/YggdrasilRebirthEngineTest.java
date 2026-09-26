package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({YggdrasilRebirthEngine.class, GrizzlyBears.class, Forest.class})
class YggdrasilRebirthEngineTest extends BaseCardTest {

    @Test
    @DisplayName("ETB exiles only creature cards from its controller's graveyard and tracks them")
    void etbExilesCreatureCardsFromGraveyard() {
        Card creature = new GrizzlyBears();
        Card land = new Forest();
        harness.setGraveyard(player1, List.of(creature, land));
        harness.setHand(player1, List.of(new YggdrasilRebirthEngine()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castArtifact(player1, 0);
        resolveAllTriggers();

        Permanent engine = findPermanent(player1, "Yggdrasil, Rebirth Engine");
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(land);
        assertThat(gd.getCardsExiledByPermanent(engine.getId())).containsExactly(creature);
    }

    @Test
    @DisplayName("Taps to exile the top three cards of its controller's library")
    void tapsToExileTopThreeCards() {
        Permanent engine = harness.addToBattlefieldAndReturn(player1, new YggdrasilRebirthEngine());
        Card first = new GrizzlyBears();
        Card second = new Forest();
        Card third = new GrizzlyBears();
        harness.setLibrary(player1, List.of(first, second, third));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.getCardsExiledByPermanent(engine.getId()))
                .containsExactlyInAnyOrder(first, second, third);
    }

    @Test
    @DisplayName("Returns a creature exiled with it under the controller's control with haste")
    void returnsCreatureWithHaste() {
        Permanent engine = harness.addToBattlefieldAndReturn(player1, new YggdrasilRebirthEngine());
        Card creature = new GrizzlyBears();
        Card land = new Forest();
        gd.addToExile(player1.getId(), creature, engine.getId());
        gd.addToExile(player1.getId(), land, engine.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        Permanent returned = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(creature.getId()))
                .findFirst().orElseThrow();
        assertThat(gqs.hasKeyword(gd, returned, Keyword.HASTE)).isTrue();
        assertThat(gd.getCardsExiledByPermanent(engine.getId())).containsExactly(land);
    }

    @Test
    @DisplayName("Choosing among multiple exiled creatures also grants haste")
    void choosingAmongMultipleCreaturesGrantsHaste() {
        Permanent engine = harness.addToBattlefieldAndReturn(player1, new YggdrasilRebirthEngine());
        Card first = new GrizzlyBears();
        Card second = new GrizzlyBears();
        gd.addToExile(player1.getId(), first, engine.getId());
        gd.addToExile(player1.getId(), second, engine.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.LibraryRevealChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(first.getId()));

        Permanent returned = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(first.getId()))
                .findFirst().orElseThrow();
        assertThat(gqs.hasKeyword(gd, returned, Keyword.HASTE)).isTrue();
        assertThat(gd.getCardsExiledByPermanent(engine.getId())).containsExactly(second);
    }
}
