package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.d.DarksteelIngot;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OnlyIKnowWhatAwaits.class, DarksteelIngot.class, Forest.class, GrizzlyBears.class})
class OnlyIKnowWhatAwaitsTest extends BaseCardTest {

    @Test
    void putsOnePermanentOfEachTypeOpponentsControl() {
        harness.addToBattlefield(player2, new DarksteelIngot());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new Forest(), new GrizzlyBears(), new DarksteelIngot()));

        resolveScheme();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Forest");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Darksteel Ingot");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void onlyOpponentsPermanentTypesMatter() {
        harness.addToBattlefield(player1, new Forest());
        harness.setHand(player1, List.of(new GrizzlyBears()));

        resolveScheme();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Forest");
    }

    @Test
    void eachPermanentTypeChoiceMayBeDeclined() {
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new Forest()));

        resolveScheme();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInHand(player1, "Forest");
        harness.assertNotOnBattlefield(player1, "Forest");
    }

    private void resolveScheme() {
        Card scheme = new OnlyIKnowWhatAwaits();
        gd.stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                scheme,
                player1.getId(),
                scheme.getName(),
                scheme.getEffects(EffectSlot.SPELL)));
        harness.passBothPriorities();
    }
}
