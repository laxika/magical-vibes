package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NoxiousNewt.class})
class NoxiousNewtTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Noxious Newt produces one green mana")
    void tappingProducesGreenMana() {
        Permanent newt = addCreatureReady(player1, new NoxiousNewt());

        gs.tapPermanent(gd, player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(newt.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Summoning-sick Noxious Newt cannot tap for mana")
    void summoningSickCannotTap() {
        Permanent newt = harness.addToBattlefieldAndReturn(player1, new NoxiousNewt());
        newt.setSummoningSick(true);

        assertThatThrownBy(() -> gs.tapPermanent(gd, player1, 0))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(newt.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Mana resolves immediately and a tapped Newt cannot produce more")
    void cannotTapTwice() {
        Permanent newt = addCreatureReady(player1, new NoxiousNewt());

        harness.tapPermanent(player1, 0);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThatThrownBy(() -> harness.tapPermanent(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(newt.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Opposing Newts kill each other with nonlethal deathtouch combat damage")
    void deathtouchKillsInCombat() {
        Permanent attacker = addCreatureReady(player1, new NoxiousNewt());
        addCreatureReady(player2, new NoxiousNewt());
        attacker.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.beginBlockerDeclarationInput();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.resolveCombatDamage();
        harness.runStateBasedActions();

        harness.assertNotOnBattlefield(player1, "Noxious Newt");
        harness.assertNotOnBattlefield(player2, "Noxious Newt");
        harness.assertInGraveyard(player1, "Noxious Newt");
        harness.assertInGraveyard(player2, "Noxious Newt");
    }
}
