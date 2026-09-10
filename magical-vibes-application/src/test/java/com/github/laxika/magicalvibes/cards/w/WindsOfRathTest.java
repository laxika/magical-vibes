package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.cards.s.SkyshroudTroll;
import com.github.laxika.magicalvibes.cards.s.SoltariFootSoldier;
import com.github.laxika.magicalvibes.cards.s.StaticOrb;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@CardUsed({
        WindsOfRath.class, Pacifism.class, SoltariFootSoldier.class, SkyshroudTroll.class, StaticOrb.class
})
class WindsOfRathTest extends BaseCardTest {

    private void castWinds() {
        harness.castFromHand(player1, new WindsOfRath(), "{3}{W}{W}");
        harness.passBothPriorities();
    }

    private void enchant(Permanent host) {
        Permanent aura = new Permanent(new Pacifism());
        aura.setAttachedTo(host.getId());
        harness.getGameData().playerBattlefields.get(player1.getId()).add(aura);
    }

    @Test
    @DisplayName("Destroys unenchanted creatures on both sides")
    void destroysUnenchantedCreatures() {
        harness.addToBattlefield(player1, new SoltariFootSoldier());
        harness.addToBattlefield(player2, new SoltariFootSoldier());

        castWinds();

        harness.assertInGraveyard(player1, "Soltari Foot Soldier");
        harness.assertInGraveyard(player2, "Soltari Foot Soldier");
    }

    @Test
    @DisplayName("Enchanted creatures survive")
    void enchantedCreaturesSurvive() {
        Permanent enchanted = harness.addToBattlefieldAndReturn(player2, new SoltariFootSoldier());
        enchant(enchanted);
        harness.addToBattlefield(player2, new SkyshroudTroll());

        castWinds();

        harness.assertOnBattlefield(player2, "Soltari Foot Soldier");
        harness.assertInGraveyard(player2, "Skyshroud Troll");
    }

    @Test
    @DisplayName("Destroyed creatures can't be regenerated")
    void cannotBeRegenerated() {
        Permanent troll = harness.addToBattlefieldAndReturn(player2, new SkyshroudTroll());
        troll.setRegenerationShield(1);

        castWinds();

        harness.assertInGraveyard(player2, "Skyshroud Troll");
    }

    @Test
    @DisplayName("Leaves noncreature permanents untouched")
    void leavesNoncreaturePermanentsUntouched() {
        harness.addToBattlefield(player1, new StaticOrb());
        harness.addToBattlefield(player2, new SoltariFootSoldier());

        castWinds();

        harness.assertOnBattlefield(player1, "Static Orb");
        harness.assertInGraveyard(player2, "Soltari Foot Soldier");
    }

    @Test
    @DisplayName("Resolves when no creatures are on the battlefield")
    void resolvesWithoutCreatures() {
        castWinds();

        harness.assertInGraveyard(player1, "Winds of Rath");
    }
}
