package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CoalitionFlag.class, GrizzlyBears.class, ProdigalPyromancer.class, Shock.class})
class CoalitionFlagTest extends BaseCardTest {

    @Test
    void enchantedCreatureBecomesFlagbearer() {
        Permanent creature = addReady(player1, new GrizzlyBears());
        attachFlag(creature);

        assertThat(gqs.effectiveCreatureSubtypes(gd, creature)).contains(CardSubtype.FLAGBEARER);
    }

    @Test
    void opponentMustTargetFlagbearerWithShockWhenAble() {
        Permanent flagbearer = addReady(player1, new GrizzlyBears());
        attachFlag(flagbearer);
        Permanent otherCreature = addReady(player1, new GrizzlyBears());

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, otherCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Flagbearer");
        harness.castInstant(player2, 0, flagbearer.getId());
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void opponentMustTargetFlagbearerWithActivatedAbilityWhenAble() {
        Permanent flagbearer = addReady(player1, new GrizzlyBears());
        attachFlag(flagbearer);
        Permanent otherCreature = addReady(player1, new GrizzlyBears());
        Permanent pyromancer = addReady(player2, new ProdigalPyromancer());

        int pyromancerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(pyromancer);
        assertThatThrownBy(() -> harness.activateAbility(player2, pyromancerIndex, null, otherCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Flagbearer");
        harness.activateAbility(player2, pyromancerIndex, null, flagbearer.getId());
        assertThat(gd.stack).hasSize(1);
    }

    private Permanent addReady(Player player, Card card) {
        return addCreatureReady(player, card);
    }

    private void attachFlag(Permanent creature) {
        Permanent aura = new Permanent(new CoalitionFlag());
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);
    }
}
