package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.c.CavesOfKoilos;
import com.github.laxika.magicalvibes.cards.d.Dodecapod;
import com.github.laxika.magicalvibes.cards.i.Index;
import com.github.laxika.magicalvibes.cards.m.MournfulZombie;
import com.github.laxika.magicalvibes.cards.w.WoodlandChangeling;
import com.github.laxika.magicalvibes.cards.z.ZombieBoa;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GraveDefiler.class, MournfulZombie.class, ZombieBoa.class, Dodecapod.class,
        CavesOfKoilos.class, Index.class, WoodlandChangeling.class})
class GraveDefilerTest extends BaseCardTest {

    private void finishAnyReorder() {
        PendingInteraction.LibraryReorder reorder = gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class);
        if (reorder != null) {
            harness.getGameService().handleInteractionAnswer(gd, player1,
                    new InteractionAnswer.CardOrder(IntStream.range(0, reorder.cards().size()).boxed().toList()));
        }
    }

    private void castGraveDefiler() {
        harness.castFromHand(player1, new GraveDefiler(), "{3}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Zombie cards among the top four go to hand and the rest go to the bottom")
    void zombieCardsGoToHand() {
        Card zombie1 = new MournfulZombie();
        Card zombie2 = new ZombieBoa();
        Card nonZombieCreature = new Dodecapod();
        Card land = new CavesOfKoilos();
        Card sorcery = new Index();
        harness.setLibrary(player1, List.of(zombie1, nonZombieCreature, zombie2, land, sorcery));

        castGraveDefiler();
        finishAnyReorder();

        assertThat(gd.playerHands.get(player1.getId())).contains(zombie1, zombie2);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(nonZombieCreature, land);
        assertThat(gd.playerDecks.get(player1.getId())).contains(nonZombieCreature, land);
    }

    @Test
    @DisplayName("Only the top four cards are revealed")
    void onlyTopFourAreRevealed() {
        Card nonZombieCreature1 = new Dodecapod();
        Card land = new CavesOfKoilos();
        Card sorcery = new Index();
        Card nonZombieCreature2 = new Dodecapod();
        Card deepZombie = new ZombieBoa();
        harness.setLibrary(player1, List.of(nonZombieCreature1, land, sorcery, nonZombieCreature2, deepZombie));

        castGraveDefiler();
        finishAnyReorder();

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(deepZombie);
        assertThat(gd.playerDecks.get(player1.getId())).contains(deepZombie);
    }

    @Test
    @DisplayName("A changeling card counts as a Zombie card")
    void changelingCountsAsZombie() {
        Card changeling = new WoodlandChangeling();
        Card nonZombie = new Dodecapod();
        harness.setLibrary(player1, List.of(changeling, nonZombie));

        castGraveDefiler();
        finishAnyReorder();

        assertThat(gd.playerHands.get(player1.getId())).contains(changeling);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(nonZombie);
    }

    @Test
    @DisplayName("Paying {1}{B} grants Grave Defiler a regeneration shield")
    void payGrantsRegenerationShield() {
        Permanent defiler = addCreatureReady(player1, new GraveDefiler());
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(defiler.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Regeneration shield saves Grave Defiler from lethal combat damage")
    void regenerationSavesFromLethalCombatDamage() {
        Permanent defiler = addCreatureReady(player1, new GraveDefiler());
        defiler.setRegenerationShield(1);
        defiler.setBlocking(true);
        defiler.addBlockingTarget(0);

        Permanent attacker = addCreatureReady(player2, new Dodecapod());
        attacker.setAttacking(true);

        resolveCombat(player2);

        harness.assertOnBattlefield(player1, "Grave Defiler");
        assertThat(defiler.isTapped()).isTrue();
        assertThat(defiler.getRegenerationShield()).isZero();
    }
}
