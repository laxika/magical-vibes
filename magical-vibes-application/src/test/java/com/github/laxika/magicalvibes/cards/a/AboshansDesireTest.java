package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.w.WoodlandDruid;
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

@CardUsed({AboshansDesire.class, WoodlandDruid.class, Plains.class})
class AboshansDesireTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature has flying")
    void enchantedCreatureHasFlying() {
        Permanent creature = addCreatureReady(player1, new WoodlandDruid());
        attachAura(player1, creature);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Enchanted creature does not have shroud below threshold")
    void noShroudBelowThreshold() {
        Permanent creature = addCreatureReady(player1, new WoodlandDruid());
        attachAura(player1, creature);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.SHROUD)).isFalse();
    }

    @Test
    @DisplayName("Enchanted creature has shroud at threshold")
    void shroudAtThreshold() {
        harness.setGraveyard(player1, graveyardWithSevenCards());
        Permanent creature = addCreatureReady(player1, new WoodlandDruid());
        attachAura(player1, creature);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.SHROUD)).isTrue();
    }

    @Test
    @DisplayName("Opponent graveyard does not enable threshold")
    void opponentGraveyardDoesNotEnableThreshold() {
        harness.setGraveyard(player2, graveyardWithSevenCards());
        Permanent creature = addCreatureReady(player1, new WoodlandDruid());
        attachAura(player1, creature);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.SHROUD)).isFalse();
    }

    @Test
    @DisplayName("Threshold uses the Aura controller's graveyard")
    void thresholdUsesAuraControllerGraveyard() {
        harness.setGraveyard(player1, graveyardWithSevenCards());
        Permanent creature = addCreatureReady(player2, new WoodlandDruid());
        attachAura(player1, creature);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.SHROUD)).isTrue();
    }

    @Test
    @DisplayName("Shroud ends when the Aura controller's graveyard drops below threshold")
    void shroudEndsBelowThreshold() {
        harness.setGraveyard(player1, graveyardWithSevenCards());
        Permanent creature = addCreatureReady(player1, new WoodlandDruid());
        attachAura(player1, creature);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.SHROUD)).isTrue();

        harness.setGraveyard(player1, graveyardWithSevenCards().subList(0, 6));

        assertThat(gqs.hasKeyword(gd, creature, Keyword.SHROUD)).isFalse();
    }

    @Test
    @DisplayName("Aboshan's Desire can target only a creature")
    void cannotTargetNonCreature() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Plains());
        harness.setHand(player1, List.of(new AboshansDesire()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private void attachAura(Player controller, Permanent creature) {
        Permanent aura = new Permanent(new AboshansDesire());
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(controller.getId()).add(aura);
    }

    private List<Card> graveyardWithSevenCards() {
        return List.of(
                new WoodlandDruid(), new WoodlandDruid(), new WoodlandDruid(), new WoodlandDruid(),
                new WoodlandDruid(), new WoodlandDruid(), new WoodlandDruid());
    }
}
