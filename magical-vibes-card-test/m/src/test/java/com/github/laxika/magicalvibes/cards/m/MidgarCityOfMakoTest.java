package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.p.PhoenixDown;
import com.github.laxika.magicalvibes.cards.d.DwarvenCastleGuard;
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

@CardUsed({MidgarCityOfMako.class, ReactorRaid.class, PhoenixDown.class,
        DwarvenCastleGuard.class, Forest.class})
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
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new PhoenixDown());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new DwarvenCastleGuard());
        harness.setHand(player1, List.of(midgar));
        harness.setLibrary(player1, List.of(new Forest(), new DwarvenCastleGuard()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAdventure(player1, 0, List.of());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class))
                .isNotNull();
        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Phoenix Down");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);
        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(card -> card.getName())
                .containsExactlyInAnyOrder("Forest", "Dwarven Castle Guard");
    }

    @Test
    @DisplayName("Reactor Raid draws during resolution after sacrificing a creature")
    void creatureSacrificeDrawsBeforePlayersReceivePriority() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new DwarvenCastleGuard());
        harness.setHand(player1, List.of(new MidgarCityOfMako()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAdventure(player1, 0, List.of());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, creature.getId());

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        harness.assertInGraveyard(player1, "Dwarven Castle Guard");
        resolveAllTriggers();
    }

    @Test
    @DisplayName("Reactor Raid cannot sacrifice a land or an opponent's permanent")
    void noEligiblePermanentDoesNotDraw() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent opposingArtifact = harness.addToBattlefieldAndReturn(player2, new PhoenixDown());
        MidgarCityOfMako midgar = new MidgarCityOfMako();
        harness.setHand(player1, List.of(midgar));
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAdventure(player1, 0, List.of());
        harness.passBothPriorities();
        if (gd.interaction.isAwaitingInput()) {
            harness.handleMayAbilityChosen(player1, true);
        }

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(land);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opposingArtifact);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(midgar);
    }

    @Test
    @DisplayName("Declining Reactor Raid does not sacrifice or draw")
    void decliningAdventureDoesNothing() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new PhoenixDown());
        MidgarCityOfMako midgar = new MidgarCityOfMako();
        harness.setHand(player1, List.of(midgar));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAdventure(player1, 0, List.of());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(artifact);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(midgar);
        harness.castFromExile(player1, midgar.getId());

        assertThat(findPermanent(player1, "Midgar, City of Mako").isTapped()).isTrue();
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(midgar);
    }
}
