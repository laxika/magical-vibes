package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.TimeWarp;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BreyasApprentice.class, FountainOfYouth.class, Forest.class, GrizzlyBears.class, TimeWarp.class})
class BreyasApprenticeTest extends BaseCardTest {

    @Test
    void createsAThopterWhenItEnters() {
        harness.enterBattlefieldAndReturn(player1, new BreyasApprentice());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Thopter")).hasSize(1);
    }

    @Test
    void exilesTheTopCardAndAllowsPlayingItUntilNextTurn() {
        Permanent apprentice = addReadyApprentice();
        Forest topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 0, 0, null);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(topCard);
        assertThat(gd.exiledCards).extracting(entry -> entry.card()).contains(topCard);
        assertThat(gd.exilePlayPermissions).containsEntry(topCard.getId(), player1.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(apprentice.getCard());
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(apprentice);
    }

    @Test
    void boostsTargetCreatureAndSacrificesAnArtifact() {
        Permanent apprentice = addReadyApprentice();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 0, 1, target.getId());
        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(2);
        assertThat(target.getToughnessModifier()).isEqualTo(0);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(apprentice);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(apprentice.getCard());
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(artifact.getCard());
    }

    @Test
    void boostModeRejectsANonCreatureTarget() {
        addReadyApprentice();
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, 1, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    void canPlayTheExiledLandAfterSacrificingTheApprentice() {
        addReadyApprentice();
        Forest land = new Forest();
        harness.setLibrary(player1, List.of(land));

        harness.activateAbility(player1, 0, 0, 0, null);
        harness.passBothPriorities();
        harness.castFromExile(player1, land.getId());

        harness.assertOnBattlefield(player1, "Forest");
        assertThat(gd.exiledCards).extracting(entry -> entry.card()).doesNotContain(land);
    }

    @Test
    void exiledSpellRequiresItsNormalManaCost() {
        addReadyApprentice();
        BreyasApprentice spell = new BreyasApprentice();
        harness.setLibrary(player1, List.of(spell));
        harness.activateAbility(player1, 0, 0, 0, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.castFromExile(player1, spell.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castFromExile(player1, spell.getId());
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Breya's Apprentice");
        assertThat(findPermanents(player1, "Thopter")).hasSize(1);
    }

    @Test
    void permissionSurvivesTheOpponentsTurnAndExpiresAfterYourNextTurn() {
        addReadyApprentice();
        Forest land = new Forest();
        harness.setLibrary(player1, List.of(land, new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new Forest(), new Forest(), new Forest()));
        harness.activateAbility(player1, 0, 0, 0, null);
        harness.passBothPriorities();

        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        assertThat(gd.exilePlayPermissions).containsEntry(land.getId(), player1.getId());
        assertThatThrownBy(() -> harness.castFromExile(player1, land.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.passUntilWithNoAttackers(player1, TurnStep.POSTCOMBAT_MAIN);
        assertThat(gd.exilePlayPermissions).containsEntry(land.getId(), player1.getId());

        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        assertThat(gd.exilePlayPermissions).doesNotContainKey(land.getId());
        assertThat(gd.exiledCards).extracting(entry -> entry.card()).contains(land);
    }

    @Test
    void emptyLibraryDoesNotPreventPayingTheSacrificeCost() {
        Permanent apprentice = addReadyApprentice();
        harness.setLibrary(player1, List.of());

        harness.activateAbility(player1, 0, 0, 0, null);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(apprentice.getCard());
        assertThat(gd.exiledCards).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void createdThopterHasTheSpecifiedCharacteristicsAndCanPayTheArtifactCost() {
        Permanent apprentice = harness.enterBattlefieldAndReturn(player1, new BreyasApprentice());
        harness.passBothPriorities();
        Permanent thopter = findPermanents(player1, "Thopter").getFirst();

        assertThat(thopter.getCard().isToken()).isTrue();
        assertThat(thopter.getCard().getColors()).isEmpty();
        assertThat(gqs.isArtifact(gd, thopter)).isTrue();
        assertThat(gqs.hasKeyword(gd, thopter, Keyword.FLYING)).isTrue();
        assertThat(gqs.getEffectivePower(gd, thopter)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, thopter)).isEqualTo(1);

        apprentice.setSummoningSick(false);
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new Forest(), new Forest()));
        harness.activateAbility(player1, 0, 0, 1, apprentice.getId());
        harness.handlePermanentChosen(player1, thopter.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(thopter);
        assertThat(apprentice.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, apprentice)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, apprentice)).isEqualTo(3);

        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        assertThat(gqs.getEffectivePower(gd, apprentice)).isEqualTo(2);
    }

    @Test
    void activationOnOpponentsTurnAllowsPlayingTheCardOnYourUpcomingTurn() {
        addReadyApprentice();
        Forest land = new Forest();
        harness.setLibrary(player1, List.of(land, new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new Forest(), new Forest(), new Forest()));
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        harness.ensurePriority(player1);
        harness.activateAbility(player1, 0, 0, 0, null);
        harness.passBothPriorities();

        harness.passUntilWithNoAttackers(player1, TurnStep.POSTCOMBAT_MAIN);
        harness.castFromExile(player1, land.getId());

        harness.assertOnBattlefield(player1, "Forest");
    }

    @Test
    void permissionExpiresAtTheEndOfYourNextTurnEvenWhenItIsAnExtraTurn() {
        addReadyApprentice();
        Forest land = new Forest();
        harness.setLibrary(player1, List.of(land, new Forest(), new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new Forest(), new Forest(), new Forest()));
        harness.activateAbility(player1, 0, 0, 0, null);
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new TimeWarp()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castSorcery(player1, 0, player1.getId());
        harness.passBothPriorities();

        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        harness.passUntilWithNoAttackers(player1, TurnStep.PRECOMBAT_MAIN);
        assertThat(gd.exilePlayPermissions).containsEntry(land.getId(), player1.getId());

        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        assertThat(gd.exilePlayPermissions).doesNotContainKey(land.getId());
        assertThat(gd.exiledCards).extracting(entry -> entry.card()).contains(land);
    }

    @Test
    void summoningSicknessPreventsActivationEvenWhenSacrificingItself() {
        Permanent apprentice = harness.addToBattlefieldAndReturn(player1, new BreyasApprentice());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, 0, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(apprentice);
        assertThat(apprentice.isTapped()).isFalse();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    private Permanent addReadyApprentice() {
        return addCreatureReady(player1, new BreyasApprentice());
    }
}
