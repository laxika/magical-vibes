package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.d.DragonFodder;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VirulentPlague.class, DragonFodder.class})
class VirulentPlagueTest extends BaseCardTest {

    @Test
    @DisplayName("Creature tokens get -2/-2 regardless of controller")
    void debuffsCreatureTokens() {
        harness.addToBattlefield(player1, new VirulentPlague());
        Permanent ownToken = harness.addToBattlefieldAndReturn(player1,
                createTokenCreature("Soldier Token", 3, 3));
        Permanent opponentToken = harness.addToBattlefieldAndReturn(player2,
                createTokenCreature("Zombie Token", 4, 4));

        assertThat(gqs.getEffectivePower(gd, ownToken)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, ownToken)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, opponentToken)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentToken)).isEqualTo(2);
    }

    @Test
    @DisplayName("Nontoken creatures are unaffected")
    void doesNotDebuffNontokenCreatures() {
        harness.addToBattlefield(player1, new VirulentPlague());
        Permanent creature = harness.addToBattlefieldAndReturn(player1,
                createCreature("Grizzly Bears", 2, 2));

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Noncreature tokens are unaffected")
    void doesNotDebuffNoncreatureTokens() {
        harness.addToBattlefield(player1, new VirulentPlague());
        Permanent token = harness.addToBattlefieldAndReturn(player1,
                createTokenArtifact("Treasure Token"));

        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(0);
    }

    @Test
    @DisplayName("A creature token with zero toughness is removed by state-based actions")
    void zeroToughnessCreatureTokenDies() {
        harness.addToBattlefield(player1, new VirulentPlague());
        Permanent token = harness.addToBattlefieldAndReturn(player1,
                createTokenCreature("Soldier Token", 2, 2));
        assertThat(gqs.getEffectiveToughness(gd, token)).isZero();

        harness.runStateBasedActions();

        assertThat(findPermanents(player1, "Soldier Token")).isEmpty();
    }

    @Test
    @DisplayName("Multiple Plagues apply cumulatively to creature tokens")
    void multiplePlaguesStack() {
        harness.addToBattlefield(player1, new VirulentPlague());
        harness.addToBattlefield(player2, new VirulentPlague());
        Permanent token = harness.addToBattlefieldAndReturn(player1,
                createTokenCreature("Dragon Token", 5, 5));

        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(1);
    }

    @Test
    @DisplayName("The modifier ends when Plague leaves the battlefield")
    void modifierEndsWhenSourceLeaves() {
        Permanent plague = harness.addToBattlefieldAndReturn(player1, new VirulentPlague());
        Permanent token = harness.addToBattlefieldAndReturn(player2,
                createTokenCreature("Dragon Token", 4, 4));
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(2);

        gd.playerBattlefields.get(player1.getId()).remove(plague);
        gd.playerGraveyards.get(player1.getId()).add(plague.getCard());

        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(4);
    }

    @Test
    @DisplayName("Tokens created while Plague is present die immediately")
    void newlyCreatedTokensDie() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player1, new VirulentPlague());

        harness.castFromHand(player1, new DragonFodder(), "{1}{R}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Dragon Fodder");
        harness.assertNotOnBattlefield(player1, "Goblin");
    }

    @Test
    @DisplayName("A token copy of Plague animated as a creature debuffs itself")
    void animatedTokenCopyDebuffsItself() {
        VirulentPlague tokenCopy = new VirulentPlague();
        tokenCopy.setToken(true);
        Permanent animatedPlague = harness.addToBattlefieldAndReturn(player1, tokenCopy);
        animatedPlague.setAnimatedUntilEndOfTurn(true);
        animatedPlague.setAnimatedPower(3);
        animatedPlague.setAnimatedToughness(3);

        assertThat(gqs.getEffectivePower(gd, animatedPlague)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, animatedPlague)).isEqualTo(1);
    }

    private Card createCreature(String name, int power, int toughness) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setColor(CardColor.GREEN);
        card.setPower(power);
        card.setToughness(toughness);
        return card;
    }

    private Card createTokenCreature(String name, int power, int toughness) {
        Card card = createCreature(name, power, toughness);
        card.setToken(true);
        return card;
    }

    private Card createTokenArtifact(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.ARTIFACT);
        card.setToken(true);
        return card;
    }
}
