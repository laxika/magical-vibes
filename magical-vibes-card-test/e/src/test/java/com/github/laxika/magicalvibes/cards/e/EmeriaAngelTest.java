package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EmeriaAngel.class, Forest.class})
class EmeriaAngelTest extends BaseCardTest {

    @Test
    @DisplayName("Landfall offers to create a 1/1 white Bird token with flying")
    void landfallMayCreateBirdToken() {
        harness.addToBattlefield(player1, new EmeriaAngel());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        Permanent bird = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getSubtypes().contains(CardSubtype.BIRD))
                .findFirst()
                .orElseThrow();
        assertThat(bird.getEffectivePower()).isEqualTo(1);
        assertThat(bird.getEffectiveToughness()).isEqualTo(1);
        assertThat(bird.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(bird.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(bird.getCard().getKeywords()).contains(Keyword.FLYING);
    }

    @Test
    @DisplayName("Declining landfall creates no Bird token")
    void decliningLandfallCreatesNoBirdToken() {
        harness.addToBattlefield(player1, new EmeriaAngel());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getSubtypes().contains(CardSubtype.BIRD)))
                .isEmpty();
    }

    @Test
    @DisplayName("An opponent's landfall does not trigger Emeria Angel")
    void opponentLandDoesNotTrigger() {
        harness.addToBattlefield(player1, new EmeriaAngel());
        harness.setHand(player2, List.of(new Forest()));

        harness.forceActivePlayer(player2);
        harness.playLand(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getSubtypes().contains(CardSubtype.BIRD)))
                .isEmpty();
    }

    @Test
    @DisplayName("A land entering without being played triggers landfall")
    void landEnteringWithoutBeingPlayedTriggers() {
        harness.addToBattlefield(player1, new EmeriaAngel());
        harness.enterBattlefieldAndReturn(player1, new Forest());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getSubtypes().contains(CardSubtype.BIRD))).hasSize(1);
    }

    @Test
    @DisplayName("Two Angels trigger independently with separate optional choices")
    void multipleAngelsOfferIndependentChoices() {
        harness.addToBattlefield(player1, new EmeriaAngel());
        harness.addToBattlefield(player1, new EmeriaAngel());
        harness.setHand(player1, List.of(new Forest()));
        harness.playLand(player1, 0);
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getSubtypes().contains(CardSubtype.BIRD))).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Landfall resolves after Emeria Angel leaves the battlefield")
    void landfallResolvesAfterSourceLeaves() {
        Permanent angel = harness.addToBattlefieldAndReturn(player1, new EmeriaAngel());
        harness.setHand(player1, List.of(new Forest()));
        harness.playLand(player1, 0);
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(angel);
        gd.playerGraveyards.get(player1.getId()).add(angel.getCard());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getSubtypes().contains(CardSubtype.BIRD))).hasSize(1);
    }
}
