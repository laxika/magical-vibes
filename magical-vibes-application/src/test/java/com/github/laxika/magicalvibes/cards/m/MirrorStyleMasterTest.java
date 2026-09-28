package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MirrorStyleMaster.class, GrizzlyBears.class})
class MirrorStyleMasterTest extends BaseCardTest {

    @Test
    @DisplayName("Backup grants the attack trigger to another creature")
    void backupGrantsAttackTriggerToAnotherCreature() {
        Permanent bears = addReadyCreature(player1, new GrizzlyBears());
        addReadyCreature(player2, new GrizzlyBears());
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
        Permanent master = addReadyCreature(player1, new MirrorStyleMaster());
        Permanent modified = addReadyCreature(player1, new GrizzlyBears());
        Permanent unmodified = addReadyCreature(player1, new GrizzlyBears());
        Permanent opposingModified = addReadyCreature(player2, new GrizzlyBears());
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
        Permanent master = addReadyCreature(player1, new MirrorStyleMaster());
        Permanent modified = addReadyCreature(player1, new GrizzlyBears());
        addReadyCreature(player2, new GrizzlyBears());
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

    private void castMirrorStyleMaster(Permanent target) {
        harness.setHand(player1, List.of(new MirrorStyleMaster()));
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.RED, 2);
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.COLORLESS, 4);
        harness.castCreature(player1, 0, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private Permanent addReadyCreature(com.github.laxika.magicalvibes.model.Player player,
                                       com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
