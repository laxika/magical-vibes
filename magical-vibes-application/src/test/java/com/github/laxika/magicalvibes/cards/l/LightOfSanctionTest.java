package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.c.CarvenCaryatid;
import com.github.laxika.magicalvibes.cards.c.Char;
import com.github.laxika.magicalvibes.cards.v.ViashinoFangtail;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LightOfSanction.class, CarvenCaryatid.class, Char.class, ViashinoFangtail.class})
class LightOfSanctionTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents damage from your spell to a creature you control but not to you")
    void preventsDamageFromYourSpellToYourCreature() {
        harness.addToBattlefield(player1, new LightOfSanction());
        Permanent creature = addCreatureReady(player1, new CarvenCaryatid());

        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new Char()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(creature.getMarkedDamage()).isZero();
        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("Does not prevent damage from an opponent's source")
    void doesNotPreventDamageFromOpponentsSource() {
        harness.addToBattlefield(player1, new LightOfSanction());
        Permanent creature = addCreatureReady(player1, new CarvenCaryatid());
        Permanent fangtail = addCreatureReady(player2, new ViashinoFangtail());

        harness.forceActivePlayer(player2);
        harness.activateAbility(player2,
                gd.playerBattlefields.get(player2.getId()).indexOf(fangtail), null, creature.getId());
        harness.passBothPriorities();

        assertThat(creature.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Prevents damage from your permanent source to a creature you control")
    void preventsDamageFromYourPermanentSourceToYourCreature() {
        harness.addToBattlefield(player1, new LightOfSanction());
        Permanent creature = addCreatureReady(player1, new CarvenCaryatid());
        Permanent fangtail = addCreatureReady(player1, new ViashinoFangtail());

        harness.forceActivePlayer(player1);
        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(fangtail), null, creature.getId());
        harness.passBothPriorities();

        assertThat(creature.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Does not prevent your damage to an opponent's creature")
    void doesNotPreventYourDamageToOpponentsCreature() {
        harness.addToBattlefield(player1, new LightOfSanction());
        Permanent creature = addCreatureReady(player2, new CarvenCaryatid());
        Permanent fangtail = addCreatureReady(player1, new ViashinoFangtail());

        harness.forceActivePlayer(player1);
        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(fangtail), null, creature.getId());
        harness.passBothPriorities();

        assertThat(creature.getMarkedDamage()).isEqualTo(1);
    }
}
