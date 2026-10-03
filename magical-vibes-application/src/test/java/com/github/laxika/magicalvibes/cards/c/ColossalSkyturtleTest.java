package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ColossalSkyturtle.class, Forest.class, GrizzlyBears.class})
class ColossalSkyturtleTest extends BaseCardTest {

    @Test
    @DisplayName("Channel returns a target card from your graveyard to your hand")
    void channelReturnsTargetCardFromGraveyard() {
        Card target = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(target));
        harness.setHand(player1, List.of(new ColossalSkyturtle()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbilityWithGraveyardTargets(player1, 0, List.of(target.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(target);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(target);
        harness.assertInGraveyard(player1, "Colossal Skyturtle");
    }

    @Test
    @DisplayName("Channel returns a target creature to its owner's hand")
    void channelReturnsTargetCreatureToItsOwnersHand() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ColossalSkyturtle()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.ensurePriority(player1);

        gs.activateHandAbility(gd, player1, 0, 1, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).contains(target.getCard());
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Colossal Skyturtle");
    }

    @Test
    @DisplayName("Channel cannot target a land for the creature bounce")
    void channelCannotTargetLandForCreatureBounce() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new ColossalSkyturtle()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.ensurePriority(player1);

        assertThatThrownBy(() -> gs.activateHandAbility(gd, player1, 0, 1, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void channelReturnsLandFromGraveyard() {
        Card target = new Forest();
        harness.setGraveyard(player1, List.of(target));
        harness.setHand(player1, List.of(new ColossalSkyturtle()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.activateHandAbilityWithGraveyardTargets(player1, 0, List.of(target.getId()));
        harness.assertInGraveyard(player1, "Colossal Skyturtle");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(target);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(target);
    }

    @Test
    void channelCannotReturnCardFromOpponentsGraveyard() {
        Card target = new Forest();
        Card source = new ColossalSkyturtle();
        harness.setGraveyard(player2, List.of(target));
        harness.setHand(player1, List.of(source));
        harness.addMana(player1, ManaColor.GREEN, 3);

        assertThatThrownBy(() -> harness.activateHandAbilityWithGraveyardTargets(
                player1, 0, List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(source);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(target);
    }

    @Test
    void channelCannotTargetTheCardDiscardedToPayItsOwnCost() {
        Card source = new ColossalSkyturtle();
        harness.setHand(player1, List.of(source));
        harness.addMana(player1, ManaColor.GREEN, 3);

        assertThatThrownBy(() -> harness.activateHandAbilityWithGraveyardTargets(
                player1, 0, List.of(source.getId())))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(source);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(source);
    }

    @Test
    void channelDoesNotReturnATargetThatLeftTheGraveyard() {
        Card target = new Forest();
        harness.setGraveyard(player1, List.of(target));
        harness.setHand(player1, List.of(new ColossalSkyturtle()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.activateHandAbilityWithGraveyardTargets(player1, 0, List.of(target.getId()));
        gd.playerGraveyards.get(player1.getId()).remove(target);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Colossal Skyturtle");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void channelCannotChooseMoreThanOneGraveyardTarget() {
        Card firstTarget = new Forest();
        Card secondTarget = new ColossalSkyturtle();
        Card source = new ColossalSkyturtle();
        harness.setGraveyard(player1, List.of(firstTarget, secondTarget));
        harness.setHand(player1, List.of(source));
        harness.addMana(player1, ManaColor.GREEN, 3);

        assertThatThrownBy(() -> harness.activateHandAbilityWithGraveyardTargets(
                player1, 0, List.of(firstTarget.getId(), secondTarget.getId())))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(source);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(firstTarget, secondTarget);
        assertThat(gd.stack).isEmpty();
    }
}
