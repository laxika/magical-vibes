package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.ArniBrokenbrow;
import com.github.laxika.magicalvibes.cards.f.Frogify;
import com.github.laxika.magicalvibes.cards.g.GrizzledOutrider;
import com.github.laxika.magicalvibes.cards.h.HarnfelHornOfBounty;
import com.github.laxika.magicalvibes.cards.s.SnowCoveredForest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BirgiGodOfStorytelling.class, HarnfelHornOfBounty.class, ArniBrokenbrow.class,
        GrizzledOutrider.class, SnowCoveredForest.class})
class BirgiGodOfStorytellingTest extends BaseCardTest {

    @Test
    void castingASpellAddsPersistentRedMana() {
        addCreatureReady(player1, new BirgiGodOfStorytelling());
        harness.setHand(player1, List.of(new GrizzledOutrider()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        ManaPool pool = gd.playerManaPools.get(player1.getId());
        assertThat(pool.get(ManaColor.RED)).isEqualTo(1);
        assertThat(pool.getPersistentMana(ManaColor.RED)).isEqualTo(1);
    }

    @Test
    void harnfelDiscardsAndExilesTwoCardsWithPlayPermissions() {
        harness.addToBattlefieldAndReturn(player1, new HarnfelHornOfBounty());
        Card discarded = new GrizzledOutrider();
        Card topLand = new SnowCoveredForest();
        Card topSpell = new GrizzledOutrider();
        harness.setHand(player1, List.of(discarded));
        harness.setLibrary(player1, List.of(topLand, topSpell));

        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardCostChoice.class);
        harness.handleCardChosen(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(discarded);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(topLand, topSpell);
        assertThat(gd.exilePlayPermissions)
                .containsEntry(topLand.getId(), player1.getId())
                .containsEntry(topSpell.getId(), player1.getId());
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn)
                .contains(topLand.getId(), topSpell.getId());
    }

    @Test
    void birgiAllowsAControlledBoastAbilityTwiceEachTurn() {
        addCreatureReady(player1, new BirgiGodOfStorytelling());
        Permanent arni = addCreatureReady(player1, new ArniBrokenbrow());
        arni.setAttackedThisTurn(true);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no more than 2 times each turn");
    }

    @Test
    void manaTriggerResolvesBeforeTheSpellAndSurvivesPhaseChangesButNotTheTurn() {
        addCreatureReady(player1, new BirgiGodOfStorytelling());
        Card spell = new GrizzledOutrider();
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        resolveAllTriggers();

        harness.passUntil(player1, TurnStep.POSTCOMBAT_MAIN);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }

    @Test
    void castingBirgiDoesNotTriggerItsOwnManaAbility() {
        harness.setHand(player1, List.of(new BirgiGodOfStorytelling()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Birgi, God of Storytelling");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }

    @Test
    void opponentCastingASpellDoesNotTriggerBirgi() {
        addCreatureReady(player1, new BirgiGodOfStorytelling());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new GrizzledOutrider()));
        harness.addMana(player2, ManaColor.GREEN, 5);

        harness.castCreature(player2, 0);
        resolveAllTriggers();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }

    @Test
    void persistentManaCanBeSpentAndPendingTriggerSurvivesBirgisDeparture() {
        Permanent birgi = addCreatureReady(player1, new BirgiGodOfStorytelling());
        harness.setHand(player1, List.of(new GrizzledOutrider(), new GrizzledOutrider()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).getPersistentMana(ManaColor.RED)).isZero();
        gd.playerBattlefields.get(player1.getId()).remove(birgi);
        gd.playerGraveyards.get(player1.getId()).add(birgi.getCard());
        resolveAllTriggers();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        harness.passUntil(player1, TurnStep.POSTCOMBAT_MAIN);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
    }

    @Test
    void backFaceCanBeCastForFiveManaAndActivated() {
        harness.setHand(player1, List.of(new BirgiGodOfStorytelling(), new SnowCoveredForest()));
        harness.setLibrary(player1, List.of(new GrizzledOutrider()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castCreature(player1, 0, 1);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Harnfel, Horn of Bounty");
        harness.assertNotOnBattlefield(player1, "Birgi, God of Storytelling");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        resolveAllTriggers();
        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void harnfelCardsCanBePlayedWithNormalCostsAfterHarnfelLeaves() {
        Permanent harnfel = harness.addToBattlefieldAndReturn(player1, new HarnfelHornOfBounty());
        Card land = new SnowCoveredForest();
        Card creature = new GrizzledOutrider();
        harness.setHand(player1, List.of(new SnowCoveredForest()));
        harness.setLibrary(player1, List.of(land, creature));
        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        resolveAllTriggers();
        gd.playerBattlefields.get(player1.getId()).remove(harnfel);
        gd.playerGraveyards.get(player1.getId()).add(harnfel.getCard());

        assertThatThrownBy(() -> harness.castFromExile(player1, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.castFromExile(player1, land.getId());
        harness.addMana(player1, ManaColor.GREEN, 5);
        harness.castFromExile(player1, creature.getId());
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Snow-Covered Forest");
        harness.assertOnBattlefield(player1, "Grizzled Outrider");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    void harnfelPermissionExpiresAndUnplayedCardsRemainExiled() {
        harness.addToBattlefield(player1, new HarnfelHornOfBounty());
        Card firstLand = new SnowCoveredForest();
        Card secondLand = new SnowCoveredForest();
        harness.setHand(player1, List.of(new GrizzledOutrider()));
        harness.setLibrary(player1, List.of(firstLand, secondLand));
        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        resolveAllTriggers();

        harness.castFromExile(player1, firstLand.getId());
        assertThatThrownBy(() -> harness.castFromExile(player1, secondLand.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(secondLand);
        assertThat(gd.exilePlayPermissions).doesNotContainKey(secondLand.getId());
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn).doesNotContain(secondLand.getId());
    }

    @Test
    void harnfelWithAnEmptyLibraryStillDiscardsTheCost() {
        harness.addToBattlefield(player1, new HarnfelHornOfBounty());
        Card discarded = new SnowCoveredForest();
        harness.setHand(player1, List.of(discarded));
        harness.setLibrary(player1, List.of());

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(discarded);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No valid card to discard");
    }

    @Test
    void birgiDoesNotRemoveTheRequirementToAttackBeforeBoasting() {
        addCreatureReady(player1, new BirgiGodOfStorytelling());
        addCreatureReady(player1, new ArniBrokenbrow());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("attacked this turn");
    }

    @Test
    void creatureThatAttackedAndChangedControllersCannotBoastTwiceOnOpponentsTurn() {
        addCreatureReady(player1, new BirgiGodOfStorytelling());
        Permanent arni = addCreatureReady(player2, new ArniBrokenbrow());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        arni.setAttackedThisTurn(true);
        gd.playerBattlefields.get(player2.getId()).remove(arni);
        gd.playerBattlefields.get(player1.getId()).add(arni);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once each turn");
    }

    @Test
    @CardUsed({Frogify.class})
    void birgiLosingAllAbilitiesRemovesItsExtraBoastPermission() {
        Permanent birgi = addCreatureReady(player1, new BirgiGodOfStorytelling());
        Permanent arni = addCreatureReady(player1, new ArniBrokenbrow());
        arni.setAttackedThisTurn(true);
        harness.setHand(player1, List.of(new Frogify()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castEnchantment(player1, 0, birgi.getId());
        resolveAllTriggers();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once each turn");
    }
}
