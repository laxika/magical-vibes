package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.ChainersEdict;
import com.github.laxika.magicalvibes.cards.h.HellBentRaider;
import com.github.laxika.magicalvibes.cards.t.TaintedIsle;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ShadesForm.class, HellBentRaider.class, ChainersEdict.class, TaintedIsle.class})
class ShadesFormTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Shade's Form attaches it to the target creature")
    void resolvingAttachesToTarget() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new HellBentRaider());
        castShadesForm(player1, creature);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard() instanceof ShadesForm
                        && p.isAttached()
                        && p.getAttachedTo().equals(creature.getId()));
    }

    @Test
    @DisplayName("The enchanted creature can pay black mana for +1/+1 until end of turn")
    void grantedAbilityBoostsUntilEndOfTurn() {
        Permanent creature = addCreatureReady(player1, new HellBentRaider());
        castShadesForm(player1, creature);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(TurnStep.UPKEEP);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("The opponent controlling the enchanted creature can activate the granted ability")
    void enchantedCreatureControllerCanActivateGrantedAbility() {
        Permanent creature = addCreatureReady(player2, new HellBentRaider());
        castShadesForm(player1, creature);
        harness.addMana(player2, ManaColor.BLACK, 1);

        harness.activateAbility(player2, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }

    @Test
    @DisplayName("When the enchanted opponent creature dies, it returns under the Aura controller's control")
    void returnsOpponentCreatureUnderAuraControllersControl() {
        Permanent creature = addCreatureReady(player2, new HellBentRaider());
        Card creatureCard = creature.getCard();
        castShadesForm(player1, creature);

        killCreature(player1, player2);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getId().equals(creatureCard.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getId().equals(creatureCard.getId()));
        Permanent returned = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getId().equals(creatureCard.getId()))
                .findFirst()
                .orElseThrow();
        assertThat(gd.stolenCreatures).containsEntry(returned.getId(), player2.getId());
    }

    @Test
    @DisplayName("Shade's Form cannot enchant a noncreature permanent")
    void cannotEnchantNonCreature() {
        Permanent nonCreature = harness.addToBattlefieldAndReturn(player2, new TaintedIsle());
        harness.setHand(player1, List.of(new ShadesForm()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, nonCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Repeated activations each give the enchanted creature +1/+1")
    void repeatedActivationsAccumulate() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new HellBentRaider());
        castShadesForm(player1, creature);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
    }

    @Test
    @DisplayName("An owned creature returns untapped without the Aura or its previous boost")
    void returnsOwnedCreatureWithoutAuraOrPreviousBoost() {
        Permanent creature = addCreatureReady(player1, new HellBentRaider());
        Card creatureCard = creature.getCard();
        castShadesForm(player1, creature);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        creature.tap();

        killCreature(player2, player1);

        Permanent returned = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getId().equals(creatureCard.getId()))
                .findFirst().orElseThrow();
        assertThat(returned.getId()).isNotEqualTo(creature.getId());
        assertThat(returned.isTapped()).isFalse();
        assertThat(gqs.getEffectivePower(gd, returned)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, returned)).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard() instanceof ShadesForm);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c instanceof ShadesForm);

        killCreature(player2, player1);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getId().equals(creatureCard.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(creatureCard.getId()));
    }

    private void castShadesForm(Player controller, Permanent target) {
        harness.setHand(controller, List.of(new ShadesForm()));
        harness.addMana(controller, ManaColor.BLACK, 3);

        harness.castEnchantment(controller, 0, target.getId());
        harness.passBothPriorities();
    }

    private void killCreature(Player caster, Player creatureController) {
        harness.forceActivePlayer(caster);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(caster, List.of(new ChainersEdict()));
        harness.addMana(caster, ManaColor.BLACK, 1);
        harness.addMana(caster, ManaColor.COLORLESS, 1);
        harness.castAndResolveSorcery(caster, 0, creatureController.getId());
        harness.passBothPriorities();
    }
}
