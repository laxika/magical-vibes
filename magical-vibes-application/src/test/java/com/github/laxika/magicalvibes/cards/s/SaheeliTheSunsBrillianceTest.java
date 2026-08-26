package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.z.ZuranOrb;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentAction;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentActionKind;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SaheeliTheSunsBrilliance.class, GrizzlyBears.class, ZuranOrb.class})
class SaheeliTheSunsBrillianceTest extends BaseCardTest {

    @Test
    @DisplayName("Creates an artifact token copy with haste and a next-end-step sacrifice")
    void createsArtifactTokenCopyWithHasteAndSacrifice() {
        addReadySaheeli(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        addActivationMana();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        Permanent token = findPermanents(player1, "Grizzly Bears").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(token.getCard().getAdditionalTypes()).contains(CardType.ARTIFACT);
        assertThat(token.getCard().getKeywords()).contains(Keyword.HASTE);
        assertThat(gd.getDelayedActions(DelayedPermanentAction.class))
                .contains(new DelayedPermanentAction(token.getId(), DelayedPermanentActionKind.SACRIFICE_AT_END_STEP));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Grizzly Bears").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList()).isEmpty();
    }

    @Test
    @DisplayName("Can target an artifact you control")
    void canTargetArtifactYouControl() {
        addReadySaheeli(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new ZuranOrb());
        addActivationMana();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Zuran Orb").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList()).hasSize(1);
    }

    @Test
    @DisplayName("Cannot target an opponent's permanent or Saheeli herself")
    void rejectsOpponentAndSelfTargets() {
        Permanent saheeli = addReadySaheeli(player1);
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        addActivationMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("another creature or artifact you control");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, saheeli.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("another creature or artifact you control");
    }

    private Permanent addReadySaheeli(Player player) {
        Permanent saheeli = new Permanent(new SaheeliTheSunsBrilliance());
        saheeli.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(saheeli);
        return saheeli;
    }

    private void addActivationMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
    }
}
