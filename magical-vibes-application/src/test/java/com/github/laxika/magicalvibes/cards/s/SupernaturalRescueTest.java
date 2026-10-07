package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.l.LanternBearer;
import com.github.laxika.magicalvibes.cards.t.TravelingMinister;
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

@CardUsed({SupernaturalRescue.class, TravelingMinister.class, LanternBearer.class})
class SupernaturalRescueTest extends BaseCardTest {

    @Test
    @DisplayName("Taps up to two opposing creatures when cast and boosts the enchanted creature")
    void castTriggerTapsOpposingCreaturesAndAuraBoosts() {
        Permanent enchanted = harness.addToBattlefieldAndReturn(player1, new TravelingMinister());
        Permanent spirit = harness.addToBattlefieldAndReturn(player1, new LanternBearer());
        Permanent firstOpponent = harness.addToBattlefieldAndReturn(player2, new TravelingMinister());
        Permanent secondOpponent = harness.addToBattlefieldAndReturn(player2, new TravelingMinister());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new SupernaturalRescue()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.passPriority(player2);

        harness.castEnchantment(player1, 0, enchanted.getId());
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, firstOpponent.getId());
        harness.handlePermanentChosen(player1, secondOpponent.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(firstOpponent.isTapped()).isTrue();
        assertThat(secondOpponent.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, enchanted)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, enchanted)).isEqualTo(3);
        assertThat(spirit.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot be cast at instant speed without controlling a Spirit")
    void cannotCastAtInstantSpeedWithoutSpirit() {
        Permanent enchanted = harness.addToBattlefieldAndReturn(player1, new TravelingMinister());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new SupernaturalRescue()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.passPriority(player2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, enchanted.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Cast trigger cannot target a creature controlled by its caster")
    void castTriggerCannotTargetOwnCreature() {
        Permanent enchanted = harness.addToBattlefieldAndReturn(player1, new TravelingMinister());
        harness.addToBattlefield(player1, new LanternBearer());
        harness.addToBattlefield(player2, new TravelingMinister());

        harness.setHand(player1, List.of(new SupernaturalRescue()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castEnchantment(player1, 0, enchanted.getId());

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, enchanted.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid permanent");
    }

    @Test
    void canChooseZeroTargetsEvenWhenOpposingCreaturesExist() {
        Permanent enchanted = harness.addToBattlefieldAndReturn(player1, new TravelingMinister());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new TravelingMinister());
        harness.setHand(player1, List.of(new SupernaturalRescue()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castEnchantment(player1, 0, enchanted.getId());
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(opponent.isTapped()).isFalse();
        assertThat(enchanted.isTapped()).isFalse();
        assertThat(gqs.getEffectivePower(gd, enchanted)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, enchanted)).isEqualTo(3);
        harness.assertOnBattlefield(player1, "Supernatural Rescue");
    }

    @Test
    void canStopAfterOneTargetAndTriggerResolvesBeforeAura() {
        Permanent enchanted = harness.addToBattlefieldAndReturn(player1, new TravelingMinister());
        Permanent chosen = harness.addToBattlefieldAndReturn(player2, new TravelingMinister());
        Permanent unchosen = harness.addToBattlefieldAndReturn(player2, new TravelingMinister());
        harness.setHand(player1, List.of(new SupernaturalRescue()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castEnchantment(player1, 0, enchanted.getId());
        harness.handlePermanentChosen(player1, chosen.getId());
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(chosen.isTapped()).isTrue();
        assertThat(unchosen.isTapped()).isFalse();
        harness.assertNotOnBattlefield(player1, "Supernatural Rescue");
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, enchanted)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, enchanted)).isEqualTo(3);
        harness.assertOnBattlefield(player1, "Supernatural Rescue");
    }

    @Test
    void canCastWithoutSpiritOrOpposingCreaturesAtSorcerySpeed() {
        Permanent enchanted = harness.addToBattlefieldAndReturn(player1, new TravelingMinister());
        harness.setHand(player1, List.of(new SupernaturalRescue()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castEnchantment(player1, 0, enchanted.getId());
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Supernatural Rescue");
        assertThat(gqs.getEffectivePower(gd, enchanted)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, enchanted)).isEqualTo(3);
    }

    @Test
    void auraCannotTargetOpposingCreature() {
        harness.addToBattlefield(player1, new TravelingMinister());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new TravelingMinister());
        harness.setHand(player1, List.of(new SupernaturalRescue()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, opponent.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertNotOnBattlefield(player1, "Supernatural Rescue");
        assertThat(opponent.isTapped()).isFalse();
    }

    @Test
    void opposingSpiritDoesNotPermitInstantSpeedCasting() {
        Permanent enchanted = harness.addToBattlefieldAndReturn(player1, new TravelingMinister());
        harness.addToBattlefield(player2, new LanternBearer());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new SupernaturalRescue()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.passPriority(player2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, enchanted.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }
}
