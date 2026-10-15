package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PheresBandRaiders.class})
class PheresBandRaidersTest extends BaseCardTest {

    @Test
    void payingManaCreatesACentaurEnchantmentCreatureToken() {
        addTappedRaiders();

        advanceToUntapStep();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        Permanent token = findPermanent(player1, "Centaur");
        assertThat(token.getCard().isToken()).isTrue();
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.GREEN);
        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.CENTAUR);
        assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(token.getCard().getAdditionalTypes()).contains(CardType.ENCHANTMENT);
        assertThat(token.getCard().getPower()).isEqualTo(3);
        assertThat(token.getCard().getToughness()).isEqualTo(3);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken()).hasSize(1);
        assertThat(token.isTapped()).isFalse();
    }

    @Test
    void decliningInspiredAbilityCreatesNoTokens() {
        addTappedRaiders();

        advanceToUntapStep();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getName().equals("Centaur"));
    }

    @Test
    void alreadyUntappedRaidersDoNotTriggerDuringUntapStep() {
        harness.addToBattlefield(player1, new PheresBandRaiders());

        advanceToUntapStep();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertNotOnBattlefield(player1, "Centaur");
    }

    @Test
    void opponentsRaidersDoNotUntapOrTriggerDuringYourUntapStep() {
        Permanent raiders = harness.addToBattlefieldAndReturn(player2, new PheresBandRaiders());
        raiders.tap();

        advanceToUntapStep();

        assertThat(raiders.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertNotOnBattlefield(player2, "Centaur");
    }

    private void addTappedRaiders() {
        Permanent raiders = harness.addToBattlefieldAndReturn(player1, new PheresBandRaiders());
        raiders.setSummoningSick(false);
        raiders.tap();
    }

    private void advanceToUntapStep() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player1, TurnStep.UPKEEP);
    }
}
