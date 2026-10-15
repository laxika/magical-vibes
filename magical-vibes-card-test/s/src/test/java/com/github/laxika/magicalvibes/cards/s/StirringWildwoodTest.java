package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.ApexHawks;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StirringWildwood.class, ApexHawks.class})
class StirringWildwoodTest extends BaseCardTest {

    @Test
    @DisplayName("Stirring Wildwood enters tapped and adds green or white mana")
    void entersTappedAndAddsChosenMana() {
        harness.setHand(player1, List.of(new StirringWildwood()));
        harness.playLand(player1, 0);

        Permanent wildwood = findPermanent(player1, "Stirring Wildwood");
        assertThat(wildwood.isTapped()).isTrue();

        wildwood.untap();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "WHITE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    @DisplayName("Stirring Wildwood becomes a 3/4 green and white Elemental with reach")
    void animatesIntoStirringWildwood() {
        Permanent wildwood = addCreatureReady(player1, new StirringWildwood());
        addAnimationMana(player1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, wildwood)).isTrue();
        assertThat(gqs.isLand(gd, wildwood)).isTrue();
        assertThat(gqs.getEffectivePower(gd, wildwood)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, wildwood)).isEqualTo(4);
        assertThat(gqs.getEffectiveColors(gd, wildwood))
                .containsExactlyInAnyOrder(CardColor.GREEN, CardColor.WHITE);
        assertThat(wildwood.getTransientSubtypes()).containsExactly(CardSubtype.ELEMENTAL);
        assertThat(gqs.hasKeyword(gd, wildwood, Keyword.REACH)).isTrue();
    }

    @Test
    @DisplayName("Stirring Wildwood stops being a creature at end of turn")
    void animationEndsAtEndOfTurn() {
        Permanent wildwood = addCreatureReady(player1, new StirringWildwood());
        addAnimationMana(player1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, wildwood)).isFalse();
        assertThat(gqs.isLand(gd, wildwood)).isTrue();
        assertThat(wildwood.getTransientSubtypes()).doesNotContain(CardSubtype.ELEMENTAL);
        assertThat(gqs.hasKeyword(gd, wildwood, Keyword.REACH)).isFalse();
    }

    private void addAnimationMana(Player player) {
        harness.addMana(player, ManaColor.COLORLESS, 1);
        harness.addMana(player, ManaColor.GREEN, 1);
        harness.addMana(player, ManaColor.WHITE, 1);
    }

    @Test
    void addsGreenManaWithoutUsingTheStack() {
        Permanent wildwood = addCreatureReady(player1, new StirringWildwood());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "GREEN");

        assertThat(wildwood.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canAnimateWhileTappedOnTheTurnItEnters() {
        harness.setHand(player1, List.of(new StirringWildwood()));
        harness.playLand(player1, 0);
        Permanent wildwood = findPermanent(player1, "Stirring Wildwood");
        addAnimationMana(player1);

        harness.activateAbility(player1, 0, 1, null, null);
        assertThat(gqs.isCreature(gd, wildwood)).isFalse();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, wildwood)).isTrue();
        assertThat(wildwood.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, wildwood)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, wildwood)).isEqualTo(4);
    }

    @Test
    void animatedWildwoodRetainsItsManaAbility() {
        Permanent wildwood = addCreatureReady(player1, new StirringWildwood());
        addAnimationMana(player1);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(wildwood.isTapped()).isFalse();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "WHITE");

        assertThat(wildwood.isTapped()).isTrue();
        assertThat(gqs.isCreature(gd, wildwood)).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
    }

    @Test
    void repeatedAnimationDoesNotIncreasePowerOrToughness() {
        Permanent wildwood = addCreatureReady(player1, new StirringWildwood());
        addAnimationMana(player1);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        addAnimationMana(player1);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, wildwood)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, wildwood)).isEqualTo(4);
        assertThat(gqs.isLand(gd, wildwood)).isTrue();
        assertThat(gqs.hasKeyword(gd, wildwood, Keyword.REACH)).isTrue();
    }

    @Test
    void animatedWildwoodCanBlockAFlyingAttacker() {
        addCreatureReady(player2, new StirringWildwood());
        addCreatureReady(player1, new ApexHawks());
        addAnimationMana(player2);
        harness.activateAbility(player2, 0, 1, null, null);
        harness.passBothPriorities();

        declareAttackersAndPrepareBlockers(player1, List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Stirring Wildwood");
        harness.assertInGraveyard(player1, "Apex Hawks");
        harness.assertLife(player2, 20);
    }
}
