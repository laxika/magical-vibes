package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GlacierwoodSiege.class, Divination.class, Forest.class, LightningBolt.class, SuntailHawk.class})
class GlacierwoodSiegeTest extends BaseCardTest {

    @Test
    @DisplayName("Temur mills four cards from a target player when you cast an instant")
    void temurModeMillsOnInstantCast() {
        castAndChoose("Temur");
        harness.setLibrary(player2, libraryOfTenCards());
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(6);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(4);
    }

    @Test
    @DisplayName("Temur does not trigger for a creature spell")
    void temurModeDoesNotTriggerForCreatureSpell() {
        castAndChoose("Temur");
        harness.castFromHand(player1, new SuntailHawk(), "{W}");

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Sultai allows playing a land from the controller's graveyard")
    void sultaiModeAllowsGraveyardLandPlay() {
        castAndChoose("Sultai");
        harness.setGraveyard(player1, List.of(new Forest()));
        harness.setHand(player1, List.of());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.playGraveyardLand(player1, 0);

        harness.assertOnBattlefield(player1, "Forest");
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Temur does not allow playing a land from the controller's graveyard")
    void temurModeDoesNotAllowGraveyardLandPlay() {
        castAndChoose("Temur");
        harness.setGraveyard(player1, List.of(new Forest()));
        harness.setHand(player1, List.of());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.playGraveyardLand(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable from graveyard");
    }

    @Test
    @DisplayName("Temur triggers for a sorcery and can mill its controller")
    void temurModeMillsControllerOnSorceryCast() {
        castAndChoose("Temur");
        harness.setLibrary(player1, libraryOfTenCards());
        harness.setGraveyard(player1, List.of());
        harness.setHand(player1, List.of(new Divination()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castSorcery(player1, 0);
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(6);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(4);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Temur mills all remaining cards when fewer than four remain")
    void temurModeMillsShortLibrary() {
        castAndChoose("Temur");
        harness.setLibrary(player2, List.of(new Forest(), new Forest()));
        harness.setGraveyard(player2, List.of());
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Sultai does not trigger when its controller casts an instant")
    void sultaiModeDoesNotMillOnInstantCast() {
        castAndChoose("Sultai");
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Temur does not trigger for an opponent's instant")
    void temurModeDoesNotTriggerForOpponentSpell() {
        castAndChoose("Temur");
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, player1.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Sultai does not grant an additional land play")
    void sultaiModeRespectsLandPlayLimit() {
        castAndChoose("Sultai");
        harness.setGraveyard(player1, List.of(new Forest(), new Forest()));
        harness.setHand(player1, List.of());

        harness.playGraveyardLand(player1, 0);

        assertThatThrownBy(() -> harness.playGraveyardLand(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard() instanceof Forest).hasSize(1);
    }

    private void castAndChoose(String mode) {
        harness.castFromHand(player1, new GlacierwoodSiege(), "{1}{G}{U}");
        harness.passBothPriorities();

        PendingInteraction.ColorChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.options()).containsExactly("Temur", "Sultai");
        harness.handleListChoice(player1, mode);

    }

    private List<Card> libraryOfTenCards() {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            cards.add(new SuntailHawk());
        }
        return cards;
    }
}
