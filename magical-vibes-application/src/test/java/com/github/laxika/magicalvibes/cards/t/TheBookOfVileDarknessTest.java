package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.j.JinnieFayJetmirsSecond;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(TheBookOfVileDarkness.class)
class TheBookOfVileDarknessTest extends BaseCardTest {

    @Test
    void createsZombieAtEndStepAfterControllerLosesTwoLife() {
        harness.addToBattlefield(player1, new TheBookOfVileDarkness());
        gd.lifeLostThisTurn.put(player1.getId(), 2);

        advanceToEndStep(player1);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Zombie")).hasSize(1);
        Permanent zombie = findPermanent(player1, "Zombie");
        assertThat(zombie.getCard().getPower()).isEqualTo(2);
        assertThat(zombie.getCard().getToughness()).isEqualTo(2);
    }

    @Test
    void doesNotCreateZombieWhenControllerLostOnlyOneLife() {
        harness.addToBattlefield(player1, new TheBookOfVileDarkness());
        gd.lifeLostThisTurn.put(player1.getId(), 1);

        advanceToEndStep(player1);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Zombie")).isEmpty();
    }

    @Test
    void createsVecnaAndCopiesTriggeredAbilitiesFromCardsExiledAsCosts() {
        harness.addToBattlefield(player1, new TheBookOfVileDarkness());
        harness.addToBattlefield(player1, artifactWithTriggeredDraw("Eye of Vecna"));
        harness.addToBattlefield(player1, artifact("Hand of Vecna"));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Vecna")).hasSize(1);
        Permanent vecna = findPermanent(player1, "Vecna");
        assertThat(vecna.getCard().getPower()).isEqualTo(8);
        assertThat(vecna.getCard().getToughness()).isEqualTo(8);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
        harness.assertNotOnBattlefield(player1, "The Book of Vile Darkness");
        harness.assertNotOnBattlefield(player1, "Eye of Vecna");
        harness.assertNotOnBattlefield(player1, "Hand of Vecna");
    }

    @Test
    @CardUsed(JinnieFayJetmirsSecond.class)
    void retainsVecnasTriggeredAbilitiesWhenTokenReplacementIsDeclined() {
        activateBookWithTokenReplacement();
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.handleListChoice(player1, "Original tokens");
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Vecna")).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
    }

    @Test
    @CardUsed(JinnieFayJetmirsSecond.class)
    void replacementCatDoesNotGainVecnasTriggeredAbilities() {
        activateBookWithTokenReplacement();
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.handleListChoice(player1, "Cat");

        assertThat(findPermanents(player1, "Cat")).hasSize(1);
        assertThat(findPermanents(player1, "Vecna")).isEmpty();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
    }

    private void activateBookWithTokenReplacement() {
        harness.addToBattlefield(player1, new TheBookOfVileDarkness());
        harness.addToBattlefield(player1, artifactWithTriggeredDraw("Eye of Vecna"));
        harness.addToBattlefield(player1, artifact("Hand of Vecna"));
        harness.addToBattlefield(player1, new JinnieFayJetmirsSecond());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
    }

    private Card artifact(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.ARTIFACT);
        return card;
    }

    private Card artifactWithTriggeredDraw(String name) {
        Card card = artifact(name);
        card.addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new DrawCardEffect(1));
        return card;
    }

    private void advanceToEndStep(com.github.laxika.magicalvibes.model.Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
