package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({StonehornChanter.class})
class StonehornChanterTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving the ability grants both vigilance and lifelink")
    void resolvingGrantsVigilanceAndLifelink() {
        Permanent chanter = addCreatureReady(player1, new StonehornChanter());
        addActivationMana(player1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.hasKeyword(gd, chanter, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, chanter, Keyword.LIFELINK)).isTrue();
    }

    @Test
    @DisplayName("Granted keywords wear off at end of turn")
    void keywordsWearOffAtEndOfTurn() {
        Permanent chanter = addCreatureReady(player1, new StonehornChanter());
        addActivationMana(player1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, chanter, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.hasKeyword(gd, chanter, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Cannot activate the ability without enough mana")
    void cannotActivateWithoutMana() {
        addCreatureReady(player1, new StonehornChanter());
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Activating the ability does not tap Stonehorn Chanter")
    void activatingDoesNotTap() {
        Permanent chanter = addCreatureReady(player1, new StonehornChanter());
        addActivationMana(player1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(chanter.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Keywords are granted only when the ability resolves and only to its source")
    void keywordsWaitForResolutionAndAffectOnlySource() {
        Permanent chanter = addCreatureReady(player1, new StonehornChanter());
        Permanent other = addCreatureReady(player1, new StonehornChanter());
        Permanent opponent = addCreatureReady(player2, new StonehornChanter());
        addActivationMana(player1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gqs.hasKeyword(gd, chanter, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.hasKeyword(gd, chanter, Keyword.LIFELINK)).isFalse();

        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, chanter, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, chanter, Keyword.LIFELINK)).isTrue();
        for (Permanent unaffected : List.of(other, opponent)) {
            assertThat(gqs.hasKeyword(gd, unaffected, Keyword.VIGILANCE)).isFalse();
            assertThat(gqs.hasKeyword(gd, unaffected, Keyword.LIFELINK)).isFalse();
        }
    }

    @Test
    @DisplayName("A tapped, summoning-sick Chanter can activate without untapping")
    void tappedSummoningSickChanterCanActivate() {
        Permanent chanter = harness.addToBattlefieldAndReturn(player1, new StonehornChanter());
        chanter.setSummoningSick(true);
        chanter.tap();
        addActivationMana(player1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(chanter.isTapped()).isTrue();
        assertThat(gqs.hasKeyword(gd, chanter, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, chanter, Keyword.LIFELINK)).isTrue();
    }

    @Test
    @DisplayName("Repeated activation keeps attacks untapped and does not multiply lifelink")
    void repeatedActivationGrantsVigilanceAndNonstackingLifelinkInCombat() {
        Permanent chanter = addCreatureReady(player1, new StonehornChanter());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 10);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        declareAttackers(List.of(0));
        resolveCombat();

        assertThat(chanter.isTapped()).isFalse();
        harness.assertLife(player1, 24);
        harness.assertLife(player2, 16);
    }

    private void addActivationMana(Player player) {
        harness.addMana(player, ManaColor.WHITE, 1);
        harness.addMana(player, ManaColor.COLORLESS, 5);
    }
}
