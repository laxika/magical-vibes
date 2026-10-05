package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.e.ElspethSunsChampion;
import com.github.laxika.magicalvibes.cards.n.NestingDovehawk;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MirrorStyleMaster.class, GrizzlyBears.class, NestingDovehawk.class, ElspethSunsChampion.class})
class MirrorStyleMasterTest extends BaseCardTest {

    @Test
    @DisplayName("Backup grants the attack trigger to another creature")
    void backupGrantsAttackTriggerToAnotherCreature() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        castMirrorStyleMaster(bears);

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);

        declareAttackers(player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(bears)));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Grizzly Bears"))
                .filteredOn(permanent -> permanent.getCard().isToken()).hasSize(1);
    }

    @Test
    @DisplayName("Copies each attacking modified creature you control")
    void copiesEachAttackingModifiedCreatureYouControl() {
        Permanent master = addCreatureReady(player1, new MirrorStyleMaster());
        Permanent modified = addCreatureReady(player1, new GrizzlyBears());
        Permanent unmodified = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposingModified = addCreatureReady(player2, new GrizzlyBears());
        modified.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        opposingModified.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        declareAttackers(player1, List.of(
                gd.playerBattlefields.get(player1.getId()).indexOf(master),
                gd.playerBattlefields.get(player1.getId()).indexOf(modified),
                gd.playerBattlefields.get(player1.getId()).indexOf(unmodified)));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Grizzly Bears"))
                .filteredOn(permanent -> permanent.getCard().isToken()).hasSize(1);
    }

    @Test
    @DisplayName("Copies are exiled at end of combat")
    void copiesAreExiledAtEndOfCombat() {
        Permanent master = addCreatureReady(player1, new MirrorStyleMaster());
        Permanent modified = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        modified.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        declareAttackers(player1, List.of(
                gd.playerBattlefields.get(player1.getId()).indexOf(master),
                gd.playerBattlefields.get(player1.getId()).indexOf(modified)));
        resolveAllTriggers();
        assertThat(findPermanents(player1, "Grizzly Bears"))
                .filteredOn(permanent -> permanent.getCard().isToken()).hasSize(1);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());

        assertThat(findPermanents(player1, "Grizzly Bears"))
                .filteredOn(permanent -> permanent.getCard().isToken()).isEmpty();
    }

    @Test
    @DisplayName("Token copies enter together and see each other's entry")
    void tokenCopiesEnterSimultaneously() {
        addCreatureReady(player1, new MirrorStyleMaster());
        Permanent first = addCreatureReady(player1, new NestingDovehawk());
        Permanent second = addCreatureReady(player1, new NestingDovehawk());
        addCreatureReady(player2, new GrizzlyBears());
        first.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        second.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        declareAttackers(player1, List.of(0, 1, 2));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Nesting Dovehawk"))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .hasSize(2)
                .allSatisfy(token ->
                        assertThat(token.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2));
    }

    @Test
    @DisplayName("The controller chooses what each attacking token attacks")
    void tokenAttackTargetCanDifferFromOriginal() {
        addCreatureReady(player1, new MirrorStyleMaster());
        Permanent modified = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        Permanent elspeth = new Permanent(new ElspethSunsChampion());
        elspeth.setCounterCount(CounterType.LOYALTY, 4);
        gd.playerBattlefields.get(player2.getId()).add(elspeth);
        modified.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        declareAttackers(player1, List.of(0, 1));
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
    }

    @Test
    @DisplayName("Copies enter tapped and attacking without copying counters")
    void copiesDoNotInheritModifications() {
        addCreatureReady(player1, new MirrorStyleMaster());
        Permanent modified = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        modified.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(player1, List.of(0, 1));
            resolveAllTriggers();
        });

        assertThat(findPermanents(player1, "Grizzly Bears"))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .hasSize(1)
                .allSatisfy(token -> {
                    assertThat(token.isTapped()).isTrue();
                    assertThat(token.isAttacking()).isTrue();
                    assertThat(token.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
                });
    }

    private void castMirrorStyleMaster(Permanent target) {
        harness.setHand(player1, List.of(new MirrorStyleMaster()));
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.RED, 2);
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.COLORLESS, 4);
        harness.castCreature(player1, 0, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

}
