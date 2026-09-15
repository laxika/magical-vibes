package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.a.AmphibiousKavu;
import com.github.laxika.magicalvibes.cards.m.ManaCylix;
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

@CardUsed({Insolence.class, AmphibiousKavu.class, ManaCylix.class})
class InsolenceTest extends BaseCardTest {

    @Test
    @DisplayName("Cannot enchant a non-creature permanent")
    void cannotEnchantNonCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new ManaCylix());
        harness.setHand(player1, List.of(new Insolence()));
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Can enchant a creature")
    void canEnchantCreature() {
        Permanent creature = addCreatureReady(player2, new AmphibiousKavu());
        harness.setHand(player1, List.of(new Insolence()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.forceActivePlayer(player1);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof Insolence
                        && creature.getId().equals(permanent.getAttachedTo()));
    }

    @Test
    @DisplayName("Tapping the enchanted creature deals 2 damage to its controller")
    void tappingEnchantedCreatureDamagesController() {
        Permanent creature = addCreatureWithAura(player2);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.withAutoStop(com.github.laxika.magicalvibes.model.TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(player2, List.of(0));
            resolveAllTriggers();
        });

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping an un-enchanted creature does not deal damage")
    void tappingUnenchantedCreatureDoesNotDealDamage() {
        addCreatureReady(player2, new AmphibiousKavu());
        harness.setLife(player2, 20);

        harness.withAutoStop(com.github.laxika.magicalvibes.model.TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(player2, List.of(0));
            resolveAllTriggers();
        });

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    private Permanent addCreatureWithAura(Player creatureController) {
        Permanent creature = addCreatureReady(creatureController, new AmphibiousKavu());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new Insolence());
        aura.setAttachedTo(creature.getId());
        return creature;
    }
}
