package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GoblinSharpshooter;
import com.github.laxika.magicalvibes.cards.g.GlorySeeker;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AbsorbingManAndTitania.class, GoblinSharpshooter.class, GlorySeeker.class, Shock.class})
class AbsorbingManAndTitaniaTest extends BaseCardTest {

    @Test
    @DisplayName("Doubles damage dealt by a creature you control")
    void doublesControlledCreatureDamage() {
        harness.addToBattlefield(player1, new AbsorbingManAndTitania());
        addCreatureReady(player1, new GoblinSharpshooter());
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 1, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Doubles combat damage dealt by a creature you control")
    void doublesControlledCreatureCombatDamage() {
        harness.addToBattlefield(player1, new AbsorbingManAndTitania());
        addCreatureReady(player1, new GlorySeeker());
        harness.setLife(player2, 20);

        declareAttackers(List.of(1));

        assertThat(gd.getLife(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Does not double damage from a noncreature source")
    void doesNotDoubleNoncreatureDamage() {
        harness.addToBattlefield(player1, new AbsorbingManAndTitania());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLife(player2, 20);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Does not double damage dealt by an opponent's creature")
    void doesNotDoubleOpponentsCreatureDamage() {
        harness.addToBattlefield(player1, new AbsorbingManAndTitania());
        Permanent attacker = addCreatureReady(player2, new GoblinSharpshooter());
        attacker.setAttacking(true);
        harness.setLife(player1, 20);

        prepareDeclareBlockers(player2);
        gs.declareBlockers(gd, player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
    }
}
