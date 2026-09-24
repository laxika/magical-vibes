package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DreyKeeper.class, GrizzlyBears.class})
class DreyKeeperTest extends BaseCardTest {

    @Test
    void entersAndCreatesTwoSquirrels() {
        harness.setHand(player1, List.of(new DreyKeeper()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Squirrel")).hasSize(2);
    }

    @Test
    void abilityBoostsOnlyYourSquirrels() {
        addCreatureReady(player1, new DreyKeeper());
        Permanent ownSquirrel = addSquirrel(player1);
        Permanent ownBear = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentSquirrel = addSquirrel(player2);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ownSquirrel)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ownSquirrel)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, ownSquirrel, Keyword.MENACE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, ownBear)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, ownBear, Keyword.MENACE)).isFalse();
        assertThat(gqs.getEffectivePower(gd, opponentSquirrel)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, opponentSquirrel, Keyword.MENACE)).isFalse();
    }

    @Test
    void abilityExpiresAtEndOfTurn() {
        addCreatureReady(player1, new DreyKeeper());
        Permanent squirrel = addSquirrel(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, squirrel)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, squirrel, Keyword.MENACE)).isFalse();
    }

    private Permanent addSquirrel(Player player) {
        Card squirrel = new Card();
        squirrel.setName("Squirrel");
        squirrel.setType(CardType.CREATURE);
        squirrel.setPower(1);
        squirrel.setToughness(1);
        squirrel.setToken(true);
        squirrel.setSubtypes(List.of(CardSubtype.SQUIRREL));

        Permanent permanent = new Permanent(squirrel);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
