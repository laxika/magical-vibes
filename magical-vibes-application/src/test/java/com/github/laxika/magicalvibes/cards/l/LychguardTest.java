package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.a.ArvadTheCursed;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MoxAmber;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Lychguard.class, ArvadTheCursed.class, GrizzlyBears.class, MoxAmber.class})
class LychguardTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing Lychguard returns all legendary creature cards from its controller's graveyard")
    void returnsAllLegendaryCreatureCards() {
        addLychguard();
        ArvadTheCursed legendaryCreature = new ArvadTheCursed();
        GrizzlyBears nonLegendaryCreature = new GrizzlyBears();
        MoxAmber legendaryArtifact = new MoxAmber();
        harness.setGraveyard(player1, List.of(legendaryCreature, nonLegendaryCreature, legendaryArtifact));
        addActivationMana();

        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() instanceof Lychguard);

        harness.passBothPriorities();

        harness.assertInHand(player1, "Arvad the Cursed");
        harness.assertNotInGraveyard(player1, "Arvad the Cursed");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Mox Amber");
        harness.assertInGraveyard(player1, "Lychguard");
    }

    @Test
    @DisplayName("Does not return matching creatures from an opponent's graveyard")
    void onlyReturnsFromControllerGraveyard() {
        addLychguard();
        Card opponentLegendaryCreature = new ArvadTheCursed();
        harness.setGraveyard(player2, List.of(opponentLegendaryCreature));
        addActivationMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Arvad the Cursed");
        harness.assertNotInHand(player1, "Arvad the Cursed");
    }

    private void addLychguard() {
        addCreatureReady(player1, new Lychguard());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
    }

    private void addActivationMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
