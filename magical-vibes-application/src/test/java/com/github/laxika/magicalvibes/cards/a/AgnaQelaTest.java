package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
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

@CardUsed({AgnaQela.class, Forest.class, Island.class})
class AgnaQelaTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped when you control no basic land")
    void entersTappedWithoutBasicLand() {
        playAgnaQela(player1);

        assertThat(findAgnaQela(player1).isTapped()).isTrue();
    }

    @Test
    @DisplayName("Enters untapped when you control a basic land")
    void entersUntappedWithBasicLand() {
        harness.addToBattlefield(player1, new Forest());

        playAgnaQela(player1);

        assertThat(findAgnaQela(player1).isTapped()).isFalse();
    }

    @Test
    @DisplayName("A nonbasic land does not satisfy the basic-land check")
    void nonbasicLandDoesNotSatisfyCheck() {
        harness.addToBattlefield(player1, new AgnaQela());

        playAgnaQela(player1);

        assertThat(gd.playerBattlefields.get(player1.getId()).get(1).isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping for blue mana produces one blue")
    void tappingProducesBlueMana() {
        Permanent agnaQela = addReadyAgnaQela(player1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(agnaQela.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The activated ability draws a card then discards a card")
    void activatedAbilityDrawsThenDiscards() {
        addReadyAgnaQela(player1);
        Card discarded = new Forest();
        Card drawn = new Island();
        harness.setHand(player1, List.of(discarded));
        gd.playerDecks.get(player1.getId()).addFirst(drawn);
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);

        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(discarded);
    }

    private void playAgnaQela(Player player) {
        harness.setHand(player, List.of(new AgnaQela()));
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.playLand(player, 0);
    }

    private Permanent addReadyAgnaQela(Player player) {
        Permanent agnaQela = harness.addToBattlefieldAndReturn(player, new AgnaQela());
        agnaQela.setSummoningSick(false);
        return agnaQela;
    }

    private Permanent findAgnaQela(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getOriginalCard() instanceof AgnaQela)
                .findFirst()
                .orElseThrow();
    }
}
