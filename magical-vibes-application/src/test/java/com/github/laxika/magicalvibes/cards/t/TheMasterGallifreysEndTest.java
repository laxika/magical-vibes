package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AdricMathematicalGenius;
import com.github.laxika.magicalvibes.cards.c.CopperMyr;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.TheMasterGallifreysEndEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheMasterGallifreysEnd.class, CopperMyr.class, GrizzlyBears.class, Shock.class, AdricMathematicalGenius.class})
class TheMasterGallifreysEndTest extends BaseCardTest {

    @Test
    void opponentCanChooseLifeLoss() {
        harness.addToBattlefield(player1, new TheMasterGallifreysEnd());
        Permanent myr = harness.addToBattlefieldAndReturn(player1, new CopperMyr());

        killWithShock(player2, myr);
        acceptExile();

        assertVillainousChoice();
        harness.handleListChoice(player2, TheMasterGallifreysEndEffect.LOSE_LIFE_OPTION);

        harness.assertLife(player2, 16);
        harness.assertNotInGraveyard(player1, "Copper Myr");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> "Copper Myr".equals(card.getName()));
        assertThat(findPermanents(player1, "Copper Myr")).isEmpty();
    }

    @Test
    void opponentCanChooseTokenCopy() {
        harness.addToBattlefield(player1, new TheMasterGallifreysEnd());
        Permanent myr = harness.addToBattlefieldAndReturn(player1, new CopperMyr());

        killWithShock(player2, myr);
        acceptExile();
        assertVillainousChoice();
        harness.handleListChoice(player2, TheMasterGallifreysEndEffect.CREATE_TOKEN_OPTION);

        Permanent copy = findPermanent(player1, "Copper Myr");
        assertThat(copy.getCard().isToken()).isTrue();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> "Copper Myr".equals(card.getName()));
    }

    @Test
    void decliningLeavesTheArtifactCreatureInTheGraveyard() {
        harness.addToBattlefield(player1, new TheMasterGallifreysEnd());
        Permanent myr = harness.addToBattlefieldAndReturn(player1, new CopperMyr());

        killWithShock(player2, myr);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Copper Myr");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(card -> "Copper Myr".equals(card.getName()));
    }

    @Test
    void ignoresNonArtifactCreatureDeaths() {
        harness.addToBattlefield(player1, new TheMasterGallifreysEnd());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        killWithShock(player2, bears);

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void faceDownCybermanTriggersAndCopiesTheFaceUpCard() {
        harness.addToBattlefield(player1, new TheMasterGallifreysEnd());
        Permanent cyberman = harness.addToBattlefieldAndReturn(player1, new AdricMathematicalGenius());
        cyberman.setFaceDown(2, 2, Set.of(CardType.ARTIFACT, CardType.CREATURE));

        killWithShock(player2, cyberman);
        acceptExile();
        assertVillainousChoice();
        harness.handleListChoice(player2, TheMasterGallifreysEndEffect.CREATE_TOKEN_OPTION);

        Permanent copy = findPermanent(player1, "Adric, Mathematical Genius");
        assertThat(copy.getCard().isToken()).isTrue();
        assertThat(copy.isFaceDown()).isFalse();
        harness.assertNotInGraveyard(player1, "Adric, Mathematical Genius");
    }

    @Test
    void faceDownMyrWithoutArtifactTypeDoesNotTrigger() {
        harness.addToBattlefield(player1, new TheMasterGallifreysEnd());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new CopperMyr());
        creature.setFaceDown(2, 2, Set.of(CardType.CREATURE));

        killWithShock(player2, creature);

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Copper Myr");
    }

    @Test
    void masterTriggersForItsOwnDeathWhenItIsAnArtifactCreature() {
        Permanent master = harness.addToBattlefieldAndReturn(player1, new TheMasterGallifreysEnd());
        master.getGrantedCardTypes().add(CardType.ARTIFACT);
        master.setMarkedDamage(3);
        harness.runStateBasedActions();
        resolveAllTriggers();

        acceptExile();
        assertVillainousChoice();
        harness.handleListChoice(player2, TheMasterGallifreysEndEffect.LOSE_LIFE_OPTION);

        harness.assertLife(player2, 16);
        harness.assertNotInGraveyard(player1, "The Master, Gallifrey's End");
    }

    @Test
    void opposingArtifactCreatureDeathDoesNotTrigger() {
        harness.addToBattlefield(player1, new TheMasterGallifreysEnd());
        Permanent myr = harness.addToBattlefieldAndReturn(player2, new CopperMyr());

        killWithShock(player1, myr);

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player2, "Copper Myr");
    }

    @Test
    void tokenCopyDeathDoesNotTriggerAgain() {
        harness.addToBattlefield(player1, new TheMasterGallifreysEnd());
        Permanent myr = harness.addToBattlefieldAndReturn(player1, new CopperMyr());
        killWithShock(player2, myr);
        acceptExile();
        harness.handleListChoice(player2, TheMasterGallifreysEndEffect.CREATE_TOKEN_OPTION);

        killWithShock(player2, findPermanent(player1, "Copper Myr"));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(findPermanents(player1, "Copper Myr")).isEmpty();
        harness.assertLife(player2, 20);
    }

    @Test
    void controllerHavingMoreLifeDoesNotPreventTheOpponentBeingChosen() {
        harness.setLife(player1, 40);
        harness.addToBattlefield(player1, new TheMasterGallifreysEnd());
        Permanent myr = harness.addToBattlefieldAndReturn(player1, new CopperMyr());

        killWithShock(player2, myr);
        acceptExile();
        assertVillainousChoice();
        harness.handleListChoice(player2, TheMasterGallifreysEndEffect.LOSE_LIFE_OPTION);

        harness.assertLife(player1, 40);
        harness.assertLife(player2, 16);
    }

    @Test
    void noVillainousChoiceWhenTheDyingCardIsNoLongerInTheGraveyard() {
        harness.addToBattlefield(player1, new TheMasterGallifreysEnd());
        Permanent myr = harness.addToBattlefieldAndReturn(player1, new CopperMyr());
        killWithShock(player2, myr);
        harness.setGraveyard(player1, List.of());

        acceptExile();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertLife(player2, 20);
        assertThat(findPermanents(player1, "Copper Myr")).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(card -> "Copper Myr".equals(card.getName()));
    }

    @Test
    void masterSeesAnotherArtifactCreatureDieSimultaneously() {
        Permanent master = harness.addToBattlefieldAndReturn(player1, new TheMasterGallifreysEnd());
        Permanent myr = harness.addToBattlefieldAndReturn(player1, new CopperMyr());
        master.setMarkedDamage(3);
        myr.setMarkedDamage(1);
        harness.runStateBasedActions();
        resolveAllTriggers();

        acceptExile();
        assertVillainousChoice();
        harness.handleListChoice(player2, TheMasterGallifreysEndEffect.LOSE_LIFE_OPTION);

        harness.assertLife(player2, 16);
        harness.assertInGraveyard(player1, "The Master, Gallifrey's End");
        harness.assertNotInGraveyard(player1, "Copper Myr");
    }

    private void acceptExile() {
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);
    }

    private void assertVillainousChoice() {
        PendingInteraction.ColorChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.options()).containsExactly(
                TheMasterGallifreysEndEffect.LOSE_LIFE_OPTION,
                TheMasterGallifreysEndEffect.CREATE_TOKEN_OPTION);
    }

    private void killWithShock(Player caster, Permanent target) {
        harness.forceActivePlayer(caster);
        harness.setHand(caster, List.of(new Shock()));
        harness.addMana(caster, ManaColor.RED, 1);
        harness.castAndResolveInstant(caster, 0, target.getId());
        resolveAllTriggers();
    }
}
