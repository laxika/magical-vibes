package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.d.Disenchant;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.Humble;
import com.github.laxika.magicalvibes.cards.i.IcyManipulator;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.t.ThranPowerSuit;
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

@CardUsed({MishraTamerOfMakFawa.class, Disenchant.class, GiantSpider.class,
        GrizzlyBears.class, IcyManipulator.class, Shock.class, ThranPowerSuit.class, Humble.class})
class MishraTamerOfMakFawaTest extends BaseCardTest {

    @Test
    @DisplayName("Grants unearth to an artifact card in your graveyard")
    void grantsUnearthToOwnedArtifactCard() {
        harness.addToBattlefield(player1, new MishraTamerOfMakFawa());
        harness.setGraveyard(player1, List.of(new IcyManipulator()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateGraveyardAbility(player1, 0, 0);
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Icy Manipulator");
        assertThat(returned.getGrantedKeywords()).contains(Keyword.HASTE);
        harness.assertNotInGraveyard(player1, "Icy Manipulator");
    }

    @Test
    @DisplayName("Does not grant unearth to a nonartifact card")
    void doesNotGrantUnearthToNonartifactCard() {
        harness.addToBattlefield(player1, new MishraTamerOfMakFawa());
        harness.setGraveyard(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Ward can be paid by sacrificing any permanent")
    void wardCanBePaidBySacrificingAnyPermanent() {
        harness.addToBattlefield(player1, new MishraTamerOfMakFawa());
        Permanent spider = addCreatureReady(player1, new GiantSpider());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new IcyManipulator());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, spider.getId());
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player2, artifact.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Icy Manipulator");
        harness.assertOnBattlefield(player1, "Giant Spider");
        harness.assertInGraveyard(player2, "Shock");
    }

    @Test
    @DisplayName("Artifact permanents you control have ward")
    void artifactPermanentsHaveWard() {
        harness.addToBattlefield(player1, new MishraTamerOfMakFawa());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new IcyManipulator());
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Disenchant()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castInstant(player2, 0, artifact.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.handlePermanentChosen(player2, sacrifice.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Icy Manipulator");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Disenchant");
    }
    @Test
    void doesNotGrantUnearthAfterLosingAllAbilities() {
        Permanent mishra = harness.addToBattlefieldAndReturn(player1, new MishraTamerOfMakFawa());
        harness.setGraveyard(player1, List.of(new ThranPowerSuit()));
        harness.setHand(player1, List.of(new Humble()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, mishra.getId());

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Thran Power Suit");
    }

    @Test
    void activatedUnearthStillResolvesAfterMishraLosesAbilities() {
        Permanent mishra = harness.addToBattlefieldAndReturn(player1, new MishraTamerOfMakFawa());
        harness.setGraveyard(player1, List.of(new ThranPowerSuit()));
        addUnearthMana();
        harness.activateGraveyardAbility(player1, 0, 0);
        harness.setHand(player1, List.of(new Humble()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, mishra.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Thran Power Suit");
        harness.assertNotInGraveyard(player1, "Thran Power Suit");
    }

    @Test
    void cannotUnearthOutsideAMainPhase() {
        harness.addToBattlefield(player1, new MishraTamerOfMakFawa());
        harness.setGraveyard(player1, List.of(new ThranPowerSuit()));
        addUnearthMana();
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotUnearthWhileTheStackIsNotEmpty() {
        Permanent mishra = harness.addToBattlefieldAndReturn(player1, new MishraTamerOfMakFawa());
        harness.setGraveyard(player1, List.of(new ThranPowerSuit()));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, mishra.getId());
        addUnearthMana();

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.passBothPriorities();
    }

    @Test
    void unearthReturnsOnlyTheActivatedCard() {
        harness.addToBattlefield(player1, new MishraTamerOfMakFawa());
        ThranPowerSuit first = new ThranPowerSuit();
        ThranPowerSuit second = new ThranPowerSuit();
        harness.setGraveyard(player1, List.of(first, second));
        addUnearthMana();

        harness.activateGraveyardAbility(player1, 1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(first);
        assertThat(findPermanent(player1, "Thran Power Suit").getCard().getId()).isEqualTo(second.getId());
    }

    @Test
    void unearthExilesAtTheNextEndStep() {
        harness.addToBattlefield(player1, new MishraTamerOfMakFawa());
        harness.setGraveyard(player1, List.of(new ThranPowerSuit()));
        addUnearthMana();
        harness.activateGraveyardAbility(player1, 0, 0);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Thran Power Suit");
        harness.assertNotInGraveyard(player1, "Thran Power Suit");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Thran Power Suit"));
    }

    @Test
    void destroyedUnearthedArtifactIsExiledInsteadOfDying() {
        harness.addToBattlefield(player1, new MishraTamerOfMakFawa());
        harness.setGraveyard(player1, List.of(new ThranPowerSuit()));
        addUnearthMana();
        harness.activateGraveyardAbility(player1, 0, 0);
        harness.passBothPriorities();
        harness.setHand(player1, List.of(new Disenchant()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, findPermanent(player1, "Thran Power Suit").getId());

        harness.assertNotOnBattlefield(player1, "Thran Power Suit");
        harness.assertNotInGraveyard(player1, "Thran Power Suit");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Thran Power Suit"));
    }

    @Test
    void cannotUnearthOnAnOpponentsTurn() {
        harness.addToBattlefield(player1, new MishraTamerOfMakFawa());
        harness.setGraveyard(player1, List.of(new ThranPowerSuit()));
        addUnearthMana();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void doesNotGrantUnearthToOpponentsArtifacts() {
        harness.addToBattlefield(player1, new MishraTamerOfMakFawa());
        harness.setGraveyard(player2, List.of(new ThranPowerSuit()));
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player2, 0, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void wardCountersSpellWhenOpponentHasNothingToSacrifice() {
        harness.addToBattlefield(player1, new MishraTamerOfMakFawa());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new ThranPowerSuit());
        harness.setHand(player2, List.of(new Disenchant()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player2, 0, artifact.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Thran Power Suit");
        harness.assertInGraveyard(player2, "Disenchant");
    }

    @Test
    void wardCountersOpponentsActivatedAbilityWhenPaymentIsDeclined() {
        Permanent mishra = harness.addToBattlefieldAndReturn(player1, new MishraTamerOfMakFawa());
        harness.addToBattlefield(player2, new IcyManipulator());
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.activateAbility(player2, 0, null, mishra.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);
        harness.passBothPriorities();

        assertThat(mishra.isTapped()).isFalse();
        harness.assertOnBattlefield(player2, "Icy Manipulator");
        assertThat(gd.stack).isEmpty();
    }

    private void addUnearthMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
    }
}
