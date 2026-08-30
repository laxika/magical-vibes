package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Opt;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Dovescape.class, DovinsVeto.class, GrizzlyBears.class, Opt.class})
class DovescapeTest extends BaseCardTest {

    @Test
    @DisplayName("Counters a noncreature spell and gives its caster Birds equal to its mana value")
    void countersNoncreatureSpellAndCreatesBirdsForCaster() {
        harness.addToBattlefield(player1, new Dovescape());
        Opt opt = new Opt();
        harness.setHand(player2, List.of(opt));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castInstant(player2, 0);
        harness.passBothPriorities();

        assertThat(findPermanents(player2, "Bird")).hasSize(1);
        assertThat(findPermanents(player2, "Bird")).allSatisfy(bird -> {
            assertThat(bird.getCard().isToken()).isTrue();
            assertThat(gqs.getEffectivePower(gd, bird)).isEqualTo(1);
            assertThat(gqs.getEffectiveToughness(gd, bird)).isEqualTo(1);
        });
        assertThat(findPermanents(player1, "Bird")).isEmpty();
        harness.assertInGraveyard(player2, "Opt");
    }

    @Test
    @DisplayName("Does not trigger for a creature spell")
    void doesNotTriggerForCreatureSpell() {
        harness.addToBattlefield(player1, new Dovescape());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Bird")).isEmpty();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Creates Birds even when the spell cannot be countered or has left the stack")
    void createsBirdsWhenCounterCannotApply() {
        harness.addToBattlefield(player1, new Dovescape());
        Opt opt = new Opt();
        DovinsVeto veto = new DovinsVeto();
        harness.setHand(player1, List.of(opt));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.setHand(player2, List.of(veto));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, opt.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player2, "Bird")).hasSize(2);
        assertThat(findPermanents(player1, "Bird")).hasSize(1);
        harness.assertInGraveyard(player1, "Opt");
        harness.assertInGraveyard(player2, "Dovin's Veto");
    }
}
