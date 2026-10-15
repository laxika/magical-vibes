package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LunarForce;
import com.github.laxika.magicalvibes.cards.t.TakeInventory;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NiblisOfFrost.class, GrizzlyBears.class, Shock.class, LunarForce.class, TakeInventory.class})
class NiblisOfFrostTest extends BaseCardTest {

    @Test
    @DisplayName("Casting an instant triggers prowess and taps and locks an opponent creature")
    void instantSpellTriggersProwessAndTapLock() {
        Permanent niblis = harness.addToBattlefieldAndReturn(player1, new NiblisOfFrost());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();
        harness.handlePermanentChosen(player1, bears.getId());

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, niblis)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, niblis)).isEqualTo(4);
        assertThat(bears.isTapped()).isTrue();
        assertThat(bears.getSkipUntapCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Casting a creature spell triggers neither prowess nor the tap ability")
    void creatureSpellDoesNotTrigger() {
        Permanent niblis = harness.addToBattlefieldAndReturn(player1, new NiblisOfFrost());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(gqs.getEffectivePower(gd, niblis)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, niblis)).isEqualTo(3);
    }

    @Test
    @DisplayName("An opponent's instant does not trigger Niblis of Frost")
    void opponentInstantDoesNotTrigger() {
        Permanent niblis = harness.addToBattlefieldAndReturn(player1, new NiblisOfFrost());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, player1.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(gqs.getEffectivePower(gd, niblis)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, niblis)).isEqualTo(3);
    }

    @Test
    @DisplayName("The tap ability cannot target a creature its controller controls")
    void cannotTargetOwnCreature() {
        harness.addToBattlefield(player1, new NiblisOfFrost());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, ownCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void sorceryTriggersBothAbilitiesBeforeTheSpellResolves() {
        Permanent niblis = harness.addToBattlefieldAndReturn(player1, new NiblisOfFrost());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new NiblisOfFrost());
        harness.setHand(player1, List.of(new TakeInventory()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castSorcery(player1, 0);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        assertThat(gqs.getEffectivePower(gd, niblis)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, niblis)).isEqualTo(4);
        assertThat(target.isTapped()).isTrue();
        harness.performUntapStep(player1);
        assertThat(target.isTapped()).isTrue();
        harness.performUntapStep(player2);
        assertThat(target.isTapped()).isTrue();
        harness.performUntapStep(player2);
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    void enchantmentTriggersOnlyProwess() {
        Permanent niblis = harness.addToBattlefieldAndReturn(player1, new NiblisOfFrost());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new NiblisOfFrost());
        harness.setHand(player1, List.of(new LunarForce()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castEnchantment(player1, 0);
        assertThat(gd.stack).hasSize(2);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, niblis)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, niblis)).isEqualTo(4);
        assertThat(target.isTapped()).isFalse();
        assertThat(target.getSkipUntapCount()).isZero();
    }

    @Test
    void prowessStillResolvesWithoutAnOpponentCreature() {
        Permanent niblis = harness.addToBattlefieldAndReturn(player1, new NiblisOfFrost());
        harness.setHand(player1, List.of(new TakeInventory()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castSorcery(player1, 0);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, niblis)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, niblis)).isEqualTo(4);
    }

    @Test
    void repeatedTriggersPreventOnlyTheNextUntap() {
        Permanent niblis = harness.addToBattlefieldAndReturn(player1, new NiblisOfFrost());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new NiblisOfFrost());
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        for (int i = 0; i < 2; i++) {
            harness.castInstant(player1, 0, player2.getId());
            harness.handlePermanentChosen(player1, target.getId());
            harness.passBothPriorities();
            harness.passBothPriorities();
        }

        assertThat(gqs.getEffectivePower(gd, niblis)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, niblis)).isEqualTo(5);
        harness.performUntapStep(player2);
        assertThat(target.isTapped()).isTrue();
        harness.performUntapStep(player2);
        assertThat(target.isTapped()).isFalse();
    }
}
