package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ScepterOfCelebration.class, GrizzlyBears.class})
class ScepterOfCelebrationTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +2/+0 and trample")
    void equippedCreatureGetsBoostAndTrample() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent scepter = addScepterReady(player1);
        scepter.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Equipped creature creates Citizens equal to combat damage dealt to a player")
    void combatDamageToPlayerCreatesCitizensEqualToDamage() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent scepter = addScepterReady(player1);
        scepter.setAttachedTo(creature.getId());
        creature.setAttacking(true);
        creature.setAttackTarget(player2.getId());

        resolveCombat();

        assertThat(findPermanents(player1, "Citizen")).hasSize(4);
    }

    @Test
    @DisplayName("Combat damage dealt only to a creature does not create Citizens")
    void combatDamageToCreatureDoesNotCreateCitizens() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent scepter = addScepterReady(player1);
        scepter.setAttachedTo(creature.getId());
        creature.setAttacking(true);
        creature.setAttackTarget(player2.getId());

        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        assertThat(findPermanents(player1, "Citizen")).isEmpty();
    }

    private Permanent addScepterReady(Player player) {
        return addReadyPermanent(player, new ScepterOfCelebration());
    }

    private Permanent addReadyPermanent(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
