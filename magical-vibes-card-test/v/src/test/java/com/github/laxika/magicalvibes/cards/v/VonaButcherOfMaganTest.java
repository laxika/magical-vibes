package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.r.RaptorCompanion;
import com.github.laxika.magicalvibes.cards.r.RaidersWake;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VonaButcherOfMagan.class, RaptorCompanion.class, Forest.class, RaidersWake.class})
class VonaButcherOfMaganTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys target nonland permanent and pays 7 life")
    void destroysNonlandPermanentAndPaysLife() {
        Permanent vona = addCreatureReady(player1, new VonaButcherOfMagan());
        harness.setLife(player1, 20);

        Permanent bears = harness.addToBattlefieldAndReturn(player2, new RaptorCompanion());

        int vonaIdx = gd.playerBattlefields.get(player1.getId()).indexOf(vona);
        harness.activateAbility(player1, vonaIdx, null, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(13);
        harness.assertNotOnBattlefield(player2, "Raptor Companion");
        harness.assertInGraveyard(player2, "Raptor Companion");
    }

    @Test
    @DisplayName("Taps Vona as cost")
    void tapsVonaAsCost() {
        Permanent vona = addCreatureReady(player1, new VonaButcherOfMagan());
        harness.setLife(player1, 20);

        Permanent bears = harness.addToBattlefieldAndReturn(player2, new RaptorCompanion());

        int vonaIdx = gd.playerBattlefields.get(player1.getId()).indexOf(vona);
        harness.activateAbility(player1, vonaIdx, null, bears.getId());

        assertThat(vona.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot activate with less than 7 life")
    void cannotActivateWithInsufficientLife() {
        Permanent vona = addCreatureReady(player1, new VonaButcherOfMagan());
        harness.setLife(player1, 6);

        Permanent bears = harness.addToBattlefieldAndReturn(player2, new RaptorCompanion());

        int vonaIdx = gd.playerBattlefields.get(player1.getId()).indexOf(vona);
        assertThatThrownBy(() -> harness.activateAbility(player1, vonaIdx, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough life");
    }

    @Test
    @DisplayName("Can activate at exactly 7 life — pays cost but SBAs end the game at 0 life")
    void canActivateAtExactlySevenLife() {
        Permanent vona = addCreatureReady(player1, new VonaButcherOfMagan());
        harness.setLife(player1, 7);

        Permanent bears = harness.addToBattlefieldAndReturn(player2, new RaptorCompanion());

        int vonaIdx = gd.playerBattlefields.get(player1.getId()).indexOf(vona);
        // Activation succeeds (7 life is enough to pay the cost),
        // but paying 7 life drops to 0 and SBAs end the game before the ability resolves.
        harness.activateAbility(player1, vonaIdx, null, bears.getId());

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(0);
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("Cannot target a land permanent")
    void cannotTargetLand() {
        Permanent vona = addCreatureReady(player1, new VonaButcherOfMagan());
        harness.setLife(player1, 20);

        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());

        int vonaIdx = gd.playerBattlefields.get(player1.getId()).indexOf(vona);
        assertThatThrownBy(() -> harness.activateAbility(player1, vonaIdx, null, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate during opponent's turn")
    void cannotActivateDuringOpponentsTurn() {
        Permanent vona = addCreatureReady(player1, new VonaButcherOfMagan());
        harness.setLife(player1, 20);

        Permanent bears = harness.addToBattlefieldAndReturn(player2, new RaptorCompanion());

        // Make it player2's turn
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        int vonaIdx = gd.playerBattlefields.get(player1.getId()).indexOf(vona);
        assertThatThrownBy(() -> harness.activateAbility(player1, vonaIdx, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("during your turn");
    }

    @Test
    @DisplayName("Can activate during combat on your turn (not sorcery speed)")
    void canActivateDuringCombatOnYourTurn() {
        Permanent vona = addCreatureReady(player1, new VonaButcherOfMagan());
        harness.setLife(player1, 20);

        Permanent bears = harness.addToBattlefieldAndReturn(player2, new RaptorCompanion());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        int vonaIdx = gd.playerBattlefields.get(player1.getId()).indexOf(vona);
        harness.activateAbility(player1, vonaIdx, null, bears.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Raptor Companion");
    }

    @Test
    @DisplayName("Cannot activate with summoning sickness")
    void cannotActivateWithSummoningSickness() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.addToBattlefield(player1, new VonaButcherOfMagan());
        // Default: summoning sick

        Permanent bears = harness.addToBattlefieldAndReturn(player2, new RaptorCompanion());

        harness.setLife(player1, 20);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can destroy an enchantment (nonland permanent)")
    void canDestroyEnchantment() {
        Permanent vona = addCreatureReady(player1, new VonaButcherOfMagan());
        harness.setLife(player1, 20);

        Permanent enchPerm = harness.addToBattlefieldAndReturn(player2, new RaidersWake());

        int vonaIdx = gd.playerBattlefields.get(player1.getId()).indexOf(vona);
        harness.activateAbility(player1, vonaIdx, null, enchPerm.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Raiders' Wake");
        harness.assertInGraveyard(player2, "Raiders' Wake");
    }

    @Test
    @DisplayName("Vona can destroy itself")
    void canTargetItself() {
        Permanent vona = addCreatureReady(player1, new VonaButcherOfMagan());
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, vona.getId());
        harness.assertLife(player1, 13);
        assertThat(vona.isTapped()).isTrue();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Vona, Butcher of Magan");
        harness.assertInGraveyard(player1, "Vona, Butcher of Magan");
    }

    @Test
    @DisplayName("Vigilance leaves Vona untapped and lifelink gains life from combat damage")
    void attacksWithoutTappingAndGainsLife() {
        Permanent vona = addCreatureReady(player1, new VonaButcherOfMagan());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        declareAttackers(List.of(0));
        assertThat(vona.isTapped()).isFalse();
        resolveCombat();

        harness.assertLife(player1, 24);
        harness.assertLife(player2, 16);
    }

    @Test
    @DisplayName("A tapped Vona cannot activate again and does not pay life again")
    void cannotActivateWhileTapped() {
        Permanent vona = addCreatureReady(player1, new VonaButcherOfMagan());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RaptorCompanion());
        harness.setLife(player1, 20);
        vona.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertLife(player1, 20);
        assertThat(gd.stack).isEmpty();
    }

}
