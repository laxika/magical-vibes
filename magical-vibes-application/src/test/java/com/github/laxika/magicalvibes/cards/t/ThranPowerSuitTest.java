package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ThranPowerSuitTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +1/+1 for each attached Aura and Equipment")
    void equippedCreatureScalesWithAttachments() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent suit = addSuitReady(player1);
        suit.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);

        Permanent scimitar = addPermanentReady(player1, new LeoninScimitar());
        scimitar.setAttachedTo(creature.getId());
        Permanent aura = addPermanentReady(player1, new Pacifism());
        aura.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(6);
    }

    @Test
    @DisplayName("Ward {2} is granted to the equipped creature")
    void wardProtectsEquippedCreature() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent suit = addSuitReady(player1);
        suit.setAttachedTo(creature.getId());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, creature.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Shock");
    }

    @Test
    @DisplayName("Equip {2} attaches Thran Power Suit to a creature you control")
    void equipAttachesToCreature() {
        Permanent suit = addSuitReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(suit.getAttachedTo()).isEqualTo(creature.getId());
    }

    private Permanent addSuitReady(Player player) {
        return addPermanentReady(player, new ThranPowerSuit());
    }

    private Permanent addPermanentReady(Player player, com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
