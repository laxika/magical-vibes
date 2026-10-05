package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.w.WanderingWolf;
import com.github.laxika.magicalvibes.cards.p.PeelFromReality;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NightshadePeddler.class, WanderingWolf.class, PeelFromReality.class})
class NightshadePeddlerTest extends BaseCardTest {

    private Permanent castAndPairWithWolf() {
        Permanent wolf = harness.addToBattlefieldAndReturn(player1, new WanderingWolf());
        harness.castFromHand(player1, new NightshadePeddler(), "{1}{G}");
        harness.passBothPriorities(); // resolve spell -> soulbond may on stack
        harness.passBothPriorities(); // resolve may -> prompt
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, wolf.getId());
        return wolf;
    }

    @Test
    void enteringAloneDoesNotTriggerSoulbond() {
        harness.castFromHand(player1, new NightshadePeddler(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(findPeddler().getPairedWithId()).isNull();
    }

    @Test
    void pairsWithCreatureEnteringLater() {
        Permanent peddler = harness.addToBattlefieldAndReturn(player1, new NightshadePeddler());
        harness.castFromHand(player1, new WanderingWolf(), "{1}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        Permanent wolf = findPermanent(player1, "Wandering Wolf");
        assertThat(peddler.getPairedWithId()).isEqualTo(wolf.getId());
        assertThat(wolf.getPairedWithId()).isEqualTo(peddler.getId());
        assertThat(gqs.hasKeyword(gd, peddler, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.hasKeyword(gd, wolf, Keyword.DEATHTOUCH)).isTrue();
    }

    @Test
    void decliningPairWithEnteringCreatureLeavesBothUnpaired() {
        Permanent peddler = harness.addToBattlefieldAndReturn(player1, new NightshadePeddler());
        harness.castFromHand(player1, new WanderingWolf(), "{1}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        Permanent wolf = findPermanent(player1, "Wandering Wolf");
        assertThat(peddler.getPairedWithId()).isNull();
        assertThat(wolf.getPairedWithId()).isNull();
        assertThat(gqs.hasKeyword(gd, peddler, Keyword.DEATHTOUCH)).isFalse();
        assertThat(gqs.hasKeyword(gd, wolf, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    void alreadyPairedPeddlerDoesNotTriggerForAnotherCreature() {
        Permanent wolf = castAndPairWithWolf();
        Permanent peddler = findPeddler();
        harness.castFromHand(player1, new WanderingWolf(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(peddler.getPairedWithId()).isEqualTo(wolf.getId());
        assertThat(wolf.getPairedWithId()).isEqualTo(peddler.getId());
    }

    @Test
    void opponentCreatureEnteringDoesNotTriggerSoulbond() {
        Permanent peddler = harness.addToBattlefieldAndReturn(player1, new NightshadePeddler());
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new WanderingWolf(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(peddler.getPairedWithId()).isNull();
    }

    @Test
    void partnerLeavingRemovesDeathtouch() {
        Permanent wolf = castAndPairWithWolf();
        Permanent peddler = findPeddler();
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new WanderingWolf());
        harness.setHand(player1, List.of(new PeelFromReality()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player1, 0, List.of(wolf.getId(), opponent.getId()));

        harness.assertInHand(player1, "Wandering Wolf");
        assertThat(peddler.getPairedWithId()).isNull();
        assertThat(gqs.hasKeyword(gd, peddler, Keyword.DEATHTOUCH)).isFalse();
    }

    private Permanent findPeddler() {
        return findPermanent(player1, "Nightshade Peddler");
    }

    @Test
    @DisplayName("Soulbond ETB pairs Nightshade Peddler with another unpaired creature")
    void soulbondPairsOnEnter() {
        Permanent wolf = castAndPairWithWolf();
        Permanent peddler = findPeddler();

        assertThat(peddler.getPairedWithId()).isEqualTo(wolf.getId());
        assertThat(wolf.getPairedWithId()).isEqualTo(peddler.getId());
    }

    @Test
    @DisplayName("While paired, both creatures have deathtouch")
    void pairedBothHaveDeathtouch() {
        Permanent wolf = castAndPairWithWolf();
        Permanent peddler = findPeddler();

        assertThat(gqs.hasKeyword(gd, peddler, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.hasKeyword(gd, wolf, Keyword.DEATHTOUCH)).isTrue();
    }

    @Test
    @DisplayName("Unpaired Nightshade Peddler does not have deathtouch")
    void unpairedHasNoDeathtouch() {
        harness.addToBattlefield(player1, new NightshadePeddler());
        Permanent peddler = findPeddler();

        assertThat(peddler.getPairedWithId()).isNull();
        assertThat(gqs.hasKeyword(gd, peddler, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    @DisplayName("Declining soulbond leaves both unpaired and without deathtouch")
    void decliningLeavesUnpairedWithoutDeathtouch() {
        Permanent wolf = harness.addToBattlefieldAndReturn(player1, new WanderingWolf());
        harness.castFromHand(player1, new NightshadePeddler(), "{1}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        Permanent peddler = findPeddler();
        assertThat(peddler.getPairedWithId()).isNull();
        assertThat(wolf.getPairedWithId()).isNull();
        assertThat(gqs.hasKeyword(gd, peddler, Keyword.DEATHTOUCH)).isFalse();
        assertThat(gqs.hasKeyword(gd, wolf, Keyword.DEATHTOUCH)).isFalse();
    }
}
