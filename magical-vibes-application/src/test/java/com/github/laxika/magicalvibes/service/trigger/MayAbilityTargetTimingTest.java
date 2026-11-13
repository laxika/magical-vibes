package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.testutil.TestStackDoor;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * "You may [do something to] target ..." of a triggered ability: the target is chosen as the ability is put on the
 * stack (CR 603.3d), the "you may" is decided as it resolves (CR 603.5), and the target is only checked again then
 * (CR 608.2b). Answering the question never chooses a target.
 */
@CardUsed({GrizzlyBears.class})
class MayAbilityTargetTimingTest extends BaseCardTest {

    private static final String QUESTION = "Have target creature get +3/+3 until end of turn?";

    private static Card raider() {
        Card card = new Card();
        card.setName("Fixture Raider");
        card.setType(CardType.CREATURE);
        card.setManaCost("{1}");
        card.setPower(2);
        card.setToughness(2);
        card.addTrigger(Triggers.attacks(), new MayEffect(new BoostTargetCreatureEffect(3, 3), QUESTION));
        return card;
    }

    private List<StackEntry> abilitiesOnStack() {
        return gd.stack.stream().filter(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY).toList();
    }

    @Test
    @DisplayName("The target is chosen as the ability goes on the stack, before its \"you may\" is asked")
    void targetIsChosenAsTheAbilityGoesOnTheStack() {
        addCreatureReady(player1, raider());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(0));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();
        assertThat(abilitiesOnStack()).isEmpty();
        harness.handlePermanentChosen(player1, bears.getId());

        assertThat(abilitiesOnStack()).hasSize(1);
        assertThat(abilitiesOnStack().getFirst().getTargetId()).isEqualTo(bears.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    @DisplayName("The \"you may\" is asked as the ability resolves, and accepting it chooses nothing")
    void acceptingOnResolutionChoosesNothing() {
        addCreatureReady(player1, raider());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, bears.getId());

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(bears.getPowerModifier()).isEqualTo(3);
        assertThat(abilitiesOnStack()).isEmpty();
    }

    @Test
    @DisplayName("An ability whose target left before it resolves asks nothing and does nothing")
    void illegalTargetOnResolutionAsksNothing() {
        addCreatureReady(player1, raider());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent otherBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, bears.getId());
        gd.playerBattlefields.get(player2.getId()).remove(bears);

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(otherBears.getPowerModifier()).isZero();
        assertThat(abilitiesOnStack()).isEmpty();
    }

    @Test
    @DisplayName("An ability on the stack with no target is not given one once its \"you may\" is accepted")
    void acceptedAbilityWithoutATargetIsGivenNone() {
        Permanent source = addCreatureReady(player1, raider());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        List<CardEffect> effects = List.of(new MayEffect(new BoostTargetCreatureEffect(3, 3), QUESTION));
        StackEntry untargeted = TestStackDoor.triggered(source.getCard(), player1.getId(),
                "Fixture Raider's ability", effects);
        gd.stack.add(untargeted);

        harness.passBothPriorities();
        if (gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class) != null) {
            harness.handleMayAbilityChosen(player1, true);
        }

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(bears.getPowerModifier()).isZero();
        assertThat(source.getPowerModifier()).isZero();
        assertThat(abilitiesOnStack()).isEmpty();
    }
}
