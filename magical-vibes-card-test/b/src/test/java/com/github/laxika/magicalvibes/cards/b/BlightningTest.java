package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.e.ElspethKnightErrant;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.d.DregscapeZombie;
import com.github.laxika.magicalvibes.cards.w.WiltLeafLiege;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Blightning.class, ElspethKnightErrant.class, Forest.class, DregscapeZombie.class,
        WiltLeafLiege.class})
class BlightningTest extends BaseCardTest {

    // "Blightning deals 3 damage to target player or planeswalker. That player or that
    //  planeswalker's controller discards two cards."

    private void giveBlightning() {
        harness.setHand(player1, List.of(new Blightning()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.RED, 1);
    }

    @Test
    @DisplayName("Deals 3 damage to the targeted player and makes them discard two cards")
    void damageAndDiscardToTargetPlayer() {
        harness.setHand(player2, List.of(new DregscapeZombie(), new Forest(), new Forest()));
        giveBlightning();
        int p2LifeBefore = gd.getLife(player2.getId());

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(p2LifeBefore - 3);

        // The targeted player (not the caster) discards two cards of their choice.
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).remainingCount()).isEqualTo(2);
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).playerId()).isEqualTo(player2.getId());

        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Empty-handed target still takes 3 damage with no discard")
    void emptyHandTargetStillTakesDamage() {
        harness.setHand(player2, List.of());
        giveBlightning();
        int p2LifeBefore = gd.getLife(player2.getId());

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(p2LifeBefore - 3);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Targeting a planeswalker removes 3 loyalty and its controller discards two cards")
    void damageAndDiscardToPlaneswalkerController() {
        Permanent elspeth = harness.addToBattlefieldAndReturn(player2, new ElspethKnightErrant());
        elspeth.setCounterCount(CounterType.LOYALTY, 4);

        harness.setHand(player2, List.of(new DregscapeZombie(), new Forest()));
        giveBlightning();

        harness.castSorcery(player1, 0, elspeth.getId());
        harness.passBothPriorities();

        assertThat(elspeth.getCounterCount(CounterType.LOYALTY)).isEqualTo(1); // 4 - 3

        // The planeswalker's controller discards, not the caster.
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).playerId()).isEqualTo(player2.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).remainingCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot target a creature")
    void cannotTargetCreature() {
        harness.addToBattlefield(player2, new DregscapeZombie());
        giveBlightning();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0,
                harness.getPermanentId(player2, "Dregscape Zombie")))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A target with one card discards it and resolution completes")
    void targetWithOneCardDiscardsOnlyAvailableCard() {
        harness.setHand(player2, List.of(new Forest()));
        giveBlightning();
        int lifeBefore = gd.getLife(player2.getId());

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);

        harness.assertLife(player2, lifeBefore - 3);
        harness.assertInGraveyard(player2, "Forest");
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Lethal planeswalker damage still makes its controller discard")
    void lethalPlaneswalkerDamageStillDiscards() {
        Permanent elspeth = harness.addToBattlefieldAndReturn(player2, new ElspethKnightErrant());
        elspeth.setCounterCount(CounterType.LOYALTY, 3);
        harness.setHand(player2, List.of(new DregscapeZombie(), new Forest()));
        giveBlightning();
        int lifeBefore = gd.getLife(player2.getId());

        harness.castSorcery(player1, 0, elspeth.getId());
        harness.passBothPriorities();
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).playerId())
                .isEqualTo(player2.getId());
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        harness.assertLife(player2, lifeBefore);
        harness.assertInGraveyard(player2, "Elspeth, Knight-Errant");
        harness.assertInGraveyard(player2, "Dregscape Zombie");
        harness.assertInGraveyard(player2, "Forest");
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A planeswalker leaving before resolution prevents both damage and discard")
    void missingPlaneswalkerTargetDoesNotDiscard() {
        Permanent elspeth = harness.addToBattlefieldAndReturn(player2, new ElspethKnightErrant());
        elspeth.setCounterCount(CounterType.LOYALTY, 4);
        harness.setHand(player2, List.of(new DregscapeZombie(), new Forest()));
        giveBlightning();
        int lifeBefore = gd.getLife(player2.getId());

        harness.castSorcery(player1, 0, elspeth.getId());
        gd.playerBattlefields.get(player2.getId()).remove(elspeth);
        gd.playerHands.get(player2.getId()).add(elspeth.getCard());
        harness.passBothPriorities();

        harness.assertLife(player2, lifeBefore);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(3);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Blightning");
    }

    @Test
    @DisplayName("Targeting yourself does not apply opponent-caused discard replacements")
    @CardUsed({Blightning.class, WiltLeafLiege.class, Forest.class})
    void selfTargetDoesNotPutWiltLeafLiegeOntoBattlefield() {
        harness.setHand(player1, List.of(new Blightning(), new WiltLeafLiege(), new Forest()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.RED, 1);
        int lifeBefore = gd.getLife(player1.getId());

        harness.castSorcery(player1, 0, player1.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        harness.assertLife(player1, lifeBefore - 3);
        harness.assertInGraveyard(player1, "Wilt-Leaf Liege");
        harness.assertNotOnBattlefield(player1, "Wilt-Leaf Liege");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Targeting your own planeswalker does not apply opponent-caused discard replacements")
    @CardUsed({Blightning.class, ElspethKnightErrant.class, WiltLeafLiege.class, Forest.class})
    void ownPlaneswalkerDoesNotPutWiltLeafLiegeOntoBattlefield() {
        Permanent elspeth = harness.addToBattlefieldAndReturn(player1, new ElspethKnightErrant());
        elspeth.setCounterCount(CounterType.LOYALTY, 4);
        harness.setHand(player1, List.of(new Blightning(), new WiltLeafLiege(), new Forest()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.RED, 1);
        int lifeBefore = gd.getLife(player1.getId());

        harness.castSorcery(player1, 0, elspeth.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(elspeth.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        harness.assertLife(player1, lifeBefore);
        harness.assertInGraveyard(player1, "Wilt-Leaf Liege");
        harness.assertNotOnBattlefield(player1, "Wilt-Leaf Liege");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("An opponent discarding Wilt-Leaf Liege puts it onto the battlefield")
    @CardUsed({Blightning.class, WiltLeafLiege.class, Forest.class})
    void opponentDiscardAppliesWiltLeafLiegeReplacement() {
        harness.setHand(player2, List.of(new WiltLeafLiege(), new Forest()));
        giveBlightning();
        int lifeBefore = gd.getLife(player2.getId());

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        harness.assertLife(player2, lifeBefore - 3);
        harness.assertOnBattlefield(player2, "Wilt-Leaf Liege");
        harness.assertNotInGraveyard(player2, "Wilt-Leaf Liege");
        harness.assertInGraveyard(player2, "Forest");
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
