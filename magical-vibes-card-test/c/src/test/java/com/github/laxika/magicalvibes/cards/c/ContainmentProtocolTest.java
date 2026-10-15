package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.DiveDown;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.Twitch;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ContainmentProtocol.class, DiveDown.class, FountainOfYouth.class, GrizzlyBears.class, Twitch.class})
class ContainmentProtocolTest extends BaseCardTest {

    @Test
    @DisplayName("Entering Containment Protocol taps the enchanted creature")
    void enteringAuraTapsEnchantedCreature() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new ContainmentProtocol()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isTrue();
        assertThat(findPermanent(player1, "Containment Protocol").getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Enchanted creature does not untap during its controller's untap step")
    void enchantedCreatureDoesNotUntap() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        creature.tap();
        attachContainmentProtocol(player1, creature);

        harness.performUntapStep(player2);

        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Enchanted creature untaps after Containment Protocol leaves the battlefield")
    void creatureUntapsAfterRemoval() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        creature.tap();
        Permanent aura = attachContainmentProtocol(player1, creature);
        gd.playerBattlefields.get(player1.getId()).remove(aura);

        harness.performUntapStep(player2);

        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Containment Protocol cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());

        harness.setHand(player1, List.of(new ContainmentProtocol()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Containment Protocol fizzles if its target is removed before resolution")
    void fizzlesIfTargetRemoved() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new ContainmentProtocol()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castEnchantment(player1, 0, creature.getId());
        gd.playerBattlefields.get(player2.getId()).remove(creature);

        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Containment Protocol");
        harness.assertNotOnBattlefield(player1, "Containment Protocol");
    }

    private Permanent attachContainmentProtocol(Player controller, Permanent creature) {
        Permanent aura = new Permanent(new ContainmentProtocol());
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(controller.getId()).add(aura);
        return aura;
    }

    @Test
    @DisplayName("Entry trigger still taps the enchanted creature if it gains hexproof")
    void entryTriggerIgnoresHexproof() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ContainmentProtocol()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Containment Protocol");
        assertThat(creature.isTapped()).isFalse();
        assertThat(gd.stack).hasSize(1);

        harness.setHand(player2, List.of(new DiveDown()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castInstant(player2, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, creature, Keyword.HEXPROOF)).isTrue();
        assertThat(creature.isTapped()).isFalse();
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isTrue();
        harness.assertOnBattlefield(player1, "Containment Protocol");
    }

    @Test
    @DisplayName("An effect can untap the enchanted creature outside its untap step")
    void spellCanUntapEnchantedCreature() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        creature.tap();
        attachContainmentProtocol(player2, creature);
        harness.setHand(player1, List.of(new Twitch()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castAndResolveInstant(player1, 0, creature.getId());
        harness.handleMayAbilityChosen(player1, true);

        assertThat(creature.isTapped()).isFalse();
        harness.performUntapStep(player1);
        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Only the enchanted creature is prevented from untapping")
    void otherCreaturesUntapNormally() {
        Permanent enchanted = addCreatureReady(player2, new GrizzlyBears());
        Permanent other = addCreatureReady(player2, new GrizzlyBears());
        enchanted.tap();
        other.tap();
        attachContainmentProtocol(player1, enchanted);

        harness.performUntapStep(player2);

        assertThat(enchanted.isTapped()).isTrue();
        assertThat(other.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Containment Protocol can enchant and tap its controller's creature")
    void canEnchantOwnCreature() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new ContainmentProtocol()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isTrue();
        harness.performUntapStep(player1);
        assertThat(creature.isTapped()).isTrue();
    }
}
