package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.AuriokGlaivemaster;
import com.github.laxika.magicalvibes.cards.h.HoverguardObserver;
import com.github.laxika.magicalvibes.cards.h.Hallow;
import com.github.laxika.magicalvibes.cards.n.NomadsEnKor;
import com.github.laxika.magicalvibes.cards.t.TangleSpider;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Flamebreak.class, TangleSpider.class, HoverguardObserver.class, AuriokGlaivemaster.class,
        Hallow.class, NomadsEnKor.class})
class FlamebreakTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 3 damage to each player and each creature without flying")
    void damagesPlayersAndNonflyingCreatures() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent ownGroundCreature = harness.addToBattlefieldAndReturn(player1, new TangleSpider());
        Permanent ownFlyingCreature = harness.addToBattlefieldAndReturn(player1, new HoverguardObserver());
        Permanent groundCreature = harness.addToBattlefieldAndReturn(player2, new TangleSpider());
        Permanent flyingCreature = harness.addToBattlefieldAndReturn(player2, new HoverguardObserver());

        castFlamebreak();

        harness.assertLife(player1, 17);
        harness.assertLife(player2, 17);
        assertThat(ownGroundCreature.getMarkedDamage()).isEqualTo(3);
        assertThat(ownFlyingCreature.getMarkedDamage()).isZero();
        assertThat(groundCreature.getMarkedDamage()).isEqualTo(3);
        assertThat(flyingCreature.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Creatures dealt damage by Flamebreak can't be regenerated this turn")
    void damagedCreaturesCannotRegenerate() {
        Permanent creature = addCreatureReady(player2, new AuriokGlaivemaster());
        creature.setRegenerationShield(1);

        castFlamebreak();

        harness.assertNotOnBattlefield(player2, "Auriok Glaivemaster");
        harness.assertInGraveyard(player2, "Auriok Glaivemaster");
    }

    @Test
    @DisplayName("Prevented damage does not stop regeneration")
    void preventedDamageDoesNotStopRegeneration() {
        Permanent creature = addCreatureReady(player2, new AuriokGlaivemaster());
        creature.setRegenerationShield(1);
        Flamebreak flamebreak = new Flamebreak();
        harness.castFromHand(player1, flamebreak, "{R}{R}{R}");
        harness.setHand(player2, List.of(new Hallow()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.castInstant(player2, 0, flamebreak.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Auriok Glaivemaster");
        assertThat(creature.getMarkedDamage()).isZero();
        assertThat(creature.isCantRegenerateThisTurn()).isFalse();
        assertThat(creature.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Flying creatures dealt redirected Flamebreak damage cannot regenerate")
    void redirectedDamageAlsoStopsRegeneration() {
        Permanent nomads = addCreatureReady(player1, new NomadsEnKor());
        Permanent flyer = addCreatureReady(player1, new HoverguardObserver());
        flyer.setRegenerationShield(1);
        for (int i = 0; i < 3; i++) {
            harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(nomads),
                    null, flyer.getId());
            harness.passBothPriorities();
        }

        castFlamebreak();

        harness.assertOnBattlefield(player1, "Nomads en-Kor");
        assertThat(nomads.getMarkedDamage()).isZero();
        harness.assertNotOnBattlefield(player1, "Hoverguard Observer");
        harness.assertInGraveyard(player1, "Hoverguard Observer");
    }

    private void castFlamebreak() {
        harness.castFromHand(player1, new Flamebreak(), "{R}{R}{R}");
        harness.passBothPriorities();
    }
}
