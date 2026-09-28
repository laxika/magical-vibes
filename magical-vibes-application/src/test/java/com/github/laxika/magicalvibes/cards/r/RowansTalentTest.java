package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.c.ChandraNovicePyromancer;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JaceBeleren;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RowansTalent.class, JaceBeleren.class, ChandraNovicePyromancer.class,
        GrizzlyBears.class, AirElemental.class})
class RowansTalentTest extends BaseCardTest {

    @Test
    void grantedLoyaltyAbilityBoostsCreatureAndCopyCanRetarget() {
        Permanent jace = addReadyPlaneswalker(player1, new JaceBeleren(), 4);
        Permanent firstBear = addCreatureReady(player1, new GrizzlyBears());
        Permanent secondBear = addCreatureReady(player1, new GrizzlyBears());
        attachTalent(jace);

        int grantedAbilityIndex = gs.getEffectiveActivatedAbilities(gd, jace).size() - 1;
        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(jace),
                grantedAbilityIndex,
                null,
                firstBear.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, secondBear.getId());
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, firstBear)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, secondBear)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, firstBear, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, firstBear, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, secondBear, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, secondBear, Keyword.TRAMPLE)).isTrue();
        assertThat(jace.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
    }

    @Test
    void copiesOnlyTheEnchantedPlaneswalkersLoyaltyAbility() {
        Permanent jace = addReadyPlaneswalker(player1, new JaceBeleren(), 4);
        Permanent chandra = addReadyPlaneswalker(player1, new ChandraNovicePyromancer(), 4);
        attachTalent(jace);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        Card firstDraw = new GrizzlyBears();
        Card secondDraw = new GrizzlyBears();
        harness.setLibrary(player1, List.of(firstDraw, secondDraw));
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));

        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(jace), 0, null, null);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstDraw, secondDraw);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(jace.getCounterCount(CounterType.LOYALTY)).isEqualTo(6);

        Permanent elemental = addCreatureReady(player1, new AirElemental());
        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(chandra), 0, null, null);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, elemental)).isEqualTo(6);
    }

    private void attachTalent(Permanent planeswalker) {
        Permanent talent = harness.addToBattlefieldAndReturn(player1, new RowansTalent());
        talent.setAttachedTo(planeswalker.getId());
    }

    private Permanent addReadyPlaneswalker(Player player, Card card, int loyalty) {
        Permanent planeswalker = new Permanent(card);
        planeswalker.setCounterCount(CounterType.LOYALTY, loyalty);
        planeswalker.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(planeswalker);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return planeswalker;
    }
}
