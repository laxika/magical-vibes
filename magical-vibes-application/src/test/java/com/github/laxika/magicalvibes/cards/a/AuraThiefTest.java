package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyStrength;
import com.github.laxika.magicalvibes.cards.r.RuleOfLaw;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AuraThiefTest extends BaseCardTest {

    @Test
    @DisplayName("When Aura Thief dies, its controller gains control of all enchantments")
    void gainsControlOfAllEnchantmentsWhenItDies() {
        Permanent auraThief = addCreatureReady(player1, new AuraThief());
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        Permanent ruleOfLaw = new Permanent(new RuleOfLaw());
        Permanent holyStrength = new Permanent(new HolyStrength());
        holyStrength.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player2.getId()).add(ruleOfLaw);
        gd.playerBattlefields.get(player2.getId()).add(holyStrength);

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, auraThief.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .contains(ruleOfLaw, holyStrength);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .contains(creature)
                .doesNotContain(ruleOfLaw, holyStrength);
        assertThat(holyStrength.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Aura Thief does not gain control of creatures")
    void doesNotGainControlOfCreatures() {
        Permanent auraThief = addCreatureReady(player1, new AuraThief());
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, auraThief.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .contains(creature);
    }
}
