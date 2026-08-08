package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class KumanosBlessingTest extends BaseCardTest {

    private boolean isExiled(String cardName) {
        return gd.exiledCards.stream().anyMatch(e -> e.card().getName().equals(cardName));
    }

    private Permanent enchantPyromancer() {
        Permanent pyro = addCreatureReady(player1, new ProdigalPyromancer());
        harness.setHand(player1, List.of(new KumanosBlessing()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castEnchantment(player1, 0, pyro.getId());
        harness.passBothPriorities();
        return pyro;
    }

    @Test
    @DisplayName("Resolving attaches to the target creature")
    void resolvingAttachesToTarget() {
        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(bears);

        harness.setHand(player1, List.of(new KumanosBlessing()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Kumano's Blessing")
                        && bears.getId().equals(p.getAttachedTo()));
    }

    @Test
    @DisplayName("A creature killed by the enchanted creature is exiled instead of dying")
    void creatureKilledByEnchantedIsExiled() {
        enchantPyromancer();
        harness.addToBattlefield(player2, new LlanowarElves());

        UUID targetId = harness.getPermanentId(player2, "Llanowar Elves");
        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Llanowar Elves");
        harness.assertNotInGraveyard(player2, "Llanowar Elves");
        assertThat(isExiled("Llanowar Elves")).isTrue();
    }

    @Test
    @DisplayName("A creature damaged earlier by the enchanted creature is exiled when another source finishes it")
    void creatureDamagedEarlierIsExiled() {
        enchantPyromancer();
        // Battlefield: enchanted pyro (0), Aura (1), second pyro (2)
        addCreatureReady(player1, new ProdigalPyromancer());
        harness.addToBattlefield(player2, new GrizzlyBears());

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player2, "Grizzly Bears");

        harness.activateAbility(player1, 2, null, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(isExiled("Grizzly Bears")).isTrue();
    }

    @Test
    @DisplayName("A creature the enchanted creature never damaged dies to the graveyard normally")
    void undamagedCreatureGoesToGraveyard() {
        enchantPyromancer();
        addCreatureReady(player1, new ProdigalPyromancer());
        harness.addToBattlefield(player2, new LlanowarElves());

        UUID targetId = harness.getPermanentId(player2, "Llanowar Elves");
        harness.activateAbility(player1, 2, null, targetId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Llanowar Elves");
    }

    @Test
    @DisplayName("The replacement stops applying once the Aura has left the battlefield")
    void replacementStopsWhenAuraLeaves() {
        enchantPyromancer();
        addCreatureReady(player1, new ProdigalPyromancer());
        harness.addToBattlefield(player2, new GrizzlyBears());

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        Permanent aura = findPermanent(player1, "Kumano's Blessing");
        gd.playerBattlefields.get(player1.getId()).remove(aura);

        // Second pyro is still at index 1 after Aura removal
        harness.activateAbility(player1, 1, null, targetId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new KumanosBlessing()));
        harness.addMana(player1, ManaColor.RED, 3);

        Permanent artifact = findPermanent(player1, "Fountain of Youth");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
