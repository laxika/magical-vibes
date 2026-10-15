package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.c.Counterspell;
import com.github.laxika.magicalvibes.cards.s.SavannahLions;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
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

@CardUsed({ProsshSkyraiderOfKher.class, SavannahLions.class, Counterspell.class})
class ProsshSkyraiderOfKherTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Prossh creates one Kobold for each mana spent")
    void createsKoboldsEqualToManaSpent() {
        harness.setHand(player1, List.of(new ProsshSkyraiderOfKher()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Kobolds of Kher Keep")).hasSize(6);
        Permanent kobold = findPermanent(player1, "Kobolds of Kher Keep");
        assertThat(kobold.getCard().isToken()).isTrue();
        assertThat(gqs.getEffectivePower(gd, kobold)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, kobold)).isEqualTo(1);
        assertThat(gqs.getEffectiveColors(gd, kobold)).containsExactly(CardColor.RED);
        assertThat(kobold.getCard().getSubtypes()).containsExactly(CardSubtype.KOBOLD);
    }

    @Test
    @DisplayName("Sacrificing another creature gives Prossh +1/+0 until end of turn")
    void sacrificingAnotherCreatureBoostsProssh() {
        Permanent prossh = addCreatureReady(player1, new ProsshSkyraiderOfKher());
        addCreatureReady(player1, new SavannahLions());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, prossh)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, prossh)).isEqualTo(5);
        harness.assertInGraveyard(player1, "Savannah Lions");
    }

    @Test
    @DisplayName("Prossh's boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent prossh = addCreatureReady(player1, new ProsshSkyraiderOfKher());
        addCreatureReady(player1, new SavannahLions());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, prossh)).isEqualTo(6);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, prossh)).isEqualTo(5);
    }

    @Test
    @DisplayName("Prossh cannot sacrifice itself")
    void cannotSacrificeItself() {
        addCreatureReady(player1, new ProsshSkyraiderOfKher());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Kobolds are created before Prossh resolves, without spending surplus mana")
    void castTriggerResolvesBeforeCreature() {
        harness.setHand(player1, List.of(new ProsshSkyraiderOfKher()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Kobolds of Kher Keep")).hasSize(6);
        harness.assertNotOnBattlefield(player1, "Prossh, Skyraider of Kher");
        assertThat(findPermanents(player2, "Kobolds of Kher Keep")).isEmpty();

        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Prossh, Skyraider of Kher");
        assertThat(findPermanents(player1, "Kobolds of Kher Keep")).hasSize(6);
    }

    @Test
    @DisplayName("Sacrifice is paid immediately and a summoning-sick Prossh can activate")
    void sacrificeIsPaidBeforeBoostResolves() {
        Permanent prossh = addCreatureReady(player1, new ProsshSkyraiderOfKher());
        prossh.setSummoningSick(true);
        Permanent lions = addCreatureReady(player1, new SavannahLions());
        lions.setSummoningSick(true);

        harness.activateAbility(player1, 0, null, null);

        harness.assertInGraveyard(player1, "Savannah Lions");
        harness.assertNotOnBattlefield(player1, "Savannah Lions");
        assertThat(gqs.getEffectivePower(gd, prossh)).isEqualTo(5);

        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, prossh)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, prossh)).isEqualTo(5);
    }

    @Test
    @DisplayName("An opponent's creature cannot pay Prossh's sacrifice cost")
    void cannotSacrificeOpponentsCreature() {
        addCreatureReady(player1, new ProsshSkyraiderOfKher());
        addCreatureReady(player2, new SavannahLions());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player2, "Savannah Lions");
        harness.assertNotInGraveyard(player2, "Savannah Lions");
    }

    @Test
    @DisplayName("Countering Prossh does not counter its Kobold cast trigger")
    void castTriggerSurvivesCounteringProssh() {
        ProsshSkyraiderOfKher prossh = new ProsshSkyraiderOfKher();
        harness.setHand(player1, List.of(prossh));
        harness.setHand(player2, List.of(new Counterspell()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.castAndResolveInstant(player2, 0, prossh.getId());
        harness.assertInGraveyard(player1, "Prossh, Skyraider of Kher");

        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Kobolds of Kher Keep")).hasSize(6);
        harness.assertNotOnBattlefield(player1, "Prossh, Skyraider of Kher");
    }
}
