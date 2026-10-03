package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.c.CounselOfTheSoratami;
import com.github.laxika.magicalvibes.cards.d.DevilthornFox;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningAxe;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.m.MagmaticChasm;
import com.github.laxika.magicalvibes.cards.n.NaggingThoughts;
import com.github.laxika.magicalvibes.cards.o.Opt;
import com.github.laxika.magicalvibes.cards.p.PressForAnswers;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BrainInAJar.class, CounselOfTheSoratami.class, GrizzlyBears.class, LlanowarElves.class,
        Opt.class, DevilthornFox.class, LightningAxe.class, MagmaticChasm.class,
        NaggingThoughts.class, PressForAnswers.class})
class BrainInAJarTest extends BaseCardTest {

    @Test
    @DisplayName("Adds a charge counter and offers a matching instant or sorcery for free")
    void addsCounterAndCastsMatchingSpell() {
        Permanent jar = harness.addToBattlefieldAndReturn(player1, new BrainInAJar());
        Opt opt = new Opt();
        harness.setHand(player1, List.of(opt, new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(jar.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
        PendingInteraction.MayAbilityChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.description()).contains("Opt");

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getId()).isEqualTo(opt.getId());
        assertThat(gd.playerHands.get(player1.getId())).noneMatch(card -> card.getId().equals(opt.getId()));
    }

    @Test
    @DisplayName("Offers no spell whose mana value differs from the charge-counter count")
    void requiresExactManaValue() {
        harness.addToBattlefield(player1, new BrainInAJar());
        CounselOfTheSoratami counsel = new CounselOfTheSoratami();
        harness.setHand(player1, List.of(counsel));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(counsel);
    }

    @Test
    @DisplayName("Removes X charge counters and scries X")
    void removesCountersAndScriesX() {
        Permanent jar = harness.addToBattlefieldAndReturn(player1, new BrainInAJar());
        jar.setCounterCount(CounterType.CHARGE, 2);
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new LlanowarElves()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 1, 2, null);
        assertThat(jar.getCounterCount(CounterType.CHARGE)).isZero();
        assertThat(jar.isTapped()).isTrue();
        harness.passBothPriorities();

        assertThat(jar.getCounterCount(CounterType.CHARGE)).isZero();
        PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry).isNotNull();
        assertThat(scry.cards()).hasSize(2);
    }

    @Test
    void decliningDoesNotRevealTheCardInHand() {
        Permanent jar = harness.addToBattlefieldAndReturn(player1, new BrainInAJar());
        jar.setCounterCount(CounterType.CHARGE, 1);
        NaggingThoughts spell = new NaggingThoughts();
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(jar.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(spell);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog).noneMatch(entry -> entry.plainText().contains("Nagging Thoughts"));
    }

    @Test
    void canCastOnlyOneMatchingSpell() {
        Permanent jar = harness.addToBattlefieldAndReturn(player1, new BrainInAJar());
        jar.setCounterCount(CounterType.CHARGE, 1);
        MagmaticChasm first = new MagmaticChasm();
        NaggingThoughts second = new NaggingThoughts();
        harness.setHand(player1, List.of(first, second, new DevilthornFox()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getId()).isEqualTo(first.getId());
        harness.assertInHand(player1, "Nagging Thoughts");
        harness.assertInHand(player1, "Devilthorn Fox");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    void usesCountersAtDepartureWhenSourceLeavesBeforeResolution() {
        Permanent jar = harness.addToBattlefieldAndReturn(player1, new BrainInAJar());
        jar.setCounterCount(CounterType.CHARGE, 1);
        MagmaticChasm spell = new MagmaticChasm();
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 0, null, null);
        jar.setCounterCount(CounterType.CHARGE, 2);
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, jar));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getId()).isEqualTo(spell.getId());
    }

    @Test
    void spellWithoutLegalTargetsStaysInHand() {
        Permanent jar = harness.addToBattlefieldAndReturn(player1, new BrainInAJar());
        jar.setCounterCount(CounterType.CHARGE, 1);
        PressForAnswers spell = new PressForAnswers();
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        if (gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class) != null) {
            harness.handleMayAbilityChosen(player1, true);
        }

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(spell);
        harness.assertNotInGraveyard(player1, "Press for Answers");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotCastLightningAxeWithoutPayingItsAdditionalCost() {
        harness.addToBattlefield(player1, new BrainInAJar());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DevilthornFox());
        LightningAxe spell = new LightningAxe();
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        if (gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class) != null) {
            try {
                harness.handleMayAbilityChosen(player1, true);
                if (gd.interaction.isAwaitingInput()) {
                    harness.handlePermanentChosen(player1, target.getId());
                }
            } catch (IllegalArgumentException | IllegalStateException ignored) {
                // Rejecting the illegal cast must preserve the card in hand.
            }
        }

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(spell);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void zeroCountersCanBeRemovedWithoutScrying() {
        Permanent jar = harness.addToBattlefieldAndReturn(player1, new BrainInAJar());
        harness.setLibrary(player1, List.of(new DevilthornFox()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 1, 0, null);
        harness.passBothPriorities();

        assertThat(jar.isTapped()).isTrue();
        assertThat(jar.getCounterCount(CounterType.CHARGE)).isZero();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    void cannotRemoveMoreCountersThanPresent() {
        Permanent jar = harness.addToBattlefieldAndReturn(player1, new BrainInAJar());
        jar.setCounterCount(CounterType.CHARGE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, 2, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(jar.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
        assertThat(jar.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }
}
