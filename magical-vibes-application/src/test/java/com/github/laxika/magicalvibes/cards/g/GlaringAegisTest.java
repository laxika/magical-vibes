package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GlaringAegisTest extends BaseCardTest {

    private void castAndResolve(UUID enchantTargetId, UUID tapTargetId) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new GlaringAegis()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castEnchantment(player1, 0, List.of(enchantTargetId, tapTargetId));
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Glaring Aegis attaches, boosts its creature, and taps the ETB target")
    void attachesBoostsAndTaps() {
        Permanent enchantedCreature = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player2.getId()).add(enchantedCreature);
        Permanent tappedCreature = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player2.getId()).add(tappedCreature);

        castAndResolve(enchantedCreature.getId(), tappedCreature.getId());

        assertThat(tappedCreature.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, enchantedCreature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, enchantedCreature)).isEqualTo(5);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Glaring Aegis")
                        && p.isAttached()
                        && p.getAttachedTo().equals(enchantedCreature.getId()));
    }

    @Test
    @DisplayName("Glaring Aegis stops boosting when it leaves the battlefield")
    void boostStopsWhenRemoved() {
        Permanent creature = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(creature);
        Permanent aura = new Permanent(new GlaringAegis());
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(5);

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Glaring Aegis cannot target your own creature for its ETB tap")
    void cannotTargetOwnCreatureForTap() {
        Permanent enchantedCreature = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(enchantedCreature);
        Permanent ownCreature = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(ownCreature);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new GlaringAegis()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castEnchantment(
                player1, 0, List.of(enchantedCreature.getId(), ownCreature.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature an opponent controls");
    }
}
