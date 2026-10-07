package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SpawningBed.class})
class SpawningBedTest extends BaseCardTest {

    @Test
    @DisplayName("Spawning Bed taps for colorless mana")
    void tapsForColorlessMana() {
        harness.addToBattlefield(player1, new SpawningBed());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .singleElement()
                .matches(Permanent::isTapped);
    }

    @Test
    @DisplayName("Spawning Bed creates three Scions that can be sacrificed for mana")
    void createsSacrificeForManaScions() {
        harness.addToBattlefield(player1, new SpawningBed());
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .hasSize(3);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() instanceof SpawningBed);

        Permanent scion = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        int scionIndex = gd.playerBattlefields.get(player1.getId()).indexOf(scion);
        harness.activateAbility(player1, scionIndex, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .hasSize(2);
    }

    @Test
    void sacrificesLandAndPaysManaBeforeTokensResolve() {
        harness.addToBattlefield(player1, new SpawningBed());
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Spawning Bed");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(3);
        assertThat(gd.playerBattlefields.get(player1.getId())).allSatisfy(token -> {
            assertThat(token.getCard().isToken()).isTrue();
            assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
            assertThat(token.getCard().getPower()).isEqualTo(1);
            assertThat(token.getCard().getToughness()).isEqualTo(1);
            assertThat(token.getCard().getColor()).isNull();
            assertThat(token.getCard().getColors()).isEmpty();
            assertThat(token.getCard().getSubtypes())
                    .containsExactlyInAnyOrder(CardSubtype.ELDRAZI, CardSubtype.SCION);
            assertThat(token.isTapped()).isFalse();
        });
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotCreateTokensWithOnlyFiveMana() {
        harness.addToBattlefield(player1, new SpawningBed());
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Spawning Bed");
        harness.assertNotInGraveyard(player1, "Spawning Bed");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotCreateTokensAfterTappingForMana() {
        harness.addToBattlefield(player1, new SpawningBed());
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.activateAbility(player1, 0, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Spawning Bed");
        harness.assertNotInGraveyard(player1, "Spawning Bed");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void allNewScionsCanBeSacrificedForManaEvenWhileTapped() {
        harness.addToBattlefield(player1, new SpawningBed());
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        for (int i = 0; i < 3; i++) {
            Permanent scion = gd.playerBattlefields.get(player1.getId()).getFirst();
            scion.setTapped(true);
            harness.activateAbility(player1, 0, 0, null, null);
            assertThat(gd.stack).isEmpty();
            assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS))
                    .isEqualTo(i + 1);
        }

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }
}
