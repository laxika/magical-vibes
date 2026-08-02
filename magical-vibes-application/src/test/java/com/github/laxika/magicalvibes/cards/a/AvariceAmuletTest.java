package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.d.Deathmark;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AvariceAmuletTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +2/+0 and has vigilance")
    void equippedCreatureGetsBoostAndVigilance() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent amulet = addAmuletReady(player1);
        amulet.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("Unequipped creature gets no boost or vigilance")
    void unequippedCreatureUnaffected() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        addAmuletReady(player1);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Equipped creature's controller draws a card at the beginning of their upkeep")
    void equippedCreatureDrawsOnUpkeep() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent amulet = addAmuletReady(player1);
        amulet.setAttachedTo(creature.getId());

        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        int before = gd.playerHands.get(player1.getId()).size();
        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(before + 1);
    }

    @Test
    @DisplayName("No upkeep draw while the Equipment is unattached")
    void noDrawWhenUnattached() {
        addCreatureReady(player1, new GrizzlyBears());
        addAmuletReady(player1);

        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        int before = gd.playerHands.get(player1.getId()).size();
        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(before);
    }

    @Test
    @DisplayName("Whenever equipped creature dies, target opponent gains control of the Equipment")
    void deathTriggerHandsEquipmentToOpponent() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent amulet = addAmuletReady(player1);
        amulet.setAttachedTo(creature.getId());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Deathmark()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.castSorcery(player2, 0, creature.getId());
        harness.passBothPriorities(); // Deathmark resolves, creature dies, death trigger queued

        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities(); // resolve the control-change trigger

        harness.assertOnBattlefield(player2, "Avarice Amulet");
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(amulet);
    }

    @Test
    @DisplayName("Amulet stays put when a creature it is not attached to dies")
    void noTriggerForUnequippedCreatureDeath() {
        Permanent equipped = addCreatureReady(player1, new GrizzlyBears());
        Permanent other = addCreatureReady(player1, new GrizzlyBears());
        Permanent amulet = addAmuletReady(player1);
        amulet.setAttachedTo(equipped.getId());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Deathmark()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.castSorcery(player2, 0, other.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Avarice Amulet");
        assertThat(amulet.getAttachedTo()).isEqualTo(equipped.getId());
    }

    @Test
    @DisplayName("Equip {2} attaches the Amulet to a creature you control")
    void equipAttachesToCreature() {
        Permanent amulet = addAmuletReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(amulet.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
    }

    private Permanent addAmuletReady(Player player) {
        Permanent perm = new Permanent(new AvariceAmulet());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
