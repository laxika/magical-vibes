package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.t.TerrainElemental;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HeraldOfTheFair.class, TerrainElemental.class})
class HeraldOfTheFairTest extends BaseCardTest {

    @Test
    @DisplayName("ETB gives target creature you control +1/+1 until end of turn")
    void etbBoostsTargetCreatureYouControl() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new TerrainElemental());
        harness.setHand(player1, List.of(new HeraldOfTheFair()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castCreature(player1, 0, target.getId());

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(4);
        assertThat(target.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Cannot target an opponent's creature")
    void cannotTargetOpponentsCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new TerrainElemental());
        harness.setHand(player1, List.of(new HeraldOfTheFair()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new TerrainElemental());
        harness.setHand(player1, List.of(new HeraldOfTheFair()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castCreature(player1, 0, target.getId());

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(3);
        assertThat(target.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("ETB fizzles if the target leaves before resolution")
    void etbFizzlesIfTargetLeaves() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new TerrainElemental());
        harness.setHand(player1, List.of(new HeraldOfTheFair()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castCreature(player1, 0, target.getId());

        harness.passBothPriorities();
        gd.playerBattlefields.get(player1.getId()).removeIf(permanent -> permanent.getId().equals(target.getId()));
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
    }

    @Test
    @DisplayName("Herald can target itself when it enters an otherwise empty battlefield")
    void canTargetItself() {
        harness.castFromHand(player1, new HeraldOfTheFair(), "{2}{W}");
        harness.passBothPriorities();

        Permanent herald = findPermanent(player1, "Herald of the Fair");
        harness.handlePermanentChosen(player1, herald.getId());
        harness.passBothPriorities();

        assertThat(herald.getEffectivePower()).isEqualTo(4);
        assertThat(herald.getEffectiveToughness()).isEqualTo(3);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The ETB boost still resolves after Herald leaves the battlefield")
    void boostResolvesAfterSourceLeaves() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new TerrainElemental());
        harness.setHand(player1, List.of(new HeraldOfTheFair()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castCreature(player1, 0, target.getId());
        harness.passBothPriorities();

        Permanent herald = findPermanent(player1, "Herald of the Fair");
        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, herald);
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(4);
        assertThat(target.getEffectiveToughness()).isEqualTo(3);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The ETB does not boost a target that an opponent gains control of")
    void targetMustRemainUnderYourControl() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new TerrainElemental());
        harness.setHand(player1, List.of(new HeraldOfTheFair()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castCreature(player1, 0, target.getId());
        harness.passBothPriorities();

        gd.playerBattlefields.get(player1.getId()).remove(target);
        gd.playerBattlefields.get(player2.getId()).add(target);
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(3);
        assertThat(target.getEffectiveToughness()).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }
}
