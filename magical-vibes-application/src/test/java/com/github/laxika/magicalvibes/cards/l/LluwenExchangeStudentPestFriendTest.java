package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

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
        GrizzlyBears creature = new GrizzlyBears();
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
        harness.passBothPriorities();
        harness.passBothPriorities();
        return findPermanent(player1, "Lluwen, Exchange Student");
    }
}
