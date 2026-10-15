package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.cards.s.SoulWarden;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NissaWorldwaker.class, Forest.class, Mountain.class, Plains.class, RuneclawBear.class, SoulWarden.class})
class NissaWorldwakerTest extends BaseCardTest {

    @Test
    @DisplayName("+1 turns a land you control into a 4/4 Elemental with trample that is still a land")
    void plusOneAnimatesOwnLand() {
        Permanent nissa = addReadyNissa(player1, 3);
        Permanent forest = addLand(player1, new Forest());

        harness.activateAbility(player1, 0, 0, forest.getId(), null);
        harness.passBothPriorities();

        assertThat(nissa.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
        assertThat(gqs.isCreature(gd, forest)).isTrue();
        assertThat(forest.getEffectivePower()).isEqualTo(4);
        assertThat(forest.getEffectiveToughness()).isEqualTo(4);
        assertThat(forest.hasKeyword(Keyword.TRAMPLE)).isTrue();
        assertThat(forest.getGrantedSubtypes()).contains(CardSubtype.ELEMENTAL);
        assertThat(gqs.isLand(gd, forest)).isTrue();
    }

    @Test
    @DisplayName("+1 animation is permanent and survives the end of the turn")
    void plusOneAnimationSurvivesEndOfTurn() {
        addReadyNissa(player1, 3);
        Permanent forest = addLand(player1, new Forest());

        harness.activateAbility(player1, 0, 0, forest.getId(), null);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.CLEANUP);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, forest)).isTrue();
        assertThat(forest.getEffectivePower()).isEqualTo(4);
    }

    @Test
    @DisplayName("+1 cannot animate a land an opponent controls")
    void plusOneCannotTargetOpponentLand() {
        addReadyNissa(player1, 3);
        Permanent oppForest = addLand(player2, new Forest());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, oppForest.getId(), null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Second +1 untaps up to four target Forests, including an opponent's")
    void plusOneUntapsForests() {
        Permanent nissa = addReadyNissa(player1, 3);
        Permanent forest1 = addLand(player1, new Forest());
        Permanent forest2 = addLand(player1, new Forest());
        Permanent oppForest = addLand(player2, new Forest());
        forest1.tap();
        forest2.tap();
        oppForest.tap();

        harness.activateAbilityWithMultiTargets(player1, 0, 1,
                List.of(forest1.getId(), forest2.getId(), oppForest.getId()));
        harness.passBothPriorities();

        assertThat(nissa.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
        assertThat(forest1.isTapped()).isFalse();
        assertThat(forest2.isTapped()).isFalse();
        assertThat(oppForest.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Second +1 cannot target a non-Forest land")
    void plusOneUntapRejectsNonForest() {
        addReadyNissa(player1, 3);
        Permanent mountain = addLand(player1, new Mountain());
        mountain.tap();

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(player1, 0, 1,
                List.of(mountain.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("−7 offers only basic land cards from the library")
    void ultimateOffersOnlyBasicLands() {
        addReadyNissa(player1, 7);
        setupLibrary();

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .hasSize(2)
                .allMatch(c -> c.hasType(CardType.LAND));
    }

    @Test
    @DisplayName("−7 puts the found basic lands onto the battlefield as 4/4 Elementals with trample")
    void ultimatePutsLandsOntoBattlefieldAnimated() {
        addReadyNissa(player1, 7);
        setupLibrary();

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        List<Permanent> newLands = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().hasType(CardType.LAND))
                .toList();
        assertThat(newLands).hasSize(2);
        assertThat(newLands).allSatisfy(land -> {
            assertThat(gqs.isCreature(gd, land)).isTrue();
            assertThat(land.getEffectivePower()).isEqualTo(4);
            assertThat(land.getEffectiveToughness()).isEqualTo(4);
            assertThat(land.hasKeyword(Keyword.TRAMPLE)).isTrue();
            assertThat(land.getGrantedSubtypes()).contains(CardSubtype.ELEMENTAL);
            assertThat(land.isTapped()).isFalse();
        });
    }

    @Test
    @DisplayName("−7 stopping the search early animates only the lands actually found")
    void ultimateStoppingEarlyAnimatesOnlyFoundLands() {
        addReadyNissa(player1, 7);
        setupLibrary();

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, -1);

        List<Permanent> newLands = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().hasType(CardType.LAND))
                .toList();
        assertThat(newLands).hasSize(1);
        assertThat(gqs.isCreature(gd, newLands.getFirst())).isTrue();
        assertThat(newLands.getFirst().getEffectivePower()).isEqualTo(4);
    }

    @Test
    @DisplayName("−7 cannot be activated with insufficient loyalty")
    void ultimateNeedsSevenLoyalty() {
        addReadyNissa(player1, 6);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough loyalty");
    }

    @Test
    @DisplayName("Second +1 can choose zero Forest targets")
    void plusOneUntapAllowsZeroTargets() {
        Permanent nissa = addReadyNissa(player1, 3);

        harness.activateAbilityWithMultiTargets(player1, 0, 1, List.of());
        harness.passBothPriorities();

        assertThat(nissa.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Ultimate does not animate lands already on the battlefield")
    void ultimateDoesNotAnimateExistingLands() {
        addReadyNissa(player1, 7);
        Permanent existing = addLand(player1, new Forest());
        harness.setLibrary(player1, List.of(new Plains()));

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gqs.isCreature(gd, existing)).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> !p.getId().equals(existing.getId()))
                .singleElement().satisfies(land -> {
                    assertThat(gqs.isCreature(gd, land)).isTrue();
                    assertThat(land.getEffectivePower()).isEqualTo(4);
                });
    }

    @Test
    @DisplayName("Ultimate can find zero lands even when basic lands are available")
    void ultimateCanFindZeroLands() {
        addReadyNissa(player1, 7);
        setupLibrary();

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Ultimate lands enter before becoming creatures and do not trigger Soul Warden")
    void ultimateDoesNotTriggerCreatureEntryAbilities() {
        addReadyNissa(player1, 7);
        harness.addToBattlefield(player2, new SoulWarden());
        harness.setLife(player2, 20);
        harness.setLibrary(player1, List.of(new Forest(), new Plains()));

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2)
                .allSatisfy(land -> assertThat(gqs.isCreature(gd, land)).isTrue());
    }

    private Permanent addReadyNissa(Player player, int loyalty) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new NissaWorldwaker());
        perm.setCounterCount(CounterType.LOYALTY, loyalty);
        perm.setSummoningSick(false);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }

    private Permanent addLand(Player player, Card land) {
        return harness.addToBattlefieldAndReturn(player, land);
    }

    private void setupLibrary() {
        harness.setLibrary(player1, List.of(new Forest(), new Plains(), new RuneclawBear()));
    }
}
