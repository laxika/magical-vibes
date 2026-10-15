package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.Deathmark;
import com.github.laxika.magicalvibes.cards.e.EndHostilities;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StoneHavenOutfitter.class, GrizzlyBears.class, LeoninScimitar.class, Deathmark.class,
        WrathOfGod.class, EndHostilities.class})
class StoneHavenOutfitterTest extends BaseCardTest {

    @Test
    void boostsEquippedCreaturesYouControlIncludingItself() {
        Permanent outfitter = addCreatureReady(player1, new StoneHavenOutfitter());
        Permanent equipped = addCreatureReady(player1, new GrizzlyBears());
        Permanent unequipped = addCreatureReady(player1, new GrizzlyBears());
        Permanent outfitterEquipment = addEquipment(player1);
        Permanent creatureEquipment = addEquipment(player1);
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        Permanent opponentEquipment = addEquipment(player2);

        outfitterEquipment.setAttachedTo(outfitter.getId());
        creatureEquipment.setAttachedTo(equipped.getId());
        opponentEquipment.setAttachedTo(opponentCreature.getId());

        assertThat(gqs.getEffectivePower(gd, outfitter)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, outfitter)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, equipped)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, equipped)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, unequipped)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, unequipped)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opponentCreature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, opponentCreature)).isEqualTo(3);
    }

    @Test
    void drawsWhenAnotherEquippedCreatureYouControlDies() {
        Card drawn = new GrizzlyBears();
        harness.setLibrary(player1, List.of(drawn));
        Permanent outfitter = addCreatureReady(player1, new StoneHavenOutfitter());
        Permanent equipped = addCreatureReady(player1, new GrizzlyBears());
        Permanent equipment = addEquipment(player1);
        equipment.setAttachedTo(equipped.getId());
        int handBefore = gd.playerHands.get(player1.getId()).size();

        destroyWithDeathmark(player2, equipped);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1).contains(drawn);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(outfitter);
    }

    @Test
    void doesNotDrawWhenAnUnequippedCreatureYouControlDies() {
        Card drawn = new GrizzlyBears();
        harness.setLibrary(player1, List.of(drawn));
        addCreatureReady(player1, new StoneHavenOutfitter());
        Permanent unequipped = addCreatureReady(player1, new GrizzlyBears());
        int handBefore = gd.playerHands.get(player1.getId()).size();

        destroyWithDeathmark(player2, unequipped);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore).doesNotContain(drawn);
    }

    @Test
    void drawsWhenItselfDiesWhileEquipped() {
        Card drawn = new GrizzlyBears();
        harness.setLibrary(player1, List.of(drawn));
        Permanent outfitter = addCreatureReady(player1, new StoneHavenOutfitter());
        Permanent equipment = addEquipment(player1);
        equipment.setAttachedTo(outfitter.getId());
        int handBefore = gd.playerHands.get(player1.getId()).size();

        destroyWithDeathmark(player2, outfitter);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1).contains(drawn);
    }

    @Test
    void doesNotDrawWhenItselfDiesUnequipped() {
        Card drawn = new GrizzlyBears();
        harness.setLibrary(player1, List.of(drawn));
        Permanent outfitter = addCreatureReady(player1, new StoneHavenOutfitter());
        int handBefore = gd.playerHands.get(player1.getId()).size();

        destroyWithDeathmark(player2, outfitter);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore).doesNotContain(drawn);
    }

    @Test
    void doesNotDrawWhenAnOpponentsEquippedCreatureDies() {
        Card drawn = new GrizzlyBears();
        harness.setLibrary(player1, List.of(drawn));
        addCreatureReady(player1, new StoneHavenOutfitter());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        addEquipment(player2).setAttachedTo(opponentCreature.getId());
        int handBefore = gd.playerHands.get(player1.getId()).size();

        destroyWithDeathmark(player2, opponentCreature);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore).doesNotContain(drawn);
    }

    @Test
    void drawsOnlyOnceForACreatureWithMultipleEquipmentRegardlessOfEquipmentController() {
        Card drawn = new GrizzlyBears();
        Card remaining = new GrizzlyBears();
        harness.setLibrary(player1, List.of(drawn, remaining));
        addCreatureReady(player1, new StoneHavenOutfitter());
        Permanent equipped = addCreatureReady(player1, new GrizzlyBears());
        addEquipment(player2).setAttachedTo(equipped.getId());
        addEquipment(player2).setAttachedTo(equipped.getId());
        int handBefore = gd.playerHands.get(player1.getId()).size();

        assertThat(gqs.getEffectivePower(gd, equipped)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, equipped)).isEqualTo(5);
        destroyWithDeathmark(player2, equipped);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1)
                .contains(drawn).doesNotContain(remaining);
    }

    @Test
    void drawsForEachEquippedCreatureWhenOutfitterDiesSimultaneously() {
        Card first = new GrizzlyBears();
        Card second = new GrizzlyBears();
        harness.setLibrary(player1, List.of(first, second));
        Permanent outfitter = addCreatureReady(player1, new StoneHavenOutfitter());
        Permanent equipped = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        addEquipment(player1).setAttachedTo(outfitter.getId());
        addEquipment(player1).setAttachedTo(equipped.getId());
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player2, new WrathOfGod(), "{2}{W}{W}");
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 2).contains(first, second);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(outfitter, equipped);
    }

    @Test
    void drawsWhenEquipmentAndEquippedCreaturesDieSimultaneously() {
        Card first = new GrizzlyBears();
        Card second = new GrizzlyBears();
        harness.setLibrary(player1, List.of(first, second));
        Permanent firstEquipment = addEquipment(player1);
        Permanent secondEquipment = addEquipment(player1);
        Permanent outfitter = addCreatureReady(player1, new StoneHavenOutfitter());
        Permanent equipped = addCreatureReady(player1, new GrizzlyBears());
        firstEquipment.setAttachedTo(outfitter.getId());
        secondEquipment.setAttachedTo(equipped.getId());
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player2, new EndHostilities(), "{3}{W}{W}");
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 2).contains(first, second);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }

    private Permanent addEquipment(Player player) {
        return harness.addToBattlefieldAndReturn(player, new LeoninScimitar());
    }

    private void destroyWithDeathmark(Player caster, Permanent target) {
        harness.setHand(caster, List.of(new Deathmark()));
        harness.addMana(caster, ManaColor.BLACK, 1);
        harness.forceActivePlayer(caster);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castAndResolveSorcery(caster, 0, target.getId());
        harness.passBothPriorities();
    }
}
