package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UlamogsDreadsire.class, Forest.class, GrizzlyBears.class, Shock.class})
class UlamogsDreadsireTest extends BaseCardTest {

    @Test
    void createsTenTenEldraziToken() {
        addCreatureReady(player1, new UlamogsDreadsire());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(token.getEffectivePower()).isEqualTo(10);
        assertThat(token.getEffectiveToughness()).isEqualTo(10);
        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.ELDRAZI);
    }

    @Test
    void wardCannotBePaidWithZeroManaValuePermanent() {
        Permanent dreadsire = harness.addToBattlefieldAndReturn(player1, new UlamogsDreadsire());
        harness.addToBattlefield(player2, new Forest());
        prepareOpponentShock(dreadsire);

        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Shock");
        harness.assertOnBattlefield(player2, "Forest");
    }

    @Test
    void wardCanBePaidBySacrificingPermanentWithManaValueAtLeastOne() {
        Permanent dreadsire = harness.addToBattlefieldAndReturn(player1, new UlamogsDreadsire());
        harness.addToBattlefield(player2, new Forest());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        prepareOpponentShock(dreadsire);

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(bears.getId())
                .doesNotContain(harness.getPermanentId(player2, "Forest"));
        harness.handlePermanentChosen(player2, bears.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Shock");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Forest");
    }

    @Test
    void createsExactlyOneUntappedColorlessCreatureTokenWithoutInheritedKeywords() {
        addCreatureReady(player1, new UlamogsDreadsire());
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Eldrazi")).hasSize(1);
        Permanent token = findPermanent(player1, "Eldrazi");
        assertThat(token.isTapped()).isFalse();
        assertThat(token.getCard().getColor()).isNull();
        assertThat(token.getCard().getKeywords()).isEmpty();
        assertThat(gqs.isCreature(gd, token)).isTrue();
        assertThat(countPermanents(player2, "Eldrazi")).isZero();
    }

    @Test
    void decliningWardCountersShockWithoutSacrificing() {
        Permanent dreadsire = harness.addToBattlefieldAndReturn(player1, new UlamogsDreadsire());
        harness.addToBattlefield(player2, new GrizzlyBears());
        prepareOpponentShock(dreadsire);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.stack).isEmpty();
        assertThat(dreadsire.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player2, "Shock");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    void controllersOwnSpellDoesNotTriggerWard() {
        Permanent dreadsire = harness.addToBattlefieldAndReturn(player1, new UlamogsDreadsire());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, dreadsire.getId());
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(dreadsire.getMarkedDamage()).isEqualTo(2);
        harness.assertInGraveyard(player1, "Shock");
    }

    @Test
    void summoningSicknessPreventsTokenActivation() {
        harness.addToBattlefield(player1, new UlamogsDreadsire());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
        assertThat(gd.stack).isEmpty();
        assertThat(countPermanents(player1, "Eldrazi")).isZero();
    }

    @Test
    void tappingForTokenPreventsAnotherActivation() {
        Permanent dreadsire = addCreatureReady(player1, new UlamogsDreadsire());
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(dreadsire.isTapped()).isTrue();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("tapped");
        assertThat(countPermanents(player1, "Eldrazi")).isEqualTo(1);
    }

    @Test
    void vigilanceLeavesDreadsireUntappedWhenAttacking() {
        Permanent dreadsire = addCreatureReady(player1, new UlamogsDreadsire());
        declareAttackersAndPrepareBlockers(List.of(0));
        assertThat(dreadsire.isTapped()).isFalse();
    }

    private void prepareOpponentShock(Permanent target) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, target.getId());
    }
}
