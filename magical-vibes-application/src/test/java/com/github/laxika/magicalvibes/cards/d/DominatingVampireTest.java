package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.b.BloodtitheHarvester;
import com.github.laxika.magicalvibes.cards.p.ParishBladeTrainee;
import com.github.laxika.magicalvibes.cards.t.TravelingMinister;
import com.github.laxika.magicalvibes.cards.v.VoldarenEpicure;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DominatingVampire.class, BloodtitheHarvester.class, ParishBladeTrainee.class, VoldarenEpicure.class,
        TravelingMinister.class})
class DominatingVampireTest extends BaseCardTest {

    @Test
    @DisplayName("Gains control of, untaps, and grants haste to a creature within the Vampire count")
    void resolvesEtbEffect() {
        Permanent target = addCreatureReady(player2, new ParishBladeTrainee());
        target.tap();
        harness.addToBattlefield(player1, new VoldarenEpicure());

        castDominatingVampire(target);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(target.isTapped()).isFalse();
        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("The temporary control and haste expire at cleanup")
    void controlAndHasteExpireAtCleanup() {
        Permanent target = addCreatureReady(player2, new TravelingMinister());

        castDominatingVampire(target);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(target);
        assertThat(target.hasKeyword(Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Counts only Vampires controlled by the creature's controller")
    void doesNotCountOpponentsVampires() {
        Permanent target = addCreatureReady(player2, new ParishBladeTrainee());
        harness.addToBattlefield(player2, new BloodtitheHarvester());
        harness.addToBattlefield(player2, new BloodtitheHarvester());

        harness.castFromHand(player1, new DominatingVampire(), "{1}{R}{R}");
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The entering Vampire counts itself and can target a creature you already control")
    void canUntapOwnCreatureWithOnlyTheEnteringVampire() {
        Permanent target = addCreatureReady(player1, new TravelingMinister());
        target.tap();

        castDominatingVampire(target);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        assertThat(target.isTapped()).isFalse();
        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Losing a Vampire before resolution makes an oversized target illegal for the entire ability")
    void vampireCountIsCheckedAgainOnResolution() {
        Permanent vampire = harness.addToBattlefieldAndReturn(player1, new VoldarenEpicure());
        Permanent target = addCreatureReady(player2, new ParishBladeTrainee());
        target.tap();
        harness.castFromHand(player1, new DominatingVampire(), "{1}{R}{R}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());

        vampire.setMarkedDamage(1);
        harness.runStateBasedActions();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(vampire, target);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(target.isTapped()).isTrue();
        assertThat(target.hasKeyword(Keyword.HASTE)).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Losing Vampires after resolution does not end temporary control or haste")
    void controlDoesNotRequireMaintainingVampireCount() {
        Permanent vampire = harness.addToBattlefieldAndReturn(player1, new VoldarenEpicure());
        Permanent target = addCreatureReady(player2, new ParishBladeTrainee());

        castDominatingVampire(target);
        vampire.setMarkedDamage(1);
        harness.runStateBasedActions();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target).doesNotContain(vampire);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("The ability still resolves after its source dies if the target remains within the Vampire count")
    void sourceNeedNotRemainOnBattlefield() {
        harness.addToBattlefield(player1, new VoldarenEpicure());
        Permanent target = addCreatureReady(player2, new TravelingMinister());
        target.tap();
        harness.castFromHand(player1, new DominatingVampire(), "{1}{R}{R}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        Permanent source = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof DominatingVampire)
                .findFirst().orElseThrow();

        source.setMarkedDamage(3);
        harness.runStateBasedActions();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target).doesNotContain(source);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(target.isTapped()).isFalse();
        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();
    }

    private void castDominatingVampire(Permanent target) {
        harness.castFromHand(player1, new DominatingVampire(), "{1}{R}{R}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
    }
}
