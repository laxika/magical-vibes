package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.d.DarkRitual;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KyloxsVoltstrider.class, GrizzlyBears.class, DarkRitual.class})
class KyloxsVoltstriderTest extends BaseCardTest {

    @Test
    void crewAnimatesVoltstrider() {
        Permanent voltstrider = addVoltstriderReady();
        addCreatureReady(player1, new GrizzlyBears());

        crewVoltstrider();

        assertThat(gqs.isCreature(gd, voltstrider)).isTrue();
        assertThat(voltstrider.isAnimatedUntilEndOfTurn()).isTrue();
    }

    @Test
    void collectingEvidenceActivatesWithoutCrewAndPaysBeforeResolution() {
        Permanent voltstrider = addVoltstriderReady();
        List<Card> evidence = List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears());
        harness.setGraveyard(player1, evidence);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleMultipleCardsChosen(player1, evidence.stream().map(Card::getId).toList());

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getCardsExiledByPermanent(voltstrider.getId())).containsExactlyInAnyOrderElementsOf(evidence);
        assertThat(gqs.isCreature(gd, voltstrider)).isFalse();
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, voltstrider)).isTrue();
    }

    @Test
    void attackingDoesNotOfferToCollectEvidence() {
        addVoltstriderReady();
        addCreatureReady(player1, new GrizzlyBears());
        List<Card> evidence = List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears());
        harness.setGraveyard(player1, evidence);
        crewVoltstrider();

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyElementsOf(evidence);
    }

    @Test
    void attackingOffersOneExiledInstantOrSorceryForItsNormalCostAndBottomsIt() {
        Permanent voltstrider = addVoltstriderReady();
        addCreatureReady(player1, new GrizzlyBears());
        DarkRitual ritual = new DarkRitual();
        GrizzlyBears exiledCreature = new GrizzlyBears();
        GrizzlyBears libraryCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(libraryCard));
        gd.addToExile(player1.getId(), ritual, voltstrider.getId());
        gd.addToExile(player1.getId(), exiledCreature, voltstrider.getId());
        harness.addMana(player1, ManaColor.BLACK, 1);
        crewVoltstrider();

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.findExiledCard(ritual.getId())).isNull();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
        assertThat(gd.stack).anyMatch(entry -> entry.getCard() == ritual);

        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard, ritual);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(ritual);
        assertThat(gd.findExiledCard(exiledCreature.getId())).isNotNull();
    }

    @Test
    void acceptingOneCastDoesNotOfferTheOtherLinkedSpell() {
        Permanent voltstrider = addVoltstriderReady();
        addCreatureReady(player1, new GrizzlyBears());
        DarkRitual first = new DarkRitual();
        DarkRitual second = new DarkRitual();
        gd.addToExile(player1.getId(), first, voltstrider.getId());
        gd.addToExile(player1.getId(), second, voltstrider.getId());
        harness.addMana(player1, ManaColor.BLACK, 2);
        crewVoltstrider();

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.getCardsExiledByPermanent(voltstrider.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).containsAnyOf(first, second);
    }

    @Test
    void decliningCastLeavesTheSpellExiled() {
        Permanent voltstrider = addVoltstriderReady();
        addCreatureReady(player1, new GrizzlyBears());
        DarkRitual ritual = new DarkRitual();
        gd.addToExile(player1.getId(), ritual, voltstrider.getId());
        harness.addMana(player1, ManaColor.BLACK, 1);
        crewVoltstrider();

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        assertThat(gd.getCardsExiledByPermanent(voltstrider.getId())).containsExactly(ritual);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
    }

    private Permanent addVoltstriderReady() {
        return addCreatureReady(player1, new KyloxsVoltstrider());
    }

    private void crewVoltstrider() {
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
    }
}
