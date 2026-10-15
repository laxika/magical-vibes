package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.o.OtterPenguin;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AppaLoyalSkyBison.class, OtterPenguin.class, Island.class})
class AppaLoyalSkyBisonTest extends BaseCardTest {

    @Test
    @DisplayName("The flying mode grants flying to a creature you control until end of turn")
    void flyingModeGrantsFlyingUntilEndOfTurn() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new OtterPenguin());

        castAppa(0, creature.getId());
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("The airbend mode exiles another nonland permanent you control")
    void airbendModeExilesAnotherOwnNonlandPermanent() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new OtterPenguin());

        castAppa(1, creature.getId());
        resolveAllTriggers();

        assertThat(gd.findExiledCard(creature.getOriginalCard().getId())).isNotNull();
        assertThat(gd.exilePlayPermissions.get(creature.getOriginalCard().getId()))
                .isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("The attack trigger airbend mode targets another own nonland permanent")
    void attackTriggerRestrictsTargetsByMode() {
        Permanent appa = addCreatureReady(player1, new AppaLoyalSkyBison());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new OtterPenguin());
        Permanent island = harness.addToBattlefieldAndReturn(player1, new Island());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "Airbend another target nonland permanent you control");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(creature.getId())
                .doesNotContain(appa.getId(), island.getId());

        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();
        assertThat(gd.findExiledCard(creature.getOriginalCard().getId())).isNotNull();
    }

    @Test
    @DisplayName("Entering without being cast allows choosing airbend and excludes lands")
    void enteringWithoutBeingCastAllowsChoosingAirbend() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new OtterPenguin());
        Permanent island = harness.addToBattlefieldAndReturn(player1, new Island());

        Permanent appa = harness.enterBattlefieldAndReturn(player1, new AppaLoyalSkyBison());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "Airbend another target nonland permanent you control");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(creature.getId())
                .doesNotContain(appa.getId(), island.getId());
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.findExiledCard(creature.getOriginalCard().getId())).isNotNull();
    }

    @Test
    @DisplayName("An attack chooses its mode and target before either player passes priority")
    void attackChoosesModeAndTargetBeforePriority() {
        addCreatureReady(player1, new AppaLoyalSkyBison());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new OtterPenguin());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new OtterPenguin());
        Permanent island = harness.addToBattlefieldAndReturn(player1, new Island());

        declareAttackers(List.of(0));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "Target creature you control gains flying until end of turn");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(creature.getId())
                .doesNotContain(opponentCreature.getId(), island.getId());
        harness.handlePermanentChosen(player1, creature.getId());
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("The owner can cast an airbent creature for two generic mana")
    void airbentCreatureCanBeCastForTwoGenericMana() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new OtterPenguin());
        java.util.UUID cardId = creature.getOriginalCard().getId();

        castAppa(1, creature.getId());
        resolveAllTriggers();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castFromExile(player1, cardId);
        resolveAllTriggers();

        assertThat(gd.findExiledCard(cardId)).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getOriginalCard().getId().equals(cardId));
    }

    @Test
    @DisplayName("The flying mode accepts Appa itself as its creature target")
    void flyingModeCanTargetAppa() {
        Permanent appa = addCreatureReady(player1, new AppaLoyalSkyBison());

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
        Permanent appa = addCreatureReady(player1, new AppaLoyalSkyBison());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new OtterPenguin());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Airbend another target nonland permanent you control");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(creature.getId())
                .doesNotContain(appa.getId());
    }

    private void castAppa(int mode, java.util.UUID targetId) {
        harness.setHand(player1, List.of(new AppaLoyalSkyBison()));
        addMana();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, mode == 0
                ? "Target creature you control gains flying until end of turn"
                : "Airbend another target nonland permanent you control");
        harness.handlePermanentChosen(player1, targetId);
        harness.passBothPriorities();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }
}
