package com.github.laxika.magicalvibes.service.ability.cost;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.PutChargeCounterOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.TapCreatureCost;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TapCreatureCostHandlerTest extends BaseCardTest {

    @Test
    @DisplayName("Auto-selects single matching untapped creature")
    void autoSelectsSingleMatchingCreature() {
        Permanent source = addReadyPermanent(player1, createCardWithTapCreatureCost());
        addReadyPermanent(player1, createBlueCreature("Blue Wizard"));

        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(source);
        harness.activateAbility(player1, idx, null, null);

        // Single blue creature should be auto-tapped
        Permanent wizard = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Blue Wizard"))
                .findFirst().orElseThrow();
        assertThat(wizard.isTapped()).isTrue();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("No matching untapped creature throws")
    void noMatchingUntappedCreatureThrows() {
        Permanent source = addReadyPermanent(player1, createCardWithTapCreatureCost());
        // No blue creatures on the battlefield

        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(source);
        assertThatThrownBy(() -> harness.activateAbility(player1, idx, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No untapped matching creature to tap");
    }

    @Test
    @DisplayName("Already tapped creature is not a valid choice")
    void alreadyTappedCreatureNotValid() {
        Permanent source = addReadyPermanent(player1, createCardWithTapCreatureCost());
        Permanent creature = addReadyPermanent(player1, createBlueCreature("Blue Wizard"));
        creature.tap();

        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(source);
        assertThatThrownBy(() -> harness.activateAbility(player1, idx, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No untapped matching creature to tap");
    }

    @Test
    @DisplayName("Non-matching color creature is not a valid choice")
    void nonMatchingColorCreatureNotValid() {
        Permanent source = addReadyPermanent(player1, createCardWithTapCreatureCost());
        addReadyPermanent(player1, createRedCreature("Red Warrior"));

        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(source);
        assertThatThrownBy(() -> harness.activateAbility(player1, idx, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No untapped matching creature to tap");
    }

    @Test
    @DisplayName("Multiple matching creatures prompt for choice")
    void multipleMatchingCreaturesPromptChoice() {
        Permanent source = addReadyPermanent(player1, createCardWithTapCreatureCost());
        addReadyPermanent(player1, createBlueCreature("Blue Wizard A"));
        addReadyPermanent(player1, createBlueCreature("Blue Wizard B"));

        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(source);
        harness.activateAbility(player1, idx, null, null);

        assertThat(gd.interaction.awaitingInputType())
                .isEqualTo(AwaitingInput.PERMANENT_CHOICE);
    }

    @Test
    @DisplayName("Completing tap choice puts ability on stack and taps creature")
    void completingTapChoicePutsAbilityOnStack() {
        Permanent source = addReadyPermanent(player1, createCardWithTapCreatureCost());
        Permanent wizardA = addReadyPermanent(player1, createBlueCreature("Blue Wizard A"));
        addReadyPermanent(player1, createBlueCreature("Blue Wizard B"));

        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(source);
        harness.activateAbility(player1, idx, null, null);
        harness.handlePermanentChosen(player1, wizardA.getId());

        assertThat(wizardA.isTapped()).isTrue();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
    }

    // =========================================================================
    // Helper methods
    // =========================================================================

    private Permanent addReadyPermanent(Player player, Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Card createCardWithTapCreatureCost() {
        Card card = new Card();
        card.setName("Test Tap Cost Artifact");
        card.setType(CardType.ARTIFACT);
        card.setManaCost("{0}");
        card.setColor(null);
        card.addActivatedAbility(new ActivatedAbility(
                false, null,
                List.of(new TapCreatureCost(new PermanentColorInPredicate(Set.of(CardColor.BLUE))),
                        new PutChargeCounterOnSelfEffect()),
                "Tap an untapped blue creature: put a charge counter"
        ));
        return card;
    }

    private Card createBlueCreature(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("{U}");
        card.setColor(CardColor.BLUE);
        card.setPower(1);
        card.setToughness(1);
        return card;
    }

    private Card createRedCreature(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("{R}");
        card.setColor(CardColor.RED);
        card.setPower(2);
        card.setToughness(2);
        return card;
    }
}
