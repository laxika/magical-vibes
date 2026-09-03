package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(RiptideBiologist.class)
class RiptideBiologistTest extends BaseCardTest {

    @Test
    void canBeCastFaceDownAndTurnedFaceUpForMorphCost() {
        harness.setHand(player1, List.of(new RiptideBiologist()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent biologist = findPermanent(player1, "Riptide Biologist");
        assertThat(biologist.isFaceDown()).isTrue();

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(biologist));
        harness.passBothPriorities();

        assertThat(biologist.isFaceDown()).isFalse();
    }

    @Test
    void takesNoCombatDamageFromBeastCreature() {
        Permanent attacker = new Permanent(createBeast());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        Permanent biologist = new Permanent(new RiptideBiologist());
        biologist.setSummoningSick(false);
        biologist.setBlocking(true);
        biologist.addBlockingTarget(0);
        gd.playerBattlefields.get(player2.getId()).add(biologist);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Riptide Biologist");
    }

    private static Card createBeast() {
        Card card = new Card();
        card.setName("Beast");
        card.setType(CardType.CREATURE);
        card.setManaCost("{3}{G}");
        card.setColor(CardColor.GREEN);
        card.setPower(3);
        card.setToughness(3);
        card.setSubtypes(List.of(CardSubtype.BEAST));
        return card;
    }
}
