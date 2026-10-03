package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.d.DragonlordDromoka;
import com.github.laxika.magicalvibes.cards.o.Opalescence;
import com.github.laxika.magicalvibes.model.DeckFormat;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CommandersInsignia.class, GrizzlyBears.class, DragonlordDromoka.class, Opalescence.class})
class CommandersInsigniaTest extends BaseCardTest {

    @Test
    void creaturesGetPlusOnePlusOneForEachCommanderCastFromCommandZone() {
        harness.addToBattlefield(player1, new CommandersInsignia());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(2);

        gd.recordCommanderCastFromCommandZone(player1.getId());
        gd.recordCommanderCastFromCommandZone(player1.getId());

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, opponentCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentCreature)).isEqualTo(2);
    }

    @Test
    void countsEarlierCastsButNotOpponentsCastsAndBoostsNewCreatures() {
        gd.recordCommanderCastFromCommandZone(player1.getId());
        gd.recordCommanderCastFromCommandZone(player2.getId());
        gd.recordCommanderCastFromCommandZone(player2.getId());
        harness.addToBattlefield(player1, new CommandersInsignia());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }

    @Test
    void multipleInsigniasStackAndTheirBonusesEndWhenTheyLeave() {
        gd.recordCommanderCastFromCommandZone(player1.getId());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new CommandersInsignia());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new CommandersInsignia());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);

        gd.playerBattlefields.get(player1.getId()).remove(first);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);

        gd.playerBattlefields.get(player1.getId()).remove(second);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    void bonusUsesNewControllersCastHistoryAfterControlChanges() {
        gd.recordCommanderCastFromCommandZone(player1.getId());
        gd.recordCommanderCastFromCommandZone(player2.getId());
        gd.recordCommanderCastFromCommandZone(player2.getId());
        Permanent insignia = harness.addToBattlefieldAndReturn(player1, new CommandersInsignia());
        Permanent firstCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent secondCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, firstCreature)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, secondCreature)).isEqualTo(2);

        gd.playerBattlefields.get(player1.getId()).remove(insignia);
        gd.playerBattlefields.get(player2.getId()).add(insignia);

        assertThat(gqs.getEffectivePower(gd, firstCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, firstCreature)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, secondCreature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, secondCreature)).isEqualTo(4);
    }

    @Test
    void commandZoneCastIncreasesBonusBeforeCommanderResolvesButHandCastDoesNot() {
        harness.addToBattlefield(player1, new CommandersInsignia());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        DragonlordDromoka commander = new DragonlordDromoka();
        commander.setOwnerId(player1.getId());
        commander.freeze();
        gd.format = DeckFormat.COMMANDER;
        gd.makeCommander(player1.getId(), commander);
        gd.playerCommandZones.put(player1.getId(), new ArrayList<>(List.of(commander)));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        gs.castCommander(gd, player1, commander.getId(),
                () -> gs.playCard(gd, player1, 0, null, null, null));

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);

        gd.stack.clear();
        harness.setHand(player1, List.of(commander));
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castCreature(player1, 0);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }

    @Test
    void animatedInsigniaReceivesItsOwnBonus() {
        gd.recordCommanderCastFromCommandZone(player1.getId());
        gd.recordCommanderCastFromCommandZone(player1.getId());
        Permanent insignia = harness.addToBattlefieldAndReturn(player1, new CommandersInsignia());
        harness.addToBattlefield(player1, new Opalescence());

        assertThat(gqs.isCreature(gd, insignia)).isTrue();
        assertThat(gqs.getEffectivePower(gd, insignia)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, insignia)).isEqualTo(6);
    }
}
