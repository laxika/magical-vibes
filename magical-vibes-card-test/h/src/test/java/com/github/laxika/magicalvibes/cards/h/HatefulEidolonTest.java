package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.NyxbornColossus;
import com.github.laxika.magicalvibes.cards.p.PlanarCleansing;
import com.github.laxika.magicalvibes.cards.s.SentinelsEyes;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HatefulEidolon.class, GrizzlyBears.class, PlanarCleansing.class, SentinelsEyes.class,
        NyxbornColossus.class})
class HatefulEidolonTest extends BaseCardTest {

    @Test
    @DisplayName("Draws for each Aura controlled by Hateful Eidolon's controller")
    void drawsForEachAuraControlledByItsController() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new HatefulEidolon());
        attachAura(player1, creature);
        attachAura(player2, creature);
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));

        int handSize = gd.playerHands.get(player1.getId()).size();
        creature.setMarkedDamage(4);
        harness.runStateBasedActions();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize + 1);
    }

    @Test
    @DisplayName("Counts Auras when Hateful Eidolon and the enchanted creature die together")
    void countsAurasWhenAllDieTogether() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new HatefulEidolon());
        attachAura(player1, creature);
        attachAura(player1, creature);
        harness.setHand(player1, List.of(new PlanarCleansing()));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    void drawsForAurasOnOpponentsCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new NyxbornColossus());
        harness.addToBattlefield(player1, new HatefulEidolon());
        attachAura(player1, creature);
        attachAura(player1, creature);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new NyxbornColossus(), new NyxbornColossus()));

        creature.setMarkedDamage(9);
        harness.runStateBasedActions();
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Nyxborn Colossus");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    void drawsWhenEnchantedEidolonItselfDies() {
        Permanent eidolon = harness.addToBattlefieldAndReturn(player1, new HatefulEidolon());
        attachAura(player1, eidolon);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new NyxbornColossus()));

        eidolon.setMarkedDamage(3);
        harness.runStateBasedActions();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Hateful Eidolon");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void doesNotDrawForOnlyOpponentsAura() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new NyxbornColossus());
        harness.addToBattlefield(player1, new HatefulEidolon());
        attachAura(player2, creature);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new NyxbornColossus()));

        creature.setMarkedDamage(8);
        harness.runStateBasedActions();
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Nyxborn Colossus");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void doesNotDrawForUnenchantedEnchantmentCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new NyxbornColossus());
        harness.addToBattlefield(player1, new HatefulEidolon());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new NyxbornColossus()));

        creature.setMarkedDamage(7);
        harness.runStateBasedActions();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Nyxborn Colossus");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    private void attachAura(Player auraController, Permanent creature) {
        Permanent aura = harness.addToBattlefieldAndReturn(auraController, new SentinelsEyes());
        aura.setAttachedTo(creature.getId());
    }
}
