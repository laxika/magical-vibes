package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VectisSilencers.class})
class VectisSilencersTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving the ability grants deathtouch until end of turn")
    void resolvingGrantsDeathtouch() {
        Permanent silencers = addCreatureReady(player1, new VectisSilencers());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.hasKeyword(gd, silencers, Keyword.DEATHTOUCH)).isTrue();
    }

    @Test
    @DisplayName("Deathtouch wears off at end of turn cleanup")
    void deathtouchResetsAtEndOfTurn() {
        Permanent silencers = addCreatureReady(player1, new VectisSilencers());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, silencers, Keyword.DEATHTOUCH)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, silencers, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    @DisplayName("Cannot activate ability without enough mana")
    void cannotActivateWithoutMana() {
        addCreatureReady(player1, new VectisSilencers());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("A tapped, summoning-sick Silencers can activate its ability")
    void canActivateWhileTappedAndSummoningSick() {
        Permanent silencers = harness.addToBattlefieldAndReturn(player1, new VectisSilencers());
        silencers.setSummoningSick(true);
        silencers.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, silencers, Keyword.DEATHTOUCH)).isTrue();
        assertThat(silencers.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Deathtouch is granted only to the source and only when the ability resolves")
    void grantsOnlyToSourceOnResolution() {
        Permanent silencers = addCreatureReady(player1, new VectisSilencers());
        Permanent other = addCreatureReady(player1, new VectisSilencers());
        Permanent opposing = addCreatureReady(player2, new VectisSilencers());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(gqs.hasKeyword(gd, silencers, Keyword.DEATHTOUCH)).isFalse();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, silencers, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.hasKeyword(gd, other, Keyword.DEATHTOUCH)).isFalse();
        assertThat(gqs.hasKeyword(gd, opposing, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    @DisplayName("Granted deathtouch makes one combat damage lethal to a blocker")
    void deathtouchDestroysBlocker() {
        Permanent silencers = addCreatureReady(player1, new VectisSilencers());
        Permanent blocker = addCreatureReady(player2, new VectisSilencers());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        silencers.setAttacking(true);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        resolveCombat();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(blocker.getCard());
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(silencers);
    }
}
