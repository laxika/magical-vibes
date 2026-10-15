package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GiantWarthog;
import com.github.laxika.magicalvibes.cards.g.GuidedStrike;
import com.github.laxika.magicalvibes.cards.k.KrosanVerge;
import com.github.laxika.magicalvibes.cards.n.NantukoMonastery;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GiantWarthog.class, GuidedStrike.class, KrosanVerge.class, NantukoMonastery.class, SylvanSafekeeper.class})
class SylvanSafekeeperTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing a land grants shroud to a creature you control")
    void sacrificeLandGrantsShroudToControlledCreature() {
        harness.addToBattlefield(player1, new SylvanSafekeeper());
        harness.addToBattlefield(player1, new KrosanVerge());
        Permanent warthog = harness.addToBattlefieldAndReturn(player1, new GiantWarthog());

        harness.activateAbility(player1, 0, null, warthog.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Krosan Verge");
        assertThat(warthog.hasKeyword(Keyword.SHROUD)).isTrue();
    }

    @Test
    @DisplayName("Granted shroud prevents the creature's controller from targeting it with a spell")
    void grantedShroudPreventsSpellTargeting() {
        harness.addToBattlefield(player1, new SylvanSafekeeper());
        harness.addToBattlefield(player1, new KrosanVerge());
        Permanent warthog = harness.addToBattlefieldAndReturn(player1, new GiantWarthog());

        harness.activateAbility(player1, 0, null, warthog.getId());
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new GuidedStrike()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, warthog.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shroud");
    }

    @Test
    @DisplayName("The granted shroud wears off at end of turn")
    void shroudWearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new SylvanSafekeeper());
        harness.addToBattlefield(player1, new KrosanVerge());
        Permanent warthog = harness.addToBattlefieldAndReturn(player1, new GiantWarthog());

        harness.activateAbility(player1, 0, null, warthog.getId());
        harness.passBothPriorities();
        assertThat(warthog.hasKeyword(Keyword.SHROUD)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(warthog.hasKeyword(Keyword.SHROUD)).isFalse();
    }

    @Test
    @DisplayName("An opponent's creature cannot be targeted")
    void onlyControlledCreatureCanBeTargeted() {
        harness.addToBattlefield(player1, new SylvanSafekeeper());
        harness.addToBattlefield(player1, new KrosanVerge());
        Permanent enemyWarthog = harness.addToBattlefieldAndReturn(player2, new GiantWarthog());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, enemyWarthog.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");
    }

    @Test
    @DisplayName("The ability cannot be activated without a land to sacrifice")
    void requiresLandToSacrifice() {
        harness.addToBattlefield(player1, new SylvanSafekeeper());
        Permanent warthog = harness.addToBattlefieldAndReturn(player1, new GiantWarthog());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, warthog.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A land you control cannot be targeted")
    void onlyCreaturesCanBeTargeted() {
        harness.addToBattlefield(player1, new SylvanSafekeeper());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new NantukoMonastery());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");
        harness.assertOnBattlefield(player1, "Nantuko Monastery");
    }

    @Test
    @DisplayName("Granted shroud prevents later abilities from targeting the creature")
    void grantedShroudPreventsLaterTargeting() {
        harness.addToBattlefield(player1, new SylvanSafekeeper());
        harness.addToBattlefield(player1, new SylvanSafekeeper());
        harness.addToBattlefield(player1, new NantukoMonastery());
        Permanent warthog = harness.addToBattlefieldAndReturn(player1, new GiantWarthog());

        harness.activateAbility(player1, 0, null, warthog.getId());
        harness.passBothPriorities();

        assertThat(warthog.hasKeyword(Keyword.SHROUD)).isTrue();
        harness.addToBattlefield(player1, new KrosanVerge());
        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, warthog.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shroud");
    }

    @Test
    @DisplayName("Safekeeper can protect itself while tapped and summoning sick, paying the cost immediately")
    void canProtectItselfWithoutTapping() {
        Permanent safekeeper = harness.addToBattlefieldAndReturn(player1, new SylvanSafekeeper());
        safekeeper.tap();
        safekeeper.setSummoningSick(true);
        harness.addToBattlefield(player1, new KrosanVerge());

        harness.activateAbility(player1, 0, null, safekeeper.getId());

        harness.assertInGraveyard(player1, "Krosan Verge");
        harness.assertNotOnBattlefield(player1, "Krosan Verge");
        assertThat(safekeeper.hasKeyword(Keyword.SHROUD)).isFalse();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(safekeeper.hasKeyword(Keyword.SHROUD)).isTrue();
        assertThat(safekeeper.isTapped()).isTrue();
    }

    @Test
    @DisplayName("An opponent's land cannot pay the sacrifice cost")
    void cannotSacrificeOpponentsLand() {
        Permanent safekeeper = harness.addToBattlefieldAndReturn(player1, new SylvanSafekeeper());
        harness.addToBattlefield(player2, new KrosanVerge());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, safekeeper.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player2, "Krosan Verge");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Granting shroud in response makes an opponent's targeted spell fail to resolve")
    void shroudInResponseStopsTargetedSpellAndItsDraw() {
        harness.addToBattlefield(player1, new SylvanSafekeeper());
        harness.addToBattlefield(player1, new KrosanVerge());
        Permanent warthog = harness.addToBattlefieldAndReturn(player1, new GiantWarthog());
        harness.setHand(player2, List.of(new GuidedStrike()));
        harness.setLibrary(player2, List.of(new GiantWarthog()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castInstant(player2, 0, warthog.getId());
        harness.activateAbility(player1, 0, null, warthog.getId());
        harness.passBothPriorities();
        assertThat(warthog.hasKeyword(Keyword.SHROUD)).isTrue();
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Guided Strike");
        harness.assertNotInHand(player2, "Giant Warthog");
        assertThat(warthog.hasKeyword(Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gd.stack).isEmpty();
    }
}
