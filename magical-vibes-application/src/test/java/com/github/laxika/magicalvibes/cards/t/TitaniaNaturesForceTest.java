package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TitaniaNaturesForce.class, Forest.class, Island.class, Murder.class})
class TitaniaNaturesForceTest extends BaseCardTest {

    @Test
    @DisplayName("Can play a Forest, but not another land, from the controller's graveyard")
    void onlyForestsCanBePlayedFromGraveyard() {
        harness.addToBattlefield(player1, new TitaniaNaturesForce());
        harness.setGraveyard(player1, List.of(new Forest(), new Island()));
        harness.setHand(player1, List.of());
        prepareMainPhase(player1);

        harness.playGraveyardLand(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Forest"));
        assertThatThrownBy(() -> harness.playGraveyardLand(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable from graveyard");
    }

    @Test
    @DisplayName("A Forest entering creates a 5/3 Elemental token")
    void forestLandfallCreatesElemental() {
        harness.addToBattlefield(player1, new TitaniaNaturesForce());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(elementalTokens(player1)).hasSize(1);
        Permanent elemental = elementalTokens(player1).get(0);
        assertThat(elemental.getEffectivePower()).isEqualTo(5);
        assertThat(elemental.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("A non-Forest land does not create an Elemental token")
    void nonForestLandDoesNotTriggerLandfall() {
        harness.addToBattlefield(player1, new TitaniaNaturesForce());
        harness.setHand(player1, List.of(new Island()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(elementalTokens(player1)).isEmpty();
    }

    @Test
    @DisplayName("An Elemental's death may mill three cards")
    void elementalDeathMayMillThreeCards() {
        Permanent elemental = createElementalToken();
        Forest first = new Forest();
        Island second = new Island();
        Forest third = new Forest();
        harness.setLibrary(player1, List.of(first, second, third));
        harness.setHand(player1, List.of(new Murder()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castInstant(player1, 0, elemental.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(first, second, third);
    }

    @Test
    @DisplayName("Declining an Elemental death trigger does not mill")
    void decliningElementalDeathTriggerDoesNotMill() {
        Permanent elemental = createElementalToken();
        Forest card = new Forest();
        harness.setLibrary(player1, List.of(card));
        harness.setHand(player1, List.of(new Murder()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castInstant(player1, 0, elemental.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(card);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(card);
    }

    private Permanent createElementalToken() {
        harness.addToBattlefield(player1, new TitaniaNaturesForce());
        harness.setHand(player1, List.of(new Forest()));
        harness.playLand(player1, 0);
        harness.passBothPriorities();
        assertThat(elementalTokens(player1)).hasSize(1);
        return elementalTokens(player1).get(0);
    }

    private List<Permanent> elementalTokens(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getName().equals("Elemental"))
                .toList();
    }

    private void prepareMainPhase(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
