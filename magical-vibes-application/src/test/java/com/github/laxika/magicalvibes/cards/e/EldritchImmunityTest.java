package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EldritchImmunity.class, GrizzlyBears.class})
class EldritchImmunityTest extends BaseCardTest {

    @Test
    void targetsOwnCreatureAndGrantsProtectionFromEachColor() {
        Permanent target = addCreature(player1);
        Permanent opponent = addCreature(player2);
        harness.setHand(player1, List.of(new EldritchImmunity()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertProtectedFromEachColor(target, true);
        assertProtectedFromEachColor(opponent, false);
    }

    @Test
    void overloadProtectsEachCreatureYouControlWithoutTarget() {
        Permanent first = addCreature(player1);
        Permanent second = addCreature(player1);
        Permanent opponent = addCreature(player2);
        harness.setHand(player1, List.of(new EldritchImmunity()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castWithOverload(player1, 0);
        harness.passBothPriorities();

        assertProtectedFromEachColor(first, true);
        assertProtectedFromEachColor(second, true);
        assertProtectedFromEachColor(opponent, false);
    }

    @Test
    void cannotTargetAnOpponentsCreature() {
        Permanent opponent = addCreature(player2);
        harness.setHand(player1, List.of(new EldritchImmunity()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, opponent.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature you control");
    }

    @Test
    void protectionWearsOffAtEndOfTurn() {
        Permanent target = addCreature(player1);
        harness.setHand(player1, List.of(new EldritchImmunity()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
        assertProtectedFromEachColor(target, true);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertProtectedFromEachColor(target, false);
    }

    private Permanent addCreature(Player player) {
        Permanent permanent = new Permanent(new GrizzlyBears());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private void assertProtectedFromEachColor(Permanent permanent, boolean expected) {
        for (CardColor color : CardColor.values()) {
            assertThat(gqs.hasProtectionFrom(gd, permanent, color)).isEqualTo(expected);
        }
    }
}
