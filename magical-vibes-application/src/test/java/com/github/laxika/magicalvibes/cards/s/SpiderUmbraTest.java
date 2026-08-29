package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SpiderUmbraTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature gets +1/+1 and reach")
    void boostsEnchantedCreatureAndGrantsReach() {
        Permanent creature = addReadyCreature(player1);
        attachUmbra(creature);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.REACH)).isTrue();
    }

    @Test
    @DisplayName("Umbra armor saves an enchanted creature from lethal damage and destroys the Aura")
    void savesFromLethalDamage() {
        Permanent creature = addReadyCreature(player1);
        Permanent aura = attachUmbra(creature);
        creature.setMarkedDamage(3);

        harness.runStateBasedActions();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Spider Umbra");
        assertThat(creature.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(aura);
    }

    @Test
    @DisplayName("Umbra armor saves an enchanted creature from a destroy effect")
    void savesFromDestroyEffect() {
        Permanent creature = addReadyCreature(player1);
        attachUmbra(creature);

        harness.setHand(player2, List.of(new DoomBlade()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, creature.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Spider Umbra");
    }

    private Permanent addReadyCreature(com.github.laxika.magicalvibes.model.Player player) {
        Permanent creature = new Permanent(new GrizzlyBears());
        creature.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(creature);
        return creature;
    }

    private Permanent attachUmbra(Permanent creature) {
        Permanent aura = new Permanent(new SpiderUmbra());
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);
        return aura;
    }
}
