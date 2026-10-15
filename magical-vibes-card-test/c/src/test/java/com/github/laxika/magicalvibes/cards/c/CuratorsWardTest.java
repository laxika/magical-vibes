package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.BlinkOfAnEye;
import com.github.laxika.magicalvibes.cards.d.DanithaCapashenParagon;
import com.github.laxika.magicalvibes.cards.d.Disperse;
import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.InBolassClutches;
import com.github.laxika.magicalvibes.cards.j.JoustingLance;
import com.github.laxika.magicalvibes.cards.k.KrosanDruid;
import com.github.laxika.magicalvibes.cards.s.Shatter;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.cards.t.TheFlameOfKeld;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CuratorsWard.class, DoomBlade.class, Disperse.class, GrizzlyBears.class,
        Shatter.class, Spellbook.class, BlinkOfAnEye.class, DanithaCapashenParagon.class,
        InBolassClutches.class, JoustingLance.class, KrosanDruid.class, TheFlameOfKeld.class})
class CuratorsWardTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted permanent gains hexproof")
    void enchantedPermanentHasHexproof() {
        Permanent creature = addCreatureWithWard(player1, player1, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.HEXPROOF)).isTrue();
    }

    @Test
    @DisplayName("Permanent loses hexproof when Curator's Ward is removed")
    void permanentLosesHexproofWhenWardRemoved() {
        Permanent creature = addCreatureWithWard(player1, player1, new GrizzlyBears());
        assertThat(gqs.hasKeyword(gd, creature, Keyword.HEXPROOF)).isTrue();

        // Remove the ward aura
        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Curator's Ward"));

        assertThat(gqs.hasKeyword(gd, creature, Keyword.HEXPROOF)).isFalse();
    }

    @Test
    @DisplayName("Draw 2 when enchanted legendary creature is destroyed")
    void drawsWhenLegendaryCreatureDestroyed() {
        // Use GrizzlyBears with legendary supertype manually set to avoid trigger interference
        GrizzlyBears legendaryBears = new GrizzlyBears();
        legendaryBears.setSupertypes(Set.of(CardSupertype.LEGENDARY));
        Permanent creature = addCreatureWithWard(player1, player1, legendaryBears);

        // Controller destroys own creature (hexproof doesn't prevent self-targeting)
        castAndResolveDoomBlade(player1, creature);
        harness.passBothPriorities(); // resolve DrawCardEffect trigger

        // Drew 2 cards from trigger
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(2);
    }

    @Test
    @DisplayName("Draw 2 when enchanted artifact is destroyed")
    void drawsWhenArtifactDestroyed() {
        Permanent artifact = addArtifactWithWard(player1, player1);

        // Controller destroys own artifact (hexproof doesn't prevent self-targeting)
        castAndResolveShatter(player1, artifact);
        harness.passBothPriorities(); // resolve DrawCardEffect trigger

        // Drew 2 cards from trigger
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(2);
    }

    @Test
    @DisplayName("No draw when non-historic creature is destroyed")
    void noDrawWhenNonHistoricDestroyed() {
        Permanent creature = addCreatureWithWard(player1, player1, new GrizzlyBears());

        castAndResolveDoomBlade(player1, creature);
        // No second pass needed — no trigger on stack

        // No cards drawn
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(0);
    }

    @Test
    @DisplayName("Draw 2 when enchanted legendary creature is bounced")
    void drawsWhenLegendaryBounced() {
        GrizzlyBears legendaryBears = new GrizzlyBears();
        legendaryBears.setSupertypes(Set.of(CardSupertype.LEGENDARY));
        Permanent creature = addCreatureWithWard(player1, player1, legendaryBears);

        castAndResolveDisperse(player1, creature);
        harness.passBothPriorities(); // resolve DrawCardEffect trigger

        // 1 (Bears returned to hand) + 2 (drew from trigger) = 3
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(3);
    }

    @Test
    @DisplayName("No draw when non-historic creature is bounced")
    void noDrawWhenNonHistoricBounced() {
        Permanent creature = addCreatureWithWard(player1, player1, new GrizzlyBears());

        castAndResolveDisperse(player1, creature);

        // 1 (Bears returned to hand) only
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(1);
    }

    @Test
    @DisplayName("Aura controller draws cards, not enchanted permanent's controller")
    void auraControllerDraws() {
        // Player 1 controls the aura on Player 2's legendary creature
        GrizzlyBears legendaryBears = new GrizzlyBears();
        legendaryBears.setSupertypes(Set.of(CardSupertype.LEGENDARY));
        Permanent creature = addCreatureWithWard(player2, player1, legendaryBears);

        int hand1Before = gd.playerHands.get(player1.getId()).size();

        // Player 2 (creature controller) can target own creature through hexproof
        castAndResolveDoomBlade(player2, creature);
        harness.passBothPriorities(); // resolve DrawCardEffect trigger

        // Aura controller (player1) draws 2 — player1's hand wasn't touched by setHand
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(hand1Before + 2);
        // Permanent controller (player2) doesn't draw from trigger — hand is 0 after casting DoomBlade
        assertThat(gd.playerHands.get(player2.getId()).size()).isEqualTo(0);
    }

    @Test
    @DisplayName("Ward can enchant a noncreature artifact and grants it hexproof")
    void enchantsNoncreatureArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new JoustingLance());
        harness.setHand(player1, List.of(new CuratorsWard()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castEnchantment(player1, 0, artifact.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Curator's Ward").getAttachedTo()).isEqualTo(artifact.getId());
        assertThat(gqs.hasKeyword(gd, artifact, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.hasKeyword(gd, findPermanent(player1, "Curator's Ward"), Keyword.HEXPROOF)).isFalse();
    }

    @Test
    @DisplayName("A nonlegendary Saga is historic and draws two when bounced")
    void drawsWhenSagaBounced() {
        Permanent saga = addCreatureWithWard(player1, player1, new TheFlameOfKeld());
        setDrawLibrary();

        castAndResolveBlink(saga);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        harness.assertInHand(player1, "The Flame of Keld");
        harness.assertInGraveyard(player1, "Curator's Ward");
    }

    @Test
    @DisplayName("A real legendary creature draws two when bounced")
    void drawsWhenDanithaBounced() {
        Permanent creature = addCreatureWithWard(player1, player1, new DanithaCapashenParagon());
        setDrawLibrary();

        castAndResolveBlink(creature);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        harness.assertInHand(player1, "Danitha Capashen, Paragon");
    }

    @Test
    @DisplayName("A permanent made legendary by In Bolas's Clutches was historic when it left")
    void drawsWhenGrantedLegendaryCreatureBounced() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new KrosanDruid());
        Permanent clutches = harness.addToBattlefieldAndReturn(player1, new InBolassClutches());
        clutches.setAttachedTo(creature.getId());
        Permanent ward = harness.addToBattlefieldAndReturn(player1, new CuratorsWard());
        ward.setAttachedTo(creature.getId());
        setDrawLibrary();
        assertThat(gqs.hasEffectiveSupertype(gd, creature, CardSupertype.LEGENDARY)).isTrue();

        castAndResolveBlink(creature);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        harness.assertInHand(player1, "Krosan Druid");
    }

    @Test
    @DisplayName("Bouncing Ward itself does not draw and removes the granted hexproof")
    void noDrawWhenWardBounced() {
        Permanent artifact = addCreatureWithWard(player1, player1, new JoustingLance());
        Permanent ward = findPermanent(player1, "Curator's Ward");

        castAndResolveBlink(ward);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertInHand(player1, "Curator's Ward");
        harness.assertOnBattlefield(player1, "Jousting Lance");
        assertThat(gqs.hasKeyword(gd, artifact, Keyword.HEXPROOF)).isFalse();
    }

    @Test
    @DisplayName("Each Ward on a historic permanent draws two when it leaves")
    void multipleWardsEachDraw() {
        Permanent artifact = addCreatureWithWard(player1, player1, new JoustingLance());
        Permanent secondWard = harness.addToBattlefieldAndReturn(player1, new CuratorsWard());
        secondWard.setAttachedTo(artifact.getId());
        setDrawLibrary();

        castAndResolveBlink(artifact);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(5);
        assertThat(gd.stack).isEmpty();
    }

    private void setDrawLibrary() {
        harness.setLibrary(player1, List.of(new KrosanDruid(), new KrosanDruid(),
                new KrosanDruid(), new KrosanDruid()));
    }

    private void castAndResolveBlink(Permanent target) {
        harness.setHand(player1, List.of(new BlinkOfAnEye()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player1, 0, target.getId());
    }

    private Permanent addCreatureWithWard(Player creatureController, Player auraController, Card creatureCard) {
        Permanent creature = harness.addToBattlefieldAndReturn(creatureController, creatureCard);
        Permanent ward = harness.addToBattlefieldAndReturn(auraController, new CuratorsWard());
        ward.setAttachedTo(creature.getId());

        return creature;
    }

    private Permanent addArtifactWithWard(Player artifactController, Player auraController) {
        return addCreatureWithWard(artifactController, auraController, new Spellbook());
    }

    private void castAndResolveDoomBlade(Player controller, Permanent target) {
        harness.forceActivePlayer(controller);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(controller, List.of(new DoomBlade()));
        harness.addMana(controller, ManaColor.BLACK, 2);
        harness.castAndResolveInstant(controller, 0, target.getId());
    }

    private void castAndResolveShatter(Player controller, Permanent target) {
        harness.forceActivePlayer(controller);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(controller, List.of(new Shatter()));
        harness.addMana(controller, ManaColor.RED, 2);
        harness.castAndResolveInstant(controller, 0, target.getId());
    }

    private void castAndResolveDisperse(Player controller, Permanent target) {
        harness.forceActivePlayer(controller);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(controller, List.of(new Disperse()));
        harness.addMana(controller, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(controller, 0, target.getId());
    }
}
