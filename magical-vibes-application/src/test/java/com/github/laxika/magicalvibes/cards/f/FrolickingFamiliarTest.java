package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FrolickingFamiliar.class, BlowOffSteam.class, FountainOfYouth.class, GrizzlyBears.class, Shock.class})
class FrolickingFamiliarTest extends BaseCardTest {

    @Test
    void adventureDealsOneDamageToAnyTargetAndExilesTheCard() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        FrolickingFamiliar card = new FrolickingFamiliar();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAdventure(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(1);
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
        assertThat(gd.exilePlayPermissions.get(card.getId())).isEqualTo(player1.getId());
    }

    @Test
    void adventureCannotTargetAnotherPermanentType() {
        FountainOfYouth nonCreaturePermanent = new FountainOfYouth();
        harness.addToBattlefield(player2, nonCreaturePermanent);
        FrolickingFamiliar card = new FrolickingFamiliar();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castAdventure(player1, 0, nonCreaturePermanent.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void creatureFaceCanBeCastFromExileAfterAdventure() {
        FrolickingFamiliar card = new FrolickingFamiliar();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAdventure(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castFromExile(player1, card.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Frolicking Familiar");
        assertThat(gd.findExiledCard(card.getId())).isNull();
    }

    @Test
    void castingAnInstantBoostsFrolickingFamiliar() {
        Permanent familiar = harness.addToBattlefieldAndReturn(player1, new FrolickingFamiliar());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(familiar.getEffectivePower()).isEqualTo(3);
        assertThat(familiar.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    void castingACreatureDoesNotBoostFrolickingFamiliar() {
        Permanent familiar = harness.addToBattlefieldAndReturn(player1, new FrolickingFamiliar());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(familiar.getEffectivePower()).isEqualTo(2);
        assertThat(familiar.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    void instantSorceryBoostWearsOffAtEndOfTurn() {
        Permanent familiar = harness.addToBattlefieldAndReturn(player1, new FrolickingFamiliar());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        assertThat(familiar.getEffectivePower()).isEqualTo(3);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.CLEANUP);

        assertThat(familiar.getEffectivePower()).isEqualTo(2);
        assertThat(familiar.getEffectiveToughness()).isEqualTo(2);
    }
}
