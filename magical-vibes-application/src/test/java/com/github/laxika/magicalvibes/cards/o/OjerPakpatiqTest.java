package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.d.DarkRitual;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.cards.t.TempleOfCyclicalTime;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.action.ReboundAtNextUpkeep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OjerPakpatiq.class, TempleOfCyclicalTime.class, DarkRitual.class, GrizzlyBears.class, Murder.class})
class OjerPakpatiqTest extends BaseCardTest {

    @Test
    @DisplayName("Gives instant spells cast from hand rebound")
    void givesInstantSpellsRebound() {
        harness.addToBattlefield(player1, new OjerPakpatiq());

        DarkRitual spell = new DarkRitual();
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(spell.getId())).isNotNull();
        assertThat(gd.delayedActions).anyMatch(action -> action instanceof ReboundAtNextUpkeep);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
    }

    @Test
    @DisplayName("Returns tapped and transformed with three time counters when it dies")
    void returnsTappedAndTransformedWithTimeCountersWhenItDies() {
        Permanent ojer = harness.addToBattlefieldAndReturn(player1, new OjerPakpatiq());
        destroyOjer(ojer);

        Permanent temple = findPermanents(player1, "Temple of Cyclical Time").getFirst();
        assertThat(temple.isTapped()).isTrue();
        assertThat(temple.isTransformed()).isTrue();
        assertThat(temple.getCounterCount(CounterType.TIME)).isEqualTo(3);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getName)
                .doesNotContain("Ojer Pakpatiq, Deepest Epoch");
    }

    @Test
    @DisplayName("Removes time counters while producing mana and transforms only with none")
    void removesTimeCountersForManaAndTransformsWithNoCounters() {
        Permanent temple = returnOjerAsTemple();
        harness.forceActivePlayer(player1);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        for (int i = 0; i < 3; i++) {
            temple.untap();
            harness.activateAbility(player1, battlefieldIndex(temple), 0, null, null);
        }
        assertThat(temple.getCounterCount(CounterType.TIME)).isZero();

        temple.untap();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, battlefieldIndex(temple), 1, null, null);
        harness.passBothPriorities();

        assertThat(temple.getCard()).isInstanceOf(OjerPakpatiq.class);
        assertThat(temple.isTransformed()).isFalse();
    }

    @Test
    @DisplayName("Cannot transform while it has time counters")
    void cannotTransformWithTimeCounters() {
        Permanent temple = returnOjerAsTemple();
        harness.forceActivePlayer(player1);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(temple), 1, null, null))
                .isInstanceOf(RuntimeException.class);
        assertThat(temple.getCard()).isInstanceOf(TempleOfCyclicalTime.class);
        assertThat(temple.getCounterCount(CounterType.TIME)).isEqualTo(3);
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }

    private void destroyOjer(Permanent ojer) {
        harness.forceActivePlayer(player2);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Murder()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castInstant(player2, 0, ojer.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private Permanent returnOjerAsTemple() {
        Permanent ojer = harness.addToBattlefieldAndReturn(player1, new OjerPakpatiq());
        destroyOjer(ojer);
        return findPermanents(player1, "Temple of Cyclical Time").getFirst();
    }
}
