package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.w.WildJhovall;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GenestealerLocus.class, WildJhovall.class})
class GenestealerLocusTest extends BaseCardTest {

    @Test
    @DisplayName("A creature attacking the Locus controller gets -1/-0")
    void weakensCreatureAttackingController() {
        addCreatureReady(player1, new GenestealerLocus());
        Permanent attacker = addCreatureReady(player2, new WildJhovall());

        declareAttackers(player2, List.of(0));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(3);
    }

    @Test
    @DisplayName("A creature attacking an opponent of the Locus controller gets +0/+1")
    void strengthensCreatureAttackingOpponent() {
        addCreatureReady(player1, new GenestealerLocus());
        Permanent attacker = addCreatureReady(player1, new WildJhovall());

        declareAttackers(player1, List.of(1));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(4);
    }

    @Test
    @DisplayName("Attacking the Locus controller does not receive the opponent bonus")
    void doesNotStrengthenCreatureAttackingController() {
        addCreatureReady(player1, new GenestealerLocus());
        Permanent attacker = addCreatureReady(player2, new WildJhovall());

        declareAttackers(player2, List.of(0));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(3);
    }

    @Test
    @DisplayName("Attacking an opponent does not receive the controller debuff")
    void doesNotWeakenCreatureAttackingOpponent() {
        addCreatureReady(player1, new GenestealerLocus());
        Permanent attacker = addCreatureReady(player1, new WildJhovall());

        declareAttackers(player1, List.of(1));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(4);
    }
}
