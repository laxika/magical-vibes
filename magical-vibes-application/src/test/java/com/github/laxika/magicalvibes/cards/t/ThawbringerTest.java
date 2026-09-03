package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Thawbringer.class, GrizzlyBears.class, Shock.class})
class ThawbringerTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield surveils 1")
    void entersWithSurveil() {
        Card topCard = new GrizzlyBears();
        gd.playerDecks.get(player1.getId()).add(0, topCard);
        int graveyardBefore = gd.playerGraveyards.get(player1.getId()).size();

        harness.setHand(player1, List.of(new Thawbringer()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(graveyardBefore + 1);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(topCard);
    }

    @Test
    @DisplayName("When it dies, Thawbringer surveils 1")
    void diesWithSurveil() {
        Permanent thawbringer = addReadyThawbringer(player1);
        Card topCard = new GrizzlyBears();
        gd.playerDecks.get(player1.getId()).add(0, topCard);

        killWithShock(player2, thawbringer.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(topCard);
    }

    @Test
    @DisplayName("Another creature's death does not trigger Thawbringer")
    void anotherCreatureDeathDoesNotTrigger() {
        addReadyThawbringer(player1);
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        killWithShock(player1, bears.getId());

        assertThat(gd.stack).isEmpty();
    }

    private Permanent addReadyThawbringer(Player player) {
        Thawbringer card = new Thawbringer();
        Permanent thawbringer = new Permanent(card);
        thawbringer.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(thawbringer);
        return thawbringer;
    }

    private void killWithShock(Player caster, UUID targetId) {
        harness.forceActivePlayer(caster);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(caster, List.of(new Shock()));
        harness.addMana(caster, ManaColor.RED, 1);
        harness.castInstant(caster, 0, targetId);
        harness.passBothPriorities();
    }
}
