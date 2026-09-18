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
        harness.setHand(player1, List.of(new SpellweaverHelix()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castArtifact(player1, 0);
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
        harness.setHand(player1, List.of(new SpellweaverHelix()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castArtifact(player1, 0);
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
        harness.setHand(player1, List.of(new SpellweaverHelix()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castArtifact(player1, 0);
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
        harness.setGraveyard(player1, List.of(first));
        harness.setGraveyard(player2, List.of(second));
        harness.setHand(player1, List.of(new SpellweaverHelix()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

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
        harness.setHand(player2, List.of(cast));
        harness.addMana(player2, ManaColor.BLUE, 3);
        harness.castSorcery(player2, 0);

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
        harness.setHand(player2, List.of(cast));
        harness.addMana(player2, ManaColor.BLUE, 3);
        harness.castSorcery(player2, 0);

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
        harness.setHand(player2, List.of(new ThirstForKnowledge()));
        harness.addMana(player2, ManaColor.BLUE, 3);
        harness.castInstant(player2, 0);

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
}
