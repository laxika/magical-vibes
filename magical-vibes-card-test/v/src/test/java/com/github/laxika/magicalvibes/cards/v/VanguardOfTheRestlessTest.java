package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.a.ApothecaryGeist;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VanguardOfTheRestless.class, ApothecaryGeist.class, GrizzlyBears.class})
class VanguardOfTheRestlessTest extends BaseCardTest {

    @Test
    void spiritsGetOnePlusOneForEachCommanderCastFromCommandZone() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        Permanent vanguard = addCreatureReady(player1, new VanguardOfTheRestless());
        Permanent spirit = addCreatureReady(player1, new ApothecaryGeist());
        Card commander = addCommanderToCommandZone();

        assertThat(gqs.getEffectivePower(gd, vanguard)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, vanguard)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, spirit)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, spirit)).isEqualTo(3);

        castCommander(commander, 1);

        assertThat(gqs.getEffectivePower(gd, vanguard)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, vanguard)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, spirit)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, spirit)).isEqualTo(4);

        gd.stack.clear();
        gd.playerCommandZones.get(player1.getId()).add(commander);
        gd.priorityPassedBy.clear();
        castCommander(commander, 3);

        assertThat(gqs.getEffectivePower(gd, vanguard)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, vanguard)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, spirit)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, spirit)).isEqualTo(5);
    }

    @Test
    void spiritEnteringLetsYouPayToReturnVanguardFromGraveyard() {
        VanguardOfTheRestless vanguard = new VanguardOfTheRestless();
        harness.setGraveyard(player1, List.of(vanguard));

        harness.enterBattlefieldAndReturn(player1, new ApothecaryGeist());
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player1.getId())).noneMatch(card -> card.getId().equals(vanguard.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(vanguard.getId()));
    }

    @Test
    void nonSpiritEnteringDoesNotTriggerReturn() {
        VanguardOfTheRestless vanguard = new VanguardOfTheRestless();
        harness.setGraveyard(player1, List.of(vanguard));

        harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(vanguard);
    }

    @Test
    void opponentSpiritEnteringDoesNotTriggerReturn() {
        VanguardOfTheRestless vanguard = new VanguardOfTheRestless();
        harness.setGraveyard(player1, List.of(vanguard));

        harness.enterBattlefieldAndReturn(player2, new VanguardOfTheRestless());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(vanguard);
    }

    @Test
    void mayDeclineReturnEvenWithEnoughMana() {
        VanguardOfTheRestless vanguard = new VanguardOfTheRestless();
        harness.setGraveyard(player1, List.of(vanguard));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.enterBattlefieldAndReturn(player1, new VanguardOfTheRestless());
        resolveAllTriggers();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(vanguard);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(vanguard.getId()));
    }

    @Test
    void returnDoesNotReturnOtherCopiesInGraveyard() {
        VanguardOfTheRestless first = new VanguardOfTheRestless();
        VanguardOfTheRestless second = new VanguardOfTheRestless();
        harness.setGraveyard(player1, List.of(first, second));
        harness.enterBattlefieldAndReturn(player1, new VanguardOfTheRestless());
        resolveAllTriggers();
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice) {
            harness.handleMayAbilityChosen(player1, false);
            resolveAllTriggers();
        }
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
    }

    @Test
    void oldTriggerCannotReturnVanguardAfterItLeavesAndReentersGraveyard() {
        VanguardOfTheRestless vanguard = new VanguardOfTheRestless();
        harness.setGraveyard(player1, List.of(vanguard));
        gd.markGraveyardEntry(vanguard);
        harness.enterBattlefieldAndReturn(player1, new VanguardOfTheRestless());

        harness.setGraveyard(player1, List.of());
        harness.setHand(player1, List.of(vanguard));
        harness.setHand(player1, List.of());
        harness.setGraveyard(player1, List.of(vanguard));
        gd.markGraveyardEntry(vanguard);

        resolveAllTriggers();
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(vanguard);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(vanguard.getId()));
    }

    @Test
    void boostExcludesNonSpiritsAndOpponentSpiritsAndOpponentCommanderCasts() {
        Permanent vanguard = addCreatureReady(player1, new VanguardOfTheRestless());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposingSpirit = addCreatureReady(player2, new VanguardOfTheRestless());
        gd.commanderCastsFromCommandZoneThisGame.put(player1.getId(), 2);
        gd.commanderCastsFromCommandZoneThisGame.put(player2.getId(), 3);

        assertThat(gqs.getEffectivePower(gd, vanguard)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, vanguard)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opposingSpirit)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, opposingSpirit)).isEqualTo(5);
    }
    private Card addCommanderToCommandZone() {
        Card commander = new Card();
        commander.setName("Test Commander");
        commander.setType(CardType.CREATURE);
        commander.setSupertypes(Set.of(CardSupertype.LEGENDARY));
        commander.setManaCost("{1}");
        commander.setPower(2);
        commander.setToughness(2);
        commander.setOwnerId(player1.getId());
        commander.freeze();
        gd.format = com.github.laxika.magicalvibes.model.DeckFormat.COMMANDER;
        gd.makeCommander(player1.getId(), commander);
        gd.playerCommandZones.put(player1.getId(), new ArrayList<>(List.of(commander)));
        return commander;
    }

    private void castCommander(Card commander, int mana) {
        harness.addMana(player1, ManaColor.COLORLESS, mana);
        gs.castCommander(gd, player1, commander.getId(),
                () -> gs.playCard(gd, player1, 0, null, null, null));
    }
}
