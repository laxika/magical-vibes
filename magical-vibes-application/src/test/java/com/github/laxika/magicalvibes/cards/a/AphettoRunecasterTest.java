package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.i.IllusionaryMask;
import com.github.laxika.magicalvibes.cards.s.ScornfulEgotist;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AphettoRunecaster.class, ScornfulEgotist.class, AstralSteel.class, IllusionaryMask.class})
class AphettoRunecasterTest extends BaseCardTest {

    @Test
    void mayDrawWhenAnyPermanentTurnsFaceUp() {
        Card drawn = new ScornfulEgotist();
        harness.setLibrary(player1, List.of(drawn));
        harness.addToBattlefield(player1, new AphettoRunecaster());
        Permanent faceDownPermanent = harness.addToBattlefieldAndReturn(player2, new ScornfulEgotist());
        faceDownPermanent.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.turnFaceUp(player2, gd.playerBattlefields.get(player2.getId()).indexOf(faceDownPermanent));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(drawn);
        assertThat(gd.playerHands.get(player1.getId())).contains(drawn);
    }

    @Test
    void mayDeclineTheDraw() {
        Card drawn = new ScornfulEgotist();
        harness.setLibrary(player1, List.of(drawn));
        harness.addToBattlefield(player1, new AphettoRunecaster());
        Permanent faceDownPermanent = harness.addToBattlefieldAndReturn(player2, new ScornfulEgotist());
        faceDownPermanent.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.turnFaceUp(player2, gd.playerBattlefields.get(player2.getId()).indexOf(faceDownPermanent));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).contains(drawn);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(drawn);
    }

    @Test
    void mayDrawWhenRunecasterItselfTurnsFaceUp() {
        Card drawn = new ScornfulEgotist();
        harness.setLibrary(player1, List.of(drawn));
        Permanent runecaster = harness.addToBattlefieldAndReturn(player1, new AphettoRunecaster());
        runecaster.setFaceDown(2, 2, Set.of(CardType.CREATURE));

        gs.turnPermanentFaceUpWithoutPayingManaCost(gd, runecaster);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(drawn);
        assertThat(gd.playerHands.get(player1.getId())).contains(drawn);
    }

    @Test
    void mayDrawWhenNoncreaturePermanentTurnsFaceUp() {
        Card drawn = new ScornfulEgotist();
        harness.setLibrary(player1, List.of(drawn));
        harness.addToBattlefield(player1, new AphettoRunecaster());
        Permanent faceDownPermanent = harness.addToBattlefieldAndReturn(player2, new AstralSteel());
        faceDownPermanent.setFaceDown(2, 2, Set.of(CardType.CREATURE));

        gs.turnPermanentFaceUpWithoutPayingManaCost(gd, faceDownPermanent);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(drawn);
        assertThat(gd.playerHands.get(player1.getId())).contains(drawn);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void mayDrawWhenIllusionaryMaskCreatureTurnsFaceUpAutomatically(boolean tapped) {
        Card drawn = new ScornfulEgotist();
        harness.setLibrary(player1, List.of(drawn));
        harness.addToBattlefield(player1, new AphettoRunecaster());
        harness.addToBattlefield(player1, new IllusionaryMask());
        harness.setHand(player1, List.of(new ScornfulEgotist()));
        harness.addMana(player1, ManaColor.COLORLESS, 7);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 1, 8, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        Permanent maskedCreature = findPermanent(player1, "Scornful Egotist");
        if (tapped) {
            maskedCreature.tap();
        } else {
            maskedCreature.addMarkedDamage(null, 1);
        }
        assertThat(maskedCreature.isFaceDown()).isFalse();
        harness.passBothPriorities();
        if (!tapped) {
            assertThat(gd.playerGraveyards.get(player1.getId())).contains(maskedCreature.getCard());
        }
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(drawn);
        assertThat(gd.playerHands.get(player1.getId())).contains(drawn);
        assertThat(maskedCreature.isPendingAutomaticTurnFaceUp()).isFalse();
        assertThat(gd.stack).isEmpty();
    }
}
