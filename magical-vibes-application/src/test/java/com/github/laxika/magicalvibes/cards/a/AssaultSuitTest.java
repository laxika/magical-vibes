package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AssaultSuit.class, GrizzlyBears.class})
class AssaultSuitTest extends BaseCardTest {

    @Test
    void equippedCreatureGetsSuitAbilities() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent suit = addSuitReady(player1);
        suit.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isTrue();
        harness.forceStep(TurnStep.END_STEP);
        assertThat(gqs.cantBeSacrificed(gd, creature)).isTrue();
    }

    @Test
    void equipAttachesSuitToCreature() {
        Permanent suit = addSuitReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(suit.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    void acceptingOpponentUpkeepChoiceStealsAndUntapsEquippedCreatureUntilEndOfTurn() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent suit = addSuitReady(player1);
        suit.setAttachedTo(creature.getId());
        creature.tap();

        advanceToUpkeep(player2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gqs.findPermanentController(gd, creature.getId())).isEqualTo(player2.getId());
        assertThat(creature.isTapped()).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.CLEANUP);

        assertThat(gqs.findPermanentController(gd, creature.getId())).isEqualTo(player1.getId());
    }

    @Test
    void decliningOpponentUpkeepChoiceLeavesEquippedCreatureAlone() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent suit = addSuitReady(player1);
        suit.setAttachedTo(creature.getId());
        creature.tap();

        advanceToUpkeep(player2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gqs.findPermanentController(gd, creature.getId())).isEqualTo(player1.getId());
        assertThat(creature.isTapped()).isTrue();
    }

    private Permanent addSuitReady(Player player) {
        Permanent suit = new Permanent(new AssaultSuit());
        suit.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(suit);
        return suit;
    }
}
