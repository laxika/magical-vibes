package com.github.laxika.magicalvibes.cards.k;

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

@CardUsed({KinsbaileAspirant.class})
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
        Permanent kithkin = harness.addToBattlefieldAndReturn(player1, new KinsbaileAspirant());
        KinsbaileAspirant aspirant = new KinsbaileAspirant();
        harness.setHand(player1, List.of(aspirant));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreatureWithBeholdPermanent(player1, 0, kithkin.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getId().equals(aspirant.getId()));
    }

    @Test
    @DisplayName("A Kithkin card in hand lets it be cast without the additional mana")
    void beholdKithkinCardAvoidsAdditionalMana() {
        KinsbaileAspirant aspirant = new KinsbaileAspirant();
        KinsbaileAspirant kithkin = new KinsbaileAspirant();
        harness.setHand(player1, List.of(aspirant, kithkin));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreatureWithBeholdHandCard(player1, 0, 1);
        assertThat(gd.gameLog).anyMatch(log -> log.plainText().contains("reveals")
                && log.plainText().contains("Kinsbaile Aspirant"));
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
        harness.setHand(player1, List.of(new KinsbaileAspirant()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreatureWithBeholdPermanent(player1, 0, aspirant.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, aspirant)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, aspirant)).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, aspirant)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, aspirant)).isEqualTo(1);
    }

    @Test
    @DisplayName("Paying the additional mana works without another Kithkin and does not trigger itself")
    void paysAdditionalManaAndDoesNotBoostItself() {
        KinsbaileAspirant card = new KinsbaileAspirant();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent aspirant = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getId().equals(card.getId())).findFirst().orElseThrow();
        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, aspirant)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, aspirant)).isEqualTo(1);
    }

    @Test
    @DisplayName("The caster can pay the additional mana instead of revealing a Kithkin in hand")
    void canPayInsteadOfRevealing() {
        harness.setHand(player1, List.of(new KinsbaileAspirant(), new KinsbaileAspirant()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castCreature(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
        assertThat(gd.gameLog).noneMatch(log -> log.plainText().contains("reveals"));
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Kinsbaile Aspirant");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("An opponent's Kithkin does not waive the additional cost")
    void opponentsKithkinDoesNotWaiveCost() {
        harness.addToBattlefield(player2, new KinsbaileAspirant());
        harness.setHand(player1, List.of(new KinsbaileAspirant()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("An opponent's creature entering does not boost the Aspirant")
    void opponentsCreatureDoesNotBoost() {
        Permanent aspirant = harness.addToBattlefieldAndReturn(player1, new KinsbaileAspirant());

        harness.enterBattlefieldAndReturn(player2, new KinsbaileAspirant());

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, aspirant)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, aspirant)).isEqualTo(1);
    }

    @Test
    @DisplayName("Each other creature entering adds a separate boost")
    void boostsAccumulate() {
        Permanent aspirant = harness.addToBattlefieldAndReturn(player1, new KinsbaileAspirant());

        harness.enterBattlefieldAndReturn(player1, new KinsbaileAspirant());
        harness.passBothPriorities();
        harness.enterBattlefieldAndReturn(player1, new KinsbaileAspirant());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, aspirant)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, aspirant)).isEqualTo(3);
    }
}
