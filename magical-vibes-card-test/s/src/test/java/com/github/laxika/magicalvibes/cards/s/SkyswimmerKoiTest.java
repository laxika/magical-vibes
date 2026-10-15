package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.e.EncroachingMycosynth;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.n.NetworkTerminal;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SkyswimmerKoi.class, Forest.class, Ornithopter.class})
class SkyswimmerKoiTest extends BaseCardTest {

    @Test
    void artifactEnteringTriggersOptionalLoot() {
        harness.addToBattlefield(player1, new SkyswimmerKoi());
        harness.setLibrary(player1, List.of(new Forest()));

        harness.castFromHand(player1, new Ornithopter(), "{0}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Forest");
    }

    @Test
    void lootCanBeDeclined() {
        harness.addToBattlefield(player1, new SkyswimmerKoi());
        harness.setLibrary(player1, List.of(new Forest()));

        harness.castFromHand(player1, new Ornithopter(), "{0}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Forest");
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    void opponentArtifactDoesNotTrigger() {
        harness.addToBattlefield(player1, new SkyswimmerKoi());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castFromHand(player2, new Ornithopter(), "{0}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    @CardUsed(NetworkTerminal.class)
    void noncreatureArtifactEnteringWithoutBeingCastTriggersLoot() {
        harness.addToBattlefield(player1, new SkyswimmerKoi());
        harness.setHand(player1, List.of(new SkyswimmerKoi()));
        harness.setLibrary(player1, List.of(new Forest()));

        harness.enterBattlefieldAndReturn(player1, new NetworkTerminal());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getName).containsExactly("Skyswimmer Koi", "Forest");
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getName).containsExactly("Forest");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getName).containsExactly("Skyswimmer Koi");
    }

    @Test
    void nonartifactCreatureEnteringDoesNotTrigger() {
        harness.addToBattlefield(player1, new SkyswimmerKoi());

        harness.enterBattlefieldAndReturn(player1, new SkyswimmerKoi());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @CardUsed(NetworkTerminal.class)
    void eachArtifactEntryCanBeAcceptedIndependently() {
        harness.addToBattlefield(player1, new SkyswimmerKoi());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));

        harness.enterBattlefieldAndReturn(player1, new NetworkTerminal());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.enterBattlefieldAndReturn(player1, new NetworkTerminal());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getName).containsExactly("Forest");
    }

    @Test
    @CardUsed(EncroachingMycosynth.class)
    void koiEnteringAsArtifactTriggersItsOwnLootAbility() {
        harness.addToBattlefield(player1, new EncroachingMycosynth());
        harness.setLibrary(player1, List.of(new Forest()));

        harness.castFromHand(player1, new SkyswimmerKoi(), "{3}{U}");
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getName).containsExactly("Forest");
    }
}
