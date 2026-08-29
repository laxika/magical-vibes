package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.ScatheZombies;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MarduRunemarkTest extends BaseCardTest {

    @Test
    void enchantedCreatureGetsPlusTwoPlusTwo() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        attach(player1, creature);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
    }

    @Test
    void enchantedCreatureHasFirstStrikeWhileAuraControllerControlsWhitePermanent() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        attach(player1, creature);
        addCreatureReady(player1, new EliteVanguard());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    void enchantedCreatureHasFirstStrikeWhileAuraControllerControlsBlackPermanent() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        attach(player1, creature);
        addCreatureReady(player1, new ScatheZombies());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    void firstStrikeRequiresAuraControllerToControlQualifyingPermanent() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        attach(player1, creature);
        addCreatureReady(player2, new EliteVanguard());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    void bonusesDisappearWhenAuraLeaves() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent aura = attach(player1, creature);
        addCreatureReady(player1, new EliteVanguard());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new MarduRunemark()));
        harness.addMana(player1, ManaColor.RED, 3);

        Permanent artifact = findPermanent(player1, "Fountain of Youth");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private Permanent attach(Player controller, Permanent creature) {
        Permanent aura = new Permanent(new MarduRunemark());
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(controller.getId()).add(aura);
        return aura;
    }
}
