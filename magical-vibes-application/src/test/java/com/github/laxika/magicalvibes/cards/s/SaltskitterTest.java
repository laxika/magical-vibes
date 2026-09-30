package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FomoriNomad;
import com.github.laxika.magicalvibes.cards.r.RiverOfTears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Saltskitter.class, FomoriNomad.class, RiverOfTears.class})
class SaltskitterTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles when another creature enters and returns at the next end step")
    void exilesWhenAnotherCreatureEntersAndReturnsAtNextEndStep() {
        Permanent saltskitter = harness.addToBattlefieldAndReturn(player1, new Saltskitter());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player2, new FomoriNomad(), "{4}{R}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(saltskitter);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(saltskitter.getCard().getId()));

        harness.passUntil(player2, TurnStep.END_STEP);

        Permanent returned = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(saltskitter.getCard().getId()))
                .findFirst()
                .orElseThrow();
        assertThat(returned.getId()).isNotEqualTo(saltskitter.getId());
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(card -> card.getId().equals(saltskitter.getCard().getId()));
    }

    @Test
    @DisplayName("Does not trigger from its own entry")
    void doesNotTriggerFromItsOwnEntry() {
        Permanent saltskitter = harness.enterBattlefieldAndReturn(player1, new Saltskitter());

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(saltskitter);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(card -> card.getId().equals(saltskitter.getCard().getId()));
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Does not trigger from a noncreature permanent entering")
    void doesNotTriggerFromNoncreatureEntry() {
        Permanent saltskitter = harness.addToBattlefieldAndReturn(player1, new Saltskitter());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new RiverOfTears()));
        harness.playLand(player2, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(saltskitter);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(card -> card.getId().equals(saltskitter.getCard().getId()));
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Returns under its owner's control when controlled by an opponent")
    void returnsUnderOwnersControlWhenControlledByOpponent() {
        Saltskitter card = new Saltskitter();
        card.setOwnerId(player1.getId());
        Permanent saltskitter = harness.addToBattlefieldAndReturn(player2, card);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player2, new FomoriNomad(), "{4}{R}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(saltskitter);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(exiled -> exiled.getId().equals(card.getId()));
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .noneMatch(exiled -> exiled.getId().equals(card.getId()));

        harness.passUntil(player2, TurnStep.END_STEP);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(card.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(card.getId()));
    }
}
