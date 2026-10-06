package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.Dominate;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SandskitterOutrider.class, Dominate.class, SealOfRemoval.class})
class SandskitterOutriderTest extends BaseCardTest {

    @Test
    @DisplayName("When Sandskitter Outrider enters, enduring with counters puts two +1/+1 counters on it")
    void enduresWithCounters() {
        castOutrider();

        resolveAllTriggers();
        harness.handleListChoice(player1, "Put 2 +1/+1 counters on this permanent");

        Permanent outrider = findPermanent(player1, "Sandskitter Outrider");
        assertThat(outrider.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(findPermanents(player1, "Spirit")).isEmpty();
    }

    @Test
    @DisplayName("When Sandskitter Outrider enters, enduring with a token creates a 2/2 Spirit")
    void enduresWithSpiritToken() {
        castOutrider();

        resolveAllTriggers();
        harness.handleListChoice(player1, "Create a 2/2 white Spirit creature token");

        Permanent outrider = findPermanent(player1, "Sandskitter Outrider");
        assertThat(outrider.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        Permanent spirit = findPermanents(player1, "Spirit").getFirst();
        assertThat(spirit.getCard().getPower()).isEqualTo(2);
        assertThat(spirit.getCard().getToughness()).isEqualTo(2);
        assertThat(spirit.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(spirit.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(spirit.getCard().getSubtypes()).containsExactly(CardSubtype.SPIRIT);
        assertThat(countPermanents(player1, "Spirit")).isEqualTo(1);
        assertThat(findPermanents(player2, "Spirit")).isEmpty();
    }

    @Test
    void createsSpiritWhenOutriderLeavesBeforeTriggerResolves() {
        harness.addToBattlefield(player2, new SealOfRemoval());
        castOutrider();
        harness.passBothPriorities();
        Permanent outrider = findPermanent(player1, "Sandskitter Outrider");
        harness.activateAbility(player2, 0, null, outrider.getId());
        harness.passBothPriorities();

        resolveAllTriggers();

        assertThat(findPermanents(player1, "Sandskitter Outrider")).isEmpty();
        harness.assertInHand(player1, "Sandskitter Outrider");
        assertThat(countPermanents(player1, "Spirit")).isEqualTo(1);
        assertThat(findPermanent(player1, "Spirit").getCard().getPower()).isEqualTo(2);
        assertThat(findPermanent(player1, "Spirit").getCard().getToughness()).isEqualTo(2);
    }

    @Test
    void currentControllerChoosesEndureAndCreatesSpirit() {
        castOutrider();
        harness.passBothPriorities();
        Permanent outrider = findPermanent(player1, "Sandskitter Outrider");
        harness.setHand(player2, List.of(new Dominate()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 5);
        harness.castInstant(player2, 0, 4, outrider.getId());
        harness.passBothPriorities();
        assertThat(gqs.findPermanentController(gd, outrider.getId())).isEqualTo(player2.getId());

        resolveAllTriggers();
        harness.handleListChoice(player2, "Create a 2/2 white Spirit creature token");

        assertThat(countPermanents(player2, "Spirit")).isEqualTo(1);
        assertThat(findPermanents(player1, "Spirit")).isEmpty();
        assertThat(outrider.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void menaceRejectsOneBlocker() {
        addCreatureReady(player1, new SandskitterOutrider());
        addCreatureReady(player2, new SandskitterOutrider());
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("two or more creatures");
    }

    @Test
    void menaceAllowsTwoBlockers() {
        addCreatureReady(player1, new SandskitterOutrider());
        Permanent first = addCreatureReady(player2, new SandskitterOutrider());
        Permanent second = addCreatureReady(player2, new SandskitterOutrider());
        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));

        assertThat(first.isBlocking()).isTrue();
        assertThat(second.isBlocking()).isTrue();
    }

    private void castOutrider() {
        harness.castFromHand(player1, new SandskitterOutrider(), "{3}{B}");
    }
}
