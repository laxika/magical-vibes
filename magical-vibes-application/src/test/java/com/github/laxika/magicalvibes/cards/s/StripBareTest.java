package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StripBareTest extends BaseCardTest {

    @Test
    @DisplayName("Strip Bare destroys both Auras and Equipment attached to the target creature")
    void destroysAurasAndEquipment() {
        Permanent creature = new Permanent(new GrizzlyBears());
        creature.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(creature);

        Permanent equipment = new Permanent(new LeoninScimitar());
        equipment.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player2.getId()).add(equipment);

        Permanent aura = new Permanent(new Pacifism());
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        harness.setHand(player1, List.of(new StripBare()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();

        // The creature itself survives.
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Leonin Scimitar");
        harness.assertInGraveyard(player2, "Leonin Scimitar");
        harness.assertNotOnBattlefield(player1, "Pacifism");
        harness.assertInGraveyard(player1, "Pacifism");
    }

    @Test
    @DisplayName("Strip Bare does not destroy attachments on other creatures")
    void doesNotAffectOtherCreatures() {
        Permanent target = new Permanent(new GrizzlyBears());
        target.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(target);

        Permanent other = new Permanent(new GrizzlyBears());
        other.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(other);

        Permanent aura = new Permanent(new Pacifism());
        aura.setAttachedTo(other.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        harness.setHand(player1, List.of(new StripBare()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Pacifism");
    }

    @Test
    @DisplayName("Strip Bare cannot target a non-creature")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player2, new LeoninScimitar());
        harness.setHand(player1, List.of(new StripBare()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        UUID targetId = harness.getPermanentId(player2, "Leonin Scimitar");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }
}
