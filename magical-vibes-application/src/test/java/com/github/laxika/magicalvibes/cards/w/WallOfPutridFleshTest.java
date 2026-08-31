package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.h.HolyStrength;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.SwordsToPlowshares;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WallOfPutridFlesh.class, HolyStrength.class, ProdigalPyromancer.class,
        Shock.class, SwordsToPlowshares.class})
class WallOfPutridFleshTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents damage from an enchanted creature")
    void preventsDamageFromEnchantedCreature() {
        Permanent wall = addCreatureReady(player2, new WallOfPutridFlesh());
        Permanent pyromancer = addCreatureReady(player1, new ProdigalPyromancer());
        attachHolyStrength(pyromancer);

        harness.activateAbility(player1, 0, null, wall.getId());
        harness.passBothPriorities();

        assertThat(wall.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Does not prevent damage from an unenchanted creature")
    void doesNotPreventDamageFromUnenchantedCreature() {
        Permanent wall = addCreatureReady(player2, new WallOfPutridFlesh());
        addCreatureReady(player1, new ProdigalPyromancer());

        harness.activateAbility(player1, 0, null, wall.getId());
        harness.passBothPriorities();

        assertThat(wall.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not prevent damage from a noncreature source")
    void doesNotPreventDamageFromNoncreatureSource() {
        Permanent wall = addCreatureReady(player2, new WallOfPutridFlesh());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, wall.getId());
        harness.passBothPriorities();

        assertThat(wall.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot be targeted by a white spell")
    void cannotBeTargetedByWhiteSpell() {
        Permanent wall = addCreatureReady(player2, new WallOfPutridFlesh());
        harness.setHand(player1, List.of(new SwordsToPlowshares()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, wall.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from white");
    }

    private void attachHolyStrength(Permanent creature) {
        Permanent aura = new Permanent(new HolyStrength());
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);
    }
}
