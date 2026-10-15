package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.z.ZephyrSpirit;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StrandsOfUndeath.class, ZephyrSpirit.class})
class StrandsOfUndeathTest extends BaseCardTest {

    @Test
    @DisplayName("When Strands of Undeath enters, target player discards two cards")
    void etbMakesTargetPlayerDiscardTwoCards() {
        Permanent zephyr = harness.addToBattlefieldAndReturn(player1, new ZephyrSpirit());
        harness.setHand(player2, new ArrayList<>(List.of(
                new ZephyrSpirit(), new ZephyrSpirit(), new ZephyrSpirit())));
        harness.setHand(player1, List.of(new StrandsOfUndeath()));
        addCastingMana();

        harness.castEnchantment(player1, 0, zephyr.getId());
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice targetChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(targetChoice.validIds()).contains(player1.getId(), player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        PendingInteraction.DiscardChoice discardChoice =
                gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class);
        assertThat(discardChoice).isNotNull();
        assertThat(discardChoice.playerId()).isEqualTo(player2.getId());
        assertThat(discardChoice.remainingCount()).isEqualTo(2);
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Zephyr Spirit", "Zephyr Spirit");
    }

    @Test
    @DisplayName("The enter-the-battlefield ability may target its controller")
    void etbCanMakeItsControllerDiscardTwoCards() {
        Permanent zephyr = harness.addToBattlefieldAndReturn(player2, new ZephyrSpirit());
        harness.setHand(player1, new ArrayList<>(List.of(
                new StrandsOfUndeath(), new ZephyrSpirit(), new ZephyrSpirit())));
        addCastingMana();

        harness.castEnchantment(player1, 0, zephyr.getId());
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice targetChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(targetChoice.validIds()).contains(player1.getId(), player2.getId());
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        PendingInteraction.DiscardChoice discardChoice =
                gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class);
        assertThat(discardChoice).isNotNull();
        assertThat(discardChoice.playerId()).isEqualTo(player1.getId());

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Zephyr Spirit", "Zephyr Spirit");
    }

    @Test
    @DisplayName("The activated ability gives the enchanted creature a regeneration shield")
    void activatedAbilityRegeneratesEnchantedCreature() {
        Permanent zephyr = addCreatureReady(player1, new ZephyrSpirit());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new StrandsOfUndeath());
        aura.setAttachedTo(zephyr.getId());

        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(zephyr.getRegenerationShield()).isEqualTo(1);
        assertThat(aura.getRegenerationShield()).isZero();
    }

    @Test
    @DisplayName("The enter-the-battlefield target must be a player")
    void etbTargetMustBeAPlayer() {
        Permanent zephyr = harness.addToBattlefieldAndReturn(player1, new ZephyrSpirit());
        Permanent invalidSecondTarget = harness.addToBattlefieldAndReturn(player2, new ZephyrSpirit());
        harness.setHand(player1, List.of(new StrandsOfUndeath()));
        addCastingMana();

        harness.castEnchantment(player1, 0, zephyr.getId());
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, invalidSecondTarget.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid permanent");
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    @DisplayName("A player with fewer than two cards discards their entire hand")
    void etbDiscardsAsManyCardsAsPossible(int handSize) {
        Permanent zephyr = harness.addToBattlefieldAndReturn(player1, new ZephyrSpirit());
        harness.setHand(player2, handSize == 0 ? List.of() : List.of(new ZephyrSpirit()));
        harness.setHand(player1, List.of(new StrandsOfUndeath()));
        addCastingMana();

        harness.castEnchantment(player1, 0, zephyr.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        if (gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class) != null) {
            harness.handleCardChosen(player2, 0);
        }

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(handSize);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The Aura controller can regenerate an opponent's enchanted creature")
    void regeneratesOpponentsCreature() {
        Permanent zephyr = harness.addToBattlefieldAndReturn(player2, new ZephyrSpirit());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new StrandsOfUndeath());
        aura.setAttachedTo(zephyr.getId());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(zephyr.getRegenerationShield()).isEqualTo(1);
        assertThat(aura.getRegenerationShield()).isZero();
        assertThat(zephyr.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Regeneration still resolves if the Aura leaves before its ability resolves")
    void regenerationResolvesAfterAuraLeaves() {
        Permanent zephyr = harness.addToBattlefieldAndReturn(player1, new ZephyrSpirit());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new StrandsOfUndeath());
        aura.setAttachedTo(zephyr.getId());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.activateAbility(player1, 1, null, null);

        gd.playerBattlefields.get(player1.getId()).remove(aura);
        gd.playerGraveyards.get(player1.getId()).add(aura.getCard());
        harness.passBothPriorities();

        assertThat(zephyr.getRegenerationShield()).isEqualTo(1);
        assertThat(aura.getRegenerationShield()).isZero();
    }

    private void addCastingMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
