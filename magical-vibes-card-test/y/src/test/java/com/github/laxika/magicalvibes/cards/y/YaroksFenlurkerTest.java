package com.github.laxika.magicalvibes.cards.y;

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

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({YaroksFenlurker.class})
class YaroksFenlurkerTest extends BaseCardTest {

    @Test
    @DisplayName("When Yarok's Fenlurker enters, each opponent exiles a card from hand")
    void etbExilesCardFromOpponentHand() {
        Card fenlurker = new YaroksFenlurker();
        Card spareCard = new YaroksFenlurker();
        Card exiledCard = new YaroksFenlurker();
        Card retainedCard = new YaroksFenlurker();
        harness.setHand(player1, new ArrayList<>(List.of(fenlurker, spareCard)));
        harness.setHand(player2, new ArrayList<>(List.of(retainedCard, exiledCard)));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ExileFromHandChoice.class);
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).playerId())
                .isEqualTo(player2.getId());

        harness.handleCardChosen(player2, 1);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(spareCard);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(retainedCard);
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(exiledCard);
    }

    @Test
    @DisplayName("Paying {2}{B} gives Yarok's Fenlurker +1/+1 until end of turn")
    void activatedAbilityBoostsSelf() {
        Permanent fenlurker = addReadyFenlurker(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(fenlurker.getPowerModifier()).isEqualTo(1);
        assertThat(fenlurker.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("The Fenlurker's temporary boost wears off at end of turn")
    void activatedAbilityWearsOffAtEndOfTurn() {
        Permanent fenlurker = addReadyFenlurker(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(fenlurker.getPowerModifier()).isZero();
        assertThat(fenlurker.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("An opponent with an empty hand has nothing to exile")
    void emptyOpponentHandDoesNotRequireAChoice() {
        Card spareCard = new YaroksFenlurker();
        harness.setHand(player1, List.of(new YaroksFenlurker(), spareCard));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(spareCard);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("The enters trigger still exiles a card after its source leaves")
    void entersTriggerIsIndependentOfItsSource() {
        Card exiledCard = new YaroksFenlurker();
        harness.setHand(player1, List.of(new YaroksFenlurker()));
        harness.setHand(player2, List.of(exiledCard));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        Permanent source = gd.playerBattlefields.get(player1.getId()).getFirst();
        gd.playerBattlefields.get(player1.getId()).remove(source);
        gd.playerGraveyards.get(player1.getId()).add(source.getCard());
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(exiledCard);
    }

    @Test
    @DisplayName("A summoning-sick Fenlurker can activate repeatedly and the boosts accumulate")
    void summoningSicknessDoesNotPreventRepeatedActivations() {
        Permanent fenlurker = addReadyFenlurker(player1);
        fenlurker.setSummoningSick(true);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(fenlurker.getPowerModifier()).isEqualTo(2);
        assertThat(fenlurker.getToughnessModifier()).isEqualTo(2);
        assertThat(fenlurker.isTapped()).isFalse();
    }

    private Permanent addReadyFenlurker(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new YaroksFenlurker());
        permanent.setSummoningSick(false);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return permanent;
    }
}
