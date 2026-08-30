package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VindictiveMob.class, GrizzlyBears.class})
class VindictiveMobTest extends BaseCardTest {

    @Test
    @DisplayName("ETB sacrifices Vindictive Mob itself when it is the only creature")
    void etbSacrificesItselfWhenOnlyCreature() {
        castVindictiveMob();

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Vindictive Mob");
        harness.assertInGraveyard(player1, "Vindictive Mob");
    }

    @Test
    @DisplayName("ETB lets the controller sacrifice another creature")
    void etbSacrificesAnotherCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        castVindictiveMob();

        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        harness.handlePermanentChosen(player1, bears.getId());

        harness.assertOnBattlefield(player1, "Vindictive Mob");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Vindictive Mob can't be blocked by a Saproling")
    void cannotBeBlockedBySaproling() {
        Permanent attacker = attackingMob();
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        Card saprolingCard = new GrizzlyBears();
        saprolingCard.setSubtypes(List.of(CardSubtype.SAPROLING));
        Permanent blocker = new Permanent(saprolingCard);
        blocker.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Vindictive Mob can be blocked by a non-Saproling creature")
    void canBeBlockedByNonSaprolingCreature() {
        Permanent attacker = attackingMob();
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        Permanent blocker = new Permanent(new GrizzlyBears());
        blocker.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    private void castVindictiveMob() {
        harness.setHand(player1, List.of(new VindictiveMob()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castCreature(player1, 0);
    }

    private Permanent attackingMob() {
        Permanent attacker = new Permanent(new VindictiveMob());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        return attacker;
    }

}
