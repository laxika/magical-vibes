package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class KinsbaileAspirantTest extends BaseCardTest {

    @Test
    @DisplayName("Without a Kithkin it costs {W} plus the additional {2}")
    void requiresAdditionalManaWithoutKithkin() {
        harness.setHand(player1, List.of(new KinsbaileAspirant()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A Kithkin permanent lets it be cast without the additional mana")
    void beholdKithkinPermanentAvoidsAdditionalMana() {
        harness.addToBattlefield(player1, new KnightOfMeadowgrain());
        KinsbaileAspirant aspirant = new KinsbaileAspirant();
        harness.setHand(player1, List.of(aspirant));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getId().equals(aspirant.getId()));
    }

    @Test
    @DisplayName("A Kithkin card in hand lets it be cast without the additional mana")
    void beholdKithkinCardAvoidsAdditionalMana() {
        KinsbaileAspirant aspirant = new KinsbaileAspirant();
        KnightOfMeadowgrain kithkin = new KnightOfMeadowgrain();
        harness.setHand(player1, List.of(aspirant, kithkin));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getId().equals(aspirant.getId()));
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(kithkin.getId()));
    }

    @Test
    @DisplayName("Another creature gives it +1/+1 until end of turn")
    void boostsOnAnotherCreatureEntering() {
        Permanent aspirant = harness.addToBattlefieldAndReturn(player1, new KinsbaileAspirant());
        harness.setHand(player1, List.of(new FugitiveWizard()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gqs.getEffectivePower(gd, aspirant)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, aspirant)).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, aspirant)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, aspirant)).isEqualTo(1);
    }
}
