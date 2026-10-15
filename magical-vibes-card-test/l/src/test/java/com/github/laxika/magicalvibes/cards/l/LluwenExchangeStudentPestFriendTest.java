package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LluwenExchangeStudentPestFriend.class})
class LluwenExchangeStudentPestFriendTest extends BaseCardTest {

    @Test
    @DisplayName("Lluwen enters prepared with a castable Pest Friend copy in exile")
    void entersPrepared() {
        Permanent lluwen = castLluwen();

        assertThat(lluwen.isPrepared()).isTrue();
        UUID copyId = lluwen.getPreparedSpellCardId();
        assertThat(copyId).isNotNull();
        assertThat(gd.findExiledCard(copyId)).isNotNull();
        assertThat(gd.exilePlayPermissions.get(copyId)).isEqualTo(player1.getId());
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn).doesNotContain(copyId);
    }

    @Test
    @DisplayName("Exiling a creature card from the graveyard prepares Lluwen at sorcery speed")
    void graveyardAbilityPreparesLluwen() {
        Permanent lluwen = addCreatureReady(player1, new LluwenExchangeStudentPestFriend());
        LluwenExchangeStudentPestFriend creature = new LluwenExchangeStudentPestFriend();
        harness.setGraveyard(player1, List.of(creature));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        int lluwenIndex = gd.playerBattlefields.get(player1.getId()).indexOf(lluwen);
        harness.activateAbility(player1, lluwenIndex, null, null);
        harness.handleGraveyardCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(creature);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(creature);
        assertThat(lluwen.isPrepared()).isTrue();
        assertThat(gd.findExiledCard(lluwen.getPreparedSpellCardId())).isNotNull();
    }

    @Test
    @DisplayName("Casting Pest Friend unprepares Lluwen, creates a Pest, and the Pest gains life when attacking")
    void pestFriendCreatesAttackingLifeGainPest() {
        Permanent lluwen = castLluwen();
        UUID copyId = lluwen.getPreparedSpellCardId();
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castFromExile(player1, copyId);
        harness.passBothPriorities();

        assertThat(lluwen.isPrepared()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().isToken());

        Permanent pest = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        pest.setSummoningSick(false);
        harness.setLife(player1, 20);

        declareAttackers(player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(pest)));
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
    }

    private Permanent castLluwen() {
        harness.setHand(player1, List.of(new LluwenExchangeStudentPestFriend()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
        return findPermanent(player1, "Lluwen, Exchange Student");
    }

    @Test
    void becomesUnpreparedWhenSpellIsCastBeforeItResolves() {
        Permanent lluwen = castLluwen();
        UUID copyId = lluwen.getPreparedSpellCardId();
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castFromExile(player1, copyId);

        assertThat(lluwen.isPrepared()).isFalse();
        assertThat(lluwen.getPreparedSpellCardId()).isNull();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
        resolveAllTriggers();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken()).hasSize(1);
    }

    @Test
    void canPrepareAgainAfterCastingPestFriend() {
        Permanent lluwen = castLluwen();
        UUID oldCopyId = lluwen.getPreparedSpellCardId();
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castFromExile(player1, oldCopyId);
        resolveAllTriggers();
        harness.setGraveyard(player1, List.of(new LluwenExchangeStudentPestFriend()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, null);
        harness.handleGraveyardCardChosen(player1, 0);
        resolveAllTriggers();

        assertThat(lluwen.isPrepared()).isTrue();
        assertThat(lluwen.getPreparedSpellCardId()).isNotEqualTo(oldCopyId);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castFromExile(player1, lluwen.getPreparedSpellCardId());
        resolveAllTriggers();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken()).hasSize(2);
    }

    @Test
    void preparingWhileAlreadyPreparedPaysCostWithoutAddingAnotherCopy() {
        Permanent lluwen = castLluwen();
        UUID copyId = lluwen.getPreparedSpellCardId();
        LluwenExchangeStudentPestFriend creature = new LluwenExchangeStudentPestFriend();
        harness.setGraveyard(player1, List.of(creature));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, null);
        harness.handleGraveyardCardChosen(player1, 0);
        resolveAllTriggers();

        assertThat(lluwen.getPreparedSpellCardId()).isEqualTo(copyId);
        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(2).contains(creature);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    void cannotActivateOutsideMainPhase() {
        Permanent lluwen = addCreatureReady(player1, new LluwenExchangeStudentPestFriend());
        harness.setGraveyard(player1, List.of(new LluwenExchangeStudentPestFriend()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(lluwen.isPrepared()).isFalse();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
    }

    @Test
    void cannotExileAnOpponentsCreatureToPayCost() {
        Permanent lluwen = addCreatureReady(player1, new LluwenExchangeStudentPestFriend());
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of(new LluwenExchangeStudentPestFriend()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(lluwen.isPrepared()).isFalse();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
    }
}
