package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.r.RhysticCave;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SquirrelWrangler.class, RhysticCave.class, SpittingSpider.class})
class SquirrelWranglerTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing a land creates two 1/1 green Squirrels")
    void sacrificeLandCreatesTwoSquirrels() {
        addCreatureReady(player1, new SquirrelWrangler());
        harness.addToBattlefield(player1, new RhysticCave());
        addManaForAbility();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Rhystic Cave");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().isToken())
                .filteredOn(p -> p.getCard().getSubtypes().contains(CardSubtype.SQUIRREL))
                .hasSize(2)
                .allSatisfy(p -> {
                    assertThat(p.getCard().getColor()).isEqualTo(CardColor.GREEN);
                    assertThat(p.getCard().getPower()).isEqualTo(1);
                    assertThat(p.getCard().getToughness()).isEqualTo(1);
                });
    }

    @Test
    @DisplayName("The second ability boosts every Squirrel until end of turn")
    void boostsAllSquirrelsUntilEndOfTurn() {
        addCreatureReady(player1, new SquirrelWrangler());
        harness.addToBattlefield(player1, new RhysticCave());
        Permanent ownSquirrel = harness.addToBattlefieldAndReturn(player1, squirrelToken());
        Permanent opponentSquirrel = harness.addToBattlefieldAndReturn(player2, squirrelToken());
        Permanent nonSquirrel = harness.addToBattlefieldAndReturn(player2, new SpittingSpider());
        addManaForAbility();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ownSquirrel)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ownSquirrel)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opponentSquirrel)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentSquirrel)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, nonSquirrel)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, nonSquirrel)).isEqualTo(5);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ownSquirrel)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, ownSquirrel)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, opponentSquirrel)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, opponentSquirrel)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, nonSquirrel)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, nonSquirrel)).isEqualTo(5);
    }

    @Test
    @DisplayName("Squirrels created after the boost resolves do not receive that boost")
    void squirrelsCreatedAfterBoostAreNotBoosted() {
        addCreatureReady(player1, new SquirrelWrangler());
        harness.addToBattlefield(player1, new RhysticCave());
        Permanent existingSquirrel = harness.addToBattlefieldAndReturn(player1, squirrelToken());
        addManaForAbility();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, existingSquirrel)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, existingSquirrel)).isEqualTo(2);

        harness.addToBattlefield(player1, new RhysticCave());
        addManaForAbility();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().isToken())
                .filteredOn(p -> p.getCard().getSubtypes().contains(CardSubtype.SQUIRREL))
                .hasSize(3)
                .filteredOn(p -> p != existingSquirrel)
                .allSatisfy(squirrel -> {
                    assertThat(gqs.getEffectivePower(gd, squirrel)).isEqualTo(1);
                    assertThat(gqs.getEffectiveToughness(gd, squirrel)).isEqualTo(1);
                });
    }

    @Test
    @DisplayName("Neither ability can be activated without a land to sacrifice")
    void requiresLandToSacrifice() {
        addCreatureReady(player1, new SquirrelWrangler());
        addManaForAbility();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Both abilities require green mana and leave the land intact when payment fails")
    void cannotPayWithOnlyColorlessMana() {
        addCreatureReady(player1, new SquirrelWrangler());
        harness.addToBattlefield(player1, new RhysticCave());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Rhystic Cave");
        harness.assertNotInGraveyard(player1, "Rhystic Cave");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An opponent's land cannot pay either sacrifice cost")
    void cannotSacrificeOpponentsLand() {
        addCreatureReady(player1, new SquirrelWrangler());
        harness.addToBattlefield(player2, new RhysticCave());
        addManaForAbility();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player2, "Rhystic Cave");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A tapped Wrangler with summoning sickness can activate both abilities")
    void tappedSummoningSickWranglerCanActivateBothAbilities() {
        Permanent wrangler = harness.addToBattlefieldAndReturn(player1, new SquirrelWrangler());
        wrangler.setSummoningSick(true);
        wrangler.tap();
        harness.addToBattlefield(player1, new RhysticCave());
        addManaForAbility();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.assertInGraveyard(player1, "Rhystic Cave");
        assertThat(countPermanents(player1, "Squirrel")).isZero();
        harness.passBothPriorities();

        harness.addToBattlefield(player1, new RhysticCave());
        addManaForAbility();
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Squirrel")).hasSize(2)
                .allSatisfy(squirrel -> {
                    assertThat(gqs.getEffectivePower(gd, squirrel)).isEqualTo(2);
                    assertThat(gqs.getEffectiveToughness(gd, squirrel)).isEqualTo(2);
                });
    }

    private void addManaForAbility() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    private Card squirrelToken() {
        Card card = new Card();
        card.setName("Squirrel");
        card.setType(CardType.CREATURE);
        card.setManaCost("");
        card.setColor(CardColor.GREEN);
        card.setPower(1);
        card.setToughness(1);
        card.setSubtypes(List.of(CardSubtype.SQUIRREL));
        card.setToken(true);
        return card;
    }
}
