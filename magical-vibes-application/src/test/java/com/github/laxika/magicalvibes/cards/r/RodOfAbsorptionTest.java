package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.o.Opt;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RodOfAbsorption.class, Divination.class, Opt.class})
class RodOfAbsorptionTest extends BaseCardTest {

    @Test
    void exilesResolvingInstantOrSorceryWithRodTracking() {
        Permanent rod = harness.addToBattlefieldAndReturn(player1, new RodOfAbsorption());
        Divination divination = new Divination();
        harness.setHand(player1, List.of(divination));
        harness.setLibrary(player1, List.of(new Opt(), new Opt(), new Opt()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.getCardsExiledByPermanent(rod.getId()))
                .extracting(Card::getId)
                .containsExactly(divination.getId());
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .doesNotContain(divination.getId());
    }

    @Test
    void sacrificesAndCastsAnyNumberWithinTheXManaValueLimit() {
        Permanent rod = harness.addToBattlefieldAndReturn(player1, new RodOfAbsorption());
        Opt opt = new Opt();
        Divination divination = new Divination();
        gd.addToExile(player1.getId(), opt, rod.getId());
        gd.addToExile(player1.getId(), divination, rod.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 4, null);
        harness.passBothPriorities();

        PendingInteraction.ImprovisationCapstoneCastChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.ImprovisationCapstoneCastChoice.class);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(opt.getId(), divination.getId());
        assertThat(choice.maxTotalManaValue()).isEqualTo(4);

        harness.handleMultipleCardsChosen(player1, List.of(opt.getId(), divination.getId()));

        assertThat(gd.findExiledCard(opt.getId())).isNull();
        assertThat(gd.findExiledCard(divination.getId())).isNull();
        assertThat(gd.stack).extracting(entry -> entry.getCard().getId())
                .contains(opt.getId(), divination.getId());
    }

    @Test
    void rejectsASelectionExceedingX() {
        Permanent rod = harness.addToBattlefieldAndReturn(player1, new RodOfAbsorption());
        Opt opt = new Opt();
        Divination divination = new Divination();
        gd.addToExile(player1.getId(), opt, rod.getId());
        gd.addToExile(player1.getId(), divination, rod.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 3, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(
                player1, List.of(opt.getId(), divination.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.ImprovisationCapstoneCastChoice.class);
    }

    @Test
    void opponentSpellResolvesNormallyBeforeBeingExiled() {
        Permanent rod = harness.addToBattlefieldAndReturn(player1, new RodOfAbsorption());
        Divination divination = new Divination();
        harness.setHand(player2, List.of(divination));
        harness.setLibrary(player2, List.of(new Opt(), new Opt(), new Opt()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player2);

        harness.castAndResolveSorcery(player2, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.getCardsExiledByPermanent(rod.getId()))
                .extracting(Card::getId).containsExactly(divination.getId());
        assertThat(gd.findExiledCard(divination.getId()).ownerId()).isEqualTo(player2.getId());
    }

    @Test
    void multipleRodsLetSpellControllerChooseAtResolution() {
        harness.addToBattlefield(player1, new RodOfAbsorption());
        harness.addToBattlefield(player2, new RodOfAbsorption());
        Divination divination = new Divination();
        harness.setHand(player1, List.of(divination));
        harness.setLibrary(player1, List.of(new Opt(), new Opt(), new Opt()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
    }

    @Test
    void opponentOwnedSpellReturnsToItsOwnersGraveyardAfterFreeCast() {
        Permanent rod = harness.addToBattlefieldAndReturn(player1, new RodOfAbsorption());
        Divination divination = new Divination();
        gd.addToExile(player2.getId(), divination, rod.getId());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Opt(), new Opt(), new Opt()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 3, null);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(divination.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .extracting(Card::getId).contains(divination.getId());
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId).doesNotContain(divination.getId());
    }

    @Test
    void mayDeclineAllSpellsEvenWhenXIsZero() {
        Permanent rod = harness.addToBattlefieldAndReturn(player1, new RodOfAbsorption());
        Opt opt = new Opt();
        gd.addToExile(player1.getId(), opt, rod.getId());

        harness.activateAbility(player1, 0, 0, null);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.findExiledCard(opt.getId())).isNotNull();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Rod of Absorption");
        harness.assertNotOnBattlefield(player1, "Rod of Absorption");
    }

    @Test
    void activationOnlyOffersCardsExiledWithThatRod() {
        Permanent firstRod = harness.addToBattlefieldAndReturn(player1, new RodOfAbsorption());
        Permanent secondRod = harness.addToBattlefieldAndReturn(player1, new RodOfAbsorption());
        Opt opt = new Opt();
        Divination divination = new Divination();
        gd.addToExile(player1.getId(), opt, firstRod.getId());
        gd.addToExile(player1.getId(), divination, secondRod.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 4, null);
        harness.passBothPriorities();

        PendingInteraction.ImprovisationCapstoneCastChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.ImprovisationCapstoneCastChoice.class);
        assertThat(choice.validCardIds()).containsExactly(opt.getId());
        harness.handleMultipleCardsChosen(player1, List.of());
        assertThat(gd.findExiledCard(divination.getId())).isNotNull();
    }

    @Test
    void instantFinishesScryAndDrawBeforeBeingExiled() {
        Permanent rod = harness.addToBattlefieldAndReturn(player1, new RodOfAbsorption());
        Opt opt = new Opt();
        Divination drawn = new Divination();
        harness.setHand(player1, List.of(opt));
        harness.setLibrary(player1, List.of(drawn, new Opt()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getId).containsExactly(drawn.getId());
        assertThat(gd.getCardsExiledByPermanent(rod.getId()))
                .extracting(Card::getId).containsExactly(opt.getId());
        harness.assertNotInGraveyard(player1, "Opt");
    }
}
