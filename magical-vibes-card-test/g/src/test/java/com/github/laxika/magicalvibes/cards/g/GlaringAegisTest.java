package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GlaringAegis.class, GreenwoodSentinel.class})
class GlaringAegisTest extends BaseCardTest {

    private void castAndResolve(UUID enchantTargetId, UUID tapTargetId) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new GlaringAegis()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castEnchantment(player1, 0, List.of(enchantTargetId, tapTargetId));
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Glaring Aegis attaches, boosts its creature, and taps the ETB target")
    void attachesBoostsAndTaps() {
        Permanent enchantedCreature = harness.addToBattlefieldAndReturn(player2, new GreenwoodSentinel());
        Permanent tappedCreature = harness.addToBattlefieldAndReturn(player2, new GreenwoodSentinel());

        castAndResolve(enchantedCreature.getId(), tappedCreature.getId());

        assertThat(tappedCreature.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, enchantedCreature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, enchantedCreature)).isEqualTo(5);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Glaring Aegis")
                        && p.isAttached()
                        && p.getAttachedTo().equals(enchantedCreature.getId()));
    }

    @Test
    @DisplayName("Glaring Aegis stops boosting when it leaves the battlefield")
    void boostStopsWhenRemoved() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GreenwoodSentinel());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new GlaringAegis());
        aura.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(5);

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Glaring Aegis cannot target your own creature for its ETB tap")
    void cannotTargetOwnCreatureForTap() {
        Permanent enchantedCreature = harness.addToBattlefieldAndReturn(player1, new GreenwoodSentinel());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GreenwoodSentinel());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new GlaringAegis()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castEnchantment(
                player1, 0, List.of(enchantedCreature.getId(), ownCreature.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature an opponent controls");
    }

    @Test
    @DisplayName("Glaring Aegis can resolve when no opponent controls a creature")
    void resolvesWithoutAnOpponentCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GreenwoodSentinel());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new GlaringAegis()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Glaring Aegis").getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(5);
        assertThat(creature.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Glaring Aegis chooses its tap target after entering and can tap its enchanted creature")
    void choosesTapTargetAfterEntering() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GreenwoodSentinel());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new GlaringAegis()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        assertThat(findPermanent(player1, "Glaring Aegis").getAttachedTo()).isEqualTo(creature.getId());
        assertThat(creature.isTapped()).isFalse();
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(5);
    }

    @Test
    @DisplayName("Glaring Aegis's tap trigger still resolves after the Aura leaves")
    void tapTriggerSurvivesAuraRemoval() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GreenwoodSentinel());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GreenwoodSentinel());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new GlaringAegis()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, opponentCreature.getId());
        gd.playerBattlefields.get(player1.getId()).remove(findPermanent(player1, "Glaring Aegis"));
        harness.passBothPriorities();

        assertThat(opponentCreature.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Glaring Aegis does not enter or tap anything when its enchant target disappears")
    void illegalEnchantTargetPreventsEntryAndTap() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GreenwoodSentinel());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GreenwoodSentinel());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new GlaringAegis()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castEnchantment(player1, 0, creature.getId());
        gd.playerBattlefields.get(player1.getId()).remove(creature);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Glaring Aegis")).isZero();
        assertThat(gd.playerGraveyards.get(player1.getId())).anyMatch(card -> card instanceof GlaringAegis);
        assertThat(opponentCreature.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }
}
