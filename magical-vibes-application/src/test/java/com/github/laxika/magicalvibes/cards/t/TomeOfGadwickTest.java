package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.b.Brainstorm;
import com.github.laxika.magicalvibes.cards.c.Consider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.ObsessiveSearch;
import com.github.laxika.magicalvibes.cards.o.Opt;
import com.github.laxika.magicalvibes.cards.p.Peek;
import com.github.laxika.magicalvibes.cards.p.Ponder;
import com.github.laxika.magicalvibes.cards.p.Preordain;
import com.github.laxika.magicalvibes.cards.q.Quicken;
import com.github.laxika.magicalvibes.cards.s.SerumVisions;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.SleightOfHand;
import com.github.laxika.magicalvibes.cards.t.ThoughtScour;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({
        TomeOfGadwick.class,
        GrizzlyBears.class,
        Shock.class,
        Brainstorm.class,
        Ponder.class,
        ObsessiveSearch.class,
        Consider.class,
        SleightOfHand.class,
        Preordain.class,
        Opt.class,
        Peek.class,
        ThoughtScour.class,
        SerumVisions.class,
        Quicken.class
})
class TomeOfGadwickTest extends BaseCardTest {

    @Test
    void equippedCreatureGetsPowerAndWard() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent tome = addTomeReady(player1);
        tome.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);

        prepareOpponentTurn();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, creature.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        assertThat(creature.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player2, "Shock");
    }

    @Test
    void attackingWithEquippedCreatureConjuresSpellbookCard() {
        harness.setHand(player1, List.of());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent tome = addTomeReady(player1);
        tome.setAttachedTo(creature.getId());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .singleElement()
                .extracting(Card::getName)
                .isIn("Brainstorm", "Ponder", "Obsessive Search", "Consider", "Sleight of Hand",
                        "Preordain", "Opt", "Peek", "Thought Scour", "Serum Visions", "Quicken");
    }

    private Permanent addTomeReady(Player player) {
        Permanent tome = new Permanent(new TomeOfGadwick());
        tome.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(tome);
        return tome;
    }

    private void prepareOpponentTurn() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
