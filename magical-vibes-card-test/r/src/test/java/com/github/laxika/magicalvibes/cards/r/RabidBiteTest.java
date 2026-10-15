package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.c.ColossalDreadmaw;
import com.github.laxika.magicalvibes.cards.c.ChildOfNight;
import com.github.laxika.magicalvibes.cards.d.DaggerbackBasilisk;
import com.github.laxika.magicalvibes.cards.g.GreenwoodSentinel;
import com.github.laxika.magicalvibes.cards.d.Disperse;
import com.github.laxika.magicalvibes.cards.t.TitanicGrowth;
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

@CardUsed({RabidBite.class, GreenwoodSentinel.class, RustwingFalcon.class, ColossalDreadmaw.class,
        Disperse.class, TitanicGrowth.class, ChildOfNight.class, DaggerbackBasilisk.class})
class RabidBiteTest extends BaseCardTest {

    @Test
    @DisplayName("Controlled creature deals damage equal to its power to an opposing creature")
    void controlledCreatureDealsPowerDamageToOpposingCreature() {
        harness.addToBattlefield(player1, new GreenwoodSentinel());
        harness.addToBattlefield(player2, new RustwingFalcon());
        harness.setHand(player1, List.of(new RabidBite()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        UUID bearId = harness.getPermanentId(player1, "Greenwood Sentinel");
        UUID elvesId = harness.getPermanentId(player2, "Rustwing Falcon");
        harness.castSorcery(player1, 0, List.of(bearId, elvesId));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Greenwood Sentinel");
        harness.assertNotOnBattlefield(player2, "Rustwing Falcon");
        harness.assertInGraveyard(player2, "Rustwing Falcon");
    }

    @Test
    @DisplayName("Damage is dealt without the controlled creature taking damage")
    void damageIsOneSided() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new GreenwoodSentinel());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ColossalDreadmaw());
        harness.setHand(player1, List.of(new RabidBite()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castSorcery(player1, 0, List.of(source.getId(), target.getId()));
        harness.passBothPriorities();

        assertThat(source.getMarkedDamage()).isZero();
        assertThat(target.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Targets must be a controlled creature and an opposing creature")
    void enforcesTargetRestrictions() {
        Permanent ownBear = harness.addToBattlefieldAndReturn(player1, new GreenwoodSentinel());
        Permanent ownOtherBear = harness.addToBattlefieldAndReturn(player1, new GreenwoodSentinel());
        harness.setHand(player1, List.of(new RabidBite()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        UUID firstId = ownBear.getId();
        UUID secondId = ownOtherBear.getId();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(firstId, secondId)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("don't control");
    }

    @Test
    void opposingCreatureCannotBeTheDamageSource() {
        Permanent source = harness.addToBattlefieldAndReturn(player2, new GreenwoodSentinel());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RustwingFalcon());
        harness.setHand(player1, List.of(new RabidBite()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(source.getId(), target.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotCastWithoutAnOpposingCreatureTarget() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new GreenwoodSentinel());
        harness.setHand(player1, List.of(new RabidBite()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(source.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void usesPowerAtResolution() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new GreenwoodSentinel());
        harness.addToBattlefield(player2, new ColossalDreadmaw());
        UUID targetId = harness.getPermanentId(player2, "Colossal Dreadmaw");
        harness.setHand(player1, List.of(new RabidBite(), new TitanicGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castSorcery(player1, 0, List.of(source.getId(), targetId));
        harness.castInstant(player1, 0, source.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Colossal Dreadmaw");
        harness.assertOnBattlefield(player1, "Greenwood Sentinel");
        assertThat(source.getMarkedDamage()).isZero();
    }

    @Test
    void noDamageWhenSourceLeavesBeforeResolution() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new GreenwoodSentinel());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ColossalDreadmaw());
        harness.setHand(player1, List.of(new RabidBite(), new Disperse()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castSorcery(player1, 0, List.of(source.getId(), target.getId()));
        harness.castInstant(player1, 0, source.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInHand(player1, "Greenwood Sentinel");
        harness.assertOnBattlefield(player2, "Colossal Dreadmaw");
        assertThat(target.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player1, "Rabid Bite");
    }

    @Test
    void noDamageWhenVictimLeavesBeforeResolution() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new GreenwoodSentinel());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ColossalDreadmaw());
        harness.setHand(player1, List.of(new RabidBite(), new Disperse()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castSorcery(player1, 0, List.of(source.getId(), target.getId()));
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInHand(player2, "Colossal Dreadmaw");
        harness.assertOnBattlefield(player1, "Greenwood Sentinel");
        assertThat(source.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player1, "Rabid Bite");
    }

    @Test
    void damageUsesTheCreaturesLifelink() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new ChildOfNight());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ColossalDreadmaw());
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new RabidBite()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castSorcery(player1, 0, List.of(source.getId(), target.getId()));
        harness.passBothPriorities();

        harness.assertLife(player1, 22);
        assertThat(target.getMarkedDamage()).isEqualTo(2);
        assertThat(source.getMarkedDamage()).isZero();
    }

    @Test
    void damageUsesTheCreaturesDeathtouch() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new DaggerbackBasilisk());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ColossalDreadmaw());
        harness.setHand(player1, List.of(new RabidBite()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castSorcery(player1, 0, List.of(source.getId(), target.getId()));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Colossal Dreadmaw");
        harness.assertOnBattlefield(player1, "Daggerback Basilisk");
        assertThat(source.getMarkedDamage()).isZero();
    }
}
