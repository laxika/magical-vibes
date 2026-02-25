package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.l.LoxodonWarhammer;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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

        GameData gd = harness.getGameData();
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
        Permanent equipment = new Permanent(new LoxodonWarhammer());
        equipment.setAttachedTo(bearsId);
        gd.playerBattlefields.get(player2.getId()).add(equipment);

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
        Permanent equip1 = new Permanent(new LoxodonWarhammer());
        equip1.setAttachedTo(bearsId);
        gd.playerBattlefields.get(player2.getId()).add(equip1);

        // Attach equipment to spider
        Permanent equip2 = new Permanent(new LoxodonWarhammer());
        equip2.setAttachedTo(spiderId);
        gd.playerBattlefields.get(player2.getId()).add(equip2);

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
        Permanent equipment = new Permanent(new LoxodonWarhammer());
        equipment.setAttachedTo(otherCreature.getId());
        gd.playerBattlefields.get(player2.getId()).add(equipment);

        harness.castInstant(player1, 0, List.of(bearsId, spiderId));
        harness.passBothPriorities();

        // Equipment on non-targeted creature should remain attached
        assertThat(equipment.getAttachedTo()).isEqualTo(otherCreature.getId());
    }
}
