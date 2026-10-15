package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.Gingerbrute;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SoulGuideLantern.class, Forest.class, Gingerbrute.class})
class SoulGuideLanternTest extends BaseCardTest {

    @Test
    void entersAndExilesTargetCardFromAnyGraveyard() {
        Card ownCard = new Gingerbrute();
        Card opponentCard = new Forest();
        harness.setGraveyard(player1, List.of(ownCard));
        harness.setGraveyard(player2, List.of(opponentCard));
        castLantern();

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(opponentCard.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(ownCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(opponentCard);
    }

    @Test
    void tapAndSacrificeExilesEachOpponentsGraveyard() {
        SoulGuideLantern lantern = new SoulGuideLantern();
        Card ownCard = new Forest();
        Card opponentCard = new Gingerbrute();
        harness.addToBattlefield(player1, lantern);
        harness.setGraveyard(player1, List.of(ownCard));
        harness.setGraveyard(player2, List.of(opponentCard));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(ownCard, lantern);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(opponentCard);
    }

    @Test
    void paidTapAndSacrificeDrawsACard() {
        SoulGuideLantern lantern = new SoulGuideLantern();
        Card draw = new Forest();
        harness.addToBattlefield(player1, lantern);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(draw));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(draw);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(lantern);
        harness.assertNotOnBattlefield(player1, "Soul-Guide Lantern");
    }

    @Test
    void enterAbilityCanExileFromControllersGraveyard() {
        Card target = new Forest();
        harness.setGraveyard(player1, List.of(target));
        castLantern();
        harness.passBothPriorities();

        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(target);
    }

    @Test
    void enterAbilityRequiresATargetWhenOneIsAvailable() {
        Card target = new Forest();
        harness.setGraveyard(player2, List.of(target));
        castLantern();
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of()))
                .isInstanceOf(IllegalStateException.class);

        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.passBothPriorities();
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(target);
    }

    @Test
    void enterAbilityIsRemovedFromStackWhenNoLegalTargetExists() {
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of());
        castLantern();

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Soul-Guide Lantern");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void enterAbilityDoesNotExileAnotherCardWhenTargetLeavesGraveyard() {
        Card target = new Forest();
        Card other = new Gingerbrute();
        harness.setGraveyard(player2, List.of(target, other));
        castLantern();
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));

        harness.setGraveyard(player2, List.of(other));
        harness.setHand(player2, List.of(target));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(other);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(target);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    void enterAbilityStillResolvesAfterLanternIsSacrificedToDraw() {
        Card target = new Forest();
        Card draw = new Gingerbrute();
        harness.setGraveyard(player2, List.of(target));
        harness.setLibrary(player1, List.of(draw));
        castLantern();
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 1, null, null);

        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).contains(draw);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(target);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(target);
        harness.assertNotOnBattlefield(player1, "Soul-Guide Lantern");
    }

    @Test
    void exileAbilitySacrificesImmediatelyAndExilesCardsPresentAtResolution() {
        SoulGuideLantern lantern = new SoulGuideLantern();
        Card first = new Forest();
        Card later = new Gingerbrute();
        harness.addToBattlefield(player1, lantern);
        harness.setGraveyard(player2, List.of(first));

        harness.activateAbility(player1, 0, 0, null, null);

        harness.assertNotOnBattlefield(player1, "Soul-Guide Lantern");
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(lantern);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(first);
        harness.setGraveyard(player2, List.of(first, later));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactlyInAnyOrder(first, later);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(lantern);
    }

    @Test
    void drawAbilityDoesNotExileGraveyards() {
        SoulGuideLantern lantern = new SoulGuideLantern();
        Card graveyardCard = new Gingerbrute();
        Card draw = new Forest();
        harness.addToBattlefield(player1, lantern);
        harness.setGraveyard(player2, List.of(graveyardCard));
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(draw));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, null);

        harness.assertNotOnBattlefield(player1, "Soul-Guide Lantern");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(draw);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(graveyardCard);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    void tappedLanternCannotActivateEitherAbility() {
        harness.addToBattlefieldAndReturn(player1, new SoulGuideLantern()).tap();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Soul-Guide Lantern");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void drawAbilityCannotBeActivatedWithoutMana() {
        harness.addToBattlefield(player1, new SoulGuideLantern());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Soul-Guide Lantern");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    private void castLantern() {
        harness.castFromHand(player1, new SoulGuideLantern(), "{1}");
    }
}
