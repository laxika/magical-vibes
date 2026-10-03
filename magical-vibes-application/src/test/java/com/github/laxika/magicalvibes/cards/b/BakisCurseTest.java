package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.c.Carapace;
import com.github.laxika.magicalvibes.cards.r.RysorianBadger;
import com.github.laxika.magicalvibes.cards.t.Torture;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BakisCurse.class, Carapace.class, RysorianBadger.class, Torture.class})
class BakisCurseTest extends BaseCardTest {

    private void attachAura(Player owner, Card aura, Permanent creature) {
        Permanent auraPerm = harness.addToBattlefieldAndReturn(owner, aura);
        auraPerm.setAttachedTo(creature.getId());
    }

    private void castCurse() {
        harness.castFromHand(player1, new BakisCurse(), "{2}{U}{U}");
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Deals 2 damage per Aura attached to that creature")
    void dealsTwoDamagePerAura() {
        Permanent oneAura = addCreatureReady(player1, new RysorianBadger());
        attachAura(player1, new Carapace(), oneAura);

        Permanent twoAuras = addCreatureReady(player2, new RysorianBadger());
        attachAura(player2, new Carapace(), twoAuras);
        attachAura(player2, new Carapace(), twoAuras);

        castCurse();

        assertThat(oneAura.getMarkedDamage()).isEqualTo(2);
        assertThat(twoAuras.getMarkedDamage()).isEqualTo(4);
    }

    @Test
    @DisplayName("Unenchanted creatures take no damage")
    void unenchantedCreatureTakesNoDamage() {
        Permanent bare = addCreatureReady(player2, new RysorianBadger());

        castCurse();

        assertThat(bare.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getId().equals(bare.getId()));
    }

    @Test
    @DisplayName("A creature dies when the Aura-scaled damage is lethal")
    void lethalDamageDestroysCreature() {
        Permanent creature = addCreatureReady(player2, new RysorianBadger());
        attachAura(player2, new Torture(), creature);
        attachAura(player2, new Torture(), creature);

        castCurse();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(creature.getId()));
    }

    @Test
    @DisplayName("Counts an Aura controlled by another player")
    void countsAuraControlledByAnotherPlayer() {
        Permanent creature = addCreatureReady(player2, new RysorianBadger());
        attachAura(player1, new Carapace(), creature);

        castCurse();

        assertThat(creature.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Counts Auras at resolution after an Aura is sacrificed in response")
    void sacrificedAuraDoesNotContributeDamage() {
        Permanent creature = addCreatureReady(player1, new RysorianBadger());
        attachAura(player1, new Carapace(), creature);

        harness.castFromHand(player1, new BakisCurse(), "{2}{U}{U}");
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(creature.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player1, "Rysorian Badger");
        harness.assertInGraveyard(player1, "Carapace");
        harness.assertInGraveyard(player1, "Baki's Curse");
    }

    @Test
    @DisplayName("Resolves on an empty battlefield without damaging players")
    void resolvesWithoutCreatures() {
        harness.setLife(player1, 17);
        harness.setLife(player2, 13);

        castCurse();

        harness.assertLife(player1, 17);
        harness.assertLife(player2, 13);
        harness.assertInGraveyard(player1, "Baki's Curse");
    }
}
