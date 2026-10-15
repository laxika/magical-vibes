package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.a.ActOfTreason;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.Card;
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

@CardUsed({LightningSecuritySergeant.class, Island.class, ActOfTreason.class, LightningBolt.class})
class LightningSecuritySergeantTest extends BaseCardTest {

    private Permanent addLightning() {
        Permanent lightning = addCreatureReady(player1, new LightningSecuritySergeant());
        lightning.setAttacking(true);
        return lightning;
    }

    private Card resolveCombatWithTopCard(Card topCard) {
        addLightning();
        harness.setLibrary(player1, List.of(topCard));
        resolveCombat();
        harness.passBothPriorities();
        return topCard;
    }

    @Test
    @DisplayName("Exiles the top card and lets its controller play it")
    void exilesTopCardAndAllowsControllerToPlayIt() {
        Card topCard = resolveCombatWithTopCard(new Island());

        assertThat(gd.findExiledCard(topCard.getId())).isNotNull();
        assertThat(gd.exilePlayPermissions).containsEntry(topCard.getId(), player1.getId());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromExile(player1, topCard.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Island");
        assertThat(gd.findExiledCard(topCard.getId())).isNull();
    }

    @Test
    @DisplayName("Loses the permission when another player gains control")
    void permissionEndsOnControlChange() {
        Permanent lightning = addLightning();
        Card topCard = new Island();
        harness.setLibrary(player1, List.of(topCard));
        resolveCombat();
        harness.passBothPriorities();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new ActOfTreason()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.castAndResolveSorcery(player2, 0, lightning.getId());

        assertThat(gd.exilePlayPermissions).doesNotContainKey(topCard.getId());
        assertThat(gd.exilePlayPermissionSourcePermanents).doesNotContainKey(topCard.getId());

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(p -> p.getId().equals(lightning.getId()));
        assertThat(gd.exilePlayPermissions).doesNotContainKey(topCard.getId());
    }

    @Test
    @DisplayName("Loses the permission when the source leaves the battlefield")
    void permissionEndsOnSourceLeavingBattlefield() {
        Permanent lightning = addLightning();
        Card topCard = new Island();
        harness.setLibrary(player1, List.of(topCard));
        resolveCombat();
        harness.passBothPriorities();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, lightning.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(p -> p.getId().equals(lightning.getId()));
        assertThat(gd.exilePlayPermissions).doesNotContainKey(topCard.getId());
        assertThat(gd.findExiledCard(topCard.getId())).isNotNull();
    }

    @Test
    @DisplayName("Exiled instants require mana and can be cast on the opponent's turn")
    void exiledInstantRequiresManaAndUsesNormalTiming() {
        Card topCard = resolveCombatWithTopCard(new LightningBolt());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castFromExile(player1, topCard.getId(), player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.findExiledCard(topCard.getId())).isNotNull();

        harness.addMana(player1, ManaColor.RED, 1);
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());
        harness.castFromExile(player1, topCard.getId(), player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, lifeBefore - 3);
        harness.assertInGraveyard(player1, "Lightning Bolt");
        assertThat(gd.findExiledCard(topCard.getId())).isNull();
    }

    @Test
    @DisplayName("Exiled sorceries cannot be cast during combat")
    void exiledSorceryUsesNormalTiming() {
        Card topCard = resolveCombatWithTopCard(new ActOfTreason());
        Permanent lightning = findPermanent(player1, "Lightning, Security Sergeant");
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castFromExile(player1, topCard.getId(), lightning.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.findExiledCard(topCard.getId())).isNotNull();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromExile(player1, topCard.getId(), lightning.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Act of Treason");
        assertThat(gd.findExiledCard(topCard.getId())).isNull();
    }

    @Test
    @DisplayName("The trigger still exiles a card if Lightning dies before resolution, without granting permission")
    void sourceLeavingBeforeResolutionStillExilesCard() {
        Permanent lightning = addLightning();
        Card topCard = new Island();
        harness.setLibrary(player1, List.of(topCard));
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.withAutoStop(TurnStep.COMBAT_DAMAGE, this::resolveCombat);
        assertThat(gd.stack).hasSize(1);

        harness.castAndResolveInstant(player2, 0, lightning.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Lightning, Security Sergeant");
        assertThat(gd.findExiledCard(topCard.getId())).isNotNull();
        assertThat(gd.exilePlayPermissions).doesNotContainKey(topCard.getId());
        assertThatThrownBy(() -> harness.castFromExile(player1, topCard.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("An empty library does not cause a draw or prevent trigger resolution")
    void emptyLibraryDoesNothing() {
        addLightning();
        harness.setLibrary(player1, List.of());
        harness.withAutoStop(TurnStep.COMBAT_DAMAGE, this::resolveCombat);
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.exilePlayPermissions).isEmpty();
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Permission survives the turn ending while Lightning remains controlled")
    void permissionSurvivesTurnEnding() {
        Card topCard = resolveCombatWithTopCard(new Island());
        harness.setLibrary(player1, List.of(new Island(), new Island()));
        harness.setLibrary(player2, List.of(new Island(), new Island()));
        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);

        assertThat(gd.exilePlayPermissions).containsEntry(topCard.getId(), player1.getId());
        assertThat(gd.findExiledCard(topCard.getId())).isNotNull();

        harness.passUntilWithNoAttackers(player1, TurnStep.PRECOMBAT_MAIN);
        harness.castFromExile(player1, topCard.getId());
        harness.assertOnBattlefield(player1, "Island");
    }
}
