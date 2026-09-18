package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GroomsFinery;
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

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HemlockVial.class, GroomsFinery.class, GrizzlyBears.class})
class HemlockVialTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield draws a card and loses 1 life")
    void entersDrawsAndLosesLife() {
        harness.setHand(player1, List.of(new HemlockVial()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 1);
    }

    @Test
    @DisplayName("Ability grants deathtouch to equipped creatures and controlled Equipment")
    void abilityGrantsDeathtouchToEquippedCreaturesAndEquipment() {
        addReadyVial(player1);
        Permanent equippedCreature = addCreatureReady(player2);
        Permanent controlledEquipment = addReadyEquipment(player1);
        Permanent opponentEquipment = addReadyEquipment(player2);
        opponentEquipment.setAttachedTo(equippedCreature.getId());
        Permanent unequippedCreature = addCreatureReady(player2);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Hemlock Vial");
        assertThat(gqs.hasKeyword(gd, controlledEquipment, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.hasKeyword(gd, equippedCreature, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponentEquipment, Keyword.DEATHTOUCH)).isFalse();
        assertThat(gqs.hasKeyword(gd, unequippedCreature, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    @DisplayName("Granted deathtouch wears off at end of turn")
    void deathtouchWearsOffAtEndOfTurn() {
        addReadyVial(player1);
        Permanent equipment = addReadyEquipment(player1);
        Permanent equippedCreature = addCreatureReady(player2);
        equipment.setAttachedTo(equippedCreature.getId());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, equipment, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.hasKeyword(gd, equippedCreature, Keyword.DEATHTOUCH)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, equipment, Keyword.DEATHTOUCH)).isFalse();
        assertThat(gqs.hasKeyword(gd, equippedCreature, Keyword.DEATHTOUCH)).isFalse();
    }

    private Permanent addReadyVial(Player player) {
        Permanent vial = new Permanent(new HemlockVial());
        vial.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(vial);
        return vial;
    }

    private Permanent addReadyEquipment(Player player) {
        Permanent equipment = new Permanent(new GroomsFinery());
        equipment.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(equipment);
        return equipment;
    }

    private Permanent addCreatureReady(Player player) {
        Permanent creature = new Permanent(new GrizzlyBears());
        creature.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(creature);
        return creature;
    }
}
