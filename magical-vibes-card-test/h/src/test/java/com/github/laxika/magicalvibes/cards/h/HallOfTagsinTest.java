package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.a.ArgothianOpportunist;
import com.github.laxika.magicalvibes.cards.e.EnergyRefractor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HallOfTagsin.class, ArgothianOpportunist.class, EnergyRefractor.class})
class HallOfTagsinTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping adds one colorless mana without using the stack")
    void tapForColorless() {
        harness.addToBattlefield(player1, new HallOfTagsin());
        GameData gd = harness.getGameData();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The filter ability spends {1} and adds one mana of the chosen color")
    void filterAddsChosenColor() {
        harness.addToBattlefield(player1, new HallOfTagsin());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        GameData gd = harness.getGameData();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "GREEN");

        var pool = gd.playerManaPools.get(player1.getId());
        assertThat(pool.get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(pool.get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The filter ability cannot be activated without {1}")
    void filterRequiresManaCost() {
        harness.addToBattlefield(player1, new HallOfTagsin());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The third ability creates a tapped Powerstone token")
    void createsTappedPowerstone() {
        createPowerstone();

        Permanent powerstone = findPermanent(player1, "Powerstone");
        assertThat(powerstone.isTapped()).isTrue();
        assertThat(powerstone.getCard().hasType(CardType.ARTIFACT)).isTrue();
        assertThat(powerstone.getCard().getSubtypes()).contains(CardSubtype.POWERSTONE);
    }

    @Test
    @DisplayName("Creating a Powerstone pays four mana and taps the land before resolving")
    void tokenCreationUsesStackAndPaysCosts() {
        harness.addToBattlefield(player1, new HallOfTagsin());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 2, null, null);

        assertThat(findPermanent(player1, "Hall of Tagsin").isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.stack).hasSize(1);
        assertThat(countPermanents(player1, "Powerstone")).isZero();
        harness.passBothPriorities();
        assertThat(countPermanents(player1, "Powerstone")).isEqualTo(1);
        assertThat(countPermanents(player2, "Powerstone")).isZero();
    }

    @Test
    @DisplayName("The Powerstone ability cannot be activated with only three mana")
    void tokenCreationRequiresFourMana() {
        harness.addToBattlefield(player1, new HallOfTagsin());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
        assertThat(countPermanents(player1, "Powerstone")).isZero();
    }

    @Test
    @DisplayName("Tapping for mana prevents activating either of the other tap abilities")
    void manaAbilityPaysTapCost() {
        harness.addToBattlefield(player1, new HallOfTagsin());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(findPermanent(player1, "Hall of Tagsin").isTapped()).isTrue();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"WHITE", "BLUE", "BLACK", "RED", "GREEN"})
    @DisplayName("The filter ability can produce each of the five colors")
    void filterCanProduceEveryColor(ManaColor color) {
        harness.addToBattlefield(player1, new HallOfTagsin());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, color.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(findPermanent(player1, "Hall of Tagsin").isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A new Powerstone must untap before producing restricted mana")
    void powerstoneProducesRestrictedMana() {
        createPowerstone();
        assertThatThrownBy(() -> harness.activateAbility(player1, 1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.performUntapStep(player1);
        harness.activateAbility(player1, 1, 0, null, null);

        assertThat(findPermanent(player1, "Powerstone").isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getPowerstoneOnlyColorless()).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Powerstone mana can pay for Hall of Tagsin's filter ability")
    void powerstoneManaCanPayForFilter() {
        createPowerstone();
        harness.performUntapStep(player1);
        harness.activateAbility(player1, 1, 0, null, null);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).getPowerstoneOnlyColorless()).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(findPermanent(player1, "Hall of Tagsin").isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Powerstone mana cannot pay the generic cost of a nonartifact spell")
    void powerstoneManaCannotCastNonartifact() {
        createPowerstone();
        harness.performUntapStep(player1);
        harness.activateAbility(player1, 1, 0, null, null);
        harness.setHand(player1, List.of(new ArgothianOpportunist()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Argothian Opportunist");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Powerstone mana can pay for an artifact spell")
    void powerstoneManaCanCastArtifact() {
        createPowerstone();
        harness.performUntapStep(player1);
        harness.activateAbility(player1, 1, 0, null, null);
        harness.setHand(player1, List.of(new EnergyRefractor()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Energy Refractor");
        assertThat(gd.playerManaPools.get(player1.getId()).getPowerstoneOnlyColorless()).isZero();
    }

    private void createPowerstone() {
        harness.addToBattlefield(player1, new HallOfTagsin());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
    }
}
