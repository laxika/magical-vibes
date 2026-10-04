package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.b.BrittleBlast;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EtherealEscort.class, GrizzlyBears.class, BrittleBlast.class})
class EtherealEscortTest extends BaseCardTest {

    @Test
    void etbPerpetuallyGrantsLifelinkToAChosenCreatureCard() {
        GrizzlyBears bear = new GrizzlyBears();
        harness.setHand(player1, List.of(new EtherealEscort(), bear));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.PerpetualPowerToughnessChoice.class);
        harness.handleCardChosen(player1, 0);

        harness.castFromHand(player1, bear, "{1}{G}");
        harness.passBothPriorities();

        Permanent enteredBear = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(bear.getId()))
                .findFirst().orElseThrow();
        assertThat(enteredBear.hasKeyword(Keyword.LIFELINK)).isTrue();
    }

    @Test
    void attackTriggerPerpetuallyGrantsLifelinkToAChosenCreatureCard() {
        GrizzlyBears bear = new GrizzlyBears();
        Permanent escort = harness.addToBattlefieldAndReturn(player1, new EtherealEscort());
        harness.setHand(player1, List.of(bear));
        escort.setSummoningSick(false);

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.PerpetualPowerToughnessChoice.class);
        harness.handleCardChosen(player1, 0);

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.castFromHand(player1, bear, "{1}{G}");
        harness.passBothPriorities();

        Permanent enteredBear = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(bear.getId()))
                .findFirst().orElseThrow();
        assertThat(enteredBear.hasKeyword(Keyword.LIFELINK)).isTrue();
    }

    @Test
    void enteringWithAnEmptyHandDoesNotRequireAChoice() {
        harness.castFromHand(player1, new EtherealEscort(), "{2}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Ethereal Escort");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void attackingWithAnEmptyHandDoesNotRequireAChoice() {
        Permanent escort = harness.addToBattlefieldAndReturn(player1, new EtherealEscort());
        harness.setHand(player1, List.of());
        escort.setSummoningSick(false);

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction())
                .isNotInstanceOf(PendingInteraction.PerpetualPowerToughnessChoice.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void onlyTheChosenCardGainsLifelink() {
        GrizzlyBears unchosen = new GrizzlyBears();
        GrizzlyBears chosen = new GrizzlyBears();
        harness.setHand(player1, List.of(unchosen, chosen));
        harness.enterBattlefieldAndReturn(player1, new EtherealEscort());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 1);

        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anySatisfy(permanent -> {
                    assertThat(permanent.getCard().getId()).isEqualTo(unchosen.getId());
                    assertThat(permanent.hasKeyword(Keyword.LIFELINK)).isFalse();
                })
                .anySatisfy(permanent -> {
                    assertThat(permanent.getCard().getId()).isEqualTo(chosen.getId());
                    assertThat(permanent.hasKeyword(Keyword.LIFELINK)).isTrue();
                });
    }

    @Test
    @CardUsed({EtherealEscort.class, BrittleBlast.class})
    void chosenDamageSpellGainsLifeWhenItDealsDamage() {
        harness.setLife(player1, 10);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new EtherealEscort());
        harness.setHand(player1, List.of(new BrittleBlast()));
        harness.enterBattlefieldAndReturn(player1, new EtherealEscort());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertLife(player1, 15);
    }
}
