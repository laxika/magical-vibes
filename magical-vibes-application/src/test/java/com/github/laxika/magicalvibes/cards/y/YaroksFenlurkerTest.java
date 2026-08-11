package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class YaroksFenlurkerTest extends BaseCardTest {

    @Test
    @DisplayName("When Yarok's Fenlurker enters, each opponent exiles a card from hand")
    void etbExilesCardFromOpponentHand() {
        Card fenlurker = new YaroksFenlurker();
        Card spareCard = new GrizzlyBears();
        Card exiledCard = new Forest();
        harness.setHand(player1, new ArrayList<>(List.of(fenlurker, spareCard)));
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), exiledCard)));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ExileFromHandChoice.class);
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).playerId())
                .isEqualTo(player2.getId());

        harness.handleCardChosen(player2, 1);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(spareCard);
        assertThat(gd.playerHands.get(player2.getId())).singleElement().isInstanceOf(GrizzlyBears.class);
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

    private Permanent addReadyFenlurker(Player player) {
        Permanent permanent = new Permanent(new YaroksFenlurker());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return permanent;
    }
}
