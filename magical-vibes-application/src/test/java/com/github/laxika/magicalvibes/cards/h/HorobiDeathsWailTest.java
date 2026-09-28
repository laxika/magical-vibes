package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Frostwielder;
import com.github.laxika.magicalvibes.cards.g.GlacialRay;
import com.github.laxika.magicalvibes.cards.w.WanderingOnes;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HorobiDeathsWail.class, WanderingOnes.class, GlacialRay.class, Frostwielder.class})
class HorobiDeathsWailTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a creature targeted by an opponent's spell")
    void destroysCreatureTargetedBySpell() {
        harness.addToBattlefield(player1, new HorobiDeathsWail());
        harness.addToBattlefield(player1, new WanderingOnes());
        UUID creatureId = harness.getPermanentId(player1, "Wandering Ones");

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new GlacialRay()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castInstant(player2, 0, creatureId);

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack.getLast().getCard().getName()).isEqualTo("Horobi, Death's Wail");

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Wandering Ones");
        harness.assertInGraveyard(player1, "Wandering Ones");
    }

    @Test
    @DisplayName("Triggers on the controller's own spell too")
    void triggersOnOwnSpell() {
        harness.addToBattlefield(player1, new HorobiDeathsWail());
        harness.addToBattlefield(player1, new WanderingOnes());
        UUID creatureId = harness.getPermanentId(player1, "Wandering Ones");

        harness.setHand(player1, List.of(new GlacialRay()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, creatureId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Wandering Ones");
        harness.assertInGraveyard(player1, "Wandering Ones");
    }

    @Test
    @DisplayName("Triggers when an activated ability targets a creature")
    void triggersOnActivatedAbility() {
        harness.addToBattlefield(player1, new HorobiDeathsWail());
        harness.addToBattlefield(player1, new WanderingOnes());
        UUID creatureId = harness.getPermanentId(player1, "Wandering Ones");

        addCreatureReady(player2, new Frostwielder());

        harness.activateAbility(player2, 0, null, creatureId);

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack.getLast().getCard().getName()).isEqualTo("Horobi, Death's Wail");

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Wandering Ones");
        harness.assertInGraveyard(player1, "Wandering Ones");
    }

    @Test
    @DisplayName("Triggers for a creature controlled by another player")
    void triggersForOpponentCreature() {
        harness.addToBattlefield(player1, new HorobiDeathsWail());
        harness.addToBattlefield(player2, new WanderingOnes());
        UUID creatureId = harness.getPermanentId(player2, "Wandering Ones");

        harness.setHand(player1, List.of(new GlacialRay()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, creatureId);

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack.getLast().getCard().getName()).isEqualTo("Horobi, Death's Wail");

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Wandering Ones");
        harness.assertInGraveyard(player2, "Wandering Ones");
    }

    @Test
    @DisplayName("Does not trigger when a spell targets a player")
    void doesNotTriggerOnPlayerTarget() {
        harness.addToBattlefield(player1, new HorobiDeathsWail());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new GlacialRay()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castInstant(player2, 0, player1.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Glacial Ray");
    }
}
