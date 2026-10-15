package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.r.RottenReunion;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({JadarGhoulcallerOfNephalia.class, RottenReunion.class})
class JadarGhoulcallerOfNephaliaTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a decayed Zombie at the beginning of your end step when you control none")
    void createsDecayedZombieWithoutAnotherDecayedCreature() {
        harness.addToBattlefield(player1, new JadarGhoulcallerOfNephalia());

        advanceToControllerEndStep();
        harness.passBothPriorities();

        Permanent zombie = findPermanent(player1, "Zombie");
        assertThat(countPermanents(player1, "Zombie")).isEqualTo(1);
        assertThat(zombie.getCard().isToken()).isTrue();
        assertThat(zombie.getCard().getPower()).isEqualTo(2);
        assertThat(zombie.getCard().getToughness()).isEqualTo(2);
        assertThat(zombie.getCard().getColor()).isEqualTo(CardColor.BLACK);
        assertThat(zombie.getCard().getSubtypes()).contains(CardSubtype.ZOMBIE);
        assertThat(zombie.getCard().getKeywords()).contains(Keyword.DECAYED);
    }

    @Test
    @DisplayName("Does not create a Zombie while you control a creature with decayed")
    void doesNotCreateWithAnotherDecayedCreature() {
        harness.addToBattlefield(player1, new JadarGhoulcallerOfNephalia());
        createDecayedZombie(player1);

        advanceToControllerEndStep();

        assertThat(countPermanents(player1, "Zombie")).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Rechecks the condition when a decayed creature enters in response")
    void doesNotCreateWhenDecayedCreatureEntersInResponse() {
        harness.addToBattlefield(player1, new JadarGhoulcallerOfNephalia());
        advanceToControllerEndStep();
        assertThat(gd.stack).hasSize(1);

        createDecayedZombie(player1);
        assertThat(countPermanents(player1, "Zombie")).isEqualTo(1);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Zombie")).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An opponent's decayed creature does not prevent creation")
    void ignoresOpponentsDecayedCreature() {
        harness.addToBattlefield(player1, new JadarGhoulcallerOfNephalia());
        createDecayedZombie(player2);

        advanceToControllerEndStep();
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Zombie")).isEqualTo(1);
        assertThat(countPermanents(player2, "Zombie")).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not trigger during an opponent's end step")
    void doesNotTriggerOnOpponentsEndStep() {
        harness.addToBattlefield(player1, new JadarGhoulcallerOfNephalia());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player2, TurnStep.END_STEP);

        assertThat(gd.stack).isEmpty();
        assertThat(countPermanents(player1, "Zombie")).isZero();
    }

    private void createDecayedZombie(Player player) {
        harness.setHand(player, List.of(new RottenReunion()));
        harness.addMana(player, ManaColor.BLACK, 1);
        harness.castAndResolveInstant(player, 0);
    }

    private void advanceToControllerEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.END_STEP);
    }
}
