package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.r.RetractionHelix;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AerieWorshippers.class, RetractionHelix.class})
class AerieWorshippersTest extends BaseCardTest {

    @Test
    void payingManaCreatesABirdEnchantmentCreatureToken() {
        addTappedWorshippers();

        advanceToWorshippersUpkeep();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);

        Permanent token = findPermanent(player1, "Bird");
        assertThat(token.getCard().isToken()).isTrue();
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.BLUE);
        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.BIRD);
        assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(token.getCard().getAdditionalTypes()).contains(CardType.ENCHANTMENT);
        assertThat(token.getCard().getKeywords()).contains(Keyword.FLYING);
        assertThat(token.getCard().getPower()).isEqualTo(2);
        assertThat(token.getCard().getToughness()).isEqualTo(2);
    }

    @Test
    void decliningInspiredAbilityCreatesNoTokens() {
        addTappedWorshippers();

        advanceToWorshippersUpkeep();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getName().equals("Bird"));
    }

    @Test
    void insufficientGenericManaCreatesNoToken() {
        addTappedWorshippers();
        advanceToWorshippersUpkeep();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.passBothPriorities();

        harness.withAutoStop(TurnStep.UPKEEP,
                () -> harness.handleMayAbilityChosen(player1, true));

        assertThat(countPermanents(player1, "Bird")).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
    }

    @Test
    void genericManaCannotReplaceTheBluePayment() {
        addTappedWorshippers();
        advanceToWorshippersUpkeep();
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.passBothPriorities();

        harness.withAutoStop(TurnStep.UPKEEP,
                () -> harness.handleMayAbilityChosen(player1, true));

        assertThat(countPermanents(player1, "Bird")).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(3);
    }

    @Test
    void alreadyUntappedWorshippersDoesNotTrigger() {
        harness.addToBattlefield(player1, new AerieWorshippers());

        advanceToWorshippersUpkeep();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(countPermanents(player1, "Bird")).isZero();
    }

    @Test
    void triggerStillCreatesTokenAfterWorshippersReturnsToHand() {
        Permanent worshippers = addTappedWorshippers();
        advanceToWorshippersUpkeep();
        harness.setHand(player1, List.of(new RetractionHelix()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstant(player1, 0, worshippers.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, worshippers.getId());
        harness.passBothPriorities();
        harness.assertInHand(player1, "Aerie Worshippers");
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);

        assertThat(countPermanents(player1, "Aerie Worshippers")).isZero();
        assertThat(countPermanents(player1, "Bird")).isEqualTo(1);
        assertThat(countPermanents(player2, "Bird")).isZero();
    }

    private Permanent addTappedWorshippers() {
        Permanent worshippers = harness.addToBattlefieldAndReturn(player1, new AerieWorshippers());
        worshippers.setSummoningSick(false);
        worshippers.tap();
        return worshippers;
    }

    private void advanceToWorshippersUpkeep() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player1, TurnStep.UPKEEP);
    }
}
