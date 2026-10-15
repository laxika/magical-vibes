package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.k.KjeldoranKnight;
import com.github.laxika.magicalvibes.cards.b.BirdMaiden;
import com.github.laxika.magicalvibes.cards.c.Camel;
import com.github.laxika.magicalvibes.cards.p.PaintersServant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Jihad.class, KjeldoranKnight.class, BirdMaiden.class, Camel.class, PaintersServant.class})
class JihadTest extends BaseCardTest {

    private static Card createPermanent(String name, CardType type, CardColor color) {
        Card card = new Card();
        card.setName(name);
        card.setType(type);
        card.setManaCost("{1}");
        card.setColor(color);
        return card;
    }

    private static Card createCreature(String name, int power, int toughness, CardColor color) {
        Card card = createPermanent(name, CardType.CREATURE, color);
        card.setPower(power);
        card.setToughness(toughness);
        return card;
    }

    private Permanent addJihad(CardColor chosenColor) {
        Permanent jihad = harness.addToBattlefieldAndReturn(player1, new Jihad());
        jihad.setChosenColor(chosenColor);
        jihad.setRememberedTargetPlayerId(player2.getId());
        return jihad;
    }

    @Test
    @DisplayName("White creatures get +2/+1 while the chosen player controls a matching nontoken permanent")
    void buffsWhiteCreaturesWhileConditionMet() {
        harness.addToBattlefield(player2, createPermanent("Red Relic", CardType.ARTIFACT, CardColor.RED));
        harness.addToBattlefield(player1, new KjeldoranKnight());
        harness.addToBattlefield(player2, new KjeldoranKnight());
        addJihad(CardColor.RED);

        Permanent ownKnight = findPermanent(player1, "Kjeldoran Knight");
        Permanent opposingKnight = findPermanent(player2, "Kjeldoran Knight");
        assertThat(gqs.getEffectivePower(gd, ownKnight)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ownKnight)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opposingKnight)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, opposingKnight)).isEqualTo(2);
    }

    @Test
    @DisplayName("Nonwhite creatures are unaffected")
    void doesNotBuffNonwhiteCreatures() {
        harness.addToBattlefield(player2, createPermanent("Red Relic", CardType.ARTIFACT, CardColor.RED));
        Permanent redCreature = harness.addToBattlefieldAndReturn(
                player1, createCreature("Red Bear", 2, 2, CardColor.RED));
        addJihad(CardColor.RED);

        assertThat(gqs.getEffectivePower(gd, redCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, redCreature)).isEqualTo(2);
    }

    @Test
    @DisplayName("A matching token does not keep Jihad active")
    void matchingTokenDoesNotCount() {
        Card token = createCreature("Red Token", 1, 1, CardColor.RED);
        token.setToken(true);
        harness.addToBattlefield(player2, token);
        Permanent knight = harness.addToBattlefieldAndReturn(player1, new KjeldoranKnight());
        addJihad(CardColor.RED);

        assertThat(gqs.getEffectivePower(gd, knight)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, knight)).isEqualTo(1);
    }

    @Test
    @DisplayName("Jihad sacrifices when the chosen player controls no matching nontoken permanent")
    void sacrificesWhenConditionFails() {
        harness.addToBattlefield(player2, createPermanent("Blue Relic", CardType.ARTIFACT, CardColor.BLUE));
        addJihad(CardColor.RED);

        harness.runStateBasedActions();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Jihad");
        harness.assertInGraveyard(player1, "Jihad");
    }

    @Test
    @DisplayName("Jihad sacrifices after its matching permanent leaves")
    void sacrificesAfterMatchingPermanentLeaves() {
        Permanent redPermanent = harness.addToBattlefieldAndReturn(
                player2, createPermanent("Red Relic", CardType.ARTIFACT, CardColor.RED));
        addJihad(CardColor.RED);

        gd.playerBattlefields.get(player2.getId()).remove(redPermanent);
        harness.runStateBasedActions();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Jihad");
        harness.assertInGraveyard(player1, "Jihad");
    }

    @Test
    @DisplayName("Casting Jihad resolves its opponent and color choices")
    void castsAndChoosesColor() {
        harness.addToBattlefield(player2, createPermanent("Red Relic", CardType.ARTIFACT, CardColor.RED));
        Permanent knight = harness.addToBattlefieldAndReturn(player1, new KjeldoranKnight());
        harness.castFromHand(player1, new Jihad(), "{W}{W}{W}");
        harness.passBothPriorities();
        harness.handleListChoice(player1, "RED");

        harness.assertOnBattlefield(player1, "Jihad");
        assertThat(gqs.getEffectivePower(gd, knight)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, knight)).isEqualTo(2);
    }

    @Test
    void continuesToCheckChosenPlayerAfterControlChanges() {
        harness.addToBattlefield(player2, new BirdMaiden());
        Permanent camel = harness.addToBattlefieldAndReturn(player1, new Camel());
        Permanent jihad = addJihad(CardColor.RED);

        gd.playerBattlefields.get(player1.getId()).remove(jihad);
        gd.playerBattlefields.get(player2.getId()).add(jihad);

        assertThat(gqs.getEffectivePower(gd, camel)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, camel)).isEqualTo(2);
        harness.runStateBasedActions();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void staticColorGrantsPreventSacrifice() {
        Permanent servant = harness.addToBattlefieldAndReturn(player2, new PaintersServant());
        servant.setChosenColor(CardColor.BLUE);
        addJihad(CardColor.BLUE);

        harness.runStateBasedActions();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Jihad");
    }

    @Test
    void sacrificeTriggerStillResolvesAfterConditionBecomesTrueAgain() {
        addJihad(CardColor.RED);
        harness.runStateBasedActions();
        assertThat(gd.stack).hasSize(1);
        harness.addToBattlefield(player2, new BirdMaiden());
        Permanent camel = harness.addToBattlefieldAndReturn(player1, new Camel());

        assertThat(gqs.getEffectivePower(gd, camel)).isEqualTo(2);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Jihad");
        assertThat(gqs.getEffectivePower(gd, camel)).isZero();
    }

    @Test
    void matchingPermanentControlledOnlyByControllerDoesNotCount() {
        harness.addToBattlefield(player1, new BirdMaiden());
        addJihad(CardColor.RED);

        harness.runStateBasedActions();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Jihad");
    }
}
