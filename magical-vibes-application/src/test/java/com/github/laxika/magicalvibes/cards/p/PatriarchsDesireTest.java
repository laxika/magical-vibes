package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.w.WildMongrel;
import com.github.laxika.magicalvibes.model.Card;
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

@CardUsed({PatriarchsDesire.class, WildMongrel.class, Plains.class})
class PatriarchsDesireTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature gets -2/-2 below threshold")
    void enchantedCreatureGetsBaseDebuff() {
        Permanent creature = addCreatureReady(player1, new WildMongrel());
        attachAura(player1, creature);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(0);
    }

    @Test
    @DisplayName("Threshold gives enchanted creature an additional +2/-2")
    void thresholdAddsAdditionalDebuff() {
        harness.setGraveyard(player1, graveyardWithSevenCards());
        Permanent creature = addCreatureReady(player1, new WildMongrel());
        attachAura(player1, creature);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(-2);
    }

    @Test
    @DisplayName("Opponent graveyard does not enable threshold")
    void opponentGraveyardDoesNotEnableThreshold() {
        harness.setGraveyard(player2, graveyardWithSevenCards());
        Permanent creature = addCreatureReady(player1, new WildMongrel());
        attachAura(player1, creature);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(0);
    }

    @Test
    @DisplayName("Threshold uses the Aura controller's graveyard")
    void thresholdUsesAuraControllersGraveyard() {
        harness.setGraveyard(player1, graveyardWithSevenCards());
        Permanent creature = addCreatureReady(player2, new WildMongrel());
        attachAura(player1, creature);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(-2);
    }

    @Test
    @DisplayName("Threshold effect stops when the Aura controller drops below seven cards")
    void thresholdStopsBelowSevenCards() {
        harness.setGraveyard(player1, graveyardWithSevenCards());
        Permanent creature = addCreatureReady(player1, new WildMongrel());
        attachAura(player1, creature);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(-2);

        harness.setGraveyard(player1, graveyardWithSevenCards().subList(0, 6));

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(0);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player1, new Plains());
        harness.setHand(player1, List.of(new PatriarchsDesire()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        Permanent land = findPermanent(player1, "Plains");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private void attachAura(Player controller, Permanent creature) {
        Permanent aura = new Permanent(new PatriarchsDesire());
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(controller.getId()).add(aura);
    }

    private List<Card> graveyardWithSevenCards() {
        return List.of(
                new WildMongrel(), new WildMongrel(), new WildMongrel(), new WildMongrel(),
                new WildMongrel(), new WildMongrel(), new WildMongrel());
    }
}
