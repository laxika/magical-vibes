package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.FinaleOfRevelation;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.NagaOracle;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EyeOfDuskmantle.class, NagaOracle.class, Shock.class, Forest.class, GrizzlyBears.class, FinaleOfRevelation.class})
class EyeOfDuskmantleTest extends BaseCardTest {

    private void surveilThree(Card cardToGraveyard) {
        Card top1 = new GrizzlyBears();
        Card top2 = new GrizzlyBears();
        harness.setLibrary(player1, List.of(cardToGraveyard, top1, top2));
        harness.setGraveyard(player1, List.of());
        harness.setHand(player1, List.of(new NagaOracle()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0, 1, 2)));
        harness.clearPriorityPassed();
    }

    @Test
    @DisplayName("casts a card surveilled this turn by paying life equal to its mana value")
    void castsSurveilledSpellByPayingLife() {
        var eye = harness.addToBattlefieldAndReturn(player1, new EyeOfDuskmantle());
        Shock shock = new Shock();
        surveilThree(shock);

        harness.castFromGraveyardTargeting(player1, 0, eye.getId());

        harness.assertLife(player1, 19);
    }

    @Test
    @DisplayName("cannot cast a graveyard card that was not surveilled this turn")
    void cannotCastUnsurveilledCard() {
        harness.addToBattlefield(player1, new EyeOfDuskmantle());
        harness.setGraveyard(player1, List.of(new Shock()));
        harness.setHand(player1, List.of());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castFromGraveyardTargeting(player1, 0,
                harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()).getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("can play a land surveilled this turn")
    void playsSurveilledLand() {
        harness.addToBattlefield(player1, new EyeOfDuskmantle());
        Forest forest = new Forest();
        surveilThree(forest);

        harness.playGraveyardLand(player1, 0);

        harness.assertOnBattlefield(player1, "Forest");
    }

    @Test
    @DisplayName("an X spell cast for life must use X equal to zero")
    void cannotChooseNonzeroXWhenPayingLife() {
        harness.addToBattlefield(player1, new EyeOfDuskmantle());
        surveilThree(new FinaleOfRevelation());

        assertThatThrownBy(() -> harness.castFlashback(player1, 0, 5, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertLife(player1, 20);
        harness.assertInGraveyard(player1, "Finale of Revelation");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("an X spell can be cast with X zero for its fixed mana value in life")
    void castsXSpellWithZeroX() {
        harness.addToBattlefield(player1, new EyeOfDuskmantle());
        surveilThree(new FinaleOfRevelation());
        harness.setLibrary(player1, List.of(new Forest()));

        harness.castFlashback(player1, 0, 0, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertNotInGraveyard(player1, "Finale of Revelation");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card instanceof FinaleOfRevelation);
    }

    @Test
    @DisplayName("a resolved spell cannot be cast again without being surveilled again")
    void cannotRecastAfterSpellReturnsToGraveyard() {
        var eye = harness.addToBattlefieldAndReturn(player1, new EyeOfDuskmantle());
        Shock shock = new Shock();
        surveilThree(shock);
        harness.castFromGraveyardTargeting(player1, 0, eye.getId());
        harness.passBothPriorities();

        int index = gd.playerGraveyards.get(player1.getId()).indexOf(shock);
        assertThat(index).isNotNegative();
        assertThatThrownBy(() -> harness.castFromGraveyardTargeting(player1, index, eye.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertLife(player1, 19);
    }

    @Test
    @DisplayName("cards surveilled before Eye enters are eligible during the same turn")
    void castsCardSurveilledBeforeEyeEntered() {
        surveilThree(new Shock());
        var eye = harness.addToBattlefieldAndReturn(player1, new EyeOfDuskmantle());

        harness.castFromGraveyardTargeting(player1, 0, eye.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
        harness.assertInGraveyard(player1, "Shock");
    }

    @Test
    @DisplayName("normal mana cannot substitute for an unaffordable life payment")
    void cannotPayNormalManaInsteadOfLife() {
        harness.addToBattlefield(player1, new EyeOfDuskmantle());
        surveilThree(new GrizzlyBears());
        harness.setLife(player1, 1);
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.assertLife(player1, 1);
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("permission to play a surveilled land does not grant an extra land play")
    void cannotPlaySecondLand() {
        harness.addToBattlefield(player1, new EyeOfDuskmantle());
        surveilThree(new Forest());
        harness.setHand(player1, List.of(new Forest()));
        harness.playLand(player1, 0);

        assertThatThrownBy(() -> harness.playGraveyardLand(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInGraveyard(player1, "Forest");
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("creature spells still require sorcery timing")
    void cannotCastCreatureDuringCombat() {
        harness.addToBattlefield(player1, new EyeOfDuskmantle());
        surveilThree(new GrizzlyBears());
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.assertLife(player1, 20);
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("a surveilled creature resolves onto the battlefield and costs its mana value in life")
    void castsSurveilledCreature() {
        harness.addToBattlefield(player1, new EyeOfDuskmantle());
        surveilThree(new GrizzlyBears());

        harness.castFromGraveyard(player1, 0);
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(card -> card instanceof GrizzlyBears).hasSize(2);
    }
}
