package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OtherworldlyOutburst.class, GrizzlyBears.class, Shock.class})
class OtherworldlyOutburstTest extends BaseCardTest {

    @Test
    @DisplayName("Gives the target creature +1/+0 until end of turn")
    void givesTargetCreaturePowerBoostUntilEndOfTurn() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new OtherworldlyOutburst()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(3);
        assertThat(target.getEffectiveToughness()).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(2);
    }

    @Test
    @DisplayName("Creates a 3/2 colorless Eldrazi Horror when the target dies this turn")
    void createsTokenWhenTargetDiesThisTurn() {
        addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new OtherworldlyOutburst(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, harness.getPermanentId(player2, "Grizzly Bears"));
        harness.passBothPriorities();

        harness.castInstant(player1, 0, harness.getPermanentId(player2, "Grizzly Bears"));
        resolveAllTriggers();

        Permanent token = findPermanent(player1, "Eldrazi Horror");
        assertThat(token.getEffectivePower()).isEqualTo(3);
        assertThat(token.getEffectiveToughness()).isEqualTo(2);
        assertThat(token.getCard().getColor()).isNull();
        assertThat(token.getCard().getSubtypes())
                .containsExactlyInAnyOrder(CardSubtype.ELDRAZI, CardSubtype.HORROR);
    }

    @Test
    @DisplayName("Does not create a token if the target survives this turn")
    void doesNotCreateTokenWhenTargetSurvives() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new OtherworldlyOutburst()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Eldrazi Horror")).isEmpty();
        assertThat(target.getEffectivePower()).isEqualTo(3);
    }

    @Test
    @DisplayName("Cannot target a player")
    void cannotTargetPlayer() {
        harness.setHand(player1, List.of(new OtherworldlyOutburst()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Each resolved Outburst creates its own token when the creature dies")
    void multipleOutburstsCreateMultipleTokens() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new OtherworldlyOutburst(), new OtherworldlyOutburst(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
        assertThat(target.getEffectivePower()).isEqualTo(4);

        harness.castInstant(player1, 0, target.getId());
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Eldrazi Horror")).isEqualTo(2);
        assertThat(findPermanents(player2, "Eldrazi Horror")).isEmpty();
    }

    @Test
    @DisplayName("Does not create a token if the creature dies before Outburst resolves")
    void targetDiesBeforeResolution() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new OtherworldlyOutburst(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, target.getId());
        harness.castInstant(player1, 0, target.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(findPermanents(player1, "Eldrazi Horror")).isEmpty();
        assertThat(findPermanents(player2, "Eldrazi Horror")).isEmpty();
    }

    @Test
    @DisplayName("The delayed trigger expires when the turn ends")
    void doesNotCreateTokenOnLaterTurn() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new OtherworldlyOutburst()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, target.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(findPermanents(player1, "Eldrazi Horror")).isEmpty();
        assertThat(findPermanents(player2, "Eldrazi Horror")).isEmpty();
    }

    @Test
    @DisplayName("Creates a token when your own targeted creature dies")
    void createsTokenWhenOwnCreatureDies() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new OtherworldlyOutburst()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.withAutoStop(gd.currentStep, () -> {
            harness.castInstant(player2, 0, target.getId());
            harness.passBothPriorities();
        });

        assertThat(findPermanents(player1, "Eldrazi Horror")).isEmpty();
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(countPermanents(player1, "Eldrazi Horror")).isEqualTo(1);
        assertThat(findPermanents(player2, "Eldrazi Horror")).isEmpty();
    }

    @Test
    @DisplayName("Another creature dying does not trigger Outburst")
    void doesNotTriggerForAnotherCreature() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        Permanent other = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new OtherworldlyOutburst(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.castInstant(player1, 0, other.getId());
        resolveAllTriggers();

        assertThat(findPermanents(player2, "Grizzly Bears")).containsExactly(target);
        assertThat(findPermanents(player1, "Eldrazi Horror")).isEmpty();
        assertThat(findPermanents(player2, "Eldrazi Horror")).isEmpty();
    }
}
