package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.u.UlamogsCrusher;
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

@CardUsed({EldritchImmunity.class, UlamogsCrusher.class})
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

    @Test
    void overloadIncludesCreaturesPresentAtResolutionButNotThoseAddedLater() {
        harness.setHand(player1, List.of(new EldritchImmunity()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castWithOverload(player1, 0);
        Permanent beforeResolution = addCreature(player1);
        harness.passBothPriorities();
        Permanent afterResolution = addCreature(player1);

        assertProtectedFromEachColor(beforeResolution, true);
        assertProtectedFromEachColor(afterResolution, false);
    }

    @Test
    void overloadCanResolveWithNoCreatures() {
        harness.setHand(player1, List.of(new EldritchImmunity()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castWithOverload(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card instanceof EldritchImmunity);
    }

    @Test
    void overloadCannotPayItsColorlessRequirementWithColoredMana() {
        addCreature(player1);
        harness.setHand(player1, List.of(new EldritchImmunity()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        assertThatThrownBy(() -> harness.castWithOverload(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void normalSpellDoesNotProtectTargetThatChangedController() {
        Permanent target = addCreature(player1);
        harness.setHand(player1, List.of(new EldritchImmunity()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(target);
        gd.playerBattlefields.get(player2.getId()).add(target);
        harness.passBothPriorities();

        assertProtectedFromEachColor(target, false);
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addCreature(Player player) {
        return addCreatureReady(player, new UlamogsCrusher());
    }

    private void assertProtectedFromEachColor(Permanent permanent, boolean expected) {
        for (CardColor color : CardColor.values()) {
            assertThat(gqs.hasProtectionFrom(gd, permanent, color)).isEqualTo(expected);
        }
    }
}
