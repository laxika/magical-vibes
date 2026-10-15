package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AvenLiberator.class})
class AvenLiberatorTest extends BaseCardTest {

    @Test
    void turningFaceUpProtectsAChosenCreatureFromAChosenColor() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new AvenLiberator());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new AvenLiberator());
        harness.setHand(player1, List.of(new AvenLiberator()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent liberator = findPermanents(player1, "Aven Liberator").getLast();
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(liberator));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(target.getId())
                .doesNotContain(opponentCreature.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNotNull();
        harness.handleListChoice(player1, "RED");

        assertThat(gqs.hasProtectionFrom(gd, target, CardColor.RED)).isTrue();
        assertThat(liberator.isFaceDown()).isFalse();
    }

    @Test
    void protectionWearsOffAtEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new AvenLiberator());
        harness.setHand(player1, List.of(new AvenLiberator()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent liberator = findPermanents(player1, "Aven Liberator").getLast();
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(liberator));
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BLUE");

        assertThat(gqs.hasProtectionFrom(gd, target, CardColor.BLUE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gqs.hasProtectionFrom(gd, target, CardColor.BLUE)).isFalse();
    }

    @Test
    void turningFaceUpCanProtectItselfFromWhite() {
        harness.setHand(player1, List.of(new AvenLiberator()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent liberator = findPermanent(player1, "Aven Liberator");
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.turnFaceUp(player1, 0);

        assertThat(liberator.isFaceDown()).isFalse();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(liberator.getId());
        harness.handlePermanentChosen(player1, liberator.getId());
        assertThat(gqs.hasProtectionFrom(gd, liberator, CardColor.WHITE)).isFalse();
        harness.passBothPriorities();
        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.options()).containsExactlyInAnyOrder("WHITE", "BLUE", "BLACK", "RED", "GREEN");
        harness.handleListChoice(player1, "WHITE");

        assertThat(gqs.hasProtectionFrom(gd, liberator, CardColor.WHITE)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, liberator, CardColor.RED)).isFalse();
    }

    @Test
    void castingFaceUpDoesNotTriggerProtection() {
        harness.setHand(player1, List.of(new AvenLiberator()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Aven Liberator");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }
}
