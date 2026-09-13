package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.c.Cromat;
import com.github.laxika.magicalvibes.cards.g.GaeasSkyfolk;
import com.github.laxika.magicalvibes.cards.g.GladeGnarr;
import com.github.laxika.magicalvibes.cards.m.MournfulZombie;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AnaSanctuary.class, Cromat.class, GaeasSkyfolk.class, GladeGnarr.class,
        MournfulZombie.class})
class AnaSanctuaryTest extends BaseCardTest {

    @Test
    @DisplayName("A blue permanent gives the target creature +1/+1")
    void boostsByOneWithBluePermanentOnly() {
        Permanent target = setUpWithTarget(new GaeasSkyfolk());

        resolveTarget(target);

        assertThat(target.getPowerModifier()).isEqualTo(1);
        assertThat(target.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("A black permanent gives the target creature +1/+1")
    void boostsByOneWithBlackPermanentOnly() {
        Permanent target = setUpWithTarget(new MournfulZombie());

        resolveTarget(target);

        assertThat(target.getPowerModifier()).isEqualTo(1);
        assertThat(target.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Blue and black permanents give the target creature +5/+5")
    void boostsByFiveWithBlueAndBlackPermanents() {
        harness.addToBattlefield(player1, new AnaSanctuary());
        harness.addToBattlefield(player1, new GaeasSkyfolk());
        harness.addToBattlefield(player1, new MournfulZombie());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GladeGnarr());

        resolveTarget(target);

        assertThat(target.getPowerModifier()).isEqualTo(5);
        assertThat(target.getToughnessModifier()).isEqualTo(5);
    }

    @Test
    @DisplayName("One blue-black permanent gives the target creature +5/+5")
    void boostsByFiveWithOneBlueBlackPermanent() {
        harness.addToBattlefield(player1, new AnaSanctuary());
        harness.addToBattlefield(player1, new Cromat());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GladeGnarr());

        resolveTarget(target);

        assertThat(target.getPowerModifier()).isEqualTo(5);
        assertThat(target.getToughnessModifier()).isEqualTo(5);
    }

    @Test
    @DisplayName("Does not trigger without a blue or black permanent")
    void doesNotTriggerWithoutBlueOrBlackPermanent() {
        harness.addToBattlefield(player1, new AnaSanctuary());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GladeGnarr());

        advanceToUpkeep(player1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Does not trigger for blue or black permanents controlled by an opponent")
    void doesNotTriggerForOpponentsColoredPermanents() {
        harness.addToBattlefield(player1, new AnaSanctuary());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GladeGnarr());
        harness.addToBattlefield(player2, new GaeasSkyfolk());

        advanceToUpkeep(player1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Triggers only during the controller's upkeep")
    void triggersOnlyDuringControllersUpkeep() {
        harness.addToBattlefield(player1, new AnaSanctuary());
        harness.addToBattlefield(player1, new GaeasSkyfolk());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GladeGnarr());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Does not trigger when a qualifying permanent appears after upkeep begins")
    void doesNotTriggerWhenQualifyingPermanentAppearsAfterUpkeepBegins() {
        harness.addToBattlefield(player1, new AnaSanctuary());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GladeGnarr());

        advanceToUpkeep(player1);
        harness.addToBattlefield(player1, new GaeasSkyfolk());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Uses +5/+5 when the second color appears before resolution")
    void usesFiveWhenSecondColorAppearsBeforeResolution() {
        harness.addToBattlefield(player1, new AnaSanctuary());
        harness.addToBattlefield(player1, new GaeasSkyfolk());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GladeGnarr());

        advanceToUpkeep(player1);
        harness.addToBattlefield(player1, new MournfulZombie());
        chooseTarget(target);

        assertThat(target.getPowerModifier()).isEqualTo(5);
        assertThat(target.getToughnessModifier()).isEqualTo(5);
    }

    @Test
    @DisplayName("Uses +1/+1 when the second color leaves before resolution")
    void usesOneWhenSecondColorLeavesBeforeResolution() {
        harness.addToBattlefield(player1, new AnaSanctuary());
        harness.addToBattlefield(player1, new GaeasSkyfolk());
        Permanent blackPermanent = harness.addToBattlefieldAndReturn(player1, new MournfulZombie());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GladeGnarr());

        advanceToUpkeep(player1);
        gd.playerBattlefields.get(player1.getId()).remove(blackPermanent);
        chooseTarget(target);

        assertThat(target.getPowerModifier()).isEqualTo(1);
        assertThat(target.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Does nothing when the qualifying permanent leaves before resolution")
    void doesNothingWhenQualifyingPermanentLeavesBeforeResolution() {
        harness.addToBattlefield(player1, new AnaSanctuary());
        Permanent bluePermanent = harness.addToBattlefieldAndReturn(player1, new GaeasSkyfolk());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GladeGnarr());

        advanceToUpkeep(player1);
        gd.playerBattlefields.get(player1.getId()).remove(bluePermanent);
        chooseTarget(target);

        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Only creatures are legal targets")
    void onlyCreaturesAreLegalTargets() {
        Permanent sanctuary = harness.addToBattlefieldAndReturn(player1, new AnaSanctuary());
        harness.addToBattlefield(player1, new GaeasSkyfolk());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GladeGnarr());

        advanceToUpkeep(player1);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(target.getId()).doesNotContain(sanctuary.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent target = setUpWithTarget(new GaeasSkyfolk());

        resolveTarget(target);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
    }

    private Permanent setUpWithTarget(com.github.laxika.magicalvibes.model.Card supportPermanent) {
        harness.addToBattlefield(player1, new AnaSanctuary());
        harness.addToBattlefield(player1, supportPermanent);
        return harness.addToBattlefieldAndReturn(player2, new GladeGnarr());
    }

    private void resolveTarget(Permanent target) {
        advanceToUpkeep(player1);
        chooseTarget(target);
    }

    private void chooseTarget(Permanent target) {
        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(target.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
    }
}
