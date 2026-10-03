package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.m.MoorlandDrifter;
import com.github.laxika.magicalvibes.cards.v.ValMaroonedSurveyor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DeclarationInStone.class, DevilthornFox.class, MoorlandDrifter.class, ValMaroonedSurveyor.class})
class DeclarationInStoneTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles same-name creatures only from the target controller and investigates for nontokens")
    void exilesControlledSameNameCreaturesAndInvestigatesForNontokens() {
        harness.addToBattlefield(player1, new DevilthornFox());
        harness.addToBattlefield(player2, new DevilthornFox());
        harness.addToBattlefield(player2, new DevilthornFox());
        DevilthornFox tokenBear = new DevilthornFox();
        tokenBear.setToken(true);
        harness.addToBattlefield(player2, tokenBear);
        harness.addToBattlefield(player2, new MoorlandDrifter());
        harness.setHand(player1, List.of(new DeclarationInStone()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID targetId = harness.getPermanentId(player2, "Devilthorn Fox");
        harness.castAndResolveSorcery(player1, 0, targetId);

        harness.assertOnBattlefield(player1, "Devilthorn Fox");
        harness.assertOnBattlefield(player2, "Moorland Drifter");
        harness.assertNotOnBattlefield(player2, "Devilthorn Fox");
        assertThat(findPermanents(player2, "Clue")).hasSize(2);
    }

    @Test
    @DisplayName("Does not create Clues when the target is removed before resolution")
    void doesNotResolveWhenTargetLeaves() {
        harness.addToBattlefield(player2, new DevilthornFox());
        harness.setHand(player1, List.of(new DeclarationInStone()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID targetId = harness.getPermanentId(player2, "Devilthorn Fox");
        harness.castSorcery(player1, 0, targetId);
        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        assertThat(findPermanents(player2, "Clue")).isEmpty();
    }

    @Test
    void tokenTargetExilesMatchingNontokenAndInvestigatesOnlyForIt() {
        DevilthornFox token = new DevilthornFox();
        token.setToken(true);
        Permanent target = harness.addToBattlefieldAndReturn(player2, token);
        harness.addToBattlefield(player2, new DevilthornFox());
        harness.setHand(player1, List.of(new DeclarationInStone()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castAndResolveSorcery(player1, 0, target.getId());
        harness.assertNotOnBattlefield(player2, "Devilthorn Fox");
        assertThat(findPermanents(player2, "Clue")).hasSize(1);
    }

    @Test
    void tokenOnlyExileDoesNotInvestigate() {
        DevilthornFox first = new DevilthornFox();
        first.setToken(true);
        DevilthornFox second = new DevilthornFox();
        second.setToken(true);
        Permanent target = harness.addToBattlefieldAndReturn(player2, first);
        harness.addToBattlefield(player2, second);
        harness.setHand(player1, List.of(new DeclarationInStone()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castAndResolveSorcery(player1, 0, target.getId());
        harness.assertNotOnBattlefield(player2, "Devilthorn Fox");
        assertThat(findPermanents(player2, "Clue")).isEmpty();
        assertThat(gd.playersWhoInvestigatedThisTurn).doesNotContain(player2.getId());
    }

    @Test
    void faceDownTargetExilesOnlyItself() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DevilthornFox());
        target.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        Permanent faceUp = harness.addToBattlefieldAndReturn(player2, new DevilthornFox());
        Permanent otherFaceDown = harness.addToBattlefieldAndReturn(player2, new DevilthornFox());
        otherFaceDown.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        harness.setHand(player1, List.of(new DeclarationInStone()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castAndResolveSorcery(player1, 0, target.getId());
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(faceUp, otherFaceDown)
                .doesNotContain(target);
        assertThat(findPermanents(player2, "Clue")).hasSize(1);
    }

    @Test
    void faceUpTargetDoesNotExileFaceDownCopy() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DevilthornFox());
        Permanent faceDown = harness.addToBattlefieldAndReturn(player2, new DevilthornFox());
        faceDown.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        harness.setHand(player1, List.of(new DeclarationInStone()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castAndResolveSorcery(player1, 0, target.getId());
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(faceDown).doesNotContain(target);
        assertThat(findPermanents(player2, "Clue")).hasSize(1);
    }

    @Test
    void investigatesSeparatelyForEachNontokenCreature() {
        harness.addToBattlefield(player2, new ValMaroonedSurveyor());
        harness.addToBattlefield(player2, new DevilthornFox());
        harness.addToBattlefield(player2, new DevilthornFox());
        harness.setHand(player1, List.of(new DeclarationInStone()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.castAndResolveSorcery(player1, 0, harness.getPermanentId(player2, "Devilthorn Fox"));
        resolveAllTriggers();
        assertThat(findPermanents(player2, "Clue")).hasSize(2);
        harness.assertLife(player1, 16);
        harness.assertLife(player2, 24);
    }
}
