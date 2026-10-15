package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.a.AcademyDrake;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SilverScrutiny;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VolsheTideturner.class, Unsummon.class, GrizzlyBears.class, AcademyDrake.class, SilverScrutiny.class})
class VolsheTideturnerTest extends BaseCardTest {

    @Test
    void tappingAddsBlueManaRestrictedToInstantsSorceriesOrKickedSpells() {
        Permanent tideturner = addReadyTideturner();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana())
                .isEqualTo(1);
        assertThat(tideturner.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void restrictedBlueManaPaysForAnInstant() {
        addReadyTideturner();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Unsummon()));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertInHand(player2, "Grizzly Bears");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    void restrictedBlueManaCannotPayForACreatureSpell() {
        addReadyTideturner();
        harness.setHand(player1, List.of(new VolsheTideturner()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(2);
    }

    @Test
    void restrictedBlueManaPaysForAKickedCreatureSpell() {
        addReadyTideturner();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.setHand(player1, List.of(new AcademyDrake()));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        assertThat(gd.playerManaPools.get(player1.getId())
                .getKickedOrInstantSorceryOnlyManaTotal()).isZero();
    }

    @Test
    void restrictedBlueManaPaysForASorceryColoredCost() {
        addReadyTideturner();
        harness.setHand(player1, List.of(new SilverScrutiny()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertInGraveyard(player1, "Silver Scrutiny");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    void restrictedBlueManaCanPayGenericSorceryCosts() {
        addReadyTideturner();
        VolsheTideturner drawnCard = new VolsheTideturner();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.setHand(player1, List.of(new SilverScrutiny()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.castAndResolveSorcery(player1, 0, 1);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    void summoningSickTideturnerCannotActivate() {
        Permanent tideturner = harness.addToBattlefieldAndReturn(player1, new VolsheTideturner());
        tideturner.setSummoningSick(true);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(tideturner.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    void tappedTideturnerCannotActivateAgain() {
        addReadyTideturner();
        harness.activateAbility(player1, 0, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(1);
    }

    private Permanent addReadyTideturner() {
        Permanent tideturner = harness.addToBattlefieldAndReturn(player1, new VolsheTideturner());
        tideturner.setSummoningSick(false);
        return tideturner;
    }
}
