package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.d.DarkRitual;
import com.github.laxika.magicalvibes.cards.e.EssenceBottle;
import com.github.laxika.magicalvibes.cards.f.FeveredConvulsions;
import com.github.laxika.magicalvibes.cards.f.Fireslinger;
import com.github.laxika.magicalvibes.cards.g.GhostTown;
import com.github.laxika.magicalvibes.cards.m.MoggFanatic;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Interdict.class, DarkRitual.class, EssenceBottle.class, FeveredConvulsions.class,
        Fireslinger.class, GhostTown.class, MoggFanatic.class})
class InterdictTest extends BaseCardTest {

    private void addReadyInterdict() {
        harness.setHand(player1, List.of(new Interdict()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
    }

    private Permanent addEssenceBottle() {
        Permanent bottle = harness.addToBattlefieldAndReturn(player2, new EssenceBottle());
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        return bottle;
    }

    private void resolveInterdict(Permanent source) {
        harness.castInstant(player1, 0, source.getCard().getId());
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Counters an activated ability and draws a card")
    void countersAbilityAndDraws() {
        addReadyInterdict();
        Permanent bottle = addEssenceBottle();

        harness.forceActivePlayer(player2);
        harness.activateAbility(player2, 0, null, null);
        harness.passPriority(player2);

        int lifeBefore = harness.getGameData().playerLifeTotals.get(player1.getId());
        int handBefore = harness.getGameData().playerHands.get(player1.getId()).size();

        resolveInterdict(bottle);

        harness.assertLife(player1, lifeBefore);
        assertThat(harness.getGameData().stack).isEmpty();
        assertThat(bottle.getCounterCount(CounterType.ELIXIR)).isZero();
        // Interdict left the hand and drew one card, so the hand size is unchanged minus the cast.
        assertThat(harness.getGameData().playerHands.get(player1.getId())).hasSize(handBefore);
    }

    @Test
    @DisplayName("The ability's source can't activate its abilities again this turn")
    void locksSourcePermanentForTheTurn() {
        addReadyInterdict();
        Permanent bottle = addEssenceBottle();

        harness.forceActivePlayer(player2);
        harness.activateAbility(player2, 0, null, null);
        harness.passPriority(player2);

        resolveInterdict(bottle);

        // Untap the Bottle so only the Interdict lock — not its tap cost — can stop the activation.
        bottle.untap();
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a spell on the stack")
    void cannotTargetSpell() {
        addReadyInterdict();

        DarkRitual darkRitual = new DarkRitual();
        harness.setHand(player2, List.of(darkRitual));
        harness.addMana(player2, ManaColor.BLACK, 1);

        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0);
        harness.passPriority(player2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, darkRitual.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Counters an activated ability from a creature")
    void countersAbilityFromCreature() {
        addReadyInterdict();
        Permanent fireslinger = addCreatureReady(player2, new Fireslinger());

        harness.forceActivePlayer(player2);
        harness.activateAbility(player2, 0, null, player1.getId());
        harness.passPriority(player2);

        int player1LifeBefore = gd.playerLifeTotals.get(player1.getId());
        int player2LifeBefore = gd.playerLifeTotals.get(player2.getId());
        resolveInterdict(fireslinger);

        harness.assertLife(player1, player1LifeBefore);
        harness.assertLife(player2, player2LifeBefore);
        assertThat(gd.stack).isEmpty();

        fireslinger.untap();
        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Counters an activated ability from an enchantment")
    void countersAbilityFromEnchantment() {
        addReadyInterdict();
        Permanent targetCreature = addCreatureReady(player1, new Fireslinger());
        Permanent convulsions = harness.addToBattlefieldAndReturn(player2, new FeveredConvulsions());
        harness.addMana(player2, ManaColor.BLACK, 4);

        harness.forceActivePlayer(player2);
        harness.activateAbility(player2, 0, null, targetCreature.getId());
        harness.passPriority(player2);

        resolveInterdict(convulsions);

        assertThat(targetCreature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
        harness.addMana(player2, ManaColor.BLACK, 4);
        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, targetCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Counters a non-mana activated ability from a land")
    void countersAbilityFromLand() {
        addReadyInterdict();
        Permanent ghostTown = harness.addToBattlefieldAndReturn(player2, new GhostTown());

        harness.forceActivePlayer(player1);
        harness.passPriority(player1);
        harness.activateAbility(player2, 0, 1, null, null);

        resolveInterdict(ghostTown);

        harness.assertOnBattlefield(player2, "Ghost Town");
        assertThat(gd.stack).isEmpty();

        harness.passPriority(player1);
        assertThatThrownBy(() -> harness.activateAbility(player2, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Mana abilities cannot be targeted")
    void cannotTargetManaAbility() {
        addReadyInterdict();
        Permanent ghostTown = harness.addToBattlefieldAndReturn(player2, new GhostTown());

        harness.forceActivePlayer(player2);
        harness.activateAbility(player2, 0, 0, null, null);

        assertThat(gd.stack).isEmpty();
        harness.passPriority(player2);
        assertThatThrownBy(() -> harness.castInstant(player1, 0, ghostTown.getCard().getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Counters an ability even when its creature source has left the battlefield")
    void countersAbilityWhoseSourceLeftBattlefield() {
        addReadyInterdict();
        MoggFanatic fanatic = new MoggFanatic();
        Permanent fanaticPermanent = harness.addToBattlefieldAndReturn(player2, fanatic);

        harness.forceActivePlayer(player2);
        harness.activateAbility(player2, 0, null, player1.getId());
        harness.passPriority(player2);

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        int handBefore = gd.playerHands.get(player1.getId()).size();
        resolveInterdict(fanaticPermanent);

        harness.assertLife(player1, lifeBefore);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        harness.assertInGraveyard(player2, "Mogg Fanatic");
    }
}
