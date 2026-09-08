package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HalvarGodOfBattle;
import com.github.laxika.magicalvibes.cards.k.KarnLiberated;
import com.github.laxika.magicalvibes.cards.t.ThePrismaticBridge;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EsikaGodOfTheTreeTest extends BaseCardTest {

    @Test
    void frontFaceGrantsOtherLegendaryCreaturesVigilanceAndAnyColorMana() {
        EsikaGodOfTheTree card = new EsikaGodOfTheTree();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        Permanent halvar = harness.addToBattlefieldAndReturn(player1, new HalvarGodOfBattle());
        halvar.setSummoningSick(false);
        Permanent nonlegendary = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        gs.playCard(gd, player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, halvar, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, nonlegendary, Keyword.VIGILANCE)).isFalse();

        int halvarIndex = gd.playerBattlefields.get(player1.getId()).indexOf(halvar);
        harness.activateAbility(player1, halvarIndex, null, null);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, ManaColor.BLUE.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(halvar.isTapped()).isTrue();
    }

    @Test
    void prismaticBridgePutsTheFirstCreatureOrPlaneswalkerOntoTheBattlefield() {
        harness.addToBattlefield(player1, new ThePrismaticBridge());
        Card nonmatch = new Forest();
        Card planeswalker = new KarnLiberated();
        harness.setLibrary(player1, List.of(nonmatch, planeswalker));

        harness.forceActivePlayer(player1);
        gd.turnNumber = 1;
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getOriginalCard().getId().equals(planeswalker.getId()));
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactly(nonmatch);
    }
}
