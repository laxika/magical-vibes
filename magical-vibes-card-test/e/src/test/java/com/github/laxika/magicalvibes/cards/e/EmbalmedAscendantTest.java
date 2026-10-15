package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EmbalmedAscendant.class, GrizzlyBears.class, Shock.class})
class EmbalmedAscendantTest extends BaseCardTest {

    @Test
    void entersAndCreatesZombieToken() {
        harness.castFromHand(player1, new EmbalmedAscendant(), "{1}{W}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .map(Permanent::getCard)
                .filter(Card::isToken)
                .count()).isEqualTo(1);
    }

    @Test
    void atMaxSpeedDrainsOpponentsAndGainsLifeWhenAllyCreatureDies() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        gd.playerSpeeds.put(player1.getId(), 4);
        harness.addToBattlefield(player1, new EmbalmedAscendant());
        harness.addToBattlefield(player1, new GrizzlyBears());

        killWithShock(player1, "Grizzly Bears");

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);
    }

    @Test
    void belowMaxSpeedDoesNotDrainOrGainLifeWhenAllyCreatureDies() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        gd.playerSpeeds.put(player1.getId(), 3);
        harness.addToBattlefield(player1, new EmbalmedAscendant());
        harness.addToBattlefield(player1, new GrizzlyBears());

        killWithShock(player1, "Grizzly Bears");

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    void atMaxSpeedTriggersWhenItDies() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        gd.playerSpeeds.put(player1.getId(), 4);
        harness.addToBattlefield(player1, new EmbalmedAscendant());

        killWithShock(player1, "Embalmed Ascendant");

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);
    }

    @Test
    void enteringStartsSpeedAtOne() {
        harness.castFromHand(player1, new EmbalmedAscendant(), "{1}{W}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerSpeeds.get(player1.getId())).isEqualTo(1);
        assertThat(gd.playerSpeeds).doesNotContainKey(player2.getId());
    }

    @Test
    void enteringDoesNotResetExistingSpeed() {
        gd.playerSpeeds.put(player1.getId(), 4);
        harness.castFromHand(player1, new EmbalmedAscendant(), "{1}{W}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerSpeeds.get(player1.getId())).isEqualTo(4);
    }

    @Test
    void atMaxSpeedTriggersWhenItsZombieTokenDies() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        gd.playerSpeeds.put(player1.getId(), 4);
        harness.castFromHand(player1, new EmbalmedAscendant(), "{1}{W}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        killWithShock(player1, "Zombie");

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
        harness.assertOnBattlefield(player1, "Embalmed Ascendant");
    }

    @Test
    void atMaxSpeedDoesNotTriggerForAnOpponentsCreature() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        gd.playerSpeeds.put(player1.getId(), 4);
        harness.addToBattlefield(player1, new EmbalmedAscendant());
        harness.addToBattlefield(player2, new GrizzlyBears());

        killWithShock(player2, "Grizzly Bears");

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    void belowMaxSpeedDoesNotTriggerWhenItDies() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        gd.playerSpeeds.put(player1.getId(), 3);
        harness.addToBattlefield(player1, new EmbalmedAscendant());

        killWithShock(player1, "Embalmed Ascendant");

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    private void killWithShock(Player caster, String targetName) {
        harness.forceActivePlayer(caster);
        harness.setHand(caster, List.of(new Shock()));
        harness.addMana(caster, ManaColor.RED, 1);
        UUID targetId = harness.getPermanentId(caster, targetName);
        harness.castAndResolveInstant(caster, 0, targetId);
        harness.passBothPriorities();
    }
}
