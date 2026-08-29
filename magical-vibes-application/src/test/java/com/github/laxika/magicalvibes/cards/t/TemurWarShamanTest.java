package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TemurWarShamanTest extends BaseCardTest {

    @Test
    void enteringManifestsTopCard() {
        Card topCard = new GrizzlyBears();
        harness.setHand(player1, List.of(new TemurWarShaman()));
        harness.setLibrary(player1, List.of(topCard));
        addShamanMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.isManifested()
                        && permanent.isFaceDown()
                        && permanent.getCard().getId().equals(topCard.getId()));
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void turnedUpCreatureMayFightOpponentCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SuntailHawk());
        Permanent manifested = resolveShamanWithManifestedCreature();
        addGrizzlyBearsFaceUpMana();

        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(manifested));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, target.getId());

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(manifested);
    }

    @Test
    void decliningFaceUpFightLeavesOpponentCreatureAlive() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SuntailHawk());
        Permanent manifested = resolveShamanWithManifestedCreature();
        addGrizzlyBearsFaceUpMana();

        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(manifested));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
    }

    private Permanent resolveShamanWithManifestedCreature() {
        harness.setHand(player1, List.of(new TemurWarShaman()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        addShamanMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(Permanent::isManifested)
                .findFirst()
                .orElseThrow();
    }

    private void addShamanMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.GREEN, 2);
    }

    private void addGrizzlyBearsFaceUpMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
    }
}
