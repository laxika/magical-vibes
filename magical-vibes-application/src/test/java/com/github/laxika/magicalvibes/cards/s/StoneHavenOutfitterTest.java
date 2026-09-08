package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.Deathmark;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
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

@CardUsed({StoneHavenOutfitter.class, GrizzlyBears.class, LeoninScimitar.class, Deathmark.class})
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

    private Permanent addEquipment(Player player) {
        return harness.addToBattlefieldAndReturn(player, new LeoninScimitar());
    }

    private void destroyWithDeathmark(Player caster, Permanent target) {
        harness.setHand(caster, List.of(new Deathmark()));
        harness.addMana(caster, ManaColor.BLACK, 1);
        harness.forceActivePlayer(caster);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castSorcery(caster, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
