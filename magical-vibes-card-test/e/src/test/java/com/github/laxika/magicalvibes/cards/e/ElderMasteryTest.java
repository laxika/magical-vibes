package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ElderMastery.class, Forest.class, GrizzlyBears.class, Mountain.class, ProdigalPyromancer.class})
class ElderMasteryTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature gets +3/+3")
    void enchantedCreatureGetsBoost() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears()); // 2/2
        attachElderMastery(bears);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(5);
    }

    @Test
    @DisplayName("Enchanted creature has flying")
    void enchantedCreatureHasFlying() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        attachElderMastery(bears);

        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Creature loses boost and flying when Elder Mastery is removed")
    void creatureLosesBuffWhenRemoved() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent aura = attachElderMastery(bears);

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Enchanted creature dealing combat damage makes the damaged player discard two cards")
    void damagedPlayerDiscardsTwo() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        attachElderMastery(bears);
        bears.setAttacking(true);
        harness.setHand(player2, new ArrayList<>(List.of(new Forest(), new GrizzlyBears(), new Mountain())));

        resolveCombatAndTrigger();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).playerId())
                .isEqualTo(player2.getId());

        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Cannot enchant a land")
    void cannotEnchantALand() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new Mountain());
        harness.setHand(player1, List.of(new ElderMastery()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        Permanent mountain = findPermanent(player1, "Mountain");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, mountain.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    void resolvesAttachedToOpponentsCreature() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ElderMastery()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Elder Mastery").getAttachedTo()).isEqualTo(bears.getId());
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isTrue();
    }

    @Test
    void damagedPlayerWithOneCardDiscardsIt() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        attachElderMastery(bears);
        bears.setAttacking(true);
        harness.setHand(player2, List.of(new Forest()));

        resolveCombatAndTrigger();
        if (gd.interaction.isAwaitingInput()) {
            harness.handleCardChosen(player2, 0);
        }

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).anyMatch(card -> card instanceof Forest);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void damageToPlayerWithEmptyHandDoesNotRequestDiscard() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        attachElderMastery(bears);
        bears.setAttacking(true);
        harness.setHand(player2, List.of());

        resolveCombatAndTrigger();

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void opponentsEnchantedCreatureMakesAuraControllerDiscardWhenDamaged() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        attachElderMastery(bears);
        bears.setAttacking(true);
        harness.setHand(player1, List.of(new Forest(), new Mountain(), new GrizzlyBears()));

        resolveCombat(player2);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void noncombatDamageAlsoMakesDamagedPlayerDiscardTwo() {
        Permanent pyromancer = addCreatureReady(player1, new ProdigalPyromancer());
        attachElderMastery(pyromancer);
        harness.setHand(player2, List.of(new Forest(), new Mountain(), new GrizzlyBears()));

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void damageToCreatureDoesNotTriggerDiscard() {
        Permanent pyromancer = addCreatureReady(player1, new ProdigalPyromancer());
        attachElderMastery(pyromancer);
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player2, List.of(new Forest(), new Mountain(), new GrizzlyBears()));

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(bears);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(3);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void damageToEnchantedCreaturesControllerAlsoTriggersDiscard() {
        Permanent pyromancer = addCreatureReady(player1, new ProdigalPyromancer());
        attachElderMastery(pyromancer);
        harness.setHand(player1, List.of(new Forest(), new Mountain(), new GrizzlyBears()));

        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private Permanent attachElderMastery(Permanent creature) {
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new ElderMastery());
        aura.setAttachedTo(creature.getId());
        return aura;
    }

    private void resolveCombatAndTrigger() {
        resolveCombat();
        resolveAllTriggers();
    }
}
