package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AngelicArmaments;
import com.github.laxika.magicalvibes.cards.a.ArcaneMelee;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.cards.r.RuleOfLaw;
import com.github.laxika.magicalvibes.cards.t.ThatcherRevolt;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DevastationTide.class, GrizzlyBears.class, GloriousAnthem.class, Island.class,
        SerraAngel.class, AngelicArmaments.class, Defang.class, ArcaneMelee.class,
        RuleOfLaw.class, ThatcherRevolt.class})
class DevastationTideTest extends BaseCardTest {

    @BeforeEach
    void emptyStartingHand() {
        harness.setHand(player1, List.of());
    }

    private void castNormally() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new DevastationTide(), "{3}{U}{U}");
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Returns all nonland permanents on both sides to their owners' hands")
    void returnsAllNonlandPermanents() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GloriousAnthem());
        harness.addToBattlefield(player2, new SerraAngel());

        castNormally();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(c -> c.getName())
                .containsExactlyInAnyOrder("Grizzly Bears", "Glorious Anthem");
        assertThat(gd.playerHands.get(player2.getId()))
                .extracting(c -> c.getName())
                .contains("Serra Angel");
        harness.assertInGraveyard(player1, "Devastation Tide");
    }

    @Test
    @DisplayName("Lands stay on the battlefield")
    void landsStay() {
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player2, new Island());
        harness.addToBattlefield(player1, new GrizzlyBears());

        castNormally();

        harness.assertOnBattlefield(player1, "Island");
        harness.assertOnBattlefield(player2, "Island");
        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(c -> c.getName())
                .containsExactly("Grizzly Bears");
    }

    @Test
    @DisplayName("Works with empty battlefields")
    void worksWithEmptyBattlefields() {
        castNormally();

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Miracle cast for {1}{U} off the first draw bounces all nonland permanents")
    void miracleCastBouncesNonlandPermanents() {
        harness.addToBattlefield(player2, new SerraAngel());
        harness.addToBattlefield(player1, new Island());
        harness.setLibrary(player1, List.of(new DevastationTide()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        harness.inMutationScope(() -> harness.getPlayerInputService().processNextMayAbility(gd));
        harness.handleMayAbilityChosen(player1, true); // reveal

        harness.passBothPriorities(); // resolve miracle trigger → cast prompt
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true); // cast for miracle cost
        harness.passBothPriorities(); // resolve Devastation Tide

        harness.assertOnBattlefield(player1, "Island");
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId()))
                .extracting(c -> c.getName())
                .contains("Serra Angel");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Declining the miracle reveal leaves the card in hand")
    void decliningRevealLeavesInHand() {
        DevastationTide tide = new DevastationTide();
        harness.setLibrary(player1, List.of(tide));

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        harness.inMutationScope(() -> harness.getPlayerInputService().processNextMayAbility(gd));
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(tide.getId()));
    }

    @Test
    void returnsAttachedAuraAndEquipmentToHandAlongWithCreature() {
        var creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        var aura = harness.addToBattlefieldAndReturn(player2, new Defang());
        aura.setAttachedTo(creature.getId());
        var equipment = harness.addToBattlefieldAndReturn(player1, new AngelicArmaments());
        equipment.setAttachedTo(creature.getId());

        castNormally();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).extracting(c -> c.getName())
                .containsExactlyInAnyOrder("Grizzly Bears", "Angelic Armaments");
        assertThat(gd.playerHands.get(player2.getId())).extracting(c -> c.getName())
                .contains("Defang");
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    void returnsPermanentToOwnerRatherThanController() {
        GrizzlyBears bears = new GrizzlyBears();
        bears.setOwnerId(player2.getId());
        harness.addToBattlefield(player1, bears);

        castNormally();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(bears);
        assertThat(gd.playerHands.get(player2.getId())).contains(bears);
    }

    @Test
    void bouncedTokensCeaseToExistInsteadOfRemainingInHand() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new ThatcherRevolt(), "{2}{R}");
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(3);

        castNormally();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(c -> c.getName())
                .containsExactlyInAnyOrder("Thatcher Revolt", "Devastation Tide");
    }

    @Test
    void secondDrawOfTurnDoesNotOfferMiracle() {
        DevastationTide tide = new DevastationTide();
        harness.setLibrary(player1, List.of(new Island(), tide));

        harness.inMutationScope(() -> {
            harness.getDrawService().resolveDrawCard(gd, player1.getId());
            harness.getDrawService().resolveDrawCard(gd, player1.getId());
            harness.getPlayerInputService().processNextMayAbility(gd);
        });

        assertThat(gd.playerHands.get(player1.getId())).contains(tide);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    void decliningMiracleCastLeavesCardAndManaUntouched() {
        harness.addToBattlefield(player2, new SerraAngel());
        harness.setLibrary(player1, List.of(new DevastationTide()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        revealMiracleAndResolveTrigger();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player2, "Serra Angel");
        assertThat(gd.playerHands.get(player1.getId())).extracting(c -> c.getName())
                .contains("Devastation Tide");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
    }

    @Test
    void miracleCanBeCastDuringOpponentsTurn() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);
        harness.addToBattlefield(player2, new SerraAngel());
        harness.setLibrary(player1, List.of(new DevastationTide()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        revealMiracleAndResolveTrigger();

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).extracting(c -> c.getName())
                .contains("Serra Angel");
        harness.assertInGraveyard(player1, "Devastation Tide");
    }

    @Test
    void miracleCostReceivesSpellCostReduction() {
        harness.addToBattlefield(player1, new ArcaneMelee());
        harness.setLibrary(player1, List.of(new DevastationTide()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        revealMiracleAndResolveTrigger();

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Devastation Tide");
        assertThat(gd.playerHands.get(player1.getId())).extracting(c -> c.getName())
                .containsExactly("Arcane Melee");
    }

    @Test
    void miracleCannotBypassRuleOfLawAfterAnotherSpell() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player1, new RuleOfLaw());
        harness.castFromHand(player1, new ThatcherRevolt(), "{2}{R}");
        harness.passBothPriorities();
        harness.setLibrary(player1, List.of(new DevastationTide()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        revealMiracleAndResolveTrigger();

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).extracting(c -> c.getName())
                .contains("Devastation Tide");
        harness.assertOnBattlefield(player1, "Rule of Law");
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(4);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
    }

    private void revealMiracleAndResolveTrigger() {
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        harness.inMutationScope(() -> harness.getPlayerInputService().processNextMayAbility(gd));
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }
}
