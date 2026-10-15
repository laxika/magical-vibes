package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BearDown;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.r.RootriderFaun;
import com.github.laxika.magicalvibes.cards.u.UpTheBeanstalk;
import com.github.laxika.magicalvibes.cards.z.ZuranOrb;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StormkeldVanguard.class, BearDown.class, ZuranOrb.class, GrizzlyBears.class,
        HillGiant.class, UpTheBeanstalk.class, RootriderFaun.class})
class StormkeldVanguardTest extends BaseCardTest {

    @Test
    void adventureDestroysTargetArtifactAndExilesTheCard() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ZuranOrb());
        StormkeldVanguard card = new StormkeldVanguard();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAdventure(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Zuran Orb");
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
        assertThat(gd.exilePlayPermissions.get(card.getId())).isEqualTo(player1.getId());
    }

    @Test
    void adventureCannotTargetCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new StormkeldVanguard()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castAdventure(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void creatureFaceCanBeCastFromExileAfterAdventure() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ZuranOrb());
        StormkeldVanguard card = new StormkeldVanguard();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAdventure(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castFromExile(player1, card.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Stormkeld Vanguard");
        assertThat(gd.findExiledCard(card.getId())).isNull();
    }

    @Test
    void cannotBeBlockedByCreatureWithPowerTwo() {
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        Permanent vanguard = addCreatureReady(player1, new StormkeldVanguard());
        vanguard.setAttacking(true);

        prepareDeclareBlockers();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(vanguard);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIdx, attackerIdx))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canBeBlockedByCreatureWithPowerThree() {
        Permanent blocker = addCreatureReady(player2, new HillGiant());
        Permanent vanguard = addCreatureReady(player1, new StormkeldVanguard());
        vanguard.setAttacking(true);

        prepareDeclareBlockers();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(vanguard);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    void adventureDestroysAnEnchantment() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new UpTheBeanstalk());
        StormkeldVanguard card = new StormkeldVanguard();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAdventure(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Up the Beanstalk");
        harness.assertInGraveyard(player2, "Up the Beanstalk");
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
    }

    @Test
    void adventureCanDestroyItsControllersEnchantment() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new UpTheBeanstalk());
        harness.setHand(player1, List.of(new StormkeldVanguard()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAdventure(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Up the Beanstalk");
        harness.assertInGraveyard(player1, "Up the Beanstalk");
    }

    @Test
    void adventureWithMissingTargetGoesToGraveyardWithoutExilePermission() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new UpTheBeanstalk());
        StormkeldVanguard card = new StormkeldVanguard();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castAdventure(player1, 0, target.getId());

        gd.playerBattlefields.get(player2.getId()).remove(target);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Stormkeld Vanguard");
        assertThat(gd.findExiledCard(card.getId())).isNull();
        assertThat(gd.exilePlayPermissions).doesNotContainKey(card.getId());
    }

    @Test
    void creatureWithPrintedPowerOneCanBlockAfterItsPowerIncreases() {
        Permanent blocker = addCreatureReady(player2, new RootriderFaun());
        blocker.setPowerModifier(2);
        Permanent vanguard = addCreatureReady(player1, new StormkeldVanguard());
        vanguard.setAttacking(true);
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    void creatureWithPrintedPowerSixCannotBlockAfterItsPowerDecreases() {
        Permanent blocker = addCreatureReady(player2, new StormkeldVanguard());
        blocker.setPowerModifier(-4);
        Permanent vanguard = addCreatureReady(player1, new StormkeldVanguard());
        vanguard.setAttacking(true);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotBeBlockedByCreatureWithPowerLessThanTwo() {
        addCreatureReady(player2, new RootriderFaun());
        Permanent vanguard = addCreatureReady(player1, new StormkeldVanguard());
        vanguard.setAttacking(true);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }
}
