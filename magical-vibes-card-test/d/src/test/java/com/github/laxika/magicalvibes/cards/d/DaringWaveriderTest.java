package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.c.CounselOfTheSoratami;
import com.github.laxika.magicalvibes.cards.r.RemoveSoul;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.Sift;
import com.github.laxika.magicalvibes.cards.t.Tidings;
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

@CardUsed({DaringWaverider.class, GrizzlyBears.class, Shock.class, Tidings.class,
        CounselOfTheSoratami.class, RemoveSoul.class, Sift.class})
class DaringWaveriderTest extends BaseCardTest {

    @Test
    @DisplayName("ETB targets only your instant or sorcery with mana value 4 or less")
    void etbTargetsOnlyQualifyingOwnSpell() {
        Card shock = new Shock();
        Card expensiveSorcery = new Tidings();
        Card creature = new GrizzlyBears();
        Card opponentShock = new Shock();
        harness.setGraveyard(player1, List.of(shock, expensiveSorcery, creature));
        harness.setGraveyard(player2, List.of(opponentShock));
        harness.setHand(player1, List.of(new DaringWaverider()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(shock.getId());
    }

    @Test
    @DisplayName("Casts the chosen spell for free and exiles it afterward")
    void castsChosenSpellForFreeAndExilesIt() {
        Shock shock = new Shock();
        harness.setGraveyard(player1, List.of(shock));
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new DaringWaverider()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(shock.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(target.getId()));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(shock.getId()));
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Declining the cast leaves the card in your graveyard")
    void decliningCastLeavesCardInGraveyard() {
        Shock shock = new Shock();
        harness.setGraveyard(player1, List.of(shock));
        harness.setHand(player1, List.of(new DaringWaverider()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(shock.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(shock.getId()));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(card -> card.getId().equals(shock.getId()));
    }

    @Test
    void includesSorceryAtManaValueFour() {
        Sift sift = new Sift();
        harness.setGraveyard(player1, List.of(sift, new Tidings()));
        harness.setHand(player1, List.of(new DaringWaverider()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(sift.getId());
    }

    @Test
    void castsSorceryDuringEtbResolutionWithoutMana() {
        CounselOfTheSoratami counsel = new CounselOfTheSoratami();
        Card firstDraw = new GrizzlyBears();
        Card secondDraw = new Shock();
        harness.setLibrary(player1, List.of(firstDraw, secondDraw));
        harness.setGraveyard(player1, List.of(counsel));
        harness.setHand(player1, List.of(new DaringWaverider()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(counsel.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(firstDraw, secondDraw);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(counsel);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(counsel);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void uncastableSpellWithoutLegalTargetsStaysInGraveyard() {
        RemoveSoul removeSoul = new RemoveSoul();
        harness.setGraveyard(player1, List.of(removeSoul));
        harness.setHand(player1, List.of(new DaringWaverider()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(removeSoul.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(removeSoul);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(removeSoul);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void targetLeavingGraveyardBeforeResolutionCannotBeCast() {
        Shock shock = new Shock();
        harness.setGraveyard(player1, List.of(shock));
        harness.setHand(player1, List.of(new DaringWaverider()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(shock.getId()));
        harness.setGraveyard(player1, List.of());
        harness.setHand(player1, List.of(shock));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(shock);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(shock);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }
}
