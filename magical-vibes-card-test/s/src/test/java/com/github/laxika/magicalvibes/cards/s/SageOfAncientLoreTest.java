package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SageOfAncientLore.class, Forest.class})
class SageOfAncientLoreTest extends BaseCardTest {

    @Test
    @DisplayName("Front face power and toughness equal the controller's hand size")
    void frontFaceUsesControllerHandSize() {
        Permanent sage = addCreatureReady(player1, new SageOfAncientLore());
        harness.setHand(player1, List.of(new Forest(), new Forest()));
        harness.setHand(player2, List.of(new Forest()));

        assertThat(gqs.getEffectivePower(gd, sage)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, sage)).isEqualTo(2);
    }

    @Test
    @DisplayName("Entering the battlefield draws a card")
    void entersAndDrawsCard() {
        harness.setHand(player1, List.of(new SageOfAncientLore(), new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.setLibrary(player1, List.of(new Forest()));

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInHand(player1, "Forest");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Front face transforms when no spells were cast last turn")
    void transformsToWerewolfWhenNoSpellsWereCast() {
        Permanent sage = addCreatureReady(player1, new SageOfAncientLore());
        harness.setHand(player1, List.of(new Forest()));
        gd.spellsCastLastTurn.clear();

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(sage.isTransformed()).isTrue();
    }

    @Test
    @DisplayName("Back face power and toughness equal all players' total hand size")
    void backFaceUsesTotalHandSize() {
        Permanent sage = addCreatureReady(player1, new SageOfAncientLore());
        harness.setHand(player1, List.of(new Forest()));
        gd.spellsCastLastTurn.clear();
        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Forest()));
        harness.setHand(player2, List.of(new Forest(), new Forest()));

        assertThat(gqs.getEffectivePower(gd, sage)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, sage)).isEqualTo(3);
    }

    @Test
    @DisplayName("Back face transforms when a player cast two spells last turn")
    void transformsBackWhenTwoSpellsWereCast() {
        Permanent sage = addCreatureReady(player1, new SageOfAncientLore());
        harness.setHand(player1, List.of(new Forest()));
        gd.spellsCastLastTurn.clear();
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        assertThat(sage.isTransformed()).isTrue();

        gd.spellsCastLastTurn.clear();
        gd.spellsCastLastTurn.put(player2.getId(), 2);
        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(sage.isTransformed()).isFalse();
        assertThat(gqs.getEffectivePower(gd, sage)).isEqualTo(gd.playerHands.get(player1.getId()).size());
        assertThat(gqs.getEffectiveToughness(gd, sage)).isEqualTo(gd.playerHands.get(player1.getId()).size());
    }

    @Test
    void emptyHandCausesDeathBeforeEnterTriggerButStillDraws() {
        harness.setLibrary(player1, List.of(new Forest()));
        harness.castFromHand(player1, new SageOfAncientLore(), "{4}{G}");

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Sage of Ancient Lore");
        harness.assertInGraveyard(player1, "Sage of Ancient Lore");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertInHand(player1, "Forest");
        harness.assertNotOnBattlefield(player1, "Sage of Ancient Lore");
    }

    @Test
    void doesNotTriggerWhenOpponentCastOneSpellLastTurn() {
        Permanent sage = addCreatureReady(player1, new SageOfAncientLore());
        harness.setHand(player1, List.of(new Forest()));
        gd.spellsCastLastTurn.clear();
        gd.spellsCastLastTurn.put(player2.getId(), 1);

        advanceToUpkeep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(sage.isTransformed()).isFalse();
    }

    @Test
    void transformsOnOpponentsUpkeepAndDoesNotDrawForTransformation() {
        Permanent sage = addCreatureReady(player1, new SageOfAncientLore());
        harness.setHand(player1, List.of(new Forest()));
        harness.setLibrary(player1, List.of(new Forest()));
        gd.spellsCastLastTurn.clear();

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(sage.isTransformed()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void oneSpellPerPlayerDoesNotTransformBack() {
        Permanent sage = addCreatureReady(player1, new SageOfAncientLore());
        harness.setHand(player1, List.of(new Forest()));
        gd.spellsCastLastTurn.clear();
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        assertThat(sage.isTransformed()).isTrue();

        gd.spellsCastLastTurn.put(player1.getId(), 1);
        gd.spellsCastLastTurn.put(player2.getId(), 1);
        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(sage.isTransformed()).isTrue();
    }

    @Test
    void frontFaceTracksHandChangesAndDiesWhenHandBecomesEmpty() {
        Permanent sage = addCreatureReady(player1, new SageOfAncientLore());
        harness.setHand(player1, List.of(new Forest(), new Forest()));
        harness.setHand(player2, List.of(new Forest(), new Forest(), new Forest()));
        assertThat(gqs.getEffectivePower(gd, sage)).isEqualTo(2);

        harness.setHand(player1, List.of(new Forest()));
        assertThat(gqs.getEffectivePower(gd, sage)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, sage)).isEqualTo(1);

        harness.setHand(player1, List.of());
        harness.runStateBasedActions();

        harness.assertNotOnBattlefield(player1, "Sage of Ancient Lore");
        harness.assertInGraveyard(player1, "Sage of Ancient Lore");
    }

    @Test
    void backFaceSurvivesEmptyControllerHandWhileOpponentHasCards() {
        Permanent sage = addCreatureReady(player1, new SageOfAncientLore());
        harness.setHand(player1, List.of(new Forest()));
        harness.setHand(player2, List.of(new Forest(), new Forest()));
        gd.spellsCastLastTurn.clear();
        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.setHand(player1, List.of());
        harness.runStateBasedActions();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(sage);
        assertThat(gqs.getEffectivePower(gd, sage)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, sage)).isEqualTo(2);

        harness.setHand(player2, List.of());
        harness.runStateBasedActions();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(sage);
        harness.assertInGraveyard(player1, "Sage of Ancient Lore");
    }

    @Test
    void backFaceAttacksWithoutTappingAndTramplesOverBlocker() {
        Permanent sage = addCreatureReady(player1, new SageOfAncientLore());
        harness.setHand(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.setHand(player2, List.of(new Forest()));
        harness.setLife(player2, 20);
        gd.spellsCastLastTurn.clear();
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        Permanent blocker = addCreatureReady(player2, new SageOfAncientLore());

        declareAttackersAndPrepareBlockers(List.of(0));
        assertThat(sage.isTapped()).isFalse();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 1,
                player2.getId(), 3));

        harness.assertLife(player2, 17);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(sage);
        assertThat(sage.isTapped()).isFalse();
    }
}
