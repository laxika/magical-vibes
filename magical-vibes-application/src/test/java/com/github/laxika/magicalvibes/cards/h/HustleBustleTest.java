package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HustleBustle.class, GrizzlyBears.class})
class HustleBustleTest extends BaseCardTest {

    @Test
    @DisplayName("Hustle requires its target to attack or block")
    void hustleRequiresTargetToAttackOrBlock() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new HustleBustle()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castModalInstant(player1, 0, 0, List.of(target.getId()));
        harness.passBothPriorities();

        assertThat(target.isMustAttackThisTurn()).isTrue();
        assertThat(target.isMustBlockThisTurnIfAble()).isFalse();
    }

    @Test
    @DisplayName("Bustle boosts your creatures and grants them trample")
    void bustleBoostsOwnCreaturesAndGrantsTrample() {
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new HustleBustle()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castModalSorcery(player1, 0, 1, List.of());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(harness.getGameQueryService().getEffectivePower(gd, ownCreature)).isEqualTo(4);
        assertThat(harness.getGameQueryService().getEffectiveToughness(gd, ownCreature)).isEqualTo(4);
        assertThat(harness.getGameQueryService().hasKeyword(gd, ownCreature, Keyword.TRAMPLE)).isTrue();
        assertThat(harness.getGameQueryService().getEffectivePower(gd, opponentCreature)).isEqualTo(2);
        assertThat(harness.getGameQueryService().hasKeyword(gd, opponentCreature, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Bustle may turn a face-down creature face up")
    void bustleMayTurnFaceDownCreatureFaceUp() {
        Permanent faceDown = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        faceDown.setFaceDownAsCloaked();
        harness.setHand(player1, List.of(new HustleBustle()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castModalSorcery(player1, 0, 1, List.of());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(faceDown.isFaceDown()).isFalse();
    }

    @Test
    @DisplayName("Bustle lets its controller choose which face-down creature to turn up")
    void bustleChoosesFaceDownCreatureToTurnFaceUp() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        first.setFaceDownAsCloaked();
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        second.setFaceDownAsCloaked();
        harness.setHand(player1, List.of(new HustleBustle()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castModalSorcery(player1, 0, 1, List.of());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, second.getId());

        assertThat(first.isFaceDown()).isTrue();
        assertThat(second.isFaceDown()).isFalse();
    }
}
