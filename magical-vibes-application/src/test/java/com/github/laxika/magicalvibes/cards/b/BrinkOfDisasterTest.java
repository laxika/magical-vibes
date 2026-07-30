package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BrinkOfDisasterTest extends BaseCardTest {

    @Test
    @DisplayName("Can cast Brink of Disaster targeting a creature")
    void canTargetCreature() {
        harness.addToBattlefield(player1, new GiantSpider());
        Permanent creature = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.setHand(player1, List.of(new BrinkOfDisaster()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.forceActivePlayer(player1);

        harness.castEnchantment(player1, 0, creature.getId());

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ENCHANTMENT_SPELL);
        assertThat(entry.getTargetId()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Can cast Brink of Disaster targeting a land")
    void canTargetLand() {
        harness.addToBattlefield(player1, new Swamp());
        Permanent land = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.setHand(player1, List.of(new BrinkOfDisaster()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.forceActivePlayer(player1);

        harness.castEnchantment(player1, 0, land.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Brink of Disaster")
                        && land.getId().equals(p.getAttachedTo()));
    }

    @Test
    @DisplayName("Cannot cast Brink of Disaster targeting a non-creature, non-land permanent")
    void cannotTargetEnchantment() {
        harness.addToBattlefield(player1, new GiantSpider()); // valid target so spell is playable
        harness.addToBattlefield(player1, new BrinkOfDisaster());
        Permanent enchantment = findPermanent(player1, "Brink of Disaster");
        harness.setHand(player1, List.of(new BrinkOfDisaster()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.forceActivePlayer(player1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, enchantment.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature or land");
    }

    @Test
    @DisplayName("Attacking with the enchanted creature destroys it")
    void tappingEnchantedCreatureDestroysIt() {
        Permanent spider = new Permanent(new GiantSpider());
        spider.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(spider);
        attachAura(player1, spider);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1, List.of(0));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Giant Spider");
    }

    @Test
    @DisplayName("Tapping the enchanted land destroys it")
    void tappingEnchantedLandDestroysIt() {
        harness.addToBattlefield(player1, new Swamp());
        Permanent swamp = gd.playerBattlefields.get(player1.getId()).getFirst();
        attachAura(player1, swamp);

        harness.tapPermanent(player1, 0);
        resolveStackFully();

        harness.assertNotOnBattlefield(player1, "Swamp");
    }

    @Test
    @DisplayName("Tapping an un-enchanted land does not trigger the destroy ability")
    void tappingUnenchantedLandDoesNotDestroy() {
        harness.addToBattlefield(player1, new Swamp());

        harness.tapPermanent(player1, 0);
        resolveStackFully();

        harness.assertOnBattlefield(player1, "Swamp");
    }

    private void attachAura(Player owner, Permanent host) {
        Permanent aura = new Permanent(new BrinkOfDisaster());
        aura.setAttachedTo(host.getId());
        gd.playerBattlefields.get(owner.getId()).add(aura);
    }

    private void resolveStackFully() {
        for (int i = 0; i < 8 && (!gd.stack.isEmpty() || !gd.pendingManaAbilityTriggers.isEmpty()); i++) {
            harness.passBothPriorities();
        }
    }
}
