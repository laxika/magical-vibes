package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.b.BalefulStrix;
import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GontiCannyAcquisitor.class, BalefulStrix.class, Divination.class, GrizzlyBears.class, Island.class})
class GontiCannyAcquisitorTest extends BaseCardTest {

    @Test
    void reducesCostOfSpellsTheControllerDoesNotOwn() {
        harness.addToBattlefield(player1, new GontiCannyAcquisitor());
        addAttacker(new GrizzlyBears());
        Divination spell = new Divination();
        spell.setOwnerId(player2.getId());
        harness.setLibrary(player2, List.of(spell, new Island()));
        harness.setLibrary(player1, List.of(new Island(), new Island(), new Island()));
        harness.setHand(player1, List.of());

        resolveCombat();
        resolveAllTriggers();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castFromExile(player1, spell.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).extracting(Card::getId)
                .contains(spell.getId());
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    void doesNotReduceCostOfSpellsTheControllerOwns() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player1, new GontiCannyAcquisitor());

        Divination spell = new Divination();
        spell.setOwnerId(player1.getId());
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void combatDamageExilesOnlyOneCardFaceDownWithPersistentAnyManaPermission() {
        Permanent gonti = harness.addToBattlefieldAndReturn(player1, new GontiCannyAcquisitor());
        addAttacker(new GrizzlyBears());
        addAttacker(new GrizzlyBears());
        Card topCard = new Divination();
        harness.setLibrary(player2, List.of(topCard, new Island()));

        resolveCombat();
        harness.passBothPriorities();

        ExiledCardEntry entry = gd.findExiledCard(topCard.getId());
        assertThat(entry).isNotNull();
        assertThat(entry.faceDown()).isTrue();
        assertThat(entry.sourcePermanentId()).isEqualTo(gonti.getId());
        assertThat(entry.exilerId()).isEqualTo(player1.getId());
        assertThat(gd.exilePlayPermissions).containsEntry(topCard.getId(), player1.getId());
        assertThat(gd.exilePlayAnyManaTypeWhileExiled).contains(topCard.getId());
        assertThat(gd.getPlayerExiledCards(player2.getId())).hasSize(1);
    }

    @Test
    void castsExiledCreatureWithOffColorManaAndReducedCost() {
        harness.addToBattlefield(player1, new GontiCannyAcquisitor());
        addAttacker(new GrizzlyBears());
        Card stolen = new GrizzlyBears();
        stolen.setOwnerId(player2.getId());
        harness.setLibrary(player2, List.of(stolen, new Island()));

        resolveCombat();
        resolveAllTriggers();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castFromExile(player1, stolen.getId());
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(stolen.getId())).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).extracting(p -> p.getCard().getId())
                .contains(stolen.getId());
    }

    @Test
    void permissionAndAnyManaPersistAfterGontiLeavesButCostReductionDoesNot() {
        Permanent gonti = harness.addToBattlefieldAndReturn(player1, new GontiCannyAcquisitor());
        addAttacker(new GrizzlyBears());
        Card stolen = new GrizzlyBears();
        stolen.setOwnerId(player2.getId());
        harness.setLibrary(player2, List.of(stolen, new Island()));

        resolveCombat();
        resolveAllTriggers();
        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, gonti);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.addMana(player1, ManaColor.RED, 1);
        assertThatThrownBy(() -> harness.castFromExile(player1, stolen.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castFromExile(player1, stolen.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).extracting(p -> p.getCard().getId())
                .contains(stolen.getId());
        assertThat(gd.findExiledCard(stolen.getId())).isNull();
    }

    @Test
    void exiledLandRequiresMainPhaseAndUsesNormalLandPlay() {
        harness.addToBattlefield(player1, new GontiCannyAcquisitor());
        addAttacker(new GrizzlyBears());
        Card stolen = new Island();
        stolen.setOwnerId(player2.getId());
        harness.setLibrary(player2, List.of(stolen, new Island()));

        resolveCombat();
        resolveAllTriggers();
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        assertThatThrownBy(() -> harness.castFromExile(player1, stolen.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.castFromExile(player1, stolen.getId());

        assertThat(gd.findExiledCard(stolen.getId())).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).extracting(p -> p.getCard().getId())
                .contains(stolen.getId());
        harness.setHand(player1, List.of(new Island()));
        assertThatThrownBy(() -> harness.playLand(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void gontisOwnCombatDamageTriggersTheAbility() {
        addAttacker(new GontiCannyAcquisitor());
        Card stolen = new Island();
        stolen.setOwnerId(player2.getId());
        harness.setLibrary(player2, List.of(stolen, new Island()));

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.findExiledCard(stolen.getId())).isNotNull();
        assertThat(gd.exilePlayPermissions).containsEntry(stolen.getId(), player1.getId());
    }

    @Test
    void emptyLibraryExilesNothingAndDoesNotCauseADrawLoss() {
        harness.addToBattlefield(player1, new GontiCannyAcquisitor());
        addAttacker(new GrizzlyBears());
        harness.setLibrary(player2, List.of());

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.exilePlayPermissions).isEmpty();
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    @Test
    void genericReductionCannotReduceColoredCostsDespiteAnyManaPermission() {
        harness.addToBattlefield(player1, new GontiCannyAcquisitor());
        addAttacker(new GrizzlyBears());
        Card stolen = new BalefulStrix();
        stolen.setOwnerId(player2.getId());
        harness.setLibrary(player2, List.of(stolen, new Island()));
        harness.setLibrary(player1, List.of(new Island(), new Island()));

        resolveCombat();
        resolveAllTriggers();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castFromExile(player1, stolen.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.findExiledCard(stolen.getId())).isNotNull();

        harness.addMana(player1, ManaColor.RED, 1);
        harness.castFromExile(player1, stolen.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).extracting(p -> p.getCard().getId())
                .contains(stolen.getId());
    }

    private void addAttacker(Card card) {
        Permanent attacker = addCreatureReady(player1, card);
        attacker.setAttacking(true);
    }
}
