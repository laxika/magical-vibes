package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.b.BlackKnight;
import com.github.laxika.magicalvibes.cards.c.ChoMannoRevolutionary;
import com.github.laxika.magicalvibes.cards.s.SerraAvatar;
import com.github.laxika.magicalvibes.cards.v.VampireNighthawk;
import com.github.laxika.magicalvibes.cards.v.VineTrellis;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.j.Justice;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WaveOfReckoning.class, WallOfSwords.class, GiantSpider.class, HillGiant.class, Justice.class,
        BlackKnight.class, ChoMannoRevolutionary.class, WallOfDistortion.class, VineTrellis.class,
        VampireNighthawk.class, SerraAvatar.class})
class WaveOfReckoningTest extends BaseCardTest {

    @Test
    @DisplayName("Each creature deals damage to itself equal to its power")
    void eachCreatureDealsItsPowerToItself() {
        Permanent wall = harness.addToBattlefieldAndReturn(player1, new WallOfSwords());
        Permanent spider = harness.addToBattlefieldAndReturn(player2, new GiantSpider());

        harness.castFromHand(player1, new WaveOfReckoning(), "{4}{W}");
        harness.passBothPriorities();

        assertThat(wall.getMarkedDamage()).isEqualTo(3);
        assertThat(spider.getMarkedDamage()).isEqualTo(2);
        harness.assertOnBattlefield(player1, "Wall of Swords");
        harness.assertOnBattlefield(player2, "Giant Spider");
    }

    @Test
    @DisplayName("Each creature is the source of its own damage")
    void eachCreatureIsItsOwnDamageSource() {
        harness.addToBattlefield(player1, new HillGiant());
        harness.addToBattlefield(player2, new Justice());
        harness.setLife(player1, 20);

        harness.castFromHand(player1, new WaveOfReckoning(), "{4}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 17);
    }

    @Test
    @DisplayName("Lethal self-damage puts the creature into its owner's graveyard")
    void lethalSelfDamageDestroysCreature() {
        harness.addToBattlefield(player1, new HillGiant());

        harness.castFromHand(player1, new WaveOfReckoning(), "{4}{W}");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Hill Giant");
        harness.assertInGraveyard(player1, "Hill Giant");
    }

    @Test
    @DisplayName("Damage prevention protects Cho-Manno without protecting other creatures")
    void damagePreventionAppliesToSelfDamage() {
        Permanent choManno = harness.addToBattlefieldAndReturn(player1, new ChoMannoRevolutionary());
        Permanent wall = harness.addToBattlefieldAndReturn(player2, new WallOfDistortion());

        harness.castFromHand(player1, new WaveOfReckoning(), "{4}{W}");
        harness.passBothPriorities();

        assertThat(choManno.getMarkedDamage()).isZero();
        assertThat(wall.getMarkedDamage()).isEqualTo(1);
        harness.assertOnBattlefield(player1, "Cho-Manno, Revolutionary");
        harness.assertOnBattlefield(player2, "Wall of Distortion");
    }

    @Test
    @DisplayName("A creature with zero power deals no damage")
    void zeroPowerDealsNoDamage() {
        Permanent trellis = harness.addToBattlefieldAndReturn(player1, new VineTrellis());

        harness.castFromHand(player1, new WaveOfReckoning(), "{4}{W}");
        harness.passBothPriorities();

        assertThat(trellis.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player1, "Vine Trellis");
    }

    @Test
    @DisplayName("Protection from white does not prevent a black creature's self-damage")
    void protectionFromSpellColorDoesNotPreventSelfDamage() {
        harness.addToBattlefield(player2, new BlackKnight());

        harness.castFromHand(player1, new WaveOfReckoning(), "{4}{W}");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Black Knight");
        harness.assertInGraveyard(player2, "Black Knight");
    }

    @Test
    @DisplayName("Simultaneous self-damage uses power before any lifelink life gain")
    void lifelinkDoesNotIncreaseAnotherCreaturesSelfDamage() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new VampireNighthawk());
        Permanent avatar = harness.addToBattlefieldAndReturn(player1, new SerraAvatar());

        harness.castFromHand(player1, new WaveOfReckoning(), "{4}{W}");
        harness.passBothPriorities();

        harness.assertLife(player1, 22);
        assertThat(avatar.getMarkedDamage()).isEqualTo(20);
        harness.assertOnBattlefield(player1, "Serra Avatar");
        harness.assertNotOnBattlefield(player1, "Vampire Nighthawk");
        harness.assertInGraveyard(player1, "Vampire Nighthawk");
    }

    @Test
    @DisplayName("Lifelink raises toughness before lethal self-damage is checked")
    void lifelinkKeepsAvatarAliveRegardlessOfBattlefieldOrder() {
        harness.setLife(player1, 20);
        Permanent avatar = harness.addToBattlefieldAndReturn(player1, new SerraAvatar());
        harness.addToBattlefield(player1, new VampireNighthawk());

        harness.castFromHand(player1, new WaveOfReckoning(), "{4}{W}");
        harness.passBothPriorities();

        harness.assertLife(player1, 22);
        assertThat(avatar.getMarkedDamage()).isEqualTo(20);
        harness.assertOnBattlefield(player1, "Serra Avatar");
        harness.assertNotOnBattlefield(player1, "Vampire Nighthawk");
        harness.assertInGraveyard(player1, "Vampire Nighthawk");
    }
}
