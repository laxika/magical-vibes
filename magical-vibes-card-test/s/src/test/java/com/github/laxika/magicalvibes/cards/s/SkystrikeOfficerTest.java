package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.ArgothianSprite;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SkystrikeOfficer.class, ArgothianSprite.class})
class SkystrikeOfficerTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking creates a 1/1 colorless Soldier artifact creature token")
    void attackingCreatesSoldierToken() {
        addCreatureReady(player1, new SkystrikeOfficer());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();

        assertThat(tokens).hasSize(1);
        Permanent token = tokens.getFirst();
        assertThat(token.getCard().getPower()).isEqualTo(1);
        assertThat(token.getCard().getToughness()).isEqualTo(1);
        assertThat(token.getCard().getColor()).isNull();
        assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(token.getCard().getAdditionalTypes()).contains(CardType.ARTIFACT);
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.SOLDIER);
    }

    @Test
    void onlyAttackingOfficersCreateTokensForTheirController() {
        addCreatureReady(player1, new SkystrikeOfficer());
        addCreatureReady(player1, new SkystrikeOfficer());
        addCreatureReady(player1, new SkystrikeOfficer());
        addCreatureReady(player2, new SkystrikeOfficer());

        declareAttackers(List.of(0, 1));
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()).toList()).hasSize(2);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    @DisplayName("Tapping three Soldiers draws a card")
    void tappingThreeSoldiersDrawsCard() {
        addCreatureReady(player1, new SkystrikeOfficer());
        addCreatureReady(player1, new SkystrikeOfficer());
        addCreatureReady(player1, new SkystrikeOfficer());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new SkystrikeOfficer()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).singleElement()
                .extracting(card -> card.getName())
                .isEqualTo("Skystrike Officer");
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.SOLDIER))
                .filter(Permanent::isTapped)
                .count()).isEqualTo(3);
    }

    @Test
    @DisplayName("The ability cannot be activated without three untapped Soldiers")
    void requiresThreeUntappedSoldiers() {
        addCreatureReady(player1, new SkystrikeOfficer());
        addCreatureReady(player1, new SkystrikeOfficer());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void newlyEnteredSoldiersCanPayTheCost() {
        harness.addToBattlefield(player1, new SkystrikeOfficer());
        harness.addToBattlefield(player1, new SkystrikeOfficer());
        harness.addToBattlefield(player1, new SkystrikeOfficer());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new SkystrikeOfficer()));

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).allMatch(Permanent::isTapped);
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void tappedOfficerCanActivateUsingOtherSoldiers() {
        Permanent officer = addCreatureReady(player1, new SkystrikeOfficer());
        officer.tap();
        addCreatureReady(player1, new SkystrikeOfficer());
        addCreatureReady(player1, new SkystrikeOfficer());
        addCreatureReady(player1, new SkystrikeOfficer());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new SkystrikeOfficer()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).allMatch(Permanent::isTapped);
    }

    @Test
    void controllerChoosesThreeSoldiersWithoutTappingTheOfficer() {
        Permanent officer = addCreatureReady(player1, new SkystrikeOfficer());
        Permanent first = addCreatureReady(player1, new SkystrikeOfficer());
        Permanent second = addCreatureReady(player1, new SkystrikeOfficer());
        Permanent third = addCreatureReady(player1, new SkystrikeOfficer());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new SkystrikeOfficer()));

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, first.getId());
        harness.handlePermanentChosen(player1, second.getId());
        harness.handlePermanentChosen(player1, third.getId());
        resolveAllTriggers();

        assertThat(officer.isTapped()).isFalse();
        assertThat(List.of(first, second, third)).allMatch(Permanent::isTapped);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void tappedSoldiersCannotPayTheCost() {
        Permanent officer = addCreatureReady(player1, new SkystrikeOfficer());
        Permanent second = addCreatureReady(player1, new SkystrikeOfficer());
        Permanent third = addCreatureReady(player1, new SkystrikeOfficer());
        third.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(officer.isTapped()).isFalse();
        assertThat(second.isTapped()).isFalse();
    }

    @Test
    void opposingSoldiersAndNonSoldiersCannotPayTheCost() {
        addCreatureReady(player1, new SkystrikeOfficer());
        addCreatureReady(player1, new SkystrikeOfficer());
        addCreatureReady(player1, new ArgothianSprite());
        addCreatureReady(player2, new SkystrikeOfficer());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(Permanent::isTapped);
        assertThat(gd.playerBattlefields.get(player2.getId())).noneMatch(Permanent::isTapped);
    }
}
