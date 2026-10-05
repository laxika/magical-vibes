package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MiasmaDemon.class, GrizzlyBears.class, HillGiant.class})
class MiasmaDemonTest extends BaseCardTest {

    @Test
    @DisplayName("Allows discarding any number of cards and gives that many creatures -2/-2")
    void discardsAndDebuffsUpToThatManyCreatures() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new MiasmaDemon(), new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.XValueChoice.class);

        harness.handleXValueChosen(player1, 2);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, first.getId());
        harness.handlePermanentChosen(player1, second.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Declining the optional discard leaves creatures and cards unchanged")
    void mayDeclineDiscard() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        GrizzlyBears card = new GrizzlyBears();
        harness.setHand(player1, List.of(new MiasmaDemon(), card));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(card);
    }

    @Test
    @CardUsed({MiasmaDemon.class})
    void canDiscardZeroCards() {
        MiasmaDemon retained = new MiasmaDemon();
        harness.setHand(player1, List.of(retained));
        Permanent demon = harness.enterBattlefieldAndReturn(player1, new MiasmaDemon());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleXValueChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(retained);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gqs.getEffectiveToughness(gd, demon)).isEqualTo(4);
    }

    @Test
    @CardUsed({MiasmaDemon.class})
    void canChooseFewerTargetsAndTargetItsOwnSourceWithASeparateTrigger() {
        harness.setHand(player1, List.of(new MiasmaDemon(), new MiasmaDemon()));
        Permanent demon = harness.enterBattlefieldAndReturn(player1, new MiasmaDemon());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleXValueChosen(player1, 2);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.handlePermanentChosen(player1, demon.getId());
        harness.handlePermanentChosen(player1, player1.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gqs.getEffectivePower(gd, demon)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, demon)).isEqualTo(4);

        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, demon)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, demon)).isEqualTo(2);
    }

    @Test
    @CardUsed({MiasmaDemon.class})
    void canDiscardWithoutChoosingTargets() {
        MiasmaDemon discarded = new MiasmaDemon();
        harness.setHand(player1, List.of(discarded));
        Permanent demon = harness.enterBattlefieldAndReturn(player1, new MiasmaDemon());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleXValueChosen(player1, 1);
        harness.handleCardChosen(player1, 0);
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discarded);
        assertThat(gqs.getEffectivePower(gd, demon)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, demon)).isEqualTo(4);
    }

    @Test
    @CardUsed({MiasmaDemon.class})
    void emptyHandDoesNotCreateADebuffTrigger() {
        harness.setHand(player1, List.of());
        Permanent demon = harness.enterBattlefieldAndReturn(player1, new MiasmaDemon());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gqs.getEffectivePower(gd, demon)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, demon)).isEqualTo(4);
    }
}
