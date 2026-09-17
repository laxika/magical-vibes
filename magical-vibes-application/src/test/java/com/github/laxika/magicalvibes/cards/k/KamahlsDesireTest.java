package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.w.WildMongrel;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({KamahlsDesire.class, WildMongrel.class, Mountain.class})
class KamahlsDesireTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature has first strike and no threshold boost below threshold")
    void enchantedCreatureHasFirstStrikeBelowThreshold() {
        Permanent creature = addCreatureReady(player1, new WildMongrel());
        attachAura(player1, creature);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Threshold gives enchanted creature +3/+0")
    void thresholdBoostsEnchantedCreature() {
        harness.setGraveyard(player1, graveyardWithSevenCards());
        Permanent creature = addCreatureReady(player1, new WildMongrel());
        attachAura(player1, creature);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Threshold uses the Aura controller's graveyard for an opponent's creature")
    void thresholdUsesAuraControllersGraveyardForOpposingCreature() {
        harness.setGraveyard(player1, graveyardWithSevenCards());
        Permanent creature = addCreatureReady(player2, new WildMongrel());
        attachAura(player1, creature);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Threshold counts noncreature cards in the Aura controller's graveyard")
    void thresholdCountsNoncreatureCards() {
        harness.setGraveyard(player1, graveyardWithSevenNoncreatureCards());
        Permanent creature = addCreatureReady(player1, new WildMongrel());
        attachAura(player1, creature);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
    }

    @Test
    @DisplayName("Opponent graveyard does not enable threshold")
    void opponentGraveyardDoesNotEnableThreshold() {
        harness.setGraveyard(player2, graveyardWithSevenCards());
        Permanent creature = addCreatureReady(player1, new WildMongrel());
        attachAura(player1, creature);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Threshold boost stops when the Aura controller drops below seven cards")
    void thresholdStopsBelowSevenCards() {
        harness.setGraveyard(player1, graveyardWithSevenCards());
        Permanent creature = addCreatureReady(player1, new WildMongrel());
        attachAura(player1, creature);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);

        harness.setGraveyard(player1, graveyardWithSevenCards().subList(0, 6));

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Enchanted creature loses the Aura's effects when it is removed")
    void effectsStopWhenAuraIsRemoved() {
        harness.setGraveyard(player1, graveyardWithSevenCards());
        Permanent creature = addCreatureReady(player1, new WildMongrel());
        Permanent aura = attachAura(player1, creature);

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player2, new WildMongrel());
        harness.addToBattlefield(player1, new Mountain());
        harness.setHand(player1, List.of(new KamahlsDesire()));
        harness.addMana(player1, ManaColor.RED, 2);

        Permanent mountain = findPermanent(player1, "Mountain");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, mountain.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private Permanent attachAura(Player controller, Permanent creature) {
        Permanent aura = new Permanent(new KamahlsDesire());
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(controller.getId()).add(aura);
        return aura;
    }

    private List<Card> graveyardWithSevenCards() {
        return List.of(
                new WildMongrel(), new WildMongrel(), new WildMongrel(), new WildMongrel(),
                new WildMongrel(), new WildMongrel(), new WildMongrel());
    }

    private List<Card> graveyardWithSevenNoncreatureCards() {
        return List.of(
                new Mountain(), new Mountain(), new Mountain(), new Mountain(),
                new Mountain(), new Mountain(), new Mountain());
    }
}
