package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.l.LoxodonWarhammer;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EatenBySpidersTest extends BaseCardTest {

    @Test
    @DisplayName("Eaten by Spiders destroys a creature with flying")
    void destroysFlyingCreature() {
        harness.addToBattlefield(player2, new SerraAngel());
        harness.setHand(player1, List.of(new EatenBySpiders()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        UUID targetId = harness.getPermanentId(player2, "Serra Angel");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Serra Angel");
        harness.assertInGraveyard(player2, "Serra Angel");
    }

    @Test
    @DisplayName("Eaten by Spiders also destroys Equipment attached to the target")
    void destroysAttachedEquipment() {
        Permanent creature = new Permanent(new SerraAngel());
        creature.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(creature);

        Permanent equip1 = new Permanent(new LeoninScimitar());
        equip1.setSummoningSick(false);
        equip1.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player2.getId()).add(equip1);

        Permanent equip2 = new Permanent(new LoxodonWarhammer());
        equip2.setSummoningSick(false);
        equip2.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player2.getId()).add(equip2);

        harness.setHand(player1, List.of(new EatenBySpiders()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Serra Angel");
        harness.assertInGraveyard(player2, "Leonin Scimitar");
        harness.assertInGraveyard(player2, "Loxodon Warhammer");
    }

    @Test
    @DisplayName("Eaten by Spiders leaves Equipment attached to other creatures alone")
    void leavesOtherEquipmentAlone() {
        Permanent target = new Permanent(new SerraAngel());
        target.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(target);

        Permanent other = new Permanent(new GrizzlyBears());
        other.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(other);

        Permanent equipment = new Permanent(new LeoninScimitar());
        equipment.setSummoningSick(false);
        equipment.setAttachedTo(other.getId());
        gd.playerBattlefields.get(player2.getId()).add(equipment);

        harness.setHand(player1, List.of(new EatenBySpiders()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Serra Angel");
        harness.assertOnBattlefield(player2, "Leonin Scimitar");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Eaten by Spiders cannot target a creature without flying")
    void cannotTargetNonFlyingCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new EatenBySpiders()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }
}
