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

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, voltstrider)).isTrue();
        assertThat(voltstrider.isAnimatedUntilEndOfTurn()).isTrue();
    }

    @Test
    void attackingMayCollectEvidenceAndAnimateTheVehicle() {
        Permanent voltstrider = addVoltstriderReady();
        addCreatureReady(player1, new GrizzlyBears());
        List<Card> evidence = List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears());
        harness.setGraveyard(player1, evidence);
        crewVoltstrider();

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, evidence.stream().map(Card::getId).toList());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gqs.isCreature(gd, voltstrider)).isTrue();
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

    private Permanent addVoltstriderReady() {
        Permanent voltstrider = new Permanent(new KyloxsVoltstrider());
        voltstrider.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(voltstrider);
        return voltstrider;
    }

    private void crewVoltstrider() {
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
    }
}
