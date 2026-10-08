package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SunWarriors.class, GrizzlyBears.class})
class SunWarriorsTest extends BaseCardTest {

    @Test
    void firebendingAddsManaEqualToControlledCreatureCountUntilEndOfCombat() {
        addCreatureReady(player1, new SunWarriors());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0));
        harness.passUntil(TurnStep.END_OF_COMBAT);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(2);

        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }

    @Test
    void activatedAbilityCreatesAnAllyToken() {
        Permanent warriors = addCreatureReady(player1, new SunWarriors());
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        List<Permanent> tokens = findPermanents(player1, "Ally");
        assertThat(tokens).hasSize(1);
        assertThat(tokens.getFirst().getCard().isToken()).isTrue();
        assertThat(tokens.getFirst().getCard().getSubtypes()).contains(CardSubtype.ALLY);
        assertThat(warriors.isTapped()).isFalse();
    }

    @Test
    @CardUsed(SunWarriors.class)
    void firebendingDoesNotCountOpponentsCreatures() {
        addCreatureReady(player1, new SunWarriors());
        Permanent opposingWarriors = addCreatureReady(player2, new SunWarriors());
        opposingWarriors.tap();

        declareAttackers(List.of(0));
        harness.passUntil(TurnStep.END_OF_COMBAT);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.RED)).isZero();
    }

    @Test
    @CardUsed(SunWarriors.class)
    void firebendingCountsTokenCreatedInResponseToAttackTrigger() {
        addCreatureReady(player1, new SunWarriors());
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            assertThat(gd.stack).hasSize(1);
            harness.activateAbility(player1, 0, 0, null, null);
            resolveAllTriggers();

            assertThat(findPermanents(player1, "Ally")).hasSize(1);
            assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(2);
        });
    }

    @Test
    @CardUsed(SunWarriors.class)
    void firebendingManaCanPayForTokenAbilityWhileWarriorsIsTapped() {
        for (int i = 0; i < 5; i++) {
            addCreatureReady(player1, new SunWarriors());
        }

        declareAttackers(List.of(0));
        harness.passUntil(TurnStep.END_OF_COMBAT);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(5);

        harness.withAutoStop(TurnStep.END_OF_COMBAT, () -> {
            harness.activateAbility(player1, 0, 0, null, null);
            resolveAllTriggers();
        });

        assertThat(findPermanents(player1, "Ally")).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    @Test
    @CardUsed(SunWarriors.class)
    void summoningSickWarriorsCanCreateMultipleWhiteOneOneAllyTokens() {
        Permanent warriors = harness.addToBattlefieldAndReturn(player1, new SunWarriors());
        harness.addMana(player1, ManaColor.COLORLESS, 10);

        harness.activateAbility(player1, 0, 0, null, null);
        resolveAllTriggers();
        harness.activateAbility(player1, 0, 0, null, null);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Ally")).hasSize(2).allSatisfy(token -> {
            assertThat(token.getCard().isToken()).isTrue();
            assertThat(token.getCard().getColor()).isEqualTo(CardColor.WHITE);
            assertThat(token.getCard().getPower()).isEqualTo(1);
            assertThat(token.getCard().getToughness()).isEqualTo(1);
            assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.ALLY);
            assertThat(gqs.isCreature(gd, token)).isTrue();
            assertThat(token.isTapped()).isFalse();
        });
        assertThat(warriors.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }
}
