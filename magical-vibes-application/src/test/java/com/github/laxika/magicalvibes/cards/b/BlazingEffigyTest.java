package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.c.ChainLightning;
import com.github.laxika.magicalvibes.cards.g.GiantStrength;
import com.github.laxika.magicalvibes.cards.m.MossMonster;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BlazingEffigy.class, ChainLightning.class, GiantStrength.class, MossMonster.class})
class BlazingEffigyTest extends BaseCardTest {

    @Test
    @DisplayName("Death trigger deals three damage to target creature")
    void deathTriggerDealsThreeDamage() {
        Permanent effigy = addCreatureReady(player1, new BlazingEffigy());
        Permanent target = addCreatureReady(player2, new MossMonster());
        harness.setHand(player1, List.of(new ChainLightning()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castSorcery(player1, 0, effigy.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("Death trigger adds damage dealt by other Blazing Effigies and ignores other sources")
    void deathTriggerCountsDamageFromOtherNamedSources() {
        Permanent otherEffigy = addCreatureReady(player1, new BlazingEffigy());
        Permanent dyingEffigy = addCreatureReady(player2, new BlazingEffigy());
        Permanent target = addCreatureReady(player2, new MossMonster());

        harness.setHand(player1, List.of(new GiantStrength(), new GiantStrength()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castEnchantment(player1, 0, otherEffigy.getId());
        harness.passBothPriorities();

        harness.castEnchantment(player1, 0, dyingEffigy.getId());
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new ChainLightning()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castSorcery(player1, 0, dyingEffigy.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        declareAttackersAndPrepareBlockers(List.of(0));
        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(dyingEffigy);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIndex, 0)));
        resolveCombat();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player2, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(5);
    }
}
