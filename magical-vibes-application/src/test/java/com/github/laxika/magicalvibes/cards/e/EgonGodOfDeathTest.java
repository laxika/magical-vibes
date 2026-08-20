package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.t.ThroneOfDeath;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EgonGodOfDeathTest extends BaseCardTest {

    @Test
    void exilesTwoGraveyardCardsAndSurvivesUpkeep() {
        Permanent egon = harness.addToBattlefieldAndReturn(player1, new EgonGodOfDeath());
        Card first = new Forest();
        Card second = new Shock();
        harness.setGraveyard(player1, List.of(first, second));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(egon);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(first, second);
    }

    @Test
    void withOnlyOneGraveyardCardEgonSacrificesAndDrawsWithoutExilingIt() {
        Permanent egon = harness.addToBattlefieldAndReturn(player1, new EgonGodOfDeath());
        Card graveyardCard = new Forest();
        Card drawCard = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(graveyardCard));
        harness.setLibrary(player1, List.of(drawCard));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(egon);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2).contains(graveyardCard);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(graveyardCard);
        assertThat(gd.playerHands.get(player1.getId())).contains(drawCard);
    }

    @Test
    void throneOfDeathMillsAndItsAbilityExilesACreatureToDraw() {
        Card milledCard = new Forest();
        Card creatureCard = new GrizzlyBears();
        Card drawCard = new Shock();
        harness.setLibrary(player1, List.of(milledCard, drawCard));
        harness.setGraveyard(player1, List.of(creatureCard));
        Permanent throne = harness.addToBattlefieldAndReturn(player1, new ThroneOfDeath());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(milledCard, creatureCard);

        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(throne), 0, null, null);
        harness.handleGraveyardCardChosen(player1,
                gd.playerGraveyards.get(player1.getId()).indexOf(creatureCard));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(creatureCard);
        assertThat(gd.playerHands.get(player1.getId())).contains(drawCard);
    }

    @Test
    void canCastTheBackFaceForItsManaCost() {
        EgonGodOfDeath card = new EgonGodOfDeath();
        Card creatureCard = new GrizzlyBears();
        Card drawCard = new Shock();
        harness.setHand(player1, List.of(card));
        harness.setGraveyard(player1, List.of(creatureCard));
        harness.setLibrary(player1, List.of(drawCard));
        harness.addMana(player1, ManaColor.BLACK, 4);

        gs.playCard(gd, player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleGraveyardCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(creatureCard);
        assertThat(gd.playerHands.get(player1.getId())).contains(drawCard);
    }
}
