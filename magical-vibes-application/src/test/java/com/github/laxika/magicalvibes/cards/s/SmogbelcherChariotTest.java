package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SmogbelcherChariot.class, SerraAngel.class, GrizzlyBears.class, Unsummon.class})
class SmogbelcherChariotTest extends BaseCardTest {

    @Test
    void attackTriggerTargetsOnlyCreatureThatCrewedItAndGrantsChosenKeywordIndefinitely() {
        addReadyChariot();
        Permanent crewer = addCreatureReady(player1, new SerraAngel());
        Permanent bystander = addCreatureReady(player1, new GrizzlyBears());

        crewChariot(crewer);
        declareAttackers(List.of(0));

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(crewer.getId()).doesNotContain(bystander.getId());

        harness.handlePermanentChosen(player1, crewer.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "MENACE");

        assertThat(gqs.hasKeyword(gd, crewer, Keyword.MENACE)).isTrue();

        gd.interaction.clearAwaitingInput();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, crewer, Keyword.MENACE)).isTrue();
    }

    @Test
    void attackTriggerDoesNotExistWithoutAValidCrewer() {
        addReadyChariot();
        Permanent crewer = addCreatureReady(player1, new SerraAngel());
        crewChariot(crewer);
        gd.playerBattlefields.get(player1.getId()).remove(crewer);
        declareAttackers(List.of(0));

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @ParameterizedTest
    @EnumSource(value = Keyword.class, names = {"MENACE", "DEATHTOUCH", "LIFELINK"})
    void chosenKeywordPersistsAfterReturningToHandAndBeingRecast(Keyword keyword) {
        addReadyChariot();
        Permanent crewer = addCreatureReady(player1, new SerraAngel());
        crewChariot(crewer);
        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, crewer.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, keyword.name());

        assertThat(gqs.hasKeyword(gd, crewer, keyword)).isTrue();

        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0, crewer.getId());
        assertThat(gd.playerHands.get(player1.getId())).contains(crewer.getCard());

        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, crewer.getCard(), "{3}{W}{W}");
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Serra Angel");
        assertThat(returned.getId()).isNotEqualTo(crewer.getId());
        assertThat(gqs.hasKeyword(gd, returned, keyword)).isTrue();
    }

    private Permanent addReadyChariot() {
        return addCreatureReady(player1, new SmogbelcherChariot());
    }

    private void crewChariot(Permanent crewer) {
        harness.activateAbility(player1, 0, null, null);
        if (gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class) != null) {
            harness.handlePermanentChosen(player1, crewer.getId());
        }
        harness.passBothPriorities();
    }
}
