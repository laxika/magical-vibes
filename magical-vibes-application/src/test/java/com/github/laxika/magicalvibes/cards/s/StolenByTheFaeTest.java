package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.t.TurnIntoAPumpkin;
import com.github.laxika.magicalvibes.cards.y.YouthfulKnight;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StolenByTheFae.class, YouthfulKnight.class, StonecoilSerpent.class, TurnIntoAPumpkin.class})
class StolenByTheFaeTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a creature with mana value X and creates X flying Faeries")
    void returnsMatchingCreatureAndCreatesFaeries() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new YouthfulKnight());
        harness.setHand(player1, List.of(new StolenByTheFae()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castAndResolveSorcery(player1, 0, 2, target.getId());

        harness.assertNotOnBattlefield(player2, "Youthful Knight");
        harness.assertInHand(player2, "Youthful Knight");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .hasSize(2)
                .allSatisfy(faerie -> {
                    assertThat(faerie.getCard().getColor()).isEqualTo(CardColor.BLUE);
                    assertThat(faerie.getCard().getSubtypes()).contains(CardSubtype.FAERIE);
                    assertThat(faerie.getCard().getKeywords()).contains(Keyword.FLYING);
                    assertThat(gqs.getEffectivePower(gd, faerie)).isEqualTo(1);
                    assertThat(gqs.getEffectiveToughness(gd, faerie)).isEqualTo(1);
                });
    }

    @Test
    @DisplayName("Cannot target a creature whose mana value is different from X")
    void cannotTargetCreatureWithDifferentManaValue() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new YouthfulKnight());
        harness.setHand(player1, List.of(new StolenByTheFae()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 1, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature with mana value X");
    }

    @Test
    void canReturnOwnCreatureAndCreateFaeries() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new YouthfulKnight());
        harness.setHand(player1, List.of(new StolenByTheFae()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castAndResolveSorcery(player1, 0, 2, target.getId());

        harness.assertInHand(player1, "Youthful Knight");
        harness.assertNotOnBattlefield(player1, "Youthful Knight");
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2)
                .allSatisfy(faerie -> assertThat(faerie.getCard().isToken()).isTrue());
    }

    @Test
    void xZeroReturnsCreatureWithXInManaCostWithoutCreatingTokens() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new StonecoilSerpent());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        harness.setHand(player1, List.of(new StolenByTheFae()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveSorcery(player1, 0, 0, target.getId());

        harness.assertInHand(player2, "Stonecoil Serpent");
        harness.assertNotOnBattlefield(player2, "Stonecoil Serpent");
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }

    @Test
    void createsNoFaeriesWhenTargetLeavesBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new YouthfulKnight());
        harness.setHand(player1, List.of(new StolenByTheFae()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.setHand(player2, List.of(new TurnIntoAPumpkin()));
        harness.setLibrary(player2, List.of(new YouthfulKnight()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        harness.castSorcery(player1, 0, 2, target.getId());
        harness.castInstant(player2, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInHand(player2, "Youthful Knight");
        harness.assertInGraveyard(player1, "Stolen by the Fae");
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }

    @Test
    void returnsStolenCreatureToOwnerRatherThanController() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new YouthfulKnight());
        gd.stolenCreatures.put(target.getId(), player2.getId());
        harness.setHand(player1, List.of(new StolenByTheFae()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castAndResolveSorcery(player1, 0, 2, target.getId());

        harness.assertInHand(player2, "Youthful Knight");
        harness.assertNotInHand(player1, "Youthful Knight");
        harness.assertNotOnBattlefield(player1, "Youthful Knight");
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    void xZeroCanReturnCreatureTokenWithoutReplacingIt() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new YouthfulKnight());
        harness.setHand(player1, List.of(new StolenByTheFae(), new StolenByTheFae()));
        harness.addMana(player1, ManaColor.BLUE, 6);
        harness.castAndResolveSorcery(player1, 0, 2, target.getId());
        Permanent faerie = gd.playerBattlefields.get(player1.getId()).getFirst();

        harness.castAndResolveSorcery(player1, 0, 0, faerie.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1)
                .noneMatch(permanent -> permanent.getId().equals(faerie.getId()));
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }
}
