package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PollenbrightWings.class, GrizzlyBears.class})
class PollenbrightWingsTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature has flying")
    void enchantedCreatureHasFlying() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        attachWings(creature);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Combat damage creates that many Saproling tokens")
    void combatDamageCreatesSaprolings() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        attachWings(creature);
        creature.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(findPermanents(player1, "Saproling")).hasSize(2);
    }

    private Permanent addCreatureReady(com.github.laxika.magicalvibes.model.Player player,
                                       GrizzlyBears card) {
        Permanent creature = new Permanent(card);
        creature.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(creature);
        return creature;
    }

    private void attachWings(Permanent creature) {
        Permanent aura = new Permanent(new PollenbrightWings());
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);
    }
}
