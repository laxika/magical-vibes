package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.p.PlagueStinger;
import com.github.laxika.magicalvibes.cards.w.WallOfSwords;
import com.github.laxika.magicalvibes.model.CounterType;
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

@CardUsed({SelfDestruct.class, GrizzlyBears.class, HillGiant.class, WallOfSwords.class,
        StiltzkinMoogleMerchant.class, PlagueStinger.class})
class SelfDestructTest extends BaseCardTest {

    @Test
    @DisplayName("The creature deals its power to another target and to itself")
    void dealsPowerDamageToOtherTargetAndItself() {
        harness.addToBattlefield(player1, new HillGiant());
        harness.addToBattlefield(player2, new WallOfSwords());
        harness.setHand(player1, List.of(new SelfDestruct()));
        harness.addMana(player1, ManaColor.RED, 2);

        UUID sourceId = harness.getPermanentId(player1, "Hill Giant");
        UUID targetId = harness.getPermanentId(player2, "Wall of Swords");
        harness.castAndResolveInstant(player1, 0, List.of(sourceId, targetId));

        harness.assertInGraveyard(player1, "Hill Giant");
        Permanent wall = findPermanent(player2, "Wall of Swords");
        assertThat(wall.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("The second target may be a player")
    void dealsPowerDamageToPlayerAndItself() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new SelfDestruct()));
        harness.addMana(player1, ManaColor.RED, 2);

        UUID sourceId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castAndResolveInstant(player1, 0, List.of(sourceId, player2.getId()));

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("The second target must be different from the source creature")
    void cannotTargetSourceCreatureAsOtherTarget() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new SelfDestruct()));
        harness.addMana(player1, ManaColor.RED, 2);

        UUID sourceId = harness.getPermanentId(player1, "Grizzly Bears");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(sourceId, sourceId)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("different");
    }

    @Test
    @DisplayName("Lifelink gains life for damage to both the other target and itself")
    void gainsLifeForBothDamageRecipients() {
        harness.addToBattlefield(player1, new StiltzkinMoogleMerchant());
        harness.setHand(player1, List.of(new SelfDestruct()));
        harness.addMana(player1, ManaColor.RED, 2);

        UUID sourceId = harness.getPermanentId(player1, "Stiltzkin, Moogle Merchant");
        harness.castAndResolveInstant(player1, 0, List.of(sourceId, player2.getId()));

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 19);
        assertThat(findPermanent(player1, "Stiltzkin, Moogle Merchant").getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Infect self-damage must not reduce X for damage to the other target")
    void calculatesPowerOnlyOnceWithInfect() {
        harness.addToBattlefield(player1, new PlagueStinger());
        harness.setHand(player1, List.of(new SelfDestruct()));
        harness.addMana(player1, ManaColor.RED, 2);

        UUID sourceId = harness.getPermanentId(player1, "Plague Stinger");
        harness.castAndResolveInstant(player1, 0, List.of(sourceId, player2.getId()));

        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(1);
        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Plague Stinger");
    }

    @Test
    @DisplayName("The creature still damages itself when the other target leaves the battlefield")
    void stillDealsSelfDamageWithMissingOtherTarget() {
        harness.addToBattlefield(player1, new HillGiant());
        harness.addToBattlefield(player2, new WallOfSwords());
        harness.setHand(player1, List.of(new SelfDestruct()));
        harness.addMana(player1, ManaColor.RED, 2);

        UUID sourceId = harness.getPermanentId(player1, "Hill Giant");
        UUID targetId = harness.getPermanentId(player2, "Wall of Swords");
        harness.castInstant(player1, 0, List.of(sourceId, targetId));
        gd.playerBattlefields.get(player2.getId()).removeIf(p -> p.getId().equals(targetId));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Hill Giant");
    }

    @Test
    @DisplayName("The creature deals no damage if it changes controllers before resolution")
    void dealsNoDamageWhenSourceControlChanges() {
        harness.addToBattlefield(player1, new StiltzkinMoogleMerchant());
        harness.setHand(player1, List.of(new SelfDestruct()));
        harness.addMana(player1, ManaColor.RED, 2);

        Permanent source = findPermanent(player1, "Stiltzkin, Moogle Merchant");
        harness.castInstant(player1, 0, List.of(source.getId(), player2.getId()));
        gd.playerBattlefields.get(player1.getId()).remove(source);
        gd.playerBattlefields.get(player2.getId()).add(source);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        assertThat(source.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("The controller may choose themselves as the other target")
    void canDealDamageToController() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new SelfDestruct()));
        harness.addMana(player1, ManaColor.RED, 2);

        UUID sourceId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castAndResolveInstant(player1, 0, List.of(sourceId, player1.getId()));

        harness.assertLife(player1, 18);
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("X uses the creature's power at resolution rather than when cast")
    void usesPowerAtResolution() {
        harness.addToBattlefield(player1, new HillGiant());
        harness.setHand(player1, List.of(new SelfDestruct()));
        harness.addMana(player1, ManaColor.RED, 2);

        Permanent source = findPermanent(player1, "Hill Giant");
        harness.castInstant(player1, 0, List.of(source.getId(), player2.getId()));
        source.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.passBothPriorities();

        harness.assertLife(player2, 15);
        harness.assertInGraveyard(player1, "Hill Giant");
    }

    @Test
    @DisplayName("The damage source must be a creature controlled by the caster")
    void cannotChooseOpponentCreatureAsSource() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SelfDestruct()));
        harness.addMana(player1, ManaColor.RED, 2);

        UUID sourceId = harness.getPermanentId(player2, "Grizzly Bears");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(sourceId, player2.getId())))
                .isInstanceOf(IllegalStateException.class);
    }
}
