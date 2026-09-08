package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.a.AngelicWall;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WarmongersChariotTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +2/+2")
    void equippedCreatureGetsBoost() {
        Permanent wall = addCreatureReady(player1, new AngelicWall());
        Permanent chariot = addChariotReady();
        chariot.setAttachedTo(wall.getId());

        assertThat(gqs.getEffectivePower(gd, wall)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, wall)).isEqualTo(6);
    }

    @Test
    @DisplayName("Equipped defender can attack")
    void equippedDefenderCanAttack() {
        Permanent wall = addCreatureReady(player1, new AngelicWall());
        Permanent chariot = addChariotReady();
        chariot.setAttachedTo(wall.getId());
        harness.addToBattlefield(player2, new GrizzlyBears());

        declareAttackers(List.of(0));

        assertThat(wall.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("Unattached defender cannot attack")
    void unattachedDefenderCannotAttack() {
        addCreatureReady(player1, new AngelicWall());
        addChariotReady();
        harness.addToBattlefield(player2, new GrizzlyBears());

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Equipping the Chariot attaches it for three mana")
    void equippingChariotAttachesIt() {
        Permanent chariot = addChariotReady();
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(chariot.getAttachedTo()).isEqualTo(creature.getId());
    }

    private Permanent addChariotReady() {
        Permanent chariot = new Permanent(new WarmongersChariot());
        gd.playerBattlefields.get(player1.getId()).add(chariot);
        return chariot;
    }
}
