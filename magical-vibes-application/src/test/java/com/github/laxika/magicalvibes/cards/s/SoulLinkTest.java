package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SoulLink.class, GrizzlyBears.class, LightningBolt.class})
class SoulLinkTest extends BaseCardTest {

    @Test
    @DisplayName("You gain life equal to damage dealt by the enchanted creature")
    void gainsLifeFromDamageDealtByEnchantedCreature() {
        GrizzlyBears card = new GrizzlyBears();
        card.setPower(3);
        Permanent creature = addCreatureReady(player1, card);
        castSoulLink(creature);

        harness.setLife(player1, 10);
        harness.setLife(player2, 20);
        creature.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(13);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("You gain life equal to damage dealt to the enchanted creature")
    void gainsLifeFromDamageDealtToEnchantedCreature() {
        GrizzlyBears creatureCard = new GrizzlyBears();
        creatureCard.setToughness(6);
        Permanent creature = addCreatureReady(player2, creatureCard);
        castSoulLink(creature);

        harness.setLife(player1, 10);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(13);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    private void castSoulLink(Permanent creature) {
        harness.setHand(player1, List.of(new SoulLink()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
    }
}
