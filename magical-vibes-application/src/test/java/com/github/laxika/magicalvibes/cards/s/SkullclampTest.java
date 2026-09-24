package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CrazedGoblin;
import com.github.laxika.magicalvibes.cards.d.DrossGolem;
import com.github.laxika.magicalvibes.cards.e.EchoingDecay;
import com.github.laxika.magicalvibes.model.Card;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Skullclamp.class, DrossGolem.class, EchoingDecay.class, CrazedGoblin.class})
class SkullclampTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +1/-1")
    void equippedCreatureGetsBoost() {
        Permanent creature = addCreatureReady(player1, new DrossGolem());
        Permanent skullclamp = addSkullclampReady(player1);
        skullclamp.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(1);
    }

    @Test
    @DisplayName("Unattached Skullclamp does not boost a creature")
    void unattachedSkullclampDoesNotBoostCreature() {
        Permanent creature = addCreatureReady(player1, new DrossGolem());
        addSkullclampReady(player1);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Equip attaches Skullclamp to a creature you control")
    void equipAttaches() {
        Permanent skullclamp = addSkullclampReady(player1);
        Permanent creature = addCreatureReady(player1, new DrossGolem());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(skullclamp.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Equip cannot target an opponent's creature")
    void cannotEquipOpponentCreature() {
        Permanent skullclamp = addSkullclampReady(player1);
        Permanent opponentCreature = addCreatureReady(player2, new DrossGolem());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(skullclamp.getAttachedTo()).isNull();
    }

    @Test
    @DisplayName("Draws two cards when the equipped creature dies")
    void drawsTwoCardsWhenEquippedCreatureDies() {
        Permanent creature = addCreatureReady(player1, new DrossGolem());
        Permanent skullclamp = addSkullclampReady(player1);
        skullclamp.setAttachedTo(creature.getId());
        Card firstDraw = new DrossGolem();
        Card secondDraw = new DrossGolem();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(firstDraw, secondDraw));

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new EchoingDecay()));
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.castAndResolveInstant(player2, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .hasSize(2)
                .anyMatch(card -> card.getId().equals(firstDraw.getId()))
                .anyMatch(card -> card.getId().equals(secondDraw.getId()));
    }

    @Test
    @DisplayName("Draws for the Equipment controller when an opponent controls the equipped creature")
    void drawsForEquipmentControllerWhenOpponentControlsEquippedCreature() {
        Permanent creature = addCreatureReady(player1, new DrossGolem());
        Permanent skullclamp = addSkullclampReady(player2);
        skullclamp.setAttachedTo(creature.getId());
        Card firstDraw = new DrossGolem();
        Card secondDraw = new DrossGolem();
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(firstDraw, secondDraw));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new EchoingDecay()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castAndResolveInstant(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId()))
                .hasSize(2)
                .anyMatch(card -> card.getId().equals(firstDraw.getId()))
                .anyMatch(card -> card.getId().equals(secondDraw.getId()));
    }

    @Test
    @DisplayName("Does not draw when an unequipped creature dies")
    void doesNotDrawWhenUnequippedCreatureDies() {
        Permanent equippedCreature = addCreatureReady(player1, new DrossGolem());
        Permanent skullclamp = addSkullclampReady(player1);
        skullclamp.setAttachedTo(equippedCreature.getId());
        Permanent unrelatedCreature = addCreatureReady(player1, new CrazedGoblin());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new DrossGolem(), new DrossGolem()));

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new EchoingDecay()));
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.castAndResolveInstant(player2, 0, unrelatedCreature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    private Permanent addSkullclampReady(Player player) {
        Permanent skullclamp = harness.addToBattlefieldAndReturn(player, new Skullclamp());
        skullclamp.setSummoningSick(false);
        return skullclamp;
    }
}
