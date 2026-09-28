package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RasaadMonkOfSelNe.class, GrizzlyBears.class, Mountain.class, Plains.class, DoomBlade.class, Shock.class})
class RasaadMonkOfSelNeTest extends BaseCardTest {

    @Test
    void exilesAnOpponentsCreatureAndReturnsItWhenRasaadLeaves() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new RasaadMonkOfSelNe()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castCreature(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(target.getCard().getId())).isNotNull();
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);

        Permanent rasaad = findPermanent(player1, "Rasaad, Monk of Selûne");
        harness.forceActivePlayer(player1);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, rasaad.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(target.getCard().getId())).isNull();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getCard().getName().equals("Grizzly Bears"));
    }

    @Test
    void radiantFaceMakesTheExiledCreatureACreaturelessOneOne() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new RasaadMonkOfSelNe(), new Plains()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castCreature(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        Card exiled = gd.findExiledCard(target.getCard().getId()).card();
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.handlePermanentChosen(player1, exiled.getId());
        harness.passBothPriorities();

        Card modified = gd.findExiledCard(exiled.getId()).card();
        assertThat(modified.getPower()).isEqualTo(1);
        assertThat(modified.getToughness()).isEqualTo(1);
        assertThat(modified.getCardText()).isEmpty();
        assertThat(modified.getActivatedAbilities()).isEmpty();
    }

    @Test
    void warriorFaceCreatesThreeSoldiers() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new RasaadMonkOfSelNe(), new Mountain()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castCreature(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.activateAbility(player1, 0, 3, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Rasaad, Warrior Monk")).isNotNull();
        Permanent warrior = findPermanent(player1, "Rasaad, Warrior Monk");
        harness.forceActivePlayer(player1);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new DoomBlade()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, warrior.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().getName().equals("Soldier"))
                .hasSize(3);
    }
}
