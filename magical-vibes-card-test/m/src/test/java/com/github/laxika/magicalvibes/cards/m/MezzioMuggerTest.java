package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Stifle;
import com.github.laxika.magicalvibes.cards.t.TorporOrb;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MezzioMugger.class, Forest.class, GiantGrowth.class, GrizzlyBears.class, TorporOrb.class, Stifle.class})
class MezzioMuggerTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking exiles the top card of each library and grants play permission")
    void attackingExilesTopCardOfEachLibrary() {
        Card ownTopCard = new Forest();
        Card opposingTopCard = new GiantGrowth();
        harness.setLibrary(player1, List.of(ownTopCard));
        harness.setLibrary(player2, List.of(opposingTopCard));
        addCreatureReady(player1, new MezzioMugger());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(ownTopCard);
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(opposingTopCard);
        assertThat(gd.exilePlayPermissions)
                .containsEntry(ownTopCard.getId(), player1.getId())
                .containsEntry(opposingTopCard.getId(), player1.getId());
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn)
                .contains(ownTopCard.getId(), opposingTopCard.getId());
        assertThat(gd.exilePlayAnyManaType).contains(opposingTopCard.getId());
        assertThat(gd.exilePlayAnyManaType).doesNotContain(ownTopCard.getId());
    }

    @Test
    @DisplayName("An exiled spell can be cast with mana of any color")
    void castsExiledSpellWithAnyColorMana() {
        Card ownTopCard = new Forest();
        GiantGrowth opposingTopCard = new GiantGrowth();
        harness.setLibrary(player1, List.of(ownTopCard));
        harness.setLibrary(player2, List.of(opposingTopCard));
        addCreatureReady(player1, new MezzioMugger());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castFromExile(player1, opposingTopCard.getId(), target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(5);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(ownTopCard);
    }

    @Test
    @DisplayName("Blitz grants haste, draws on death, and sacrifices at the next end step")
    void blitzGrantsHasteDrawsAndSacrifices() {
        harness.setHand(player1, List.of(new MezzioMugger()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreatureWithAlternateCost(player1, 0, List.of());
        resolveAllTriggers();

        Permanent mugger = findPermanent(player1, "Mezzio Mugger");
        assertThat(gqs.hasKeyword(gd, mugger, Keyword.HASTE)).isTrue();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Mezzio Mugger");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    void castsOwnExiledSpellWithAnyColorMana() {
        GiantGrowth spell = new GiantGrowth();
        harness.setLibrary(player1, List.of(spell));
        harness.setLibrary(player2, List.of(new Forest()));
        Permanent mugger = addCreatureReady(player1, new MezzioMugger());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castFromExile(player1, spell.getId(), mugger.getId());
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, mugger)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, mugger)).isEqualTo(6);
        harness.assertInGraveyard(player1, "Giant Growth");
    }

    @Test
    void emptyLibraryDoesNotPreventExilingOtherPlayersCard() {
        Forest land = new Forest();
        harness.setLibrary(player1, List.of());
        harness.setLibrary(player2, List.of(land));
        addCreatureReady(player1, new MezzioMugger());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(land);
        assertThat(gd.exilePlayPermissions).containsEntry(land.getId(), player1.getId());
    }

    @Test
    void canPlayOpponentsExiledLandButNotAnExtraLand() {
        Forest ownLand = new Forest();
        Forest opposingLand = new Forest();
        harness.setLibrary(player1, List.of(ownLand));
        harness.setLibrary(player2, List.of(opposingLand));
        addCreatureReady(player1, new MezzioMugger());

        declareAttackers(List.of(0));
        resolveAllTriggers();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromExile(player1, opposingLand.getId());

        harness.assertOnBattlefield(player1, "Forest");
        assertThat(gd.getPlayerExiledCards(player2.getId())).doesNotContain(opposingLand);
        assertThatThrownBy(() -> harness.castFromExile(player1, ownLand.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(ownLand);
    }

    @Test
    void playPermissionExpiresAfterTheTurn() {
        GiantGrowth spell = new GiantGrowth();
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(spell, new Forest()));
        Permanent mugger = addCreatureReady(player1, new MezzioMugger());

        declareAttackers(List.of(0));
        resolveAllTriggers();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.UPKEEP);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castFromExile(player1, spell.getId(), mugger.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(spell);
    }

    @Test
    void normalCastingDoesNotGrantHasteOrScheduleSacrifice() {
        harness.setHand(player1, List.of(new MezzioMugger()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        Permanent mugger = findPermanent(player1, "Mezzio Mugger");
        assertThat(gqs.hasKeyword(gd, mugger, Keyword.HASTE)).isFalse();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Mezzio Mugger");
    }

    @Test
    void blitzHasHasteEvenWhenEntersTriggersAreSuppressed() {
        harness.addToBattlefield(player2, new TorporOrb());
        harness.setHand(player1, List.of(new MezzioMugger()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreatureWithAlternateCost(player1, 0, List.of());
        resolveAllTriggers();

        Permanent mugger = findPermanent(player1, "Mezzio Mugger");
        assertThat(gqs.hasKeyword(gd, mugger, Keyword.HASTE)).isTrue();
    }

    @Test
    void blitzStillSacrificesWhenEntersTriggersAreSuppressed() {
        harness.addToBattlefield(player2, new TorporOrb());
        harness.setHand(player1, List.of(new MezzioMugger()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreatureWithAlternateCost(player1, 0, List.of());
        resolveAllTriggers();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Mezzio Mugger");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    void blitzRetainsHasteAfterItsSacrificeIsCountered() {
        harness.setHand(player1, List.of(new MezzioMugger(), new Stifle()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setLibrary(player2, List.of(new Forest()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreatureWithAlternateCost(player1, 0, List.of());
        resolveAllTriggers();

        Permanent mugger = findPermanent(player1, "Mezzio Mugger");
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.END_STEP);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0, gd.stack.getLast().getCard().getId());
        harness.passUntil(player2, TurnStep.UPKEEP);

        harness.assertOnBattlefield(player1, "Mezzio Mugger");
        assertThat(gqs.hasKeyword(gd, mugger, Keyword.HASTE)).isTrue();
    }
}
