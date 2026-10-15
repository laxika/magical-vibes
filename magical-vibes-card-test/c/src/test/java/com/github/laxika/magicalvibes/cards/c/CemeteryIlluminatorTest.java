package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.a.Abrade;
import com.github.laxika.magicalvibes.cards.p.PullFromEternity;
import com.github.laxika.magicalvibes.cards.s.Shock;
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

@CardUsed({CemeteryIlluminator.class, GrizzlyBears.class, Shock.class, Abrade.class, PullFromEternity.class})
class CemeteryIlluminatorTest extends BaseCardTest {

    @Test
    @DisplayName("Its enters-the-battlefield ability exiles and remembers a graveyard card")
    void exilesAndImprintsOnEnter() {
        Card exiled = new GrizzlyBears();
        Permanent illuminator = enterIlluminatorWith(exiled);

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(exiled);
        assertThat(gd.getImprintedCard(illuminator.getCard())).isSameAs(exiled);
    }

    @Test
    @DisplayName("Attacking exiles and remembers another graveyard card")
    void exilesAndImprintsOnAttack() {
        Card firstExiled = new GrizzlyBears();
        Permanent illuminator = enterIlluminatorWith(firstExiled);
        illuminator.setSummoningSick(false);

        Card secondExiled = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(secondExiled));
        declareAttackers(List.of(0));
        harness.passBothPriorities();

        harness.handleMultipleCardsChosen(player1, List.of(secondExiled.getId()));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(firstExiled, secondExiled);
        assertThat(gd.getImprintedCard(illuminator.getCard())).isSameAs(secondExiled);
    }

    @Test
    @DisplayName("Casts one matching spell from the top of the library each turn")
    void castsMatchingTopSpellOnlyOnceEachTurn() {
        enterIlluminatorWith(new GrizzlyBears());
        Card firstSpell = new GrizzlyBears();
        harness.setLibrary(player1, List.of(firstSpell));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAndResolveFromLibraryTop(player1);

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        Card secondSpell = new GrizzlyBears();
        harness.setLibrary(player1, List.of(secondSpell));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(secondSpell);
    }

    @Test
    @DisplayName("Cannot cast a top spell without a shared card type")
    void rejectsNonMatchingTopSpell() {
        enterIlluminatorWith(new GrizzlyBears());
        Card shock = new Shock();
        harness.setLibrary(player1, List.of(shock));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(shock);
    }

    @Test
    void castingLimitResetsOnOpponentsTurn() {
        enterIlluminatorWith(new Shock());
        harness.setLibrary(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveFromLibraryTop(player1, player2.getId());
        harness.setLibrary(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1, player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.passUntil(player2, TurnStep.UPKEEP);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveFromLibraryTop(player1, player2.getId());

        harness.assertLife(player2, 16);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void matchingCreatureStillRequiresNormalTiming() {
        enterIlluminatorWith(new CemeteryIlluminator());
        Card spell = new CemeteryIlluminator();
        harness.setLibrary(player1, List.of(spell));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);
        harness.addMana(player1, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(spell);
    }

    @Test
    void matchingSpellStillRequiresCorrectManaColors() {
        enterIlluminatorWith(new CemeteryIlluminator());
        Card spell = new CemeteryIlluminator();
        harness.setLibrary(player1, List.of(spell));
        harness.addMana(player1, ManaColor.GREEN, 3);

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(spell);

        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castAndResolveFromLibraryTop(player1);

        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(spell);
    }

    @Test
    void retainsCastingPermissionFromEarlierExiledCards() {
        Card firstExiled = new CemeteryIlluminator();
        Permanent illuminator = enterIlluminatorWith(firstExiled);
        illuminator.setSummoningSick(false);
        Card secondExiled = new Abrade();
        harness.setGraveyard(player2, List.of(secondExiled));
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(0)));
        harness.passBothPriorities();
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> harness.handleMultipleCardsChosen(player1, List.of(secondExiled.getId())));
        harness.passBothPriorities();
        harness.passUntilWithNoAttackers(player1, TurnStep.POSTCOMBAT_MAIN);

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(firstExiled, secondExiled);
        Card spell = new CemeteryIlluminator();
        harness.setLibrary(player1, List.of(spell));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castAndResolveFromLibraryTop(player1);

        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(spell);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(spell.getId()));
    }

    @Test
    void losesMatchingPermissionWhenExiledCardLeavesExile() {
        Card exiled = new CemeteryIlluminator();
        enterIlluminatorWith(exiled);
        harness.setHand(player1, List.of(new PullFromEternity()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, exiled.getId());

        assertThat(gd.findExiledCard(exiled.getId())).isNull();
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(exiled);
        Card spell = new CemeteryIlluminator();
        harness.setLibrary(player1, List.of(spell));
        harness.addMana(player1, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(spell);
    }

    @Test
    void privatelyShowsTopCardWithoutExiledCardsOrPriority() {
        harness.addToBattlefield(player1, new CemeteryIlluminator());
        harness.setLibrary(player1, List.of(new Abrade()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearMessages();

        harness.publishState();

        assertThat(harness.getConn1().getSentMessages())
                .anyMatch(message -> message.contains("\"revealedLibraryTopCards\":[[{")
                        && message.contains("Abrade"));
        assertThat(harness.getConn2().getSentMessages())
                .noneMatch(message -> message.contains("Abrade"));
    }

    @Test
    void emptyGraveyardsDoNotGrantCastingPermission() {
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of());
        harness.enterBattlefieldAndReturn(player1, new CemeteryIlluminator());
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        Card spell = new CemeteryIlluminator();
        harness.setLibrary(player1, List.of(spell));
        harness.addMana(player1, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(spell);
    }

    @Test
    void canChooseFromControllersOwnGraveyard() {
        Card exiled = new Abrade();
        harness.setGraveyard(player1, List.of(exiled));
        harness.setGraveyard(player2, List.of());
        harness.enterBattlefieldAndReturn(player1, new CemeteryIlluminator());
        harness.passBothPriorities();

        harness.handleMultipleCardsChosen(player1, List.of(exiled.getId()));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(exiled);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(exiled);
    }

    private Permanent enterIlluminatorWith(Card card) {
        harness.setGraveyard(player2, List.of(card));
        Permanent illuminator = harness.enterBattlefieldAndReturn(player1, new CemeteryIlluminator());
        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(card.getId());

        harness.handleMultipleCardsChosen(player1, List.of(card.getId()));
        harness.passBothPriorities();
        return illuminator;
    }
}
