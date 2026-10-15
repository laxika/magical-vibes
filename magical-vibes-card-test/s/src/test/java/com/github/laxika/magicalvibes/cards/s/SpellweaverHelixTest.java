package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.Annul;
import com.github.laxika.magicalvibes.cards.f.Fabricate;
import com.github.laxika.magicalvibes.cards.o.Override;
import com.github.laxika.magicalvibes.cards.t.ThirstForKnowledge;
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

@CardUsed({
        SpellweaverHelix.class, Fabricate.class, SylvanScrying.class,
        Annul.class, ThirstForKnowledge.class, Override.class
})
class SpellweaverHelixTest extends BaseCardTest {

    @Test
    @DisplayName("ETB exiles exactly two target sorceries from one graveyard")
    void etbExilesTwoSorceriesFromOneGraveyard() {
        Fabricate first = new Fabricate();
        SylvanScrying second = new SylvanScrying();
        Annul instant = new Annul();
        harness.setGraveyard(player2, List.of(first, second, instant));
        harness.castFromHand(player1, new SpellweaverHelix(), "{3}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.minCount()).isEqualTo(2);
        assertThat(choice.maxCount()).isEqualTo(2);
        assertThat(choice.validCardIds()).containsExactly(first.getId(), second.getId());

        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        Permanent helix = findPermanent(player1, "Spellweaver Helix");
        assertThat(gd.getCardsExiledByPermanent(helix.getId()))
                .extracting(Card::getId)
                .containsExactly(first.getId(), second.getId());
        harness.assertInGraveyard(player2, "Annul");
    }

    @Test
    @DisplayName("Declining the ETB may ability leaves the selected sorceries in the graveyard")
    void decliningEtbMayAbilityLeavesCardsInGraveyard() {
        Fabricate first = new Fabricate();
        SylvanScrying second = new SylvanScrying();
        Annul instant = new Annul();
        harness.setGraveyard(player2, List.of(first, second, instant));
        harness.castFromHand(player1, new SpellweaverHelix(), "{3}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        Permanent helix = findPermanent(player1, "Spellweaver Helix");
        assertThat(gd.getCardsExiledByPermanent(helix.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Fabricate");
        harness.assertInGraveyard(player2, "Sylvan Scrying");
        harness.assertInGraveyard(player2, "Annul");
    }

    @Test
    @DisplayName("ETB cannot choose only one sorcery when two are required")
    void etbCannotChooseOnlyOneSorcery() {
        Fabricate onlySorcery = new Fabricate();
        harness.setGraveyard(player2, List.of(onlySorcery));
        harness.castFromHand(player1, new SpellweaverHelix(), "{3}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(onlySorcery.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("ETB cannot combine sorceries from different graveyards")
    void etbCannotCombineDifferentGraveyards() {
        Fabricate first = new Fabricate();
        SylvanScrying second = new SylvanScrying();
        harness.setGraveyard(player1, List.of(first, new SylvanScrying()));
        harness.setGraveyard(player2, List.of(second));
        harness.castFromHand(player1, new SpellweaverHelix(), "{3}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(
                player1, List.of(first.getId(), second.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Triggers for another player's matching cast and copies the other exiled card")
    void triggersForAnyPlayersMatchingCast() {
        SpellweaverHelix helixCard = new SpellweaverHelix();
        Permanent helix = harness.addToBattlefieldAndReturn(player1, helixCard);

        Fabricate imprinted = new Fabricate();
        SylvanScrying other = new SylvanScrying();
        gd.addToExile(player1.getId(), imprinted, helix.getId());
        gd.addToExile(player1.getId(), other, helix.getId());

        harness.forceActivePlayer(player2);
        Fabricate cast = new Fabricate();
        harness.castFromHand(player2, cast, "{2}{U}");

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.stack).anyMatch(entry ->
                entry.getCard().getName().equals("Sylvan Scrying") && entry.isCopy());
        assertThat(gd.getCardsExiledByPermanent(helix.getId()))
                .extracting(Card::getId)
                .containsExactly(imprinted.getId(), other.getId());
    }

    @Test
    @DisplayName("Declining to cast the copied card leaves the imprint intact")
    void decliningToCastCopyLeavesImprintIntact() {
        SpellweaverHelix helixCard = new SpellweaverHelix();
        Permanent helix = harness.addToBattlefieldAndReturn(player1, helixCard);
        Fabricate imprinted = new Fabricate();
        SylvanScrying other = new SylvanScrying();
        gd.addToExile(player1.getId(), imprinted, helix.getId());
        gd.addToExile(player1.getId(), other, helix.getId());

        Fabricate cast = new Fabricate();
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, cast, "{2}{U}");

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.stack).noneMatch(entry -> entry.isCopy());
        assertThat(gd.getCardsExiledByPermanent(helix.getId()))
                .extracting(Card::getId)
                .containsExactly(imprinted.getId(), other.getId());
    }

    @Test
    @DisplayName("A nonmatching spell does not trigger the Helix")
    void nonmatchingSpellDoesNotTrigger() {
        SpellweaverHelix helixCard = new SpellweaverHelix();
        Permanent helix = harness.addToBattlefieldAndReturn(player1, helixCard);
        Fabricate imprinted = new Fabricate();
        SylvanScrying other = new SylvanScrying();
        gd.addToExile(player1.getId(), imprinted, helix.getId());
        gd.addToExile(player1.getId(), other, helix.getId());

        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new ThirstForKnowledge(), "{2}{U}");

        assertThat(gd.stack).noneMatch(entry -> entry.getCard().getName().equals("Spellweaver Helix"));
    }

    @Test
    @DisplayName("Copies the other card if the matching spell is countered before the trigger resolves")
    void copiesOtherCardWhenMatchingSpellIsCounteredBeforeTriggerResolves() {
        SpellweaverHelix helixCard = new SpellweaverHelix();
        Permanent helix = harness.addToBattlefieldAndReturn(player1, helixCard);
        SylvanScrying imprintedMatching = new SylvanScrying();
        Fabricate other = new Fabricate();
        gd.addToExile(player1.getId(), imprintedMatching, helix.getId());
        gd.addToExile(player1.getId(), other, helix.getId());

        SylvanScrying cast = new SylvanScrying();
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, cast, "{1}{G}");
        harness.passPriority(player2);
        harness.setHand(player1, List.of(new Override()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castInstant(player1, 0, cast.getId());

        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.stack)
                .extracting(entry -> entry.getCard().getName() + ":" + entry.isCopy())
                .contains("Fabricate:true");
    }

    @Test
    @DisplayName("Declining to copy creates no spell and leaves both imprinted cards exiled")
    void decliningToCopyLeavesBothCardsExiled() {
        Permanent helix = harness.addToBattlefieldAndReturn(player1, new SpellweaverHelix());
        Fabricate matching = new Fabricate();
        SylvanScrying other = new SylvanScrying();
        gd.addToExile(player1.getId(), matching, helix.getId());
        gd.addToExile(player1.getId(), other, helix.getId());

        harness.castFromHand(player1, new Fabricate(), "{2}{U}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.stack).noneMatch(entry -> entry.isCopy());
        assertThat(gd.getCardsExiledByPermanent(helix.getId()))
                .extracting(Card::getId).containsExactly(matching.getId(), other.getId());
    }

    @Test
    @DisplayName("Two imprinted cards with the same name produce one copy without retriggering")
    void sameNamedImprintedCardsProduceOneCopy() {
        Permanent helix = harness.addToBattlefieldAndReturn(player1, new SpellweaverHelix());
        gd.addToExile(player1.getId(), new Fabricate(), helix.getId());
        gd.addToExile(player1.getId(), new Fabricate(), helix.getId());
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new Fabricate(), "{2}{U}");

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.stack.stream().filter(entry -> entry.isCopy()).toList())
                .singleElement().satisfies(entry -> {
                    assertThat(entry.getCard().getName()).isEqualTo("Fabricate");
                    assertThat(entry.getControllerId()).isEqualTo(player1.getId());
                });
        assertThat(gd.stack).noneMatch(entry -> entry.getCard().getName().equals("Spellweaver Helix"));
    }

    @Test
    @DisplayName("A single imprinted card still causes a matching cast to trigger, but cannot produce a copy")
    void singleImprintedCardStillTriggersButCannotCopy() {
        Permanent helix = harness.addToBattlefieldAndReturn(player1, new SpellweaverHelix());
        gd.addToExile(player1.getId(), new Fabricate(), helix.getId());
        harness.castFromHand(player1, new Fabricate(), "{2}{U}");

        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getName().equals("Spellweaver Helix"));
        harness.passBothPriorities();
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice) {
            harness.handleMayAbilityChosen(player1, true);
        }
        assertThat(gd.stack).noneMatch(entry -> entry.isCopy());
    }

    @Test
    @DisplayName("No copy is made if the other imprinted card leaves exile before resolution")
    void otherImprintedCardLeavingExilePreventsCopy() {
        Permanent helix = harness.addToBattlefieldAndReturn(player1, new SpellweaverHelix());
        Fabricate matching = new Fabricate();
        SylvanScrying other = new SylvanScrying();
        gd.addToExile(player1.getId(), matching, helix.getId());
        gd.addToExile(player1.getId(), other, helix.getId());
        harness.castFromHand(player1, new Fabricate(), "{2}{U}");
        gd.removeFromExile(other.getId());
        gd.playerHands.get(player1.getId()).add(other);

        harness.passBothPriorities();
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice) {
            harness.handleMayAbilityChosen(player1, true);
        }

        assertThat(gd.stack).noneMatch(entry -> entry.isCopy());
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.getCardsExiledByPermanent(helix.getId()))
                .extracting(Card::getId).containsExactly(matching.getId());
    }

