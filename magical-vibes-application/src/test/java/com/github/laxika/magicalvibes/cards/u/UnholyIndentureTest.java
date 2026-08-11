package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UnholyIndentureTest extends BaseCardTest {

    @Test
    void returnsEnchantedCreatureUnderAuraControllersControlWithCounter() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        Card creatureCard = creature.getCard();

        castUnholyIndenture(player1, creature);
        killCreature(creature);

        Permanent returned = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getId().equals(creatureCard.getId()))
                .findFirst()
                .orElse(null);
        assertThat(returned).isNotNull();
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getId().equals(creatureCard.getId()));
    }

    @Test
    void auraGoesToGraveyardWhenEnchantedCreatureDies() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());

        castUnholyIndenture(player1, creature);
        killCreature(creature);

        harness.assertInGraveyard(player1, "Unholy Indenture");
        harness.assertNotOnBattlefield(player1, "Unholy Indenture");
    }

    @Test
    void cannotEnchantNonCreaturePermanent() {
        Permanent nonCreature = new Permanent(new UnholyIndenture());
        gd.playerBattlefields.get(player2.getId()).add(nonCreature);

        harness.setHand(player1, List.of(new UnholyIndenture()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, nonCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castUnholyIndenture(Player controller, Permanent target) {
        harness.setHand(controller, List.of(new UnholyIndenture()));
        harness.addMana(controller, ManaColor.BLACK, 3);

        harness.castEnchantment(controller, 0, target.getId());
        harness.passBothPriorities();
    }

    private void killCreature(Permanent creature) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new DoomBlade()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
