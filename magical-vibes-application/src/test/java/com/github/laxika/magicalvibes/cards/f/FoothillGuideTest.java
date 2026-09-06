package com.github.laxika.magicalvibes.cards.f;

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

@CardUsed(FoothillGuide.class)
class FoothillGuideTest extends BaseCardTest {

    @Test
    void morphsFaceDownAndCanBeTurnedFaceUpForWhite() {
        harness.setHand(player1, List.of(new FoothillGuide()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent guide = findPermanent(player1, "Foothill Guide");
        assertThat(guide.isFaceDown()).isTrue();

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(guide));
        harness.passBothPriorities();

        assertThat(guide.isFaceDown()).isFalse();
    }

    @Test
    void takesNoCombatDamageFromGoblinCreature() {
        Card goblin = new Card();
        goblin.setName("Goblin Piker");
        goblin.setType(CardType.CREATURE);
        goblin.setManaCost("{1}");
        goblin.setColor(CardColor.RED);
        goblin.setPower(2);
        goblin.setToughness(1);
        goblin.setSubtypes(List.of(CardSubtype.GOBLIN));

        Permanent attacker = new Permanent(goblin);
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        Permanent guide = new Permanent(new FoothillGuide());
        guide.setSummoningSick(false);
        guide.setBlocking(true);
        guide.addBlockingTarget(0);
        gd.playerBattlefields.get(player2.getId()).add(guide);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Foothill Guide");
    }
}
