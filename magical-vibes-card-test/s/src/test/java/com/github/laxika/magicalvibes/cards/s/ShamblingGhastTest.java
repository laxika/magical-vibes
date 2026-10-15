package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.b.BoneSplinters;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ShamblingGhast.class, BoneSplinters.class, AirElemental.class})
class ShamblingGhastTest extends BaseCardTest {

    @Test
    void createsTreasureTokenWhenChosen() {
        Permanent ghast = addGhastAndTarget();

        castBoneSplintersSacrificing(ghast);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Create a Treasure token.");
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Treasure")).isEqualTo(1);
    }

    @Test
    void weakensTargetCreatureAnOpponentControlsWhenChosen() {
        Permanent ghast = addGhastAndTarget();
        Permanent target = findPermanent(player2, "Air Elemental");

        castBoneSplintersSacrificing(ghast);
        harness.passBothPriorities();
        harness.handleListChoice(player1,
                "Target creature an opponent controls gets -1/-1 until end of turn.");
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
    }

    @Test
    void cannotTargetCreatureControlledByTriggerController() {
        Permanent ghast = addGhastAndTarget();
        Permanent friendly = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        Permanent target = findPermanent(player2, "Air Elemental");

        castBoneSplintersSacrificing(ghast);
        harness.passBothPriorities();
        harness.handleListChoice(player1,
                "Target creature an opponent controls gets -1/-1 until end of turn.");

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, friendly.getId()))
                .isInstanceOf(IllegalArgumentException.class);

        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, friendly)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, friendly)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(countPermanents(player1, "Treasure")).isZero();
    }

    @Test
    void createsTreasureWithoutOpposingCreatures() {
        Permanent ghast = harness.addToBattlefieldAndReturn(player1, new ShamblingGhast());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        harness.setHand(player1, List.of(new BoneSplinters()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorceryWithSacrifice(player1, 0, target.getId(), ghast.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Create a Treasure token.");
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Treasure")).isEqualTo(1);
        assertThat(findPermanent(player1, "Treasure").isTapped()).isFalse();
        harness.assertInGraveyard(player1, "Shambling Ghast");
        harness.assertInGraveyard(player1, "Air Elemental");
    }

    @Test
    void weakeningExpiresAtEndOfTurn() {
        Permanent ghast = addGhastAndTarget();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());

        castBoneSplintersSacrificing(ghast);
        harness.passBothPriorities();
        harness.handleListChoice(player1,
                "Target creature an opponent controls gets -1/-1 until end of turn.");
        harness.handlePermanentChosen(player1, target.getId());
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);

        harness.passUntilWithNoAttackers(player1, TurnStep.CLEANUP);

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(4);
        assertThat(countPermanents(player1, "Treasure")).isZero();
    }

    private Permanent addGhastAndTarget() {
        Permanent ghast = harness.addToBattlefieldAndReturn(player1, new ShamblingGhast());
        harness.addToBattlefield(player2, new AirElemental());
        return ghast;
    }

    private void castBoneSplintersSacrificing(Permanent ghast) {
        harness.setHand(player1, List.of(new BoneSplinters()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castSorceryWithSacrifice(player1, 0,
                findPermanent(player2, "Air Elemental").getId(), ghast.getId());
    }
}
