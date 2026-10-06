package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.c.CephalidAristocrat;
import com.github.laxika.magicalvibes.cards.p.PropheticPrism;
import com.github.laxika.magicalvibes.cards.s.SkywingAven;
import com.github.laxika.magicalvibes.cards.s.StrengthOfIsolation;
import com.github.laxika.magicalvibes.cards.t.TerohsFaithful;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RetracedImage.class, CephalidAristocrat.class, PropheticPrism.class,
        SkywingAven.class, StrengthOfIsolation.class, TerohsFaithful.class})
class RetracedImageTest extends BaseCardTest {

    @Test
    @DisplayName("Revealed card sharing a permanent name enters the battlefield")
    void matchingCardEntersBattlefield() {
        harness.addToBattlefield(player2, new SkywingAven());
        harness.setHand(player1, List.of(new RetracedImage(), new CephalidAristocrat(), new SkywingAven()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(harness.getGameData().interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.RetracedImageCardChoice.class);
        harness.handleCardChosen(player1, 1);

        harness.assertOnBattlefield(player1, "Skywing Aven");
        harness.assertInHand(player1, "Cephalid Aristocrat");
        assertThat(harness.getGameData().playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Revealed card without a matching permanent remains in hand")
    void nonmatchingCardRemainsInHand() {
        harness.addToBattlefield(player2, new SkywingAven());
        harness.setHand(player1, List.of(new RetracedImage(), new CephalidAristocrat()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Cephalid Aristocrat");
        harness.assertNotOnBattlefield(player1, "Cephalid Aristocrat");
        assertThat(harness.getGameData().interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A creature put onto the battlefield through Retraced Image triggers its enter ability")
    void enteringCreatureTriggersEnterAbility() {
        harness.addToBattlefield(player2, new TerohsFaithful());
        harness.setHand(player1, List.of(new RetracedImage(), new TerohsFaithful()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Teroh's Faithful");
        harness.assertLife(player1, 24);
    }

    @Test
    @DisplayName("A noncreature permanent put onto the battlefield through Retraced Image triggers its enter ability")
    void enteringNoncreaturePermanentTriggersEnterAbility() {
        harness.addToBattlefield(player2, new PropheticPrism());
        harness.setHand(player1, List.of(new RetracedImage(), new PropheticPrism()));
        harness.setLibrary(player1, List.of(new CephalidAristocrat()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Prophetic Prism");
        harness.assertInHand(player1, "Cephalid Aristocrat");
    }

    @Test
    @DisplayName("Resolving with no other card in hand does not prompt")
    void emptyHandDoesNotPrompt() {
        harness.castFromHand(player1, new RetracedImage(), "{U}");
        harness.passBothPriorities();

        assertThat(harness.getGameData().interaction.activeInteraction()).isNull();
        assertThat(harness.getGameData().playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Retraced Image");
    }

    @Test
    @DisplayName("A matching permanent controlled by the caster also permits entry")
    void matchingControlledPermanentPermitsEntry() {
        harness.addToBattlefield(player1, new SkywingAven());
        harness.setHand(player1, List.of(new RetracedImage(), new SkywingAven()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        harness.assertNotInHand(player1, "Skywing Aven");
    }

    @Test
    @DisplayName("A matching card in a graveyard is not a permanent")
    void graveyardCardDoesNotPermitEntry() {
        harness.setGraveyard(player2, List.of(new SkywingAven()));
        harness.setHand(player1, List.of(new RetracedImage(), new SkywingAven()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Skywing Aven");
        harness.assertNotOnBattlefield(player1, "Skywing Aven");
        harness.assertInGraveyard(player1, "Retraced Image");
    }

    @Test
    @DisplayName("A face-down permanent has no name to match the revealed card")
    void faceDownPermanentDoesNotPermitEntry() {
        Permanent manifested = harness.addToBattlefieldAndReturn(player2, new SkywingAven());
        manifested.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        harness.setHand(player1, List.of(new RetracedImage(), new SkywingAven()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Skywing Aven");
        harness.assertNotOnBattlefield(player1, "Skywing Aven");
    }

    @Test
    @DisplayName("A matching Aura enters attached to a legally chosen creature")
    void matchingAuraEntersAttached() {
        Permanent originalHost = harness.addToBattlefieldAndReturn(player2, new SkywingAven());
        Permanent existingAura = harness.addToBattlefieldAndReturn(player2, new StrengthOfIsolation());
        existingAura.setAttachedTo(originalHost.getId());
        Permanent chosenHost = harness.addToBattlefieldAndReturn(player1, new CephalidAristocrat());
        harness.setHand(player1, List.of(new RetracedImage(), new StrengthOfIsolation()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNotNull();
        harness.handlePermanentChosen(player1, chosenHost.getId());

        harness.assertOnBattlefield(player1, "Strength of Isolation");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anySatisfy(permanent -> {
                    assertThat(permanent.getCard()).isInstanceOf(StrengthOfIsolation.class);
                    assertThat(permanent.getAttachedTo()).isEqualTo(chosenHost.getId());
                });
        harness.assertNotInHand(player1, "Strength of Isolation");
        harness.assertNotInGraveyard(player1, "Strength of Isolation");
    }
}
