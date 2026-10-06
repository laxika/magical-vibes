package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ShinyImpetus.class, GrizzlyBears.class})
class ShinyImpetusTest extends BaseCardTest {

    @Test
    @DisplayName("Boosts the enchanted creature by +2/+2")
    void boostsEnchantedCreature() {
        Permanent creature = addReadyCreature(player1);
        attachAura(player1, creature);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
    }

    @Test
    @DisplayName("Goads the enchanted creature while attached")
    void goadsEnchantedCreature() {
        Permanent creature = addReadyCreature(player1);
        attachAura(player1, creature);

        assertThatThrownBy(() -> declareAttackers(player1, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @DisplayName("Creates a Treasure when the enchanted creature attacks")
    void attackCreatesTreasure() {
        Permanent creature = addReadyCreature(player1);
        attachAura(player1, creature);

        declareAttackers(player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(creature)));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }

    @Test
    @DisplayName("Can resolve enchanting an opponent's creature")
    void resolvesOnOpponentsCreature() {
        Permanent creature = addReadyCreature(player2);
        harness.setHand(player1, List.of(new ShinyImpetus()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Shiny Impetus").getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        assertThat(gqs.isGoaded(gd, creature)).isTrue();
    }

    @Test
    @DisplayName("An opponent's enchanted creature gives Treasure to the Aura controller")
    void opponentsAttackCreatesTreasureForAuraController() {
        Permanent creature = addReadyCreature(player2);
        attachAura(player1, creature);

        declareAttackers(player2, List.of(gd.playerBattlefields.get(player2.getId()).indexOf(creature)));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
        assertThat(findPermanents(player2, "Treasure")).isEmpty();
        assertThat(findPermanent(player1, "Treasure").isTapped()).isFalse();
    }

    @Test
    @DisplayName("The boost and goad end as soon as the Aura leaves")
    void auraLeavingEndsContinuousEffects() {
        Permanent creature = addReadyCreature(player1);
        Permanent aura = attachAura(player1, creature);
        assertThat(gqs.isGoaded(gd, creature)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(aura);
        gd.playerGraveyards.get(player1.getId()).add(aura.getCard());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.isGoaded(gd, creature)).isFalse();
        declareAttackers(player1, List.of());
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
    }

    @Test
    @DisplayName("A triggered Treasure ability survives the Aura leaving")
    void treasureTriggerSurvivesAuraLeaving() {
        Permanent creature = addReadyCreature(player2);
        Permanent aura = attachAura(player1, creature);
        declareAttackers(player2, List.of(gd.playerBattlefields.get(player2.getId()).indexOf(creature)));
        assertThat(gd.stack).hasSize(1);

        gd.playerBattlefields.get(player1.getId()).remove(aura);
        gd.playerGraveyards.get(player1.getId()).add(aura.getCard());
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
        assertThat(findPermanents(player2, "Treasure")).isEmpty();
    }

    @Test
    @DisplayName("Only the enchanted creature receives the boost and goad or triggers Treasure")
    void unrelatedCreatureIsUnaffected() {
        Permanent enchanted = addReadyCreature(player1);
        Permanent other = addReadyCreature(player1);
        attachAura(player1, enchanted);
        enchanted.setTapped(true);

        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(2);
        assertThat(gqs.isGoaded(gd, other)).isFalse();
        declareAttackers(player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(other)));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Treasure")).isEmpty();
    }

    @Test
    @DisplayName("Goad does not force a summoning-sick creature to attack")
    void summoningSickCreatureMayStayBack() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        creature.setSummoningSick(true);
        attachAura(player1, creature);

        declareAttackers(player1, List.of());
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Treasure")).isEmpty();
    }

    @Test
    @DisplayName("Static goad continues through the Aura controller's next turn")
    void goadDoesNotExpireAtControllersNextTurn() {
        Permanent creature = addReadyCreature(player1);
        attachAura(player1, creature);

        advanceToUpkeep(player1);

        assertThat(gqs.isGoaded(gd, creature)).isTrue();
        assertThatThrownBy(() -> declareAttackers(player1, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @DisplayName("Two attached copies each boost the creature and create a Treasure")
    void multipleAurasEachTrigger() {
        Permanent creature = addReadyCreature(player2);
        attachAura(player1, creature);
        attachAura(player1, creature);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(6);
        declareAttackers(player2, List.of(gd.playerBattlefields.get(player2.getId()).indexOf(creature)));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Treasure")).hasSize(2);
        assertThat(findPermanents(player2, "Treasure")).isEmpty();
    }

    private Permanent addReadyCreature(Player player) {
        return addCreatureReady(player, new GrizzlyBears());
    }

    private Permanent attachAura(Player controller, Permanent creature) {
        Permanent aura = harness.addToBattlefieldAndReturn(controller, new ShinyImpetus());
        aura.setAttachedTo(creature.getId());
        return aura;
    }
}
