package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.e.Ephemerate;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.OrnithopterOfParadise;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.u.UnboundedPotential;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ConstableOfTheRealm.class, GrizzlyBears.class, Plains.class,
        OrnithopterOfParadise.class, UnboundedPotential.class, Ephemerate.class})
class ConstableOfTheRealmTest extends BaseCardTest {

    @Test
    @DisplayName("Renown 2 puts two counters on Constable and exiles another nonland permanent")
    void renownExilesAnotherNonlandPermanentUntilConstableLeaves() {
        Permanent constable = addCreatureReady(player1, new ConstableOfTheRealm());
        Permanent plains = harness.addToBattlefieldAndReturn(player1, new Plains());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(player1, List.of(0));
        gs.declareBlockers(gd, player2, List.of());
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(constable.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(constable.isRenowned()).isTrue();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, constable.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, plains.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.findExiledCard(target.getOriginalCard().getId())).isNotNull();

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, constable));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    void countersFromSpellsCanExileOwnArtifactAndTriggerAgain() {
        Permanent constable = harness.addToBattlefieldAndReturn(player1, new ConstableOfTheRealm());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new OrnithopterOfParadise());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new OrnithopterOfParadise());

        putCounterOn(constable);
        harness.handlePermanentChosen(player1, first.getId());
        resolveAllTriggers();
        assertThat(gd.findExiledCard(first.getOriginalCard().getId())).isNotNull();
        assertThat(constable.isRenowned()).isFalse();

        putCounterOn(constable);
        harness.handlePermanentChosen(player1, second.getId());
        resolveAllTriggers();
        assertThat(constable.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.findExiledCard(second.getOriginalCard().getId())).isNotNull();

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, constable));
        assertThat(gd.findExiledCard(first.getOriginalCard().getId())).isNull();
        assertThat(gd.findExiledCard(second.getOriginalCard().getId())).isNull();
        harness.assertOnBattlefield(player1, "Ornithopter of Paradise");
        harness.assertOnBattlefield(player2, "Ornithopter of Paradise");
    }

    @Test
    void mayChooseNoTargetDespiteHavingALegalTarget() {
        Permanent constable = harness.addToBattlefieldAndReturn(player1, new ConstableOfTheRealm());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new OrnithopterOfParadise());

        putCounterOn(constable);
        harness.handlePermanentChosen(player1, player1.getId());
        resolveAllTriggers();

        assertThat(constable.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.findExiledCard(target.getOriginalCard().getId())).isNull();
        harness.assertOnBattlefield(player2, "Ornithopter of Paradise");
    }

    @Test
    void counterPlacementWithNoOtherPermanentDoesNotRequireATarget() {
        Permanent constable = harness.addToBattlefieldAndReturn(player1, new ConstableOfTheRealm());

        putCounterOn(constable);
        resolveAllTriggers();

        assertThat(constable.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertOnBattlefield(player1, "Constable of the Realm");
    }

    @Test
    void leavingBeforeExileResolvesPreventsExile() {
        Permanent constable = harness.addToBattlefieldAndReturn(player1, new ConstableOfTheRealm());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new OrnithopterOfParadise());

        putCounterOn(constable);
        harness.withAutoStop(gd.currentStep, () -> {
            harness.handlePermanentChosen(player1, target.getId());
            harness.inMutationScope(() -> harness.getPermanentRemovalService()
                    .removePermanentToGraveyard(gd, constable));
        });
        resolveAllTriggers();

        assertThat(gd.findExiledCard(target.getOriginalCard().getId())).isNull();
        harness.assertOnBattlefield(player2, "Ornithopter of Paradise");
    }

    @Test
    void leavingAndReturningBeforeExileResolvesStillPreventsExile() {
        Permanent constable = harness.addToBattlefieldAndReturn(player1, new ConstableOfTheRealm());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new OrnithopterOfParadise());

        putCounterOn(constable);
        harness.setHand(player1, List.of(new Ephemerate()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.withAutoStop(gd.currentStep, () -> {
            harness.handlePermanentChosen(player1, target.getId());
            harness.castInstant(player1, 0, constable.getId());
        });
        resolveAllTriggers();

        Permanent returned = findPermanent(player1, "Constable of the Realm");
        assertThat(returned.getId()).isNotEqualTo(constable.getId());
        assertThat(gd.findExiledCard(target.getOriginalCard().getId())).isNull();
        harness.assertOnBattlefield(player2, "Ornithopter of Paradise");
    }

    private void putCounterOn(Permanent constable) {
        harness.setHand(player1, List.of(new UnboundedPotential()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castModalInstantWithModes(player1, 0, 1, 2, new int[]{0}, List.of(constable.getId()));
        harness.passBothPriorities();
        resolveAllTriggers();
    }
}
