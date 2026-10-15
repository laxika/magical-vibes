package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.s.SnarlingWolf;
import com.github.laxika.magicalvibes.cards.s.StrionicResonator;
import com.github.laxika.magicalvibes.cards.m.MaliciousInvader;
import com.github.laxika.magicalvibes.cards.t.TravelingMinister;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({InnocentTraveler.class, MaliciousInvader.class, SnarlingWolf.class, StrionicResonator.class, TravelingMinister.class})
class InnocentTravelerTest extends BaseCardTest {

    @Test
    @DisplayName("Transforms when no opponent sacrifices a creature")
    void transformsWhenNoOpponentSacrifices() {
        Permanent traveler = harness.addToBattlefieldAndReturn(player1, new InnocentTraveler());

        triggerUpkeep(player1);

        assertThat(traveler.isTransformed()).isTrue();
    }

    @Test
    @DisplayName("An opponent who declines causes the Traveler to transform")
    void decliningCausesTransform() {
        Permanent traveler = harness.addToBattlefieldAndReturn(player1, new InnocentTraveler());
        harness.addToBattlefield(player2, new SnarlingWolf());

        triggerUpkeep(player1);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, false);

        assertThat(traveler.isTransformed()).isTrue();
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("An opponent who sacrifices a creature prevents the transform")
    void sacrificingPreventsTransform() {
        Permanent traveler = harness.addToBattlefieldAndReturn(player1, new InnocentTraveler());
        Permanent wolf = harness.addToBattlefieldAndReturn(player2, new SnarlingWolf());

        triggerUpkeep(player1);

        harness.handleMayAbilityChosen(player2, true);

        assertThat(traveler.isTransformed()).isFalse();
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(wolf);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(wolf.getCard());
    }

    @Test
    @DisplayName("Malicious Invader gets its conditional power bonus from an opposing Human")
    void backFaceGetsBonusFromOpposingHuman() {
        Permanent traveler = harness.addToBattlefieldAndReturn(player1, new InnocentTraveler());
        triggerUpkeep(player1);
        int powerWithoutHuman = gqs.getEffectivePower(gd, traveler);

        harness.addToBattlefield(player2, new TravelingMinister());

        assertThat(gqs.getEffectivePower(gd, traveler)).isEqualTo(powerWithoutHuman + 2);
    }

    @Test
    @DisplayName("The upkeep ability does not trigger during an opponent's upkeep")
    void doesNotTriggerDuringOpponentsUpkeep() {
        Permanent traveler = harness.addToBattlefieldAndReturn(player1, new InnocentTraveler());

        advanceToUpkeep(player2);

        assertThat(traveler.isTransformed()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The opponent chooses which creature to sacrifice")
    void opponentChoosesCreatureToSacrifice() {
        Permanent traveler = harness.addToBattlefieldAndReturn(player1, new InnocentTraveler());
        Permanent wolf = harness.addToBattlefieldAndReturn(player2, new SnarlingWolf());
        Permanent minister = harness.addToBattlefieldAndReturn(player2, new TravelingMinister());

        triggerUpkeep(player1);
        harness.handleMayAbilityChosen(player2, true);
        harness.handlePermanentChosen(player2, minister.getId());

        assertThat(traveler.isTransformed()).isFalse();
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(wolf);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(minister.getCard());
    }

    @Test
    @DisplayName("Only opposing Humans grant a single power bonus")
    void onlyOpposingHumansGrantBonus() {
        Permanent traveler = harness.addToBattlefieldAndReturn(player1, new InnocentTraveler());
        triggerUpkeep(player1);
        int basePower = gqs.getEffectivePower(gd, traveler);
        int baseToughness = gqs.getEffectiveToughness(gd, traveler);

        harness.addToBattlefield(player1, new TravelingMinister());
        harness.addToBattlefield(player2, new SnarlingWolf());
        assertThat(gqs.getEffectivePower(gd, traveler)).isEqualTo(basePower);

        harness.addToBattlefield(player2, new TravelingMinister());
        harness.addToBattlefield(player2, new TravelingMinister());
        assertThat(gqs.getEffectivePower(gd, traveler)).isEqualTo(basePower + 2);
        assertThat(gqs.getEffectiveToughness(gd, traveler)).isEqualTo(baseToughness);

        gd.playerBattlefields.get(player2.getId()).removeIf(p -> p.getCard() instanceof TravelingMinister);
        assertThat(gqs.getEffectivePower(gd, traveler)).isEqualTo(basePower);
    }

    @Test
    @DisplayName("Malicious Invader does not trigger the front face's upkeep ability")
    void transformedFaceDoesNotTriggerAtUpkeep() {
        Permanent traveler = harness.addToBattlefieldAndReturn(player1, new InnocentTraveler());
        triggerUpkeep(player1);

        advanceToUpkeep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(traveler.isTransformed()).isTrue();
    }

    @Test
    @DisplayName("Transforming grants flying for block legality")
    void transformedFaceCannotBeBlockedByGroundCreature() {
        Permanent traveler = harness.addToBattlefieldAndReturn(player1, new InnocentTraveler());
        Permanent wolf = harness.addToBattlefieldAndReturn(player2, new SnarlingWolf());
        assertThat(bls.canBlockAttacker(gd, wolf, traveler,
                gd.playerBattlefields.get(player2.getId()))).isTrue();

        triggerUpkeep(player1);
        harness.handleMayAbilityChosen(player2, false);

        assertThat(bls.canBlockAttacker(gd, wolf, traveler,
                gd.playerBattlefields.get(player2.getId()))).isFalse();
    }

    @Test
    @CardUsed(StrionicResonator.class)
    @DisplayName("A copied upkeep trigger cannot transform the source back")
    void copiedTriggerDoesNotTransformBack() {
        Permanent traveler = harness.addToBattlefieldAndReturn(player1, new InnocentTraveler());
        harness.addToBattlefield(player1, new StrionicResonator());
        advanceToUpkeep(player1);
        var triggerId = gd.stack.getFirst().getTargetableId();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 1, null, triggerId);
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(traveler.isTransformed()).isTrue();

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(traveler.isTransformed()).isTrue();
    }

    private void triggerUpkeep(Player activePlayer) {
        advanceToUpkeep(activePlayer);
        harness.passBothPriorities();
    }
}
