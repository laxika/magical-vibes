package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.t.TerrorOfTheFairgrounds;
import com.github.laxika.magicalvibes.cards.f.FiligreeFamiliar;
import com.github.laxika.magicalvibes.cards.s.ServoExhibition;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import com.github.laxika.magicalvibes.service.battlefield.CreatureControlService;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AcrobaticManeuver.class, TerrorOfTheFairgrounds.class, ServoExhibition.class, FiligreeFamiliar.class})
class AcrobaticManeuverTest extends BaseCardTest {

    @Test
    @DisplayName("Flickers a creature and draws a card")
    void flickersCreatureAndDrawsCard() {
        harness.addToBattlefield(player1, new TerrorOfTheFairgrounds());
        harness.setHand(player1, List.of(new AcrobaticManeuver()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        UUID bearsId = harness.getPermanentId(player1, "Terror of the Fairgrounds");
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.castAndResolveInstant(player1, 0, bearsId);

        harness.assertOnBattlefield(player1, "Terror of the Fairgrounds");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(card -> card.getName().equals("Terror of the Fairgrounds"));
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
    }

    @Test
    @DisplayName("Returned creature has summoning sickness")
    void returnedCreatureHasSummoningSickness() {
        harness.addToBattlefield(player1, new TerrorOfTheFairgrounds());
        harness.setHand(player1, List.of(new AcrobaticManeuver()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        UUID bearsId = harness.getPermanentId(player1, "Terror of the Fairgrounds");

        harness.castAndResolveInstant(player1, 0, bearsId);

        Permanent returned = findPermanent(player1, "Terror of the Fairgrounds");
        assertThat(returned.isSummoningSick()).isTrue();
    }

    @Test
    @DisplayName("Cannot target an opponent's creature")
    void cannotTargetOpponentCreature() {
        harness.addToBattlefield(player2, new TerrorOfTheFairgrounds());
        harness.setHand(player1, List.of(new AcrobaticManeuver()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        UUID opponentBearsId = harness.getPermanentId(player2, "Terror of the Fairgrounds");

        assertThatThrownBy(() -> harness.castInstant(player1, 0, opponentBearsId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A controlled creature returns to its owner and the caster draws")
    void returnsToOwnerAndCasterDraws() {
        TerrorOfTheFairgrounds card = new TerrorOfTheFairgrounds();
        card.setOwnerId(player2.getId());
        Permanent stolen = harness.addToBattlefieldAndReturn(player2, card);
        harness.inMutationScope(() -> GameTestEngineContext.get().getBean(CreatureControlService.class)
                .applyControlEffect(gd, player1.getId(), stolen,
                        new GainControlOfTargetEffect(ControlDuration.PERMANENT), EffectDuration.PERMANENT,
                        null, "Test setup"));
        harness.setHand(player1, List.of(new AcrobaticManeuver()));
        harness.setLibrary(player1, List.of(new TerrorOfTheFairgrounds()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castAndResolveInstant(player1, 0, stolen.getId());

        harness.assertNotOnBattlefield(player1, "Terror of the Fairgrounds");
        harness.assertOnBattlefield(player2, "Terror of the Fairgrounds");
        assertThat(findPermanent(player2, "Terror of the Fairgrounds").getId()).isNotEqualTo(stolen.getId());
        harness.assertInHand(player1, "Terror of the Fairgrounds");
        harness.assertNotInHand(player2, "Terror of the Fairgrounds");
    }

    @Test
    @DisplayName("Does not draw when its only target has left the battlefield")
    void missingTargetPreventsDraw() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new TerrorOfTheFairgrounds());
        harness.setHand(player1, List.of(new AcrobaticManeuver()));
        harness.setLibrary(player1, List.of(new TerrorOfTheFairgrounds()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castInstant(player1, 0, target.getId());
        harness.getPermanentRemovalService().removePermanentToExile(gd, target);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        harness.assertInGraveyard(player1, "Acrobatic Maneuver");
    }

    @Test
    @DisplayName("Exiled creature tokens do not return, but the caster still draws")
    void tokenDoesNotReturnButStillDraws() {
        harness.setHand(player1, List.of(new ServoExhibition(), new AcrobaticManeuver()));
        harness.setLibrary(player1, List.of(new TerrorOfTheFairgrounds()));
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.castAndResolveSorcery(player1, 0, 0);
        UUID servoId = harness.getPermanentId(player1, "Servo");

        harness.castAndResolveInstant(player1, 0, servoId);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().getName().equals("Servo"))
                .hasSize(1);
        harness.assertInHand(player1, "Terror of the Fairgrounds");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(card -> card.getName().equals("Servo"));
    }

    @Test
    @DisplayName("Returning a creature triggers its enter ability without a death trigger")
    void returningCreatureTriggersEnterAbility() {
        harness.addToBattlefield(player1, new FiligreeFamiliar());
        harness.setHand(player1, List.of(new AcrobaticManeuver()));
        harness.setLibrary(player1, List.of(new TerrorOfTheFairgrounds(), new TerrorOfTheFairgrounds()));
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Filigree Familiar"));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Filigree Familiar");
        harness.assertLife(player1, 22);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        harness.assertNotInGraveyard(player1, "Filigree Familiar");
    }
}
