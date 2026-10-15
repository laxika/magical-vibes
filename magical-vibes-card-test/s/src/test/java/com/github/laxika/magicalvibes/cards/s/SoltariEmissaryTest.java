package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(SoltariEmissary.class)
class SoltariEmissaryTest extends BaseCardTest {

    @Test
    @DisplayName("Activating the ability puts an activated ability on the stack")
    void activatingPutsAbilityOnStack() {
        addEmissaryReady(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
    }

    @Test
    @DisplayName("Resolving the ability grants shadow until end of turn")
    void resolvingGrantsShadow() {
        Permanent emissary = addEmissaryReady(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThat(gqs.hasKeyword(gd, emissary, Keyword.SHADOW)).isFalse();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.hasKeyword(gd, emissary, Keyword.SHADOW)).isTrue();
    }

    @Test
    @DisplayName("Granted shadow prevents a non-shadow creature from blocking")
    void grantedShadowPreventsNonShadowCreatureFromBlocking() {
        Permanent emissary = addEmissaryReady(player1);
        addEmissaryReady(player2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        emissary.setAttacking(true);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shadow");
    }

    @Test
    @DisplayName("Granted shadow wears off at end of turn")
    void shadowWearsOffAtEndOfTurn() {
        Permanent emissary = addEmissaryReady(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, emissary, Keyword.SHADOW)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, emissary, Keyword.SHADOW)).isFalse();
    }

    @Test
    @DisplayName("Activating does not tap Soltari Emissary and works with summoning sickness")
    void activatingDoesNotTapAndIgnoresSummoningSickness() {
        Permanent emissary = harness.addToBattlefieldAndReturn(player1, new SoltariEmissary());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(emissary.isTapped()).isFalse();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Cannot activate without enough mana")
    void cannotActivateWithoutMana() {
        addEmissaryReady(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Shadow is granted only to the source and only on resolution")
    void shadowIsGrantedOnlyToSourceOnResolution() {
        Permanent emissary = addEmissaryReady(player1);
        Permanent other = addEmissaryReady(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gqs.hasKeyword(gd, emissary, Keyword.SHADOW)).isFalse();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, emissary, Keyword.SHADOW)).isTrue();
        assertThat(gqs.hasKeyword(gd, other, Keyword.SHADOW)).isFalse();
    }

    @Test
    @DisplayName("A tapped Soltari Emissary can gain shadow")
    void tappedEmissaryCanGainShadow() {
        Permanent emissary = addEmissaryReady(player1);
        emissary.tap();
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(emissary.isTapped()).isTrue();
        assertThat(gqs.hasKeyword(gd, emissary, Keyword.SHADOW)).isTrue();
    }

    @Test
    @DisplayName("A creature with granted shadow cannot block a creature without shadow")
    void grantedShadowCannotBlockNonShadowCreature() {
        addEmissaryReady(player1);
        addEmissaryReady(player2);
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.activateAbility(player2, 0, 0, null, null);
        harness.passBothPriorities();
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shadow");
    }

    @Test
    @DisplayName("Creatures with granted shadow can block each other")
    void creaturesWithGrantedShadowCanBlockEachOther() {
        addEmissaryReady(player1);
        Permanent blocker = addEmissaryReady(player2);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player2, 0, 0, null, null);
        harness.passBothPriorities();
        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlockedThisTurn()).isTrue();
    }

    private Permanent addEmissaryReady(Player player) {
        return addCreatureReady(player, new SoltariEmissary());
    }
}
