package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.l.LoxodonWarhammer;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FulgentDistraction.class, GrizzlyBears.class, GiantSpider.class, LoxodonWarhammer.class})
class FulgentDistractionTest extends BaseCardTest {

    @Test
    @DisplayName("Taps two target creatures")
    void tapsTwoTargetCreatures() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GiantSpider());
        harness.setHand(player1, List.of(new FulgentDistraction()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        List<Permanent> bf = gd.playerBattlefields.get(player2.getId());
        UUID bearsId = bf.get(0).getId();
        UUID spiderId = bf.get(1).getId();

        harness.castInstant(player1, 0, List.of(bearsId, spiderId));
        harness.passBothPriorities();

        Permanent bears = gqs.findPermanentById(gd, bearsId);
        Permanent spider = gqs.findPermanentById(gd, spiderId);
        assertThat(bears.isTapped()).isTrue();
        assertThat(spider.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Unattaches equipment from target creatures")
    void unattachesEquipmentFromTargetCreatures() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GiantSpider());
        harness.setHand(player1, List.of(new FulgentDistraction()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        List<Permanent> bf = gd.playerBattlefields.get(player2.getId());
        UUID bearsId = bf.get(0).getId();
        UUID spiderId = bf.get(1).getId();

        // Attach equipment to bears
        Permanent equipment = harness.addToBattlefieldAndReturn(player2, new LoxodonWarhammer());
        equipment.setAttachedTo(bearsId);

        harness.castInstant(player1, 0, List.of(bearsId, spiderId));
        harness.passBothPriorities();

        // Equipment should be unattached
        assertThat(equipment.getAttachedTo()).isNull();
        // Equipment should still be on battlefield
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(equipment);
    }

    @Test
    @DisplayName("Unattaches multiple equipment from different target creatures")
    void unattachesMultipleEquipmentFromDifferentTargets() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GiantSpider());
        harness.setHand(player1, List.of(new FulgentDistraction()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        List<Permanent> bf = gd.playerBattlefields.get(player2.getId());
        UUID bearsId = bf.get(0).getId();
        UUID spiderId = bf.get(1).getId();

        // Attach equipment to bears
        Permanent equip1 = harness.addToBattlefieldAndReturn(player2, new LoxodonWarhammer());
        equip1.setAttachedTo(bearsId);

        // Attach equipment to spider
        Permanent equip2 = harness.addToBattlefieldAndReturn(player2, new LoxodonWarhammer());
        equip2.setAttachedTo(spiderId);

        harness.castInstant(player1, 0, List.of(bearsId, spiderId));
        harness.passBothPriorities();

        assertThat(equip1.getAttachedTo()).isNull();
        assertThat(equip2.getAttachedTo()).isNull();
        // Both equipment remain on battlefield
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(equip1, equip2);
    }

    @Test
    @DisplayName("Creatures without equipment are just tapped")
    void creaturesWithoutEquipmentAreJustTapped() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GiantSpider());
        harness.setHand(player1, List.of(new FulgentDistraction()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        List<Permanent> bf = gd.playerBattlefields.get(player2.getId());
        UUID bearsId = bf.get(0).getId();
        UUID spiderId = bf.get(1).getId();

        harness.castInstant(player1, 0, List.of(bearsId, spiderId));
        harness.passBothPriorities();

        Permanent bears = gqs.findPermanentById(gd, bearsId);
        Permanent spider = gqs.findPermanentById(gd, spiderId);
        assertThat(bears.isTapped()).isTrue();
        assertThat(spider.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot cast with only 1 target")
    void cannotCastWithOnlyOneTarget() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new FulgentDistraction()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(bearsId)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Must target between");
    }

    @Test
    @DisplayName("Equipment not attached to targets is not affected")
    void equipmentNotAttachedToTargetsIsNotAffected() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GiantSpider());
        harness.setHand(player1, List.of(new FulgentDistraction()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        List<Permanent> bf = gd.playerBattlefields.get(player2.getId());
        UUID bearsId = bf.get(0).getId();
        UUID spiderId = bf.get(1).getId();

        // Add a third creature with equipment that is NOT targeted
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent otherCreature = bf.get(bf.size() - 1);
        Permanent equipment = harness.addToBattlefieldAndReturn(player2, new LoxodonWarhammer());
        equipment.setAttachedTo(otherCreature.getId());

        harness.castInstant(player1, 0, List.of(bearsId, spiderId));
        harness.passBothPriorities();

        // Equipment on non-targeted creature should remain attached
        assertThat(equipment.getAttachedTo()).isEqualTo(otherCreature.getId());
    }

    @Test
    void unattachesAllEquipmentFromAlreadyTappedCreatureAcrossControllers() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent spider = harness.addToBattlefieldAndReturn(player2, new GiantSpider());
        bears.tap();
        Permanent firstEquipment = harness.addToBattlefieldAndReturn(player1, new LoxodonWarhammer());
        Permanent secondEquipment = harness.addToBattlefieldAndReturn(player2, new LoxodonWarhammer());
        firstEquipment.setAttachedTo(bears.getId());
        secondEquipment.setAttachedTo(bears.getId());
        harness.setHand(player1, List.of(new FulgentDistraction()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castInstant(player1, 0, List.of(bears.getId(), spider.getId()));
        harness.passBothPriorities();

        assertThat(bears.isTapped()).isTrue();
        assertThat(spider.isTapped()).isTrue();
        assertThat(firstEquipment.getAttachedTo()).isNull();
        assertThat(secondEquipment.getAttachedTo()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(bears, firstEquipment);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(spider, secondEquipment);
    }

    @Test
    void resolvesForRemainingTargetWhenFirstTargetLeavesBattlefield() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent spider = harness.addToBattlefieldAndReturn(player2, new GiantSpider());
        Permanent equipment = harness.addToBattlefieldAndReturn(player2, new LoxodonWarhammer());
        equipment.setAttachedTo(spider.getId());
        harness.setHand(player1, List.of(new FulgentDistraction()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castInstant(player1, 0, List.of(bears.getId(), spider.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(bears);
        gd.playerGraveyards.get(player2.getId()).add(bears.getCard());
        harness.passBothPriorities();

        assertThat(spider.isTapped()).isTrue();
        assertThat(equipment.getAttachedTo()).isNull();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(spider, equipment);
    }

    @Test
    void cannotChooseSameCreatureTwice() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new FulgentDistraction()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(bears.getId(), bears.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotTargetNoncreatureEquipment() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent equipment = harness.addToBattlefieldAndReturn(player2, new LoxodonWarhammer());
        harness.setHand(player1, List.of(new FulgentDistraction()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(bears.getId(), equipment.getId())))
                .isInstanceOf(IllegalStateException.class);
    }
}
