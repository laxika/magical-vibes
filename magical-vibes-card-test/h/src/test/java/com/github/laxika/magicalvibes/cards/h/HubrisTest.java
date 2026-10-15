package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.a.ArmorOfFaith;
import com.github.laxika.magicalvibes.cards.a.AngelicChorus;
import com.github.laxika.magicalvibes.cards.e.EssenceFlare;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.d.DarksteelMutation;
import com.github.laxika.magicalvibes.cards.t.Thragtusk;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Hubris.class, ArmorOfFaith.class, AngelicChorus.class, EssenceFlare.class,
        GrizzlyBears.class, DarksteelMutation.class, Thragtusk.class})
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

        harness.castAndResolveInstant(player1, 0, creature.getId());

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

    @Test
    @DisplayName("Returns an opposing creature with no Auras to its owner's hand")
    void returnsOpposingCreatureWithoutAuras() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Hubris()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveInstant(player1, 0, creature.getId());

        harness.assertInHand(player2, "Grizzly Bears");
        harness.assertNotInHand(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Hubris");
    }

    @Test
    @DisplayName("Leaves Auras attached to other creatures on the battlefield")
    void leavesUnrelatedAurasOnBattlefield() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent other = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new ArmorOfFaith());
        aura.setAttachedTo(other.getId());
        harness.setHand(player1, List.of(new Hubris()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Armor of Faith");
        harness.assertNotInHand(player2, "Armor of Faith");
        assertThat(aura.getAttachedTo()).isEqualTo(other.getId());
    }

    @Test
    @DisplayName("Returning an ability-removing Aura simultaneously does not restore leave triggers")
    void doesNotRestoreAbilitiesBeforeCreatureLeaves() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new Thragtusk());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new DarksteelMutation());
        aura.setAttachedTo(creature.getId());
        harness.setHand(player1, List.of(new Hubris()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveInstant(player1, 0, creature.getId());

        harness.assertInHand(player2, "Thragtusk");
        harness.assertInHand(player1, "Darksteel Mutation");
        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player2, "Beast");
    }
}
