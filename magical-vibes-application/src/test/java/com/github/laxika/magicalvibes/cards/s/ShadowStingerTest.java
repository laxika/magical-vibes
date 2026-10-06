package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GuulDrazMucklord;
import com.github.laxika.magicalvibes.cards.z.ZulaportDuelist;
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

@CardUsed({ShadowStinger.class, ZulaportDuelist.class, GuulDrazMucklord.class})
class ShadowStingerTest extends BaseCardTest {

    @Test
    void tappingAnotherRogueGrantsDeathtouchUntilEndOfTurn() {
        Permanent stinger = addCreatureReady(player1, new ShadowStinger());
        Permanent rogue = addCreatureReady(player1, new ZulaportDuelist());

        harness.activateAbility(player1, battlefieldIndex(stinger), null, null);

        harness.passBothPriorities();

        assertThat(rogue.isTapped()).isTrue();
        assertThat(stinger.isTapped()).isFalse();
        assertThat(gqs.hasKeyword(gd, stinger, Keyword.DEATHTOUCH)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, stinger, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    void cannotTapItselfOrNonRogueToPayAbility() {
        Permanent stinger = addCreatureReady(player1, new ShadowStinger());
        addCreatureReady(player1, new GuulDrazMucklord());

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(stinger), null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void combatDamageMakesDamagedPlayerMillThreeCards() {
        Card first = new GuulDrazMucklord();
        Card second = new GuulDrazMucklord();
        Card third = new GuulDrazMucklord();
        harness.setLibrary(player2, List.of(first, second, third));
        addCreatureReady(player1, new ShadowStinger());

        declareAttackers(List.of(0));
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactlyInAnyOrder(first, second, third);
    }

    @Test
    void summoningSickRogueCanPayForSummoningSickTappedStinger() {
        Permanent stinger = harness.addToBattlefieldAndReturn(player1, new ShadowStinger());
        Permanent rogue = harness.addToBattlefieldAndReturn(player1, new ZulaportDuelist());
        stinger.setSummoningSick(true);
        rogue.setSummoningSick(true);
        stinger.tap();

        harness.activateAbility(player1, battlefieldIndex(stinger), null, null);

        assertThat(rogue.isTapped()).isTrue();
        assertThat(gqs.hasKeyword(gd, stinger, Keyword.DEATHTOUCH)).isFalse();

        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, stinger, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.hasKeyword(gd, rogue, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    void cannotTapAlreadyTappedRogueToPayAbility() {
        Permanent stinger = addCreatureReady(player1, new ShadowStinger());
        Permanent rogue = addCreatureReady(player1, new ZulaportDuelist());
        rogue.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(stinger), null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotTapOpponentsRogueToPayAbility() {
        Permanent stinger = addCreatureReady(player1, new ShadowStinger());
        Permanent rogue = addCreatureReady(player2, new ZulaportDuelist());

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(stinger), null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(rogue.isTapped()).isFalse();
    }

    @Test
    void millsOnlyRemainingCardsAndLeavesControllersLibraryAlone() {
        Card remaining = new GuulDrazMucklord();
        Card controllersCard = new GuulDrazMucklord();
        harness.setLibrary(player2, List.of(remaining));
        harness.setLibrary(player1, List.of(controllersCard));
        addCreatureReady(player1, new ShadowStinger());

        declareAttackers(List.of(0));
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(remaining);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(controllersCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