    @Test
    @DisplayName("Matching cards from a second imprint trigger also trigger the Helix")
    void matchingCardBeyondFirstImprintedPairTriggers() {
        Permanent helix = harness.addToBattlefieldAndReturn(player1, new SpellweaverHelix());
        gd.addToExile(player1.getId(), new Fabricate(), helix.getId());
        gd.addToExile(player1.getId(), new Fabricate(), helix.getId());
        gd.addToExile(player1.getId(), new SylvanScrying(), helix.getId());
        gd.addToExile(player1.getId(), new SylvanScrying(), helix.getId());

        harness.castFromHand(player1, new SylvanScrying(), "{1}{G}");

        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getName().equals("Spellweaver Helix"));
    }

    @Test
    @DisplayName("A copied imprint trigger allows copies of every other linked card")
    void multipleImprintedPairsAllowCopiesOfAllOtherCards() {
        Permanent helix = harness.addToBattlefieldAndReturn(player1, new SpellweaverHelix());
        gd.addToExile(player1.getId(), new Fabricate(), helix.getId());
        gd.addToExile(player1.getId(), new Fabricate(), helix.getId());
        gd.addToExile(player1.getId(), new SylvanScrying(), helix.getId());
        gd.addToExile(player1.getId(), new SylvanScrying(), helix.getId());
        harness.castFromHand(player1, new Fabricate(), "{2}{U}");

        harness.passBothPriorities();
        for (int choices = 0; choices < 8
                && gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice; choices++) {
            harness.handleMayAbilityChosen(player1, true);
        }

        assertThat(gd.stack.stream().filter(entry -> entry.isCopy()).toList())
                .extracting(entry -> entry.getCard().getName())
                .containsExactlyInAnyOrder("Fabricate", "Sylvan Scrying", "Sylvan Scrying");
        assertThat(gd.getCardsExiledByPermanent(helix.getId())).hasSize(4);
    }

    @Test
    @DisplayName("Imprint exiles its remaining legal target when the other target leaves the graveyard")
    void imprintExilesRemainingLegalTarget() {
        Fabricate first = new Fabricate();
        SylvanScrying second = new SylvanScrying();
        harness.setGraveyard(player2, List.of(first, second));
        harness.castFromHand(player1, new SpellweaverHelix(), "{3}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId()));
        harness.setGraveyard(player2, List.of(second));
        harness.setHand(player2, List.of(first));

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        Permanent helix = findPermanent(player1, "Spellweaver Helix");
        assertThat(gd.getCardsExiledByPermanent(helix.getId()))
                .extracting(Card::getId).containsExactly(second.getId());
        harness.assertNotInGraveyard(player2, "Sylvan Scrying");
    }

    @Test
    @DisplayName("Removing Helix after it triggers does not prevent copying the other card")
    void copyAbilitySurvivesSourceLeavingBattlefield() {
        Permanent helix = harness.addToBattlefieldAndReturn(player1, new SpellweaverHelix());
        gd.addToExile(player1.getId(), new Fabricate(), helix.getId());
        gd.addToExile(player1.getId(), new SylvanScrying(), helix.getId());
        harness.castFromHand(player1, new Fabricate(), "{2}{U}");
        gd.playerBattlefields.get(player1.getId()).remove(helix);
        gd.playerGraveyards.get(player1.getId()).add(helix.getCard());

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.stack.stream().filter(entry -> entry.isCopy()).toList())
                .singleElement().satisfies(entry ->
                        assertThat(entry.getCard().getName()).isEqualTo("Sylvan Scrying"));
    }
}
