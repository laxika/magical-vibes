package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VanishingTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Vanishing attaches it to the target creature")
    void resolvingAttachesToTarget() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new Vanishing()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        gs.playCard(gd, player1, 0, 0, bears.getId(), null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Vanishing")
                        && p.isAttached()
                        && p.getAttachedTo().equals(bears.getId()));
    }

    @Test
    @DisplayName("{U}{U} phases out the enchanted creature; Aura phases out with it")
    void abilityPhasesOutEnchantedCreatureAndAura() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent aura = attachVanishing(bears);

        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(bears, aura);
        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(bears, aura);
        assertThat(aura.isPhasedOutIndirectly()).isTrue();
    }

    @Test
    @DisplayName("Enchanted creature and Aura phase back in during the creature's controller's next untap")
    void phasesBackInOnControllersNextUntap() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent aura = attachVanishing(bears);

        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();
        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(bears, aura);

        advanceTurn(); // player2's turn — still phased out
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(bears, aura);

        advanceTurn(); // player1's untap — both phase in
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(bears, aura);
        assertThat(aura.getAttachedTo()).isEqualTo(bears.getId());
    }

    @Test
    @DisplayName("Can phase out an opponent's enchanted creature; Aura follows under its controller")
    void phasesOutOpponentsCreature() {
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        Permanent aura = new Permanent(new Vanishing());
        aura.setAttachedTo(opponentCreature.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(opponentCreature);
        assertThat(gd.phasedOutPermanents.get(player2.getId())).contains(opponentCreature);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(aura);
        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(aura);
        assertThat(aura.isPhasedOutIndirectly()).isTrue();
    }

    @Test
    @DisplayName("Cannot enchant a land")
    void cannotEnchantALand() {
        harness.addToBattlefield(player1, new Mountain());
        harness.setHand(player1, List.of(new Vanishing()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        Permanent mountain = findPermanent(player1, "Mountain");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, mountain.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent attachVanishing(Permanent host) {
        Permanent aura = new Permanent(new Vanishing());
        aura.setAttachedTo(host.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);
        return aura;
    }

    private void advanceTurn() {
        harness.forceStep(TurnStep.CLEANUP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
