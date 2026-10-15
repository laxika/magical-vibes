package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.c.CruelEdict;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Extractor Demon")
@CardUsed({ExtractorDemon.class, CruelEdict.class, Forest.class, GrizzlyBears.class, Unsummon.class})
class ExtractorDemonTest extends BaseCardTest {

    @Test
    @DisplayName("Another creature dying lets the controller make a target player mill two cards")
    void anotherCreatureDyingMillsTargetPlayer() {
        harness.addToBattlefield(player1, new ExtractorDemon());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setLibrary(player2, List.of(new Forest(), new Forest(), new Forest()));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new CruelEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities(); // Cruel Edict resolves → player2 sacrifices Grizzly Bears
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities(); // Extractor Demon trigger resolves → "may" prompt

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .filteredOn(c -> c.getName().equals("Forest")).hasSize(2);
    }

    @Test
    @DisplayName("Declining the trigger mills no one")
    void decliningMillsNoOne() {
        harness.addToBattlefield(player1, new ExtractorDemon());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setLibrary(player2, List.of(new Forest(), new Forest(), new Forest()));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new CruelEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(3);
        harness.assertNotInGraveyard(player2, "Forest");
    }

    @Test
    @DisplayName("A creature leaving via bounce (not just dying) also triggers the ability")
    void bounceAlsoTriggers() {
        harness.addToBattlefield(player1, new ExtractorDemon());
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent bears = findPermanent(player2, "Grizzly Bears");

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0, bears.getId());
        harness.passBothPriorities(); // Unsummon resolves → Grizzly Bears returns to hand
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities(); // Extractor Demon trigger resolves → "may" prompt

        harness.assertInHand(player2, "Grizzly Bears");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("Unearth returns Extractor Demon to the battlefield with haste")
    void unearthReturnsWithHaste() {
        harness.setGraveyard(player1, List.of(new ExtractorDemon()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        Permanent perm = findPermanent(player1, "Extractor Demon");
        assertThat(gqs.hasKeyword(gd, perm, Keyword.HASTE)).isTrue();
        harness.assertNotInGraveyard(player1, "Extractor Demon");
    }

    @Test
    @DisplayName("Unearthed Extractor Demon is exiled at the next end step")
    void unearthExiledAtEndStep() {
        harness.setGraveyard(player1, List.of(new ExtractorDemon()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Extractor Demon");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Extractor Demon"));
    }

    @Test
    @DisplayName("The controller may target themselves and mills only the available cards")
    void canMillOwnShortLibrary() {
        harness.addToBattlefield(player1, new ExtractorDemon());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0, findPermanent(player2, "Grizzly Bears").getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Forest");
    }

    @Test
    @DisplayName("Bouncing an unearthed Demon exiles it and does not trigger its own ability")
    void unearthBounceExilesWithoutSelfTrigger() {
        harness.setGraveyard(player1, List.of(new ExtractorDemon()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();
        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0, findPermanent(player1, "Extractor Demon").getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Extractor Demon");
        harness.assertNotInGraveyard(player1, "Extractor Demon");
        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(c -> c instanceof ExtractorDemon);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c instanceof ExtractorDemon);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Unearth cannot be activated outside a main phase")
    void unearthRequiresMainPhase() {
        harness.setGraveyard(player1, List.of(new ExtractorDemon()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInGraveyard(player1, "Extractor Demon");
        harness.assertNotOnBattlefield(player1, "Extractor Demon");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Unearth requires its full mana cost")
    void unearthRequiresThreeMana() {
        harness.setGraveyard(player1, List.of(new ExtractorDemon()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInGraveyard(player1, "Extractor Demon");
        harness.assertNotOnBattlefield(player1, "Extractor Demon");
        assertThat(gd.stack).isEmpty();
    }

}
