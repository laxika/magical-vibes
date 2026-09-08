package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CruelEdict;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
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

@CardUsed({SlaughterPriestOfMogis.class, CruelEdict.class, GrizzlyBears.class, GloriousAnthem.class})
class SlaughterPriestOfMogisTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +2/+0 whenever you sacrifice a permanent")
    void getsBoostWhenControllerSacrificesPermanent() {
        Permanent priest = addReadyPriest(player1);
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castEdictAt(player1);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(priest.getEffectivePower()).isEqualTo(4);
        assertThat(priest.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not trigger when an opponent sacrifices a permanent")
    void doesNotBoostWhenOpponentSacrificesPermanent() {
        Permanent priest = addReadyPriest(player1);
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castEdictAt(player2);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(priest.getEffectivePower()).isEqualTo(2);
    }

    @Test
    @DisplayName("Sacrificing another creature grants first strike")
    void sacrificesAnotherCreatureAndGainsFirstStrike() {
        Permanent priest = addReadyPriest(player1);
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent enchantment = harness.addToBattlefieldAndReturn(player1, new GloriousAnthem());
        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, battlefieldIndex(priest), null, null);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(bears.getId(), enchantment.getId());
        assertThat(choice.validIds()).doesNotContain(priest.getId(), opponentBears.getId());

        harness.handlePermanentChosen(player1, bears.getId());
        resolveAllTriggers();

        assertThat(priest.getEffectivePower()).isEqualTo(4);
        assertThat(priest.hasKeyword(Keyword.FIRST_STRIKE)).isTrue();
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("First strike and the boost wear off at end of turn")
    void temporaryEffectsWearOff() {
        Permanent priest = addReadyPriest(player1);
        harness.addToBattlefield(player1, new GloriousAnthem());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, battlefieldIndex(priest), null, null);
        resolveAllTriggers();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(priest.getEffectivePower()).isEqualTo(2);
        assertThat(priest.hasKeyword(Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Cannot activate without another creature or an enchantment")
    void cannotActivateWithoutSacrifice() {
        Permanent priest = addReadyPriest(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(priest), null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sacrifice");
    }

    private Permanent addReadyPriest(Player player) {
        Permanent priest = harness.addToBattlefieldAndReturn(player, new SlaughterPriestOfMogis());
        priest.setSummoningSick(false);
        return priest;
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }

    private void castEdictAt(Player target) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new CruelEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castSorcery(player1, 0, target.getId());
    }
}
