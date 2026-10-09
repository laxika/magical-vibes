package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.b.BonescytheSliver;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.Humble;
import com.github.laxika.magicalvibes.cards.u.UniversalAutomaton;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Dregscape Sliver")
@CardUsed({DregscapeSliver.class, GrizzlyBears.class, BonescytheSliver.class, Humble.class, UniversalAutomaton.class, Unsummon.class})
class DregscapeSliverTest extends BaseCardTest {

    @Test
    @DisplayName("Grants unearth to Sliver creature cards in its controller's graveyard")
    void grantsUnearthToSliversInGraveyard() {
        harness.addToBattlefield(player1, new DregscapeSliver());
        harness.setGraveyard(player1, List.of(new BonescytheSliver()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Bonescythe Sliver");
        assertThat(gqs.hasKeyword(gd, returned, Keyword.HASTE)).isTrue();
        harness.assertNotInGraveyard(player1, "Bonescythe Sliver");
    }

    @Test
    @DisplayName("Does not grant unearth to non-Sliver creature cards")
    void doesNotGrantUnearthToNonSlivers() {
        harness.addToBattlefield(player1, new DregscapeSliver());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can unearth itself without a Dregscape Sliver on the battlefield")
    void unearthsItself() {
        harness.setGraveyard(player1, List.of(new DregscapeSliver()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Dregscape Sliver");
        assertThat(gqs.hasKeyword(gd, returned, Keyword.HASTE)).isTrue();
        harness.assertNotInGraveyard(player1, "Dregscape Sliver");
    }

    @Test
    @DisplayName("Unearthing Dregscape Sliver enables unearthing another Sliver")
    void returnedSliverGrantsUnearth() {
        harness.setGraveyard(player1, List.of(new DregscapeSliver(), new BonescytheSliver()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();
        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Dregscape Sliver");
        harness.assertOnBattlefield(player1, "Bonescythe Sliver");
    }

    @Test
    @DisplayName("Both native and granted unearth exile the returned Slivers at the next end step")
    void unearthExilesAtNextEndStep() {
        harness.setGraveyard(player1, List.of(new DregscapeSliver(), new BonescytheSliver()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();
        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Dregscape Sliver");
        harness.assertNotOnBattlefield(player1, "Bonescythe Sliver");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(c -> c.getName())
                .contains("Dregscape Sliver", "Bonescythe Sliver");
        harness.assertNotInGraveyard(player1, "Dregscape Sliver");
        harness.assertNotInGraveyard(player1, "Bonescythe Sliver");
    }

    @Test
    @DisplayName("Does not grant unearth to Slivers in an opponent's graveyard")
    void doesNotGrantUnearthToOpponentsSlivers() {
        harness.addToBattlefield(player1, new DregscapeSliver());
        harness.setGraveyard(player2, List.of(new BonescytheSliver()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player2, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no graveyard activated ability");
    }

    @Test
    @DisplayName("Other Slivers have no unearth while Dregscape Sliver is only in the graveyard")
    void doesNotGrantUnearthFromGraveyard() {
        harness.setGraveyard(player1, List.of(new DregscapeSliver(), new BonescytheSliver()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 1))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no graveyard activated ability");
    }

    @Test
    @DisplayName("Granted unearth cannot be activated outside a main phase")
    void grantedUnearthRequiresSorceryTiming() {
        harness.addToBattlefield(player1, new DregscapeSliver());
        harness.setGraveyard(player1, List.of(new BonescytheSliver()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceStep(TurnStep.END_STEP);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
        harness.assertInGraveyard(player1, "Bonescythe Sliver");
    }

    @Test
    @DisplayName("Changeling creature cards in the graveyard receive unearth as Slivers")
    void grantsUnearthToChangeling() {
        harness.addToBattlefield(player1, new DregscapeSliver());
        harness.setGraveyard(player1, List.of(new UniversalAutomaton()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Universal Automaton");
        harness.assertNotInGraveyard(player1, "Universal Automaton");
        assertThat(findPermanent(player1, "Universal Automaton").getGrantedKeywords())
                .contains(Keyword.HASTE);
    }

    @Test
    @DisplayName("Losing all abilities stops Dregscape Sliver from granting unearth")
    void losingAbilitiesStopsUnearthGrant() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new DregscapeSliver());
        harness.setGraveyard(player1, List.of(new BonescytheSliver()));
        harness.setHand(player1, List.of(new Humble()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castAndResolveInstant(player1, 0, source.getId());

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no graveyard activated ability");
        harness.assertInGraveyard(player1, "Bonescythe Sliver");
    }

    @Test
    @DisplayName("Removing the grant after activation does not stop an unearth ability already on the stack")
    void activatedUnearthSurvivesLossOfGrant() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new DregscapeSliver());
        harness.setGraveyard(player1, List.of(new BonescytheSliver()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateGraveyardAbility(player1, 0);
        harness.setHand(player2, List.of(new Humble()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0, source.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Bonescythe Sliver");
        harness.assertNotInGraveyard(player1, "Bonescythe Sliver");
    }

    @Test
    @DisplayName("An unearthed Sliver is exiled instead of returning to its owner's hand")
    void unearthedSliverIsExiledInsteadOfBounced() {
        harness.addToBattlefield(player1, new DregscapeSliver());
        harness.setGraveyard(player1, List.of(new BonescytheSliver()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player2, 0, harness.getPermanentId(player1, "Bonescythe Sliver"));

        harness.assertNotOnBattlefield(player1, "Bonescythe Sliver");
        harness.assertNotInHand(player1, "Bonescythe Sliver");
        harness.assertNotInGraveyard(player1, "Bonescythe Sliver");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(c -> c.getName()).contains("Bonescythe Sliver");
    }

    @Test
    @DisplayName("Unearth already activated resolves after Dregscape Sliver leaves, but no new unearth can be activated")
    void activatedUnearthSurvivesSourceLeavingBattlefield() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new DregscapeSliver());
        harness.setGraveyard(player1, List.of(new BonescytheSliver(), new BonescytheSliver()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateGraveyardAbility(player1, 0);
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player2, 0, source.getId());
        harness.passBothPriorities();

        harness.assertInHand(player1, "Dregscape Sliver");
        harness.assertNotOnBattlefield(player1, "Dregscape Sliver");
        harness.assertOnBattlefield(player1, "Bonescythe Sliver");
        harness.assertInGraveyard(player1, "Bonescythe Sliver");
        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no graveyard activated ability");
    }
}
