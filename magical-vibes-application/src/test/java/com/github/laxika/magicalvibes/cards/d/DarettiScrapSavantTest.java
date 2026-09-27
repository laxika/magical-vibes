package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.FurnaceSkullbomb;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.action.DelayedGraveyardToBattlefieldUnderControl;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DarettiScrapSavant.class, FurnaceSkullbomb.class, Spellbook.class})
class DarettiScrapSavantTest extends BaseCardTest {

    @Test
    @DisplayName("+2 discards up to two cards and draws that many")
    void plusTwoRummages() {
        Permanent daretti = addReadyDaretti(5);
        Card first = new Spellbook();
        Card second = new Spellbook();
        harness.setHand(player1, List.of(first, second));
        harness.setLibrary(player1, List.of(new Spellbook(), new Spellbook()));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleXValueChosen(player1, 2);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(daretti.getCounterCount(CounterType.LOYALTY)).isEqualTo(7);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(first, second);
    }

    @Test
    @DisplayName("-2 sacrifices an artifact before returning a targeted artifact")
    void minusTwoSacrificesAndReturnsArtifact() {
        Permanent daretti = addReadyDaretti(5);
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new Spellbook());
        Card returnTarget = new Spellbook();
        harness.setGraveyard(player1, List.of(returnTarget));

        harness.activateAbility(player1, 0, 1, null, returnTarget.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, sacrifice.getId());
        harness.passBothPriorities();

        assertThat(daretti.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(returnTarget.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(sacrifice.getId()));
    }

    @Test
    @DisplayName("-2 only targets artifact cards in your graveyard")
    void minusTwoRejectsNonArtifactTarget() {
        // The target is chosen before the sacrifice cost resolves.
        addReadyDaretti(5);
        harness.addToBattlefield(player1, new Spellbook());
        Card nonArtifact = new DarettiScrapSavant();
        harness.setGraveyard(player1, List.of(nonArtifact));

        assertThatThrownBy(() -> harness.activateAbility(
                player1, 0, 1, null, nonArtifact.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("-10 emblem returns an artifact put into your graveyard at the next end step")
    void minusTenEmblemReturnsArtifactAtNextEndStep() {
        addReadyDaretti(10);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        Permanent skullbomb = harness.addToBattlefieldAndReturn(player1, new FurnaceSkullbomb());
        harness.setLibrary(player1, List.of(new Spellbook()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(skullbomb.getCard().getId()));
        assertThat(gd.getDelayedActions(DelayedGraveyardToBattlefieldUnderControl.class)).hasSize(1);

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        gs.advanceStep(gd);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(skullbomb.getCard().getId()));
    }

    private Permanent addReadyDaretti(int loyalty) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, new DarettiScrapSavant());
        permanent.setCounterCount(CounterType.LOYALTY, loyalty);
        permanent.setSummoningSick(false);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return permanent;
    }
}
