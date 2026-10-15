package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.t.TempleOfCultivation;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OjerKaslemDeepestGrowth.class, TempleOfCultivation.class, GrizzlyBears.class,
        Forest.class, Shock.class, Murder.class})
class OjerKaslemDeepestGrowthTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage reveals that many cards and may put a creature and a land onto the battlefield")
    void putsCreatureAndLandOntoBattlefieldAfterCombatDamage() {
        Permanent ojer = addCreatureReady(player1, new OjerKaslemDeepestGrowth());
        ojer.setAttacking(true);
        harness.setLibrary(player1, List.of(
                new GrizzlyBears(), new Forest(), new Shock(), new Shock(), new Shock(), new Shock()));

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        harness.handleCardChosen(player1, 0);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof GrizzlyBears)
                .anyMatch(permanent -> permanent.getCard() instanceof Forest);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(4);
    }

    @Test
    @DisplayName("Returns tapped and transformed under its owner's control when it dies")
    void returnsTappedAndTransformedWhenItDies() {
        Permanent ojer = harness.addToBattlefieldAndReturn(player1, new OjerKaslemDeepestGrowth());
        destroyOjer(ojer);

        Permanent temple = findPermanent(player1, "Temple of Cultivation");
        assertThat(temple.isTapped()).isTrue();
        assertThat(temple.isTransformed()).isTrue();
    }

    @Test
    @DisplayName("Transforms back when its controller controls at least ten permanents")
    void transformsBackWithTenPermanents() {
        Permanent temple = returnOjerAsTemple();
        temple.untap();
        addForests(9);
        prepareSorcerySpeedActivation();
        addTransformMana();

        harness.activateAbility(player1, battlefieldIndex(temple), 1, null, null);
        harness.passBothPriorities();

        assertThat(temple.getCard()).isInstanceOf(OjerKaslemDeepestGrowth.class);
        assertThat(temple.isTransformed()).isFalse();
    }

    @Test
    @DisplayName("Cannot transform back with fewer than ten permanents")
    void cannotTransformBackWithFewerThanTenPermanents() {
        Permanent temple = returnOjerAsTemple();
        temple.untap();
        addForests(8);
        prepareSorcerySpeedActivation();
        addTransformMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(temple), 1, null, null))
                .isInstanceOf(RuntimeException.class);
        assertThat(temple.getCard()).isInstanceOf(TempleOfCultivation.class);
        assertThat(temple.isTapped()).isFalse();
    }

    @Test
    @DisplayName("May decline both the creature and the land")
    void mayDeclineBothSelections() {
        Permanent ojer = addCreatureReady(player1, new OjerKaslemDeepestGrowth());
        ojer.setAttacking(true);
        GrizzlyBears creature = new GrizzlyBears();
        Forest land = new Forest();
        harness.setLibrary(player1, List.of(creature, land));

        resolveCombat();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(ojer);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(creature, land);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("A short library can yield just a land without requiring a creature")
    void mayPutOnlyLandFromShortLibrary() {
        Permanent ojer = addCreatureReady(player1, new OjerKaslemDeepestGrowth());
        ojer.setAttacking(true);
        Forest land = new Forest();
        Shock unchosen = new Shock();
        harness.setLibrary(player1, List.of(land, unchosen));

        resolveCombat();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Forest");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(unchosen);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("A selected creature still enters when the land is declined")
    void mayPutOnlyCreatureAndBottomTheRest() {
        Permanent ojer = addCreatureReady(player1, new OjerKaslemDeepestGrowth());
        ojer.setAttacking(true);
        GrizzlyBears creature = new GrizzlyBears();
        Forest land = new Forest();
        Shock unchosen = new Shock();
        harness.setLibrary(player1, List.of(creature, land, new Shock(), new Shock(), new Shock(), new Shock(), unchosen));

        resolveCombat();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, -1);

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Forest");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(6).first().isSameAs(unchosen);
        assertThat(gd.playerDecks.get(player1.getId())).contains(land);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Temple can tap for green without controlling ten permanents")
    void templeAddsGreenMana() {
        Permanent temple = returnOjerAsTemple();
        temple.untap();
        prepareSorcerySpeedActivation();

        harness.activateAbility(player1, battlefieldIndex(temple), 0, null, null);

        assertThat(temple.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Temple cannot transform outside a main phase even with ten permanents")
    void cannotTransformDuringCombat() {
        Permanent temple = returnOjerAsTemple();
        temple.untap();
        addForests(9);
        prepareSorcerySpeedActivation();
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        addTransformMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(temple), 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(temple.getCard()).isInstanceOf(TempleOfCultivation.class);
        assertThat(temple.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The ten-permanent restriction is checked on activation, not resolution")
    void transformsEvenIfPermanentCountDropsBeforeResolution() {
        Permanent temple = returnOjerAsTemple();
        temple.untap();
        addForests(8);
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        prepareSorcerySpeedActivation();
        addTransformMana();
        harness.activateAbility(player1, battlefieldIndex(temple), 1, null, null);
        harness.setHand(player2, List.of(new Murder()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0, creature.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(9);
        harness.passBothPriorities();

        assertThat(temple.getCard()).isInstanceOf(OjerKaslemDeepestGrowth.class);
        assertThat(temple.isTapped()).isTrue();
    }
    private void destroyOjer(Permanent ojer) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Murder()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0, ojer.getId());
        harness.passBothPriorities();
    }

    private Permanent returnOjerAsTemple() {
        Permanent ojer = harness.addToBattlefieldAndReturn(player1, new OjerKaslemDeepestGrowth());
        destroyOjer(ojer);
        return findPermanent(player1, "Temple of Cultivation");
    }

    private void addForests(int count) {
        for (int i = 0; i < count; i++) {
            harness.addToBattlefield(player1, new Forest());
        }
    }

    private void prepareSorcerySpeedActivation() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private void addTransformMana() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
