package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DauthiVoidwalker.class, Forest.class, GrizzlyBears.class, Shock.class})
class DauthiVoidwalkerTestMarRegression extends BaseCardTest {

    @Test
    @DisplayName("Exiles an opponent's spell with a void counter instead of its graveyard")
    void exilesOpponentSpellWithVoidCounter() {
        addCreatureReady(player1, new DauthiVoidwalker());
        Shock shock = new Shock();
        harness.setHand(player2, List.of(shock));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId()))
                .noneMatch(card -> card.getId().equals(shock.getId()));
        assertThat(gd.findExiledCard(shock.getId())).isNotNull();
        assertThat(gd.exiledCardsWithVoidCounters).contains(shock.getId());
    }

    @Test
    @DisplayName("Sacrifice ability offers an opponent-owned void-counter creature")
    void sacrificesAndPlaysVoidCounterCreature() {
        addCreatureReady(player1, new DauthiVoidwalker());
        GrizzlyBears exiled = new GrizzlyBears();
        gd.addToExileWithVoidCounter(player2.getId(), exiled);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ExiledCardMayPlayChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(exiled.getId()));
        harness.castFromExile(player1, exiled.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Dauthi Voidwalker");
        assertThat(gd.findExiledCard(exiled.getId())).isNull();
    }

    @Test
    @DisplayName("Sacrifice ability can play an opponent-owned land for free")
    void playsVoidCounterLand() {
        addCreatureReady(player1, new DauthiVoidwalker());
        Forest exiled = new Forest();
        gd.addToExileWithVoidCounter(player2.getId(), exiled);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(exiled.getId()));
        harness.castFromExile(player1, exiled.getId());

        harness.assertOnBattlefield(player1, "Forest");
        assertThat(gd.landsPlayedThisTurn.get(player1.getId())).isEqualTo(1);
        assertThat(gd.findExiledCard(exiled.getId())).isNull();
    }

    @Test
    @DisplayName("Sacrifice ability ignores own cards and cards without void counters")
    void ignoresIneligibleExiledCards() {
        addCreatureReady(player1, new DauthiVoidwalker());
        Card ownCard = new GrizzlyBears();
        Card unmarkedOpponentCard = new Forest();
        gd.addToExileWithVoidCounter(player1.getId(), ownCard);
        gd.addToExile(player2.getId(), unmarkedOpponentCard);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.findExiledCard(ownCard.getId())).isNotNull();
        assertThat(gd.findExiledCard(unmarkedOpponentCard.getId())).isNotNull();
    }
}
