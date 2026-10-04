package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.c.CoralhelmGuide;
import com.github.laxika.magicalvibes.cards.d.Dispel;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GoblinWarPaint;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EmeriaShepherd.class, Forest.class, CoralhelmGuide.class, Plains.class, Dispel.class, GoblinWarPaint.class})
class EmeriaShepherdTest extends BaseCardTest {

    @Test
    void nonPlainsLandfallReturnsTargetToHand() {
        harness.addToBattlefield(player1, new EmeriaShepherd());
        Card returned = new CoralhelmGuide();
        harness.setGraveyard(player1, List.of(returned));
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        chooseLandfallTarget(returned);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).contains(returned);
        harness.assertNotOnBattlefield(player1, "Coralhelm Guide");
    }

    @Test
    void PlainsLandfallMayReturnTargetToBattlefield() {
        harness.addToBattlefield(player1, new EmeriaShepherd());
        Card returned = new CoralhelmGuide();
        harness.setGraveyard(player1, List.of(returned));
        harness.setHand(player1, List.of(new Plains()));

        harness.playLand(player1, 0);
        chooseLandfallTarget(returned);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Coralhelm Guide");
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(returned);
    }

    @Test
    void decliningBattlefieldReturnMayReturnTargetToHand() {
        harness.addToBattlefield(player1, new EmeriaShepherd());
        Card returned = new CoralhelmGuide();
        harness.setGraveyard(player1, List.of(returned));
        harness.setHand(player1, List.of(new Plains()));

        harness.playLand(player1, 0);
        chooseLandfallTarget(returned);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        if (gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class) != null) {
            harness.handleMayAbilityChosen(player1, true);
        }

        assertThat(gd.playerHands.get(player1.getId())).contains(returned);
        harness.assertNotOnBattlefield(player1, "Coralhelm Guide");
    }

    @Test
    void nonPlainsLandfallMayLeaveTargetInGraveyard() {
        harness.addToBattlefield(player1, new EmeriaShepherd());
        Card returned = new CoralhelmGuide();
        harness.setGraveyard(player1, List.of(returned));
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        chooseLandfallTarget(returned);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(returned);
        harness.assertNotInHand(player1, "Coralhelm Guide");
        harness.assertNotOnBattlefield(player1, "Coralhelm Guide");
    }

    @Test
    void plainsLandfallMayLeaveTargetInGraveyard() {
        harness.addToBattlefield(player1, new EmeriaShepherd());
        Card returned = new CoralhelmGuide();
        harness.setGraveyard(player1, List.of(returned));
        harness.setHand(player1, List.of(new Plains()));

        harness.playLand(player1, 0);
        chooseLandfallTarget(returned);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        if (gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class) != null) {
            harness.handleMayAbilityChosen(player1, false);
        }

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(returned);
        harness.assertNotInHand(player1, "Coralhelm Guide");
        harness.assertNotOnBattlefield(player1, "Coralhelm Guide");
    }

    @Test
    void targetChoicesExcludeLandsInstantsAndOpponentsGraveyard() {
        harness.addToBattlefield(player1, new EmeriaShepherd());
        Card valid = new GoblinWarPaint();
        harness.setGraveyard(player1, List.of(valid, new Forest(), new Dispel()));
        harness.setGraveyard(player2, List.of(new CoralhelmGuide()));
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.cards()).containsExactly(valid);
        chooseLandfallTarget(valid);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).contains(valid);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(valid);
    }

    @Test
    void plainsLandfallReturnsAuraAttachedToChosenCreature() {
        harness.addToBattlefield(player1, new EmeriaShepherd());
        harness.addToBattlefield(player2, new CoralhelmGuide());
        Card aura = new GoblinWarPaint();
        harness.setGraveyard(player1, List.of(aura));
        harness.setHand(player1, List.of(new Plains()));

        harness.playLand(player1, 0);
        chooseLandfallTarget(aura);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        UUID hostId = harness.getPermanentId(player2, "Coralhelm Guide");
        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validPermanentIds()).contains(hostId);
        harness.handlePermanentChosen(player1, hostId);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anySatisfy(permanent -> {
                    assertThat(permanent.getCard().getId()).isEqualTo(aura.getId());
                    assertThat(permanent.getAttachedTo()).isEqualTo(hostId);
                });
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(aura);
    }

    @Test
    void opponentsLandDoesNotTriggerShepherd() {
        harness.addToBattlefield(player1, new EmeriaShepherd());
        Card returned = new CoralhelmGuide();
        harness.setGraveyard(player1, List.of(returned));
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new Plains()));

        harness.playLand(player2, 0);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(returned);
    }

    @Test
    void plainsLeavingBattlefieldDoesNotRemoveBattlefieldReturnOption() {
        harness.addToBattlefield(player1, new EmeriaShepherd());
        Card returned = new CoralhelmGuide();
        Card plains = new Plains();
        harness.setGraveyard(player1, List.of(returned));
        harness.setHand(player1, List.of(plains));

        harness.playLand(player1, 0);
        chooseLandfallTarget(returned);
        gd.playerBattlefields.get(player1.getId())
                .removeIf(permanent -> permanent.getCard().getId().equals(plains.getId()));
        harness.setHand(player1, List.of(plains));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Coralhelm Guide");
        harness.assertNotInHand(player1, "Coralhelm Guide");
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(returned);
    }

    @Test
    void targetLeavingGraveyardBeforeResolutionIsNotReturned() {
        harness.addToBattlefield(player1, new EmeriaShepherd());
        Card returned = new CoralhelmGuide();
        harness.setGraveyard(player1, List.of(returned));
        harness.setHand(player1, List.of(new Plains()));

        harness.playLand(player1, 0);
        chooseLandfallTarget(returned);
        harness.setGraveyard(player1, List.of());
        harness.setLibrary(player1, List.of(returned));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Coralhelm Guide");
        harness.assertNotInHand(player1, "Coralhelm Guide");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).contains(returned);
    }

    private void chooseLandfallTarget(Card returned) {
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class))
                .isNotNull();
        harness.handleMultipleCardsChosen(player1, List.of(returned.getId()));
    }
}
