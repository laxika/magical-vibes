package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DeathcultRogue;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ShadowStinger.class, DeathcultRogue.class, GrizzlyBears.class})
class ShadowStingerTest extends BaseCardTest {

    @Test
    void tappingAnotherRogueGrantsDeathtouchUntilEndOfTurn() {
        Permanent stinger = addCreatureReady(player1, new ShadowStinger());
        Permanent rogue = addCreatureReady(player1, new DeathcultRogue());

        harness.activateAbility(player1, battlefieldIndex(stinger), null, null);


        harness.passBothPriorities();

        assertThat(rogue.isTapped()).isTrue();
        assertThat(stinger.isTapped()).isFalse();
        assertThat(gqs.hasKeyword(gd, stinger, Keyword.DEATHTOUCH)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, stinger, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    void cannotTapItselfOrNonRogueToPayAbility() {
        Permanent stinger = addCreatureReady(player1, new ShadowStinger());
        addCreatureReady(player1, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(stinger), null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void combatDamageMakesDamagedPlayerMillThreeCards() {
        Card first = new GrizzlyBears();
        Card second = new GrizzlyBears();
        Card third = new GrizzlyBears();
        harness.setLibrary(player2, List.of(first, second, third));
        addCreatureReady(player1, new ShadowStinger());

        declareAttackers(List.of(0));
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactlyInAnyOrder(first, second, third);
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
