package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.y.YoshimaruEverFaithful;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ConfessionDial.class, YoshimaruEverFaithful.class, GrizzlyBears.class, Forest.class, Shock.class})
class ConfessionDialTest extends BaseCardTest {

    @Test
    @DisplayName("Enters and surveils three cards")
    void entersAndSurveilsThree() {
        List<Card> library = List.of(new Forest(), new Shock(), new Forest(), new Forest());
        harness.setLibrary(player1, library);
        harness.setHand(player1, List.of(new ConfessionDial()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        prepareMainPhase();

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(library.get(0), library.get(1), library.get(2));
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0, 1), List.of(2)));

        assertThat(gd.playerDecks.get(player1.getId()).subList(0, 2))
                .containsExactly(library.get(0), library.get(1));
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(library.get(2));
    }

    @Test
    @DisplayName("Grants escape to a targeted legendary creature card")
    void grantsEscapeToTargetedLegendaryCreature() {
        Permanent dial = addReadyDial();
        YoshimaruEverFaithful target = new YoshimaruEverFaithful();
        Forest firstExile = new Forest();
        Shock secondExile = new Shock();
        Forest thirdExile = new Forest();
        harness.setGraveyard(player1, List.of(target, firstExile, secondExile, thirdExile));
        harness.addMana(player1, ManaColor.WHITE, 1);
        prepareMainPhase();

        int dialIndex = gd.playerBattlefields.get(player1.getId()).indexOf(dial);
        harness.activateAbility(player1, dialIndex, 0, null, target.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        harness.castFromGraveyard(player1, 0, List.of(0, 1, 2));
        harness.passBothPriorities();

        Permanent escaped = findPermanent(player1, "Yoshimaru, Ever Faithful");
        assertThat(escaped.isEscaped()).isTrue();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyInAnyOrder(
                firstExile, secondExile, thirdExile);
    }

    @Test
    @DisplayName("Only legendary creature cards can be targeted")
    void onlyLegendaryCreatureCardsAreTargetable() {
        Permanent dial = addReadyDial();
        GrizzlyBears target = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(target));
        prepareMainPhase();

        int dialIndex = gd.playerBattlefields.get(player1.getId()).indexOf(dial);
        assertThatThrownBy(() -> harness.activateAbility(
                player1, dialIndex, 0, null, target.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyDial() {
        Permanent dial = harness.addToBattlefieldAndReturn(player1, new ConfessionDial());
        dial.setSummoningSick(false);
        return dial;
    }

    private void prepareMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
