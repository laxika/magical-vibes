package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.a.AlphaKavu;
import com.github.laxika.magicalvibes.cards.c.CavernHarpy;
import com.github.laxika.magicalvibes.cards.m.ManaCylix;
import com.github.laxika.magicalvibes.cards.t.Terminate;
import com.github.laxika.magicalvibes.cards.v.VolcanoImp;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Hobble.class, AlphaKavu.class, ManaCylix.class, VolcanoImp.class,
        CavernHarpy.class, Terminate.class})
class HobbleTest extends BaseCardTest {

    @Test
    @DisplayName("When Hobble enters, its controller draws a card")
    void drawsCardWhenItEnters() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new AlphaKavu());
        harness.setLibrary(player1, List.of(new ManaCylix()));
        harness.setHand(player1, List.of(new Hobble()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0, creature.getId());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Hobble prevents the enchanted creature from attacking")
    void enchantedCreatureCannotAttack() {
        Permanent creature = addCreatureReady(player1, new AlphaKavu());
        attachAura(player2, creature);

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Hobble also prevents a black enchanted creature from attacking")
    void blackEnchantedCreatureCannotAttack() {
        Permanent creature = addCreatureReady(player1, new VolcanoImp());
        attachAura(player2, creature);

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Hobble prevents a black enchanted creature from blocking")
    void blackEnchantedCreatureCannotBlock() {
        Permanent creature = addCreatureReady(player2, new VolcanoImp());
        attachAura(player1, creature);
        Permanent attacker = addCreatureReady(player1, new AlphaKavu());
        attacker.setAttacking(true);

        prepareDeclareBlockers(player1);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 1))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid blocker index");
    }

    @Test
    @DisplayName("Hobble allows a nonblack enchanted creature to block")
    void nonblackEnchantedCreatureCanBlock() {
        Permanent creature = addCreatureReady(player2, new AlphaKavu());
        attachAura(player1, creature);
        Permanent attacker = addCreatureReady(player1, new AlphaKavu());
        attacker.setAttacking(true);

        prepareDeclareBlockers(player1);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));

        assertThat(creature.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Hobble cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new ManaCylix());
        harness.setHand(player1, List.of(new Hobble()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Hobble prevents a multicolored black creature from blocking")
    void multicoloredBlackCreatureCannotBlock() {
        Permanent creature = addCreatureReady(player2, new CavernHarpy());
        attachAura(player1, creature);
        Permanent attacker = addCreatureReady(player1, new AlphaKavu());
        attacker.setAttacking(true);

        prepareDeclareBlockers(player1);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 1))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid blocker index");
    }

    @Test
    @DisplayName("Hobble does not draw a card when its target is destroyed before resolution")
    void noDrawWhenTargetIsDestroyedBeforeResolution() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new AlphaKavu());
        harness.setLibrary(player1, List.of(new ManaCylix()));
        harness.setHand(player1, List.of(new Hobble()));
        harness.setHand(player2, List.of(new Terminate()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.castInstant(player2, 0, creature.getId());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        harness.assertNotOnBattlefield(player1, "Hobble");
        harness.assertInGraveyard(player1, "Hobble");
        harness.assertInGraveyard(player2, "Alpha Kavu");
    }

    private void attachAura(Player controller, Permanent creature) {
        Permanent aura = harness.addToBattlefieldAndReturn(controller, new Hobble());
        aura.setAttachedTo(creature.getId());
    }

}
