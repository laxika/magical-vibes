package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.k.KrarkClanIronworks;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.EmblemArtifactGraveyardReturnTriggerEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DarettiScrapSavant.class, GrizzlyBears.class, Forest.class,
        LeoninScimitar.class, KrarkClanIronworks.class})
class DarettiScrapSavantTest extends BaseCardTest {

    @Test
    @DisplayName("+2 discards up to two cards, then draws that many")
    void plusTwoDiscardsAndDrawsThatMany() {
        Permanent daretti = addReadyDaretti(player1, 3);
        harness.setHand(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleXValueChosen(player1, 2);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(daretti.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("-2 sacrifices an artifact and returns a target artifact card")
    void minusTwoSacrificesAndReturnsArtifact() {
        Permanent daretti = addReadyDaretti(player1, 2);
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());
        Card returnTarget = new LeoninScimitar();
        harness.setGraveyard(player1, List.of(returnTarget));

        harness.activateAbilityWithGraveyardTargets(player1, 0, 1, List.of(returnTarget.getId()));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, sacrifice.getId());
        harness.passBothPriorities();

        assertThat(daretti.getCounterCount(CounterType.LOYALTY)).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(returnTarget.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(permanent -> permanent.getId().equals(sacrifice.getId()));
    }

    @Test
    @DisplayName("-10 creates an emblem that returns sacrificed artifacts at the next end step")
    void ultimateReturnsArtifactAtNextEndStep() {
        Permanent ironworks = harness.addToBattlefieldAndReturn(player1, new KrarkClanIronworks());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());
        addReadyDaretti(player1, 10);

        harness.activateAbility(player1, 2, 2, null, null);
        harness.passBothPriorities();

        assertThat(gd.emblems).hasSize(1);
        assertThat(gd.emblems.getFirst().staticEffects()).singleElement()
                .isInstanceOf(EmblemArtifactGraveyardReturnTriggerEffect.class);

        int ironworksIndex = gd.playerBattlefields.get(player1.getId()).indexOf(ironworks);
        harness.activateAbility(player1, ironworksIndex, 0, null, null);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(Card::getId).contains(artifact.getCard().getId());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(artifact.getCard().getId()));
    }

    private Permanent addReadyDaretti(Player player, int loyalty) {
        Permanent daretti = harness.addToBattlefieldAndReturn(player, new DarettiScrapSavant());
        daretti.setCounterCount(CounterType.LOYALTY, loyalty);
        daretti.setSummoningSick(false);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return daretti;
    }
}
