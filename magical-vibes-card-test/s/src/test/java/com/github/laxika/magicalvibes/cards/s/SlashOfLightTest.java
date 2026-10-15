package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.e.EchoCirclet;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IronGiant;
import com.github.laxika.magicalvibes.cards.w.WarriorsSword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SlashOfLight.class, EchoCirclet.class, GrizzlyBears.class, IronGiant.class, WarriorsSword.class})
class SlashOfLightTest extends BaseCardTest {

    @Test
    @DisplayName("Slash of Light deals damage equal to your creatures plus Equipment")
    void dealsDamageEqualToControlledCreaturesAndEquipment() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new EchoCirclet());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SlashOfLight()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Grizzly Bears"));

        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Slash of Light counts only permanents controlled by its controller")
    void countsOnlyControllersPermanents() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new EchoCirclet());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SlashOfLight()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Grizzly Bears"));

        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Slash of Light counts creatures and Equipment at resolution")
    void countsPermanentsAtResolution() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new EchoCirclet());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SlashOfLight()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        var equipmentId = harness.getPermanentId(player1, "Echo Circlet");
        var targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstant(player1, 0, targetId);
        gd.playerBattlefields.get(player1.getId()).removeIf(permanent -> permanent.getId().equals(equipmentId));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Unattached Equipment contributes damage even without controlled creatures")
    void countsEquipmentWithoutCreatures() {
        harness.addToBattlefield(player1, new WarriorsSword());
        harness.addToBattlefield(player1, new WarriorsSword());
        var target = harness.addToBattlefieldAndReturn(player2, new IronGiant());
        harness.setHand(player1, List.of(new SlashOfLight()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getMarkedDamage()).isEqualTo(2);
        harness.assertOnBattlefield(player2, "Iron Giant");
    }

    @Test
    @DisplayName("Slash of Light can target your own creature and counts that creature")
    void canTargetOwnCreature() {
        var target = harness.addToBattlefieldAndReturn(player1, new IronGiant());
        harness.setHand(player1, List.of(new SlashOfLight()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getMarkedDamage()).isEqualTo(1);
        harness.assertOnBattlefield(player1, "Iron Giant");
    }

    @Test
    @DisplayName("Creatures and Equipment entering before resolution increase the damage")
    void countsPermanentsAddedBeforeResolution() {
        var target = harness.addToBattlefieldAndReturn(player2, new IronGiant());
        harness.setHand(player1, List.of(new SlashOfLight()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castInstant(player1, 0, target.getId());
        harness.addToBattlefield(player1, new IronGiant());
        harness.addToBattlefield(player1, new WarriorsSword());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Slash of Light cannot target a player")
    void cannotTargetPlayer() {
        harness.setHand(player1, List.of(new SlashOfLight()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Slash of Light cannot target noncreature Equipment")
    void cannotTargetNoncreatureEquipment() {
        var target = harness.addToBattlefieldAndReturn(player2, new WarriorsSword());
        harness.setHand(player1, List.of(new SlashOfLight()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
