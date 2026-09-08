package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@DisplayName("So Tiny")
@CardUsed({SoTiny.class, FountainOfYouth.class, GrizzlyBears.class})
class SoTinyTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature gets -2/-0 normally")
    void enchantedCreatureGetsNormalDebuff() {
        Permanent creature = addCreature(player1);
        attachAura(player1, creature);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Enchanted creature gets -6/-0 with seven cards in its controller's graveyard")
    void enchantedCreatureGetsThresholdDebuff() {
        harness.setGraveyard(player1, graveyardWithSevenCards());
        Permanent creature = addCreature(player1);
        attachAura(player1, creature);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(-4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("The threshold uses the enchanted creature's controller")
    void thresholdUsesEnchantedCreatureController() {
        harness.setGraveyard(player2, graveyardWithSevenCards());
        Permanent creature = addCreature(player2);
        attachAura(player1, creature);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(-4);
    }

    @Test
    @DisplayName("The threshold debuff ends when the graveyard drops below seven cards")
    void thresholdDebuffEndsBelowSevenCards() {
        harness.setGraveyard(player1, graveyardWithSevenCards());
        Permanent creature = addCreature(player1);
        attachAura(player1, creature);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(-4);

        harness.setGraveyard(player1, graveyardWithSevenCards().subList(0, 6));

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(0);
    }

    @Test
    @DisplayName("Resolves attached to a target creature")
    void resolvesAttachedToTargetCreature() {
        Permanent creature = addCreature(player1);
        harness.setHand(player1, List.of(new SoTiny()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getName().equals("So Tiny")
                        && creature.getId().equals(permanent.getAttachedTo()));
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new SoTiny()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private Permanent addCreature(Player player) {
        return harness.addToBattlefieldAndReturn(player, new GrizzlyBears());
    }

    private void attachAura(Player controller, Permanent creature) {
        Permanent aura = new Permanent(new SoTiny());
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(controller.getId()).add(aura);
    }

    private List<Card> graveyardWithSevenCards() {
        return List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears());
    }
}
