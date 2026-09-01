package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AppaLoyalSkyBison.class, GrizzlyBears.class, Island.class})
class AppaLoyalSkyBisonTest extends BaseCardTest {

    @Test
    @DisplayName("The flying mode grants flying to a creature you control until end of turn")
    void flyingModeGrantsFlyingUntilEndOfTurn() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castAppa(0, bears.getId());
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("The airbend mode exiles another nonland permanent you control")
    void airbendModeExilesAnotherOwnNonlandPermanent() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castAppa(1, bears.getId());
        resolveAllTriggers();

        assertThat(gd.findExiledCard(bears.getOriginalCard().getId())).isNotNull();
        assertThat(gd.exilePlayPermissions.get(bears.getOriginalCard().getId()))
                .isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("The attack trigger airbend mode targets another own nonland permanent")
    void attackTriggerRestrictsTargetsByMode() {
        Permanent appa = addReadyAppa(player1);
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent island = harness.addToBattlefieldAndReturn(player1, new Island());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "Airbend another target nonland permanent you control");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(bears.getId())
                .doesNotContain(appa.getId(), island.getId());

        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();
        assertThat(gd.findExiledCard(bears.getOriginalCard().getId())).isNotNull();
    }

    @Test
    @DisplayName("The airbend mode rejects a land target")
    void airbendRejectsLandTarget() {
        Permanent island = harness.addToBattlefieldAndReturn(player1, new Island());

        harness.setHand(player1, List.of(new AppaLoyalSkyBison()));
        addMana();
        assertThatThrownBy(() -> harness.castCreature(player1, 0, 1, island.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nonland");
    }

    @Test
    @DisplayName("The flying mode accepts Appa itself as its creature target")
    void flyingModeCanTargetAppa() {
        Permanent appa = addReadyAppa(player1);

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Target creature you control gains flying until end of turn");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(appa.getId());

        harness.handlePermanentChosen(player1, appa.getId());
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, appa, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("The airbend mode does not target Appa itself")
    void airbendRejectsSourceTarget() {
        Permanent appa = addReadyAppa(player1);
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Airbend another target nonland permanent you control");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(bears.getId())
                .doesNotContain(appa.getId());
    }

    private void castAppa(int mode, java.util.UUID targetId) {
        harness.setHand(player1, List.of(new AppaLoyalSkyBison()));
        addMana();
        harness.castCreature(player1, 0, mode, targetId);
        harness.passBothPriorities();
    }

    private Permanent addReadyAppa(Player player) {
        Permanent appa = new Permanent(new AppaLoyalSkyBison());
        appa.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(appa);
        return appa;
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }
}
