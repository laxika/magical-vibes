package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.a.ArgothianSprite;
import com.github.laxika.magicalvibes.cards.e.EnergyRefractor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HurkylsFinalMeditation.class, ArgothianSprite.class, Forest.class, EnergyRefractor.class})
class HurkylsFinalMeditationTest extends BaseCardTest {

    @Test
    @DisplayName("Returns all nonland permanents to their owners' hands, then ends the turn")
    void returnsNonlandsAndEndsTurn() {
        ArgothianSprite player1Bears = new ArgothianSprite();
        Forest player1Forest = new Forest();
        ArgothianSprite player2Bears = new ArgothianSprite();
        Forest player2Forest = new Forest();
        HurkylsFinalMeditation spell = new HurkylsFinalMeditation();
        harness.addToBattlefield(player1, player1Bears);
        harness.addToBattlefield(player1, player1Forest);
        harness.addToBattlefield(player2, player2Bears);
        harness.addToBattlefield(player2, player2Forest);
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).contains(player1Bears);
        assertThat(gd.playerHands.get(player2.getId())).contains(player2Bears);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == player1Forest);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getCard() == player2Forest);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.activePlayerId).isNotEqualTo(player1.getId());
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(spell);
    }

    @Test
    @DisplayName("Costs {3} more to cast during another player's turn")
    void costsMoreOnAnotherPlayersTurn() {
        harness.forceActivePlayer(player2);
        harness.setHand(player1, List.of(new HurkylsFinalMeditation()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castInstant(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    void castsForTenManaOnOpponentsTurnAndExilesOtherSpell() {
        ArgothianSprite creatureSpell = new ArgothianSprite();
        HurkylsFinalMeditation meditation = new HurkylsFinalMeditation();
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(creatureSpell));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.setHand(player1, List.of(meditation));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.castCreature(player2, 0);
        harness.passPriority(player2);
        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(creatureSpell);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(meditation);
        assertThat(gd.activePlayerId).isEqualTo(player1.getId());
        harness.assertNotOnBattlefield(player2, "Argothian Sprite");
        harness.assertNotInGraveyard(player2, "Argothian Sprite");
    }

    @Test
    void returnsArtifactsAndStolenCreaturesToTheirOwners() {
        ArgothianSprite stolenCreature = new ArgothianSprite();
        EnergyRefractor artifact = new EnergyRefractor();
        var permanent = harness.addToBattlefieldAndReturn(player2, stolenCreature);
        gd.stolenCreatures.put(permanent.getId(), player1.getId());
        harness.addToBattlefield(player2, artifact);
        harness.setHand(player1, List.of(new HurkylsFinalMeditation()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).contains(stolenCreature);
        assertThat(gd.playerHands.get(player2.getId())).contains(artifact).doesNotContain(stolenCreature);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    void returnedPermanentsCountTowardActivePlayersCleanupDiscard() {
        harness.setHand(player1, List.of(new HurkylsFinalMeditation()));
        harness.setHand(player2, List.of());
        for (int i = 0; i < 8; i++) {
            harness.addToBattlefield(player1, new ArgothianSprite());
            harness.addToBattlefield(player2, new ArgothianSprite());
        }
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castAndResolveInstant(player1, 0);

        var choice = gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.remainingCount()).isEqualTo(1);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(8);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(7);
        harness.assertInGraveyard(player1, "Argothian Sprite");
    }
}
