package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SpiritFlare.class, GrizzlyBears.class, LlanowarElves.class})
class SpiritFlareTest extends BaseCardTest {

    @Test
    @DisplayName("Taps the first target and deals its power to an attacking creature")
    void tapsAndDealsPowerDamage() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent attacker = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());
        attacker.setAttacking(true);

        castSpiritFlare(source, attacker);
        harness.passBothPriorities();

        assertThat(source.isTapped()).isTrue();
        harness.assertNotOnBattlefield(player2, "Llanowar Elves");
    }

    @Test
    @DisplayName("Does not deal damage when the first target is tapped before resolution")
    void doesNotDealDamageWhenFirstTargetBecomesTapped() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent attacker = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());
        attacker.setAttacking(true);

        castSpiritFlare(source, attacker);
        source.tap();
        harness.passBothPriorities();

        assertThat(source.isTapped()).isTrue();
        harness.assertOnBattlefield(player2, "Llanowar Elves");
    }

    @Test
    @DisplayName("Requires an attacking or blocking creature an opponent controls as the second target")
    void rejectsIllegalSecondTarget() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new LlanowarElves());
        harness.setHand(player1, List.of(new SpiritFlare()));
        addMana(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(source.getId(), ownCreature.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("attacking or blocking creature an opponent controls");
    }

    @Test
    @DisplayName("Flashback pays life and exiles Spirit Flare after resolving")
    void flashbackPaysLifeAndExiles() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent attacker = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());
        attacker.setAttacking(true);
        harness.setGraveyard(player1, List.of(new SpiritFlare()));
        addMana(player1, true);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        harness.castFlashback(player1, 0, List.of(source.getId(), attacker.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(17);
        assertThat(gd.lifeLostThisTurn.get(player1.getId())).isEqualTo(3);
        harness.assertNotInGraveyard(player1, "Spirit Flare");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Spirit Flare"));
    }

    private void castSpiritFlare(Permanent source, Permanent attacker) {
        harness.setHand(player1, List.of(new SpiritFlare()));
        addMana(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.castInstant(player1, 0, List.of(source.getId(), attacker.getId()));
    }

    private void addMana(Player player) {
        addMana(player, false);
    }

    private void addMana(Player player, boolean flashback) {
        harness.addMana(player, ManaColor.WHITE, 1);
        harness.addMana(player, ManaColor.COLORLESS, flashback ? 1 : 3);
    }
}
