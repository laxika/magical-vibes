package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CommandingPresence.class, GrizzlyBears.class})
class CommandingPresenceTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature gets +2/+2 and first strike")
    void boostsEnchantedCreatureAndGrantsFirstStrike() {
        Permanent creature = addEnchantedCreature();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Creates a Soldier token when the enchanted creature deals combat damage to a player")
    void createsSoldierOnCombatDamage() {
        Permanent creature = addEnchantedCreature();
        creature.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
        assertThat(findPermanents(player1, "Soldier")).hasSize(1);
    }

    @Test
    @DisplayName("Does not create a Soldier token when the enchanted creature deals no combat damage to a player")
    void doesNotCreateSoldierWhenBlocked() {
        Permanent creature = addEnchantedCreature();
        creature.setAttacking(true);

        Permanent blocker = addReadyCreature(player2);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(findPermanents(player1, "Soldier")).isEmpty();
    }

    private Permanent addEnchantedCreature() {
        Permanent creature = addReadyCreature(player1);
        Permanent aura = new Permanent(new CommandingPresence());
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);
        return creature;
    }

    private Permanent addReadyCreature(com.github.laxika.magicalvibes.model.Player player) {
        Permanent creature = new Permanent(new GrizzlyBears());
        creature.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(creature);
        return creature;
    }
}
