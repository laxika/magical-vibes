package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.p.Python;
import com.github.laxika.magicalvibes.cards.q.Quicksand;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Vanishing.class, Python.class, Quicksand.class})
class VanishingTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Vanishing attaches it to the target creature")
    void resolvingAttachesToTarget() {
        Permanent creature = addCreatureReady(player2, new Python());

        harness.setHand(player1, List.of(new Vanishing()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        gs.playCard(gd, player1, 0, 0, creature.getId(), null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.isAttached() && p.getAttachedTo().equals(creature.getId()));
    }

    @Test
    @DisplayName("{U}{U} phases out the enchanted creature; Aura phases out with it")
    void abilityPhasesOutEnchantedCreatureAndAura() {
        Permanent creature = addCreatureReady(player1, new Python());
        Permanent aura = attachVanishing(creature);

        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(creature, aura);
        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(creature, aura);
        assertThat(aura.isPhasedOutIndirectly()).isTrue();
        assertThat(aura.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Ability requires two blue mana")
    void abilityRequiresTwoBlueMana() {
        Permanent creature = addCreatureReady(player1, new Python());
        Permanent aura = attachVanishing(creature);

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature, aura);
    }

    @Test
    @DisplayName("Enchanted creature and Aura phase back in during the creature's controller's next untap")
    void phasesBackInOnControllersNextUntap() {
        Permanent creature = addCreatureReady(player1, new Python());
        Permanent aura = attachVanishing(creature);

        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();
        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(creature, aura);

        advanceToUpkeep(player2); // player2's turn — still phased out
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(creature, aura);

        advanceToUpkeep(player1); // player1's untap — both phase in
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature, aura);
        assertThat(aura.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Can phase out an opponent's enchanted creature; Aura follows under its controller")
    void phasesOutOpponentsCreature() {
        Permanent opponentCreature = addCreatureReady(player2, new Python());
        Permanent aura = attachVanishing(opponentCreature);

        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(opponentCreature);
        assertThat(gd.phasedOutPermanents.get(player2.getId())).contains(opponentCreature);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(aura);
        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(aura);
        assertThat(aura.isPhasedOutIndirectly()).isTrue();

        advanceToUpkeep(player1);
        assertThat(gd.phasedOutPermanents.get(player2.getId())).contains(opponentCreature);
        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(aura);

        advanceToUpkeep(player2);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opponentCreature);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(aura);
        assertThat(aura.getAttachedTo()).isEqualTo(opponentCreature.getId());
    }

    @Test
    @DisplayName("Cannot enchant a land")
    void cannotEnchantALand() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Quicksand());
        harness.setHand(player1, List.of(new Vanishing()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent attachVanishing(Permanent host) {
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new Vanishing());
        aura.setAttachedTo(host.getId());
        return aura;
    }
}
