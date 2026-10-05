package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.a.AlpineGrizzly;
import com.github.laxika.magicalvibes.cards.f.ForceAway;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.SundialOfTheInfinite;
import com.github.laxika.magicalvibes.cards.t.TreasureCruise;
import com.github.laxika.magicalvibes.cards.w.WetlandSambar;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentAction;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentActionKind;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KheruLichLord.class, GrizzlyBears.class, LightningBolt.class, Shock.class,
        AlpineGrizzly.class, ForceAway.class, SundialOfTheInfinite.class,
        TreasureCruise.class, WetlandSambar.class})
class KheruLichLordTest extends BaseCardTest {

    @Test
    @DisplayName("Paying the upkeep cost returns a random creature with the granted abilities")
    void payingUpkeepCostReturnsRandomCreatureWithAbilities() {
        harness.addToBattlefield(player1, new KheruLichLord());
        Card creature = new GrizzlyBears();
        Card spell = new LightningBolt();
        harness.setGraveyard(player1, List.of(creature, spell));

        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        Permanent returned = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(creature.getId()))
                .findFirst()
                .orElseThrow();
        assertThat(returned.hasKeyword(Keyword.FLYING)).isTrue();
        assertThat(returned.hasKeyword(Keyword.TRAMPLE)).isTrue();
        assertThat(returned.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(returned.isExileIfLeavesBattlefield()).isTrue();
        assertThat(gd.getDelayedActions(DelayedPermanentAction.class))
                .contains(new DelayedPermanentAction(returned.getId(), DelayedPermanentActionKind.EXILE_AT_END_STEP));
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(spell);
    }

    @Test
    @DisplayName("The returned creature is exiled at the next end step")
    void returnedCreatureIsExiledAtNextEndStep() {
        Card creature = new GrizzlyBears();
        harness.addToBattlefield(player1, new KheruLichLord());
        harness.setGraveyard(player1, List.of(creature));

        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(creature.getId()));
    }

    @Test
    @DisplayName("The leave-the-battlefield replacement exiles the returned creature")
    void returnedCreatureIsExiledInsteadOfGoingToGraveyard() {
        Card creature = new GrizzlyBears();
        harness.addToBattlefield(player1, new KheruLichLord());
        harness.setGraveyard(player1, List.of(creature));

        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        Permanent returned = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(creature.getId()))
                .findFirst()
                .orElseThrow();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, returned.getId());

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(creature.getId()));
    }

    @Test
    @DisplayName("Declining the upkeep cost does not return a creature")
    void decliningUpkeepCostDoesNotReturnCreature() {
        Card creature = new GrizzlyBears();
        harness.addToBattlefield(player1, new KheruLichLord());
        harness.setGraveyard(player1, List.of(creature));

        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void returnsExactlyOneOfMultipleCreaturesWithoutAChoice() {
        harness.addToBattlefield(player1, new KheruLichLord());
        Card first = new AlpineGrizzly();
        Card second = new WetlandSambar();
        Card spell = new TreasureCruise();
        harness.setGraveyard(player1, List.of(first, spell, second));

        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().getId().equals(first.getId())
                        || p.getCard().getId().equals(second.getId()))
                .hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2).contains(spell);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void payingWithNoEligibleCreatureDoesNotReturnANoncreatureOrOpponentsCreature() {
        harness.addToBattlefield(player1, new KheruLichLord());
        Card spell = new TreasureCruise();
        Card creature = new AlpineGrizzly();
        harness.setGraveyard(player1, List.of(spell));
        harness.setGraveyard(player2, List.of(creature));

        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(spell);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(creature);
        harness.assertNotOnBattlefield(player1, "Alpine Grizzly");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void doesNotTriggerDuringOpponentsUpkeep() {
        harness.addToBattlefield(player1, new KheruLichLord());
        Card creature = new AlpineGrizzly();
        harness.setGraveyard(player1, List.of(creature));

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(creature);
    }

    @Test
    void bounceExilesTheReturnedCreatureInsteadOfPuttingItInHand() {
        harness.addToBattlefield(player1, new KheruLichLord());
        Card creature = new WetlandSambar();
        harness.setGraveyard(player1, List.of(creature));

        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.setHand(player2, List.of(new ForceAway()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player2, 0, harness.getPermanentId(player1, "Wetland Sambar"));

        harness.assertNotOnBattlefield(player1, "Wetland Sambar");
        harness.assertNotInHand(player1, "Wetland Sambar");
        harness.assertNotInGraveyard(player1, "Wetland Sambar");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(creature);
    }

    @Test
    void skippingYourEndStepWaitsForYourNextEndStep() {
        harness.addToBattlefield(player1, new KheruLichLord());
        harness.addToBattlefield(player1, new SundialOfTheInfinite());
        Card creature = new AlpineGrizzly();
        harness.setGraveyard(player1, List.of(creature));

        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();
        assertThat(gd.activePlayerId).isEqualTo(player2.getId());

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Alpine Grizzly");
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(creature);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Alpine Grizzly");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(creature);
    }
}
