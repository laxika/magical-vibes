package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.q.QuilledWolf;
import com.github.laxika.magicalvibes.cards.t.TrueFaithCenser;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StrengthOfArms.class, QuilledWolf.class, TrueFaithCenser.class})
class StrengthOfArmsTest extends BaseCardTest {

    @Test
    @DisplayName("Gives +2/+2 without creating a token when no Equipment controlled")
    void boostsWithoutEquipmentNoToken() {
        harness.addToBattlefield(player1, new QuilledWolf());
        harness.setHand(player1, List.of(new StrengthOfArms()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        UUID bearId = harness.getPermanentId(player1, "Quilled Wolf");
        harness.castAndResolveInstant(player1, 0, bearId);

        Permanent bear = findPermanent(player1, "Quilled Wolf");
        assertThat(bear.getEffectivePower()).isEqualTo(4);
        assertThat(bear.getEffectiveToughness()).isEqualTo(4);
        harness.assertNotOnBattlefield(player1, "Human Soldier");
    }

    @Test
    @DisplayName("Creates a 1/1 Human Soldier token when controlling Equipment")
    void createsTokenWhenControllingEquipment() {
        harness.addToBattlefield(player1, new QuilledWolf());
        harness.addToBattlefield(player1, new TrueFaithCenser());
        harness.setHand(player1, List.of(new StrengthOfArms()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        UUID bearId = harness.getPermanentId(player1, "Quilled Wolf");
        harness.castAndResolveInstant(player1, 0, bearId);

        Permanent bear = findPermanent(player1, "Quilled Wolf");
        assertThat(bear.getEffectivePower()).isEqualTo(4);
        assertThat(bear.getEffectiveToughness()).isEqualTo(4);

        Permanent token = findPermanent(player1, "Human Soldier");
        assertThat(token.getEffectivePower()).isEqualTo(1);
        assertThat(token.getEffectiveToughness()).isEqualTo(1);
        assertThat(token.getCard().getSubtypes()).containsExactlyInAnyOrder(CardSubtype.HUMAN, CardSubtype.SOLDIER);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(token.isTapped()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Opponent Equipment does not create a token")
    void opponentEquipmentDoesNotCreateToken() {
        harness.addToBattlefield(player1, new QuilledWolf());
        harness.addToBattlefield(player2, new TrueFaithCenser());
        harness.setHand(player1, List.of(new StrengthOfArms()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        UUID bearId = harness.getPermanentId(player1, "Quilled Wolf");
        harness.castAndResolveInstant(player1, 0, bearId);

        harness.assertNotOnBattlefield(player1, "Human Soldier");
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOff() {
        harness.addToBattlefield(player1, new QuilledWolf());
        harness.setHand(player1, List.of(new StrengthOfArms()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        UUID bearId = harness.getPermanentId(player1, "Quilled Wolf");
        harness.castAndResolveInstant(player1, 0, bearId);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent bear = findPermanent(player1, "Quilled Wolf");
        assertThat(bear.getEffectivePower()).isEqualTo(2);
        assertThat(bear.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player1, new QuilledWolf());
        harness.addToBattlefield(player1, new TrueFaithCenser());
        harness.setHand(player1, List.of(new StrengthOfArms()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        UUID targetId = harness.getPermanentId(player1, "True-Faith Censer");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Equipment acquired after casting counts at resolution")
    void equipmentAcquiredBeforeResolutionCreatesToken() {
        harness.addToBattlefield(player1, new QuilledWolf());
        harness.setHand(player1, List.of(new StrengthOfArms()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Quilled Wolf"));
        harness.addToBattlefield(player1, new TrueFaithCenser());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Human Soldier");
        assertThat(findPermanent(player1, "Quilled Wolf").getEffectivePower()).isEqualTo(4);
    }

    @Test
    @DisplayName("Equipment lost before resolution does not create a token")
    void equipmentLostBeforeResolutionDoesNotCreateToken() {
        harness.addToBattlefield(player1, new QuilledWolf());
        harness.addToBattlefield(player1, new TrueFaithCenser());
        harness.setHand(player1, List.of(new StrengthOfArms()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Quilled Wolf"));
        gd.playerBattlefields.get(player1.getId()).removeIf(p -> p.getCard() instanceof TrueFaithCenser);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Human Soldier");
        assertThat(findPermanent(player1, "Quilled Wolf").getEffectivePower()).isEqualTo(4);
    }

    @Test
    @DisplayName("Illegal sole target prevents token creation even with Equipment")
    void removedTargetPreventsTokenCreation() {
        harness.addToBattlefield(player1, new QuilledWolf());
        harness.addToBattlefield(player1, new TrueFaithCenser());
        harness.setHand(player1, List.of(new StrengthOfArms()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Quilled Wolf"));
        gd.playerBattlefields.get(player1.getId()).removeIf(p -> p.getCard() instanceof QuilledWolf);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Human Soldier");
        harness.assertInGraveyard(player1, "Strength of Arms");
    }

    @Test
    @DisplayName("Can boost an opponent's creature while creating only one token for the caster")
    void opponentCreatureIsLegalTarget() {
        harness.addToBattlefield(player2, new QuilledWolf());
        harness.addToBattlefield(player1, new TrueFaithCenser());
        harness.addToBattlefield(player1, new TrueFaithCenser());
        harness.setHand(player1, List.of(new StrengthOfArms()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Quilled Wolf"));

        assertThat(findPermanent(player2, "Quilled Wolf").getEffectivePower()).isEqualTo(4);
        assertThat(findPermanent(player2, "Quilled Wolf").getEffectiveToughness()).isEqualTo(4);
        harness.assertOnBattlefield(player1, "Human Soldier");
        harness.assertNotOnBattlefield(player2, "Human Soldier");
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(3);
    }
}
