package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.Rattlechains;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SupernaturalRescue.class, GrizzlyBears.class, Rattlechains.class})
class SupernaturalRescueTest extends BaseCardTest {

    @Test
    @DisplayName("Taps up to two opposing creatures when cast and boosts the enchanted creature")
    void castTriggerTapsOpposingCreaturesAndAuraBoosts() {
        Permanent enchanted = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent spirit = harness.addToBattlefieldAndReturn(player1, new Rattlechains());
        Permanent firstOpponent = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent secondOpponent = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new SupernaturalRescue()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.getGameService().passPriority(harness.getGameData(), player2);

        harness.castEnchantment(player1, 0, enchanted.getId());
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, firstOpponent.getId());
        harness.handlePermanentChosen(player1, secondOpponent.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(firstOpponent.isTapped()).isTrue();
        assertThat(secondOpponent.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, enchanted)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, enchanted)).isEqualTo(4);
        assertThat(spirit.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot be cast at instant speed without controlling a Spirit")
    void cannotCastAtInstantSpeedWithoutSpirit() {
        Permanent enchanted = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new SupernaturalRescue()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.getGameService().passPriority(harness.getGameData(), player2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, enchanted.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Cast trigger cannot target a creature controlled by its caster")
    void castTriggerCannotTargetOwnCreature() {
        Permanent enchanted = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new Rattlechains());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new SupernaturalRescue()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castEnchantment(player1, 0, enchanted.getId());

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, enchanted.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid permanent");
    }
}
