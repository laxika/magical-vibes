package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.e.EchoCirclet;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({SlashOfLight.class, EchoCirclet.class, GrizzlyBears.class})
class SlashOfLightTest extends BaseCardTest {

    @Test
    @DisplayName("Slash of Light deals damage equal to your creatures plus Equipment")
    void dealsDamageEqualToControlledCreaturesAndEquipment() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new EchoCirclet());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SlashOfLight()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castInstant(player1, 0, harness.getPermanentId(player2, "Grizzly Bears"));
        harness.passBothPriorities();

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

        harness.castInstant(player1, 0, harness.getPermanentId(player2, "Grizzly Bears"));
        harness.passBothPriorities();

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
}
