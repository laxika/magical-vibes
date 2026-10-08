package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.e.EdgarMarkov;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WillOfTheMardu.class, EdgarMarkov.class, GrizzlyBears.class, HillGiant.class})
class WillOfTheMarduTest extends BaseCardTest {

    @Test
    void createsWarriorsForEachCreatureTargetPlayerControls() {
        addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        castSingleMode(0, player2.getId());

        assertThat(countPermanents(player1, "Warrior")).isEqualTo(2);
        assertThat(countPermanents(player2, "Warrior")).isZero();
    }

    @Test
    void dealsDamageEqualToCreaturesYouControl() {
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        Permanent target = addCreatureReady(player2, new HillGiant());

        castSingleMode(1, target.getId());

        assertThat(target.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    void commanderAllowsBothModes() {
        EdgarMarkov commander = new EdgarMarkov();
        gd.playerCommanders.put(player1.getId(), List.of(commander));
        addCreatureReady(player1, commander);
        addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        Permanent target = addCreatureReady(player2, new HillGiant());

        harness.setHand(player1, List.of(new WillOfTheMardu()));
        addMana();
        harness.castModalInstantWithModes(player1, 0, 1, 2, new int[]{0, 1},
                List.of(player2.getId(), target.getId()));
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Warrior")).isEqualTo(3);
        assertThat(target.getMarkedDamage()).isEqualTo(4);
        harness.assertInGraveyard(player2, "Hill Giant");
    }

    @Test
    void cannotChooseBothModesWithoutACommander() {
        Permanent target = addCreatureReady(player2, new HillGiant());
        harness.setHand(player1, List.of(new WillOfTheMardu()));
        addMana();

        assertThatThrownBy(() -> harness.castModalInstantWithModes(
                player1, 0, 1, 2, new int[]{0, 1}, List.of(player2.getId(), target.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canTargetYourselfForTokensWithoutCountingNewTokens() {
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());

        castSingleMode(0, player1.getId());

        assertThat(countPermanents(player1, "Warrior")).isEqualTo(2);
    }

    @Test
    void createsNoTokensWhenTargetPlayerControlsNoCreatures() {
        castSingleMode(0, player2.getId());

        assertThat(countPermanents(player1, "Warrior")).isZero();
    }

    @Test
    void tokenCountUsesCreaturesPresentAtResolution() {
        addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new WillOfTheMardu()));
        addMana();
        harness.castModalInstantWithModes(player1, 0, 1, 2, new int[]{0}, List.of(player2.getId()));
        addCreatureReady(player2, new GrizzlyBears());

        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Warrior")).isEqualTo(2);
    }

    @Test
    void damageCountUsesCreaturesPresentAtResolution() {
        Permanent target = addCreatureReady(player2, new HillGiant());
        harness.setHand(player1, List.of(new WillOfTheMardu()));
        addMana();
        harness.castModalInstantWithModes(player1, 0, 1, 2, new int[]{1}, List.of(target.getId()));
        addCreatureReady(player1, new GrizzlyBears());

        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    void dealsNoDamageWhenYouControlNoCreatures() {
        Permanent target = addCreatureReady(player2, new HillGiant());

        castSingleMode(1, target.getId());

        assertThat(target.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Hill Giant");
    }

    @Test
    void commanderInCommandZoneDoesNotAllowBothModes() {
        EdgarMarkov commander = new EdgarMarkov();
        gd.playerCommanders.put(player1.getId(), List.of(commander));
        gd.playerCommandZones.get(player1.getId()).add(commander);
        Permanent target = addCreatureReady(player2, new HillGiant());
        harness.setHand(player1, List.of(new WillOfTheMardu()));
        addMana();

        assertThatThrownBy(() -> harness.castModalInstantWithModes(
                player1, 0, 1, 2, new int[]{0, 1}, List.of(player2.getId(), target.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void losingCommanderAfterCastingDoesNotRemoveChosenModes() {
        EdgarMarkov commander = new EdgarMarkov();
        gd.playerCommanders.put(player1.getId(), List.of(commander));
        Permanent commanderPermanent = addCreatureReady(player1, commander);
        Permanent target = addCreatureReady(player2, new HillGiant());
        harness.setHand(player1, List.of(new WillOfTheMardu()));
        addMana();
        harness.castModalInstantWithModes(player1, 0, 1, 2, new int[]{0, 1},
                List.of(player2.getId(), target.getId()));
        gd.playerBattlefields.get(player1.getId()).remove(commanderPermanent);
        gd.playerCommandZones.get(player1.getId()).add(commander);

        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Warrior")).isEqualTo(1);
        assertThat(target.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    void stillCreatesTokensWhenDamageTargetLeavesBattlefield() {
        EdgarMarkov commander = new EdgarMarkov();
        gd.playerCommanders.put(player1.getId(), List.of(commander));
        addCreatureReady(player1, commander);
        addCreatureReady(player2, new GrizzlyBears());
        Permanent target = addCreatureReady(player2, new HillGiant());
        harness.setHand(player1, List.of(new WillOfTheMardu()));
        addMana();
        harness.castModalInstantWithModes(player1, 0, 1, 2, new int[]{0, 1},
                List.of(player2.getId(), target.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerGraveyards.get(player2.getId()).add(target.getOriginalCard());

        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Warrior")).isEqualTo(1);
    }

    private void castSingleMode(int mode, java.util.UUID targetId) {
        harness.setHand(player1, List.of(new WillOfTheMardu()));
        addMana();
        harness.castModalInstantWithModes(player1, 0, 1, 2, new int[]{mode}, List.of(targetId));
        harness.passBothPriorities();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);
    }

}
