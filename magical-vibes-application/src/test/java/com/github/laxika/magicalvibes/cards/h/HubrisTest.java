package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.a.ArmorOfFaith;
import com.github.laxika.magicalvibes.cards.a.AngelicChorus;
import com.github.laxika.magicalvibes.cards.e.EssenceFlare;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HubrisTest extends BaseCardTest {

    @Test
    @DisplayName("Returns the target creature and every attached Aura to their owners' hands")
    void returnsTargetCreatureAndAllAttachedAuras() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent ownAura = harness.addToBattlefieldAndReturn(player1, new ArmorOfFaith());
        Permanent opponentAura = harness.addToBattlefieldAndReturn(player2, new EssenceFlare());
        ownAura.setAttachedTo(creature.getId());
        opponentAura.setAttachedTo(creature.getId());

        harness.setHand(player1, List.of(new Hubris()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Armor of Faith");
        harness.assertInHand(player2, "Essence Flare");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Armor of Faith");
        harness.assertNotOnBattlefield(player2, "Essence Flare");
        harness.assertNotInGraveyard(player1, "Armor of Faith");
        harness.assertNotInGraveyard(player2, "Essence Flare");
    }

    @Test
    @DisplayName("Cannot target a non-creature")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new AngelicChorus());
        harness.setHand(player1, List.of(new Hubris()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        UUID targetId = harness.getPermanentId(player2, "Angelic Chorus");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
