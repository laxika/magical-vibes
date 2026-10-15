package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RaiseTheAlarm;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SplinterTheMentor.class, GrizzlyBears.class, RaiseTheAlarm.class})
class SplinterTheMentorTest extends BaseCardTest {

    @Test
    void createsMutagenWhenAnotherNontokenCreatureLeaves() {
        harness.addToBattlefield(player1, new SplinterTheMentor());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        removeFromBattlefield(creature);

        assertThat(findPermanents(player1, "Mutagen")).hasSize(1);
    }

    @Test
    void doesNotCreateMutagenWhenAnotherTokenCreatureLeaves() {
        harness.addToBattlefield(player1, new SplinterTheMentor());
        createSoldierTokens(player1);
        Permanent soldier = findPermanents(player1, "Soldier").getFirst();

        removeFromBattlefield(soldier);

        assertThat(findPermanents(player1, "Mutagen")).isEmpty();
    }

    @Test
    void createsMutagenWhenSplinterLeaves() {
        Permanent splinter = harness.addToBattlefieldAndReturn(player1, new SplinterTheMentor());

        removeFromBattlefield(splinter);

        assertThat(findPermanents(player1, "Mutagen")).hasSize(1);
    }

    @Test
    void createdTokenHasMutagenSubtype() {
        Permanent splinter = harness.addToBattlefieldAndReturn(player1, new SplinterTheMentor());

        removeFromBattlefield(splinter);

        assertThat(gqs.hasEffectiveSubtype(gd, findPermanent(player1, "Mutagen"), CardSubtype.MUTAGEN))
                .isTrue();
    }

    @Test
    void createsMutagenWhenAnotherCreatureReturnsToHand() {
        harness.addToBattlefield(player1, new SplinterTheMentor());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, creature));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Mutagen")).hasSize(1);
    }

    @Test
    void createsMutagenWhenSplinterIsExiled() {
        Permanent splinter = harness.addToBattlefieldAndReturn(player1, new SplinterTheMentor());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToExile(gd, splinter));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Mutagen")).hasSize(1);
    }

    @Test
    void doesNotTriggerForOpponentsCreature() {
        harness.addToBattlefield(player1, new SplinterTheMentor());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        removeFromBattlefield(creature);

        assertThat(findPermanents(player1, "Mutagen")).isEmpty();
        assertThat(findPermanents(player2, "Mutagen")).isEmpty();
    }

    @Test
    void triggersForEachNontokenCreatureLeavingSimultaneouslyWithSplinter() {
        Permanent splinter = harness.addToBattlefieldAndReturn(player1, new SplinterTheMentor());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.inMutationScope(() -> {
            var removal = harness.getPermanentRemovalService();
            removal.performSimultaneousRemovals(gd, List.of(splinter, creature), () -> {
                removal.removePermanentToGraveyard(gd, splinter);
                removal.removePermanentToGraveyard(gd, creature);
            });
        });
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Mutagen")).hasSize(2);
    }

    @Test
    void mutagenCanImmediatelyPutCounterOnOpponentsCreature() {
        Permanent splinter = harness.addToBattlefieldAndReturn(player1, new SplinterTheMentor());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        removeFromBattlefield(splinter);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        resolveAllTriggers();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(findPermanents(player1, "Mutagen")).isEmpty();
    }

    @Test
    void mutagenCannotBeActivatedOutsideMainPhase() {
        Permanent splinter = harness.addToBattlefieldAndReturn(player1, new SplinterTheMentor());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        removeFromBattlefield(splinter);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
        assertThat(findPermanents(player1, "Mutagen")).hasSize(1);
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private void createSoldierTokens(Player player) {
        harness.setHand(player, List.of(new RaiseTheAlarm()));
        harness.addMana(player, ManaColor.WHITE, 2);
        harness.castAndResolveInstant(player, 0);
    }

    private void removeFromBattlefield(Permanent permanent) {
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, permanent));
        resolveAllTriggers();
    }
}
