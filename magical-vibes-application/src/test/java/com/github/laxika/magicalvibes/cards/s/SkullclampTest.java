package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.Deathmark;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SkullclampTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +1/-1")
    void equippedCreatureGetsBoost() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent skullclamp = addSkullclampReady(player1);
        skullclamp.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(1);
    }

    @Test
    @DisplayName("Equip attaches Skullclamp to a creature you control")
    void equipAttaches() {
        Permanent skullclamp = addSkullclampReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(skullclamp.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Equip cannot target an opponent's creature")
    void cannotEquipOpponentCreature() {
        Permanent skullclamp = addSkullclampReady(player1);
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(skullclamp.getAttachedTo()).isNull();
    }

    @Test
    @DisplayName("Draws two cards when the equipped creature dies")
    void drawsTwoCardsWhenEquippedCreatureDies() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent skullclamp = addSkullclampReady(player1);
        skullclamp.setAttachedTo(creature.getId());
        Card firstDraw = new GrizzlyBears();
        Card secondDraw = new GrizzlyBears();
        gd.playerDecks.get(player1.getId()).addFirst(secondDraw);
        gd.playerDecks.get(player1.getId()).addFirst(firstDraw);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Deathmark()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.castSorcery(player2, 0, creature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(firstDraw.getId()))
                .anyMatch(card -> card.getId().equals(secondDraw.getId()));
    }

    private Permanent addSkullclampReady(Player player) {
        Permanent skullclamp = new Permanent(new Skullclamp());
        skullclamp.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(skullclamp);
        return skullclamp;
    }
}
