package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.c.ChandraHopesBeacon;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.InvasionOfVryn;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VibrantOutburst.class, AirElemental.class, GrizzlyBears.class, InvasionOfVryn.class, ChandraHopesBeacon.class})
class VibrantOutburstTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 3 damage to a creature and taps a different creature (damage target not tapped)")
    void damagesCreatureAndTapsAnother() {
        harness.addToBattlefield(player2, new AirElemental()); // damage target (survives)
        harness.addToBattlefield(player2, new GrizzlyBears()); // tap target
        harness.setHand(player1, List.of(new VibrantOutburst()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        UUID elementalId = harness.getPermanentId(player2, "Air Elemental");
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        harness.castAndResolveInstant(player1, 0, List.of(elementalId, bearsId));

        GameData gd = harness.getGameData();
        // Air Elemental (4/4) takes 3 damage and survives, and is NOT the tap target
        Permanent elemental = gqs.findPermanentById(gd, elementalId);
        assertThat(elemental.getMarkedDamage()).isEqualTo(3);
        assertThat(elemental.isTapped()).isFalse();
        // Grizzly Bears is tapped
        Permanent bears = gqs.findPermanentById(gd, bearsId);
        assertThat(bears.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Deals 3 damage to a player and taps a creature")
    void damagesPlayerAndTapsCreature() {
        harness.addToBattlefield(player2, new AirElemental());
        harness.setHand(player1, List.of(new VibrantOutburst()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        UUID elementalId = harness.getPermanentId(player2, "Air Elemental");

        harness.castAndResolveInstant(player1, 0, List.of(player2.getId(), elementalId));

        GameData gd = harness.getGameData();
        harness.assertLife(player2, 17);
        Permanent elemental = gqs.findPermanentById(gd, elementalId);
        assertThat(elemental.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Can deal 3 damage to its controller")
    void damagesController() {
        harness.setHand(player1, List.of(new VibrantOutburst()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, List.of(player1.getId()));

        harness.assertLife(player1, 17);
    }

    @Test
    @DisplayName("Resolves with only the damage target (tap is up to one)")
    void resolvesWithOnlyDamageTarget() {
        harness.addToBattlefield(player2, new AirElemental());
        harness.setHand(player1, List.of(new VibrantOutburst()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        UUID elementalId = harness.getPermanentId(player2, "Air Elemental");

        harness.castAndResolveInstant(player1, 0, List.of(elementalId));

        GameData gd = harness.getGameData();
        Permanent elemental = gqs.findPermanentById(gd, elementalId);
        assertThat(elemental.getMarkedDamage()).isEqualTo(3);
        assertThat(elemental.isTapped()).isFalse();
        harness.assertInGraveyard(player1, "Vibrant Outburst");
    }

    @Test
    @DisplayName("Cannot choose a player as the tap target")
    void cannotTapPlayer() {
        harness.addToBattlefield(player2, new AirElemental());
        harness.setHand(player1, List.of(new VibrantOutburst()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        UUID elementalId = harness.getPermanentId(player2, "Air Elemental");

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(elementalId, player2.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can choose the same creature for damage and tapping")
    void damagesAndTapsSameCreature() {
        Permanent elemental = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.setHand(player1, List.of(new VibrantOutburst()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, List.of(elemental.getId(), elemental.getId()));

        assertThat(elemental.getMarkedDamage()).isEqualTo(3);
        assertThat(elemental.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Deals damage to a battle and taps a creature")
    void damagesBattleAndTapsCreature() {
        Permanent battle = harness.addToBattlefieldAndReturn(player2, new InvasionOfVryn());
        battle.setCounterCount(CounterType.DEFENSE, 4);
        Permanent elemental = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.setHand(player1, List.of(new VibrantOutburst()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, List.of(battle.getId(), elemental.getId()));

        assertThat(battle.getCounterCount(CounterType.DEFENSE)).isEqualTo(1);
        assertThat(elemental.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Still taps the creature when the damage target leaves before resolution")
    void tapsCreatureWhenDamageTargetLeaves() {
        Permanent damageTarget = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        Permanent tapTarget = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new VibrantOutburst()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, List.of(damageTarget.getId(), tapTarget.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(damageTarget);
        gd.playerGraveyards.get(player2.getId()).add(damageTarget.getCard());
        harness.passBothPriorities();

        assertThat(tapTarget.isTapped()).isTrue();
        assertThat(tapTarget.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Deals 3 damage to a planeswalker without choosing a tap target")
    void damagesPlaneswalker() {
        Permanent chandra = harness.addToBattlefieldAndReturn(player2, new ChandraHopesBeacon());
        chandra.setCounterCount(CounterType.LOYALTY, 5);
        harness.setHand(player1, List.of(new VibrantOutburst()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, List.of(chandra.getId()));

        assertThat(chandra.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        assertThat(chandra.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Still deals damage when the tap target leaves before resolution")
    void damagesPlayerWhenTapTargetLeaves() {
        Permanent tapTarget = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new VibrantOutburst()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, List.of(player2.getId(), tapTarget.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(tapTarget);
        gd.playerGraveyards.get(player2.getId()).add(tapTarget.getCard());
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
    }
}
