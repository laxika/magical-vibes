package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.h.HopeCharm;
import com.github.laxika.magicalvibes.cards.j.JamuraanLion;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TarPitWarrior.class, HopeCharm.class, JamuraanLion.class})
class TarPitWarriorTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices itself when it becomes the target of a spell")
    void sacrificesWhenTargetedBySpell() {
        Permanent warrior = harness.addToBattlefieldAndReturn(player1, new TarPitWarrior());

        harness.setHand(player2, List.of(new HopeCharm()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.castInstant(player2, 0, 0, warrior.getId());

        assertThat(gd.stack).hasSizeGreaterThanOrEqualTo(2);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Tar Pit Warrior");
        harness.assertInGraveyard(player1, "Tar Pit Warrior");
    }

    @Test
    @DisplayName("Sacrifices itself when it becomes the target of an activated ability")
    void sacrificesWhenTargetedByAbility() {
        Permanent warrior = harness.addToBattlefieldAndReturn(player1, new TarPitWarrior());

        Permanent lion = harness.addToBattlefieldAndReturn(player2, new JamuraanLion());
        lion.setSummoningSick(false);

        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.activateAbility(player2, gd.playerBattlefields.get(player2.getId()).indexOf(lion),
                null, warrior.getId());

        assertThat(gd.stack).hasSizeGreaterThanOrEqualTo(2);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Tar Pit Warrior");
        harness.assertInGraveyard(player1, "Tar Pit Warrior");
    }

    @Test
    @DisplayName("Stays on the battlefield when nothing targets it")
    void staysWhenNotTargeted() {
        Permanent warrior = harness.addToBattlefieldAndReturn(player1, new TarPitWarrior());

        harness.setHand(player2, List.of(new HopeCharm()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.castInstant(player2, 0, 1, player1.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Tar Pit Warrior");
    }
}
