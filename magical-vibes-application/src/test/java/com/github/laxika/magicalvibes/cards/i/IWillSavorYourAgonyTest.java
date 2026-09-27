package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IWillSavorYourAgony.class, GrizzlyBears.class, Forest.class})
class IWillSavorYourAgonyTest extends BaseCardTest {

    private static final String DESTROY = "Destroy target creature.";
    private static final String DRAW = "Target player draws a card.";
    private static final String GAIN = "Target player gains 5 life.";

    @Test
    void resolvesEachSelectedModeWithItsOwnTarget() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        int handBefore = gd.playerHands.get(player1.getId()).size();
        gd.playerDecks.put(player1.getId(), new ArrayList<>(List.of(new Forest())));
        harness.setLife(player1, 10);

        resolveScheme();

        harness.handleListChoice(player1, DESTROY);
        harness.handleListChoice(player1, DRAW);
        harness.handleListChoice(player1, GAIN);
        harness.handlePermanentChosen(player1, creature.getId());
        harness.handlePermanentChosen(player1, player1.getId());
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(findPermanents(player2, "Grizzly Bears")).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(gd.getLife(player1.getId())).isEqualTo(15);
    }

    @Test
    void allowsChoosingTheSameModeThreeTimes() {
        Permanent first = addCreatureReady(player2, new GrizzlyBears());
        Permanent second = addCreatureReady(player2, new GrizzlyBears());
        Permanent third = addCreatureReady(player2, new GrizzlyBears());

        resolveScheme();

        harness.handleListChoice(player1, DESTROY);
        harness.handleListChoice(player1, DESTROY);
        harness.handleListChoice(player1, DESTROY);
        harness.handlePermanentChosen(player1, first.getId());
        harness.handlePermanentChosen(player1, second.getId());
        harness.handlePermanentChosen(player1, third.getId());
        harness.passBothPriorities();

        assertThat(findPermanents(player2, "Grizzly Bears")).isEmpty();
    }

    private void resolveScheme() {
        IWillSavorYourAgony scheme = new IWillSavorYourAgony();
        gd.stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                scheme,
                player1.getId(),
                scheme.getName(),
                scheme.getEffects(EffectSlot.SPELL)));
        harness.passBothPriorities();
    }
}
