package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.b.BoundaryLandsRanger;
import com.github.laxika.magicalvibes.cards.c.CoopedUp;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ArchonOfTheWildRose.class, BoundaryLandsRanger.class, CoopedUp.class})
class ArchonOfTheWildRoseTest extends BaseCardTest {

    @Test
    @DisplayName("Other creatures you control enchanted by your Auras become 4/4 flyers")
    void boostsCreaturesEnchantedByYourAuras() {
        addCreatureReady(player1, new ArchonOfTheWildRose());
        Permanent ownCreature = addCreatureReady(player1, new BoundaryLandsRanger());
        Permanent opposingCreature = addCreatureReady(player2, new BoundaryLandsRanger());

        attachAura(player1, ownCreature);
        attachAura(player2, opposingCreature);

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.FLYING)).isTrue();
        assertThat(gqs.getEffectivePower(gd, opposingCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opposingCreature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, opposingCreature, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("An Aura controlled by an opponent does not enable the ability")
    void doesNotBoostCreatureEnchantedByOpponentsAura() {
        addCreatureReady(player1, new ArchonOfTheWildRose());
        Permanent ownCreature = addCreatureReady(player1, new BoundaryLandsRanger());

        attachAura(player2, ownCreature);

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.FLYING)).isFalse();
    }

    @Test
    void doesNotBoostUnenchantedCreatureOrOpponentsCreatureWithYourAura() {
        addCreatureReady(player1, new ArchonOfTheWildRose());
        Permanent ownCreature = addCreatureReady(player1, new BoundaryLandsRanger());
        Permanent opposingCreature = addCreatureReady(player2, new BoundaryLandsRanger());
        attachAura(player1, opposingCreature);

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.FLYING)).isFalse();
        assertThat(gqs.getEffectivePower(gd, opposingCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opposingCreature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, opposingCreature, Keyword.FLYING)).isFalse();
    }

    @Test
    void bonusesEndWhenLastControlledAuraLeaves() {
        addCreatureReady(player1, new ArchonOfTheWildRose());
        Permanent creature = addCreatureReady(player1, new BoundaryLandsRanger());
        Permanent aura = attachAura(player1, creature);
        attachAura(player2, creature);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isTrue();
        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isFalse();
    }

    @Test
    void bonusesEndWhenArchonLeaves() {
        Permanent archon = addCreatureReady(player1, new ArchonOfTheWildRose());
        Permanent creature = addCreatureReady(player1, new BoundaryLandsRanger());
        attachAura(player1, creature);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isTrue();
        gd.playerBattlefields.get(player1.getId()).remove(archon);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isFalse();
    }

    @Test
    void countersApplyOnTopOfBaseStatsAndMultipleAurasDoNotStack() {
        addCreatureReady(player1, new ArchonOfTheWildRose());
        Permanent creature = addCreatureReady(player1, new BoundaryLandsRanger());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent firstAura = attachAura(player1, creature);
        attachAura(player1, creature);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isTrue();
        gd.playerBattlefields.get(player1.getId()).remove(firstAura);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isTrue();
    }

    private Permanent attachAura(com.github.laxika.magicalvibes.model.Player controller, Permanent host) {
        Permanent aura = harness.addToBattlefieldAndReturn(controller, new CoopedUp());
        aura.setAttachedTo(host.getId());
        return aura;
    }
}
