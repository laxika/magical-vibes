package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.ReactorRaid;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MidgarCityOfMako.class, ReactorRaid.class, FountainOfYouth.class,
        GrizzlyBears.class, Forest.class})
class MidgarCityOfMakoTest extends BaseCardTest {

    @Test
    @DisplayName("Midgar enters tapped and produces black mana")
    void entersTappedAndProducesBlackMana() {
        harness.setHand(player1, List.of(new MidgarCityOfMako()));

        harness.playLand(player1, 0);
        Permanent midgar = findPermanent(player1, "Midgar, City of Mako");
        assertThat(midgar.isTapped()).isTrue();

        midgar.untap();
        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
    }

    @Test
    @DisplayName("Reactor Raid can sacrifice an artifact or creature to draw two cards")
    void adventureSacrificesArtifactOrCreatureToDrawTwoCards() {
        MidgarCityOfMako midgar = new MidgarCityOfMako();
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(midgar));
        harness.setLibrary(player1, List.of(new Forest(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        gs.playCardWithAdventure(gd, player1, 0, 0, null, null, List.of());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class))
                .isNotNull();
        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Fountain of Youth");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);
        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(card -> card.getName())
                .containsExactlyInAnyOrder("Forest", "Grizzly Bears");
    }

    @Test
    @DisplayName("Declining Reactor Raid does not sacrifice or draw")
    void decliningAdventureDoesNothing() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        MidgarCityOfMako midgar = new MidgarCityOfMako();
        harness.setHand(player1, List.of(midgar));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        gs.playCardWithAdventure(gd, player1, 0, 0, null, null, List.of());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(artifact);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }
}
