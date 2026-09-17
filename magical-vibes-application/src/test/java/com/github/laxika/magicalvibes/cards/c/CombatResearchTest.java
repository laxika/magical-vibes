package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IsamaruHoundOfKonda;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CombatResearch.class, GrizzlyBears.class, IsamaruHoundOfKonda.class, Shock.class})
class CombatResearchTest extends BaseCardTest {

    @Test
    @DisplayName("Legendary enchanted creature gets +1/+1")
    void legendaryCreatureGetsBoost() {
        Permanent creature = addCreatureReady(player1, new IsamaruHoundOfKonda());
        attachResearch(creature);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }

    @Test
    @DisplayName("Enchanted creature draws after dealing combat damage to a player")
    void enchantedCreatureDrawsOnCombatDamage() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        attachResearch(creature);
        harness.setHand(player1, List.of());

        declareAttackers(List.of(0));
        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Legendary enchanted creature has ward {1}")
    void legendaryCreatureHasWard() {
        Permanent creature = addCreatureReady(player1, new IsamaruHoundOfKonda());
        attachResearch(creature);
        castOpponentShock(creature);

        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Shock");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);
    }

    @Test
    @DisplayName("Nonlegendary enchanted creature does not have the legendary bonuses")
    void nonlegendaryCreatureDoesNotGetLegendaryBonuses() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        attachResearch(creature);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);

        castOpponentShock(creature);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(creature);
    }

    private void attachResearch(Permanent creature) {
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new CombatResearch());
        aura.setAttachedTo(creature.getId());
    }

    private void castOpponentShock(Permanent target) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, target.getId());
    }
}
