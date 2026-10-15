package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.PeelFromReality;
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

@CardUsed({NearheathPilgrim.class, GrizzlyBears.class, PeelFromReality.class})
class NearheathPilgrimTest extends BaseCardTest {

    private Permanent castAndPairWithBears() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.castFromHand(player1, new NearheathPilgrim(), "{1}{W}");
        harness.passBothPriorities(); // resolve spell -> soulbond may on stack
        harness.passBothPriorities(); // resolve may -> prompt
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, bears.getId());
        return bears;
    }

    private Permanent findPilgrim() {
        return findPermanent(player1, "Nearheath Pilgrim");
    }

    @Test
    @DisplayName("Soulbond ETB pairs Nearheath Pilgrim with another unpaired creature")
    void soulbondPairsOnEnter() {
        Permanent bears = castAndPairWithBears();
        Permanent pilgrim = findPilgrim();

        assertThat(pilgrim.getPairedWithId()).isEqualTo(bears.getId());
        assertThat(bears.getPairedWithId()).isEqualTo(pilgrim.getId());
    }

    @Test
    @DisplayName("While paired, both creatures have lifelink")
    void pairedBothHaveLifelink() {
        Permanent bears = castAndPairWithBears();
        Permanent pilgrim = findPilgrim();

        assertThat(gqs.hasKeyword(gd, pilgrim, Keyword.LIFELINK)).isTrue();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.LIFELINK)).isTrue();
    }

    @Test
    @DisplayName("Unpaired Nearheath Pilgrim does not have lifelink")
    void unpairedHasNoLifelink() {
        harness.addToBattlefield(player1, new NearheathPilgrim());
        Permanent pilgrim = findPilgrim();

        assertThat(pilgrim.getPairedWithId()).isNull();
        assertThat(gqs.hasKeyword(gd, pilgrim, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Declining soulbond leaves both unpaired and without lifelink")
    void decliningLeavesUnpairedWithoutLifelink() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.castFromHand(player1, new NearheathPilgrim(), "{1}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        Permanent pilgrim = findPilgrim();
        assertThat(pilgrim.getPairedWithId()).isNull();
        assertThat(bears.getPairedWithId()).isNull();
        assertThat(gqs.hasKeyword(gd, pilgrim, Keyword.LIFELINK)).isFalse();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.LIFELINK)).isFalse();
    }

    @Test
    void enteringAloneDoesNotTriggerSoulbond() {
        harness.castFromHand(player1, new NearheathPilgrim(), "{1}{W}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(findPilgrim().getPairedWithId()).isNull();
    }

    @Test
    void pairsWithAnotherCreatureEnteringLater() {
        Permanent pilgrim = harness.addToBattlefieldAndReturn(player1, new NearheathPilgrim());
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(pilgrim.getPairedWithId()).isEqualTo(bears.getId());
        assertThat(bears.getPairedWithId()).isEqualTo(pilgrim.getId());
        assertThat(gqs.hasKeyword(gd, pilgrim, Keyword.LIFELINK)).isTrue();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.LIFELINK)).isTrue();
    }

    @Test
    void decliningPairingWithEnteringCreatureLeavesBothWithoutLifelink() {
        Permanent pilgrim = harness.addToBattlefieldAndReturn(player1, new NearheathPilgrim());
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(pilgrim.getPairedWithId()).isNull();
        assertThat(bears.getPairedWithId()).isNull();
        assertThat(gqs.hasKeyword(gd, pilgrim, Keyword.LIFELINK)).isFalse();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.LIFELINK)).isFalse();
    }

    @Test
    void bothPairedCreaturesGainLifeFromCombatDamage() {
        Permanent bears = castAndPairWithBears();
        Permanent pilgrim = findPilgrim();
        bears.setSummoningSick(false);
        pilgrim.setSummoningSick(false);
        bears.setAttacking(true);
        pilgrim.setAttacking(true);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.resolveCombatDamage();

        harness.assertLife(player1, 24);
        harness.assertLife(player2, 16);
    }

    @Test
    void partnerLeavingBreaksPairAndRemovesLifelink() {
        Permanent bears = castAndPairWithBears();
        Permanent pilgrim = findPilgrim();
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new PeelFromReality()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player1, 0, List.of(bears.getId(), opponent.getId()));

        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(pilgrim.getPairedWithId()).isNull();
        assertThat(gqs.hasKeyword(gd, pilgrim, Keyword.LIFELINK)).isFalse();
    }
}
