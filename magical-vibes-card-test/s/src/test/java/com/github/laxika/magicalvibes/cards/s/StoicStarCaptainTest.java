package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import org.junit.jupiter.api.DisplayName;
import com.github.laxika.magicalvibes.testutil.PermanentGrants;
import com.github.laxika.magicalvibes.cards.e.ExtinguisherBattleship;
import com.github.laxika.magicalvibes.cards.g.GiantBeaver;
import com.github.laxika.magicalvibes.cards.i.IrontreadCrusher;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
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

@CardUsed({StoicStarCaptain.class, IrontreadCrusher.class, ExtinguisherBattleship.class, GiantBeaver.class,
        GrizzlyBears.class})
class StoicStarCaptainTest extends BaseCardTest {

    @Test
    void creaturesYouControlCrewVehiclesAsThoughTheirPowerWereTwoGreater() {
        Permanent captain = addReady(player1, new StoicStarCaptain());
        Permanent vehicle = addReady(player1, new IrontreadCrusher());

        harness.activateAbility(player1, 1, 0, null, null);
        harness.passBothPriorities();

        assertThat(captain.isTapped()).isTrue();
        assertThat(gqs.isCreature(gd, vehicle)).isTrue();
    }

    @Test
    void creaturesYouControlStationAsThoughTheirPowerWereTwoGreater() {
        addReady(player1, new StoicStarCaptain());
        Permanent spacecraft = addReady(player1, new ExtinguisherBattleship());

        harness.activateAbility(player1, 1, 0, null, null);
        harness.passBothPriorities();

        assertThat(spacecraft.getCounterCount(CounterType.CHARGE)).isEqualTo(4);
    }

    @Test
    void seeksARandomMatchingSpacecraftIntoHand() {
        Permanent captain = addReady(player1, new StoicStarCaptain());
        StoicStarCaptain nonSpacecraft = new StoicStarCaptain();
        ExtinguisherBattleship spacecraft = new ExtinguisherBattleship();
        harness.setLibrary(player1, List.of(nonSpacecraft, spacecraft));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(captain.isTapped()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).contains(spacecraft);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nonSpacecraft);
    }

    @Test
    void exhaustCanBeActivatedWhileTapped() {
        Permanent captain = addReady(player1, new StoicStarCaptain());
        captain.tap();
        ExtinguisherBattleship spacecraft = new ExtinguisherBattleship();
        harness.setLibrary(player1, List.of(spacecraft));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(captain.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).contains(spacecraft);
    }

    @Test
    void exhaustCanBeActivatedWithSummoningSickness() {
        Permanent captain = addReady(player1, new StoicStarCaptain());
        captain.setSummoningSick(true);
        ExtinguisherBattleship spacecraft = new ExtinguisherBattleship();
        harness.setLibrary(player1, List.of(spacecraft));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(captain.isTapped()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).contains(spacecraft);
    }

    @Test
    void exhaustCannotBeActivatedTwiceEvenWhenNothingMatches() {
        Permanent captain = addReady(player1, new StoicStarCaptain());
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        captain.untap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once");
    }

    @Test
    void powerBonusDoesNotApplyToSaddling() {
        addReady(player1, new StoicStarCaptain());
        addReady(player1, new GiantBeaver());

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough creature power");
    }

    @Test
    @DisplayName("A creature that loses all abilities still crews as though its power were 2 greater")
    void crewBonusSurvivesTheCreatureLosingAllAbilities() {
        Permanent captain = addReady(player1, new StoicStarCaptain());
        Permanent vehicle = addReady(player1, new IrontreadCrusher());
        Permanent bears = addReady(player1, new GrizzlyBears());
        captain.tap();
        PermanentGrants.loseAllAbilitiesUntilEndOfTurn(gd, bears);

        harness.activateAbility(player1, 1, 0, null, null);
        harness.passBothPriorities();

        assertThat(bears.isTapped()).isTrue();
        assertThat(gqs.isCreature(gd, vehicle)).isTrue();
    }

    @Test
    @DisplayName("A creature that loses all abilities still stations as though its power were 2 greater")
    void stationBonusSurvivesTheCreatureLosingAllAbilities() {
        Permanent captain = addReady(player1, new StoicStarCaptain());
        Permanent spacecraft = addReady(player1, new ExtinguisherBattleship());
        Permanent bears = addReady(player1, new GrizzlyBears());
        captain.tap();
        PermanentGrants.loseAllAbilitiesUntilEndOfTurn(gd, bears);

        harness.activateAbility(player1, 1, 0, null, null);
        harness.passBothPriorities();

        assertThat(spacecraft.getCounterCount(CounterType.CHARGE)).isEqualTo(4);
    }

    @Test
    @DisplayName("Once Stoic Star-Captain loses all abilities, its creatures crew with their own power")
    void noCrewBonusOnceTheCaptainLosesAllAbilities() {
        Permanent captain = addReady(player1, new StoicStarCaptain());
        addReady(player1, new IrontreadCrusher());
        Permanent bears = addReady(player1, new GrizzlyBears());
        captain.tap();
        PermanentGrants.loseAllAbilitiesUntilEndOfTurn(gd, captain);

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough creature power");
        assertThat(bears.isTapped()).isFalse();
    }

    private Permanent addReady(Player player, Card card) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, card);
        permanent.setSummoningSick(false);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return permanent;
    }
}
