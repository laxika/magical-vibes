package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.o.ObsidianBattleAxe;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SpringjackPasture.class, ObsidianBattleAxe.class})
class SpringjackPastureTest extends BaseCardTest {

    @Test
    @DisplayName("First ability adds {C}")
    void firstAbilityAddsColorless() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Permanent pasture = addPasture();
        int idx = indexOf(pasture);

        harness.activateAbility(player1, idx, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(pasture.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Second ability creates a 0/1 white Goat token for {4}")
    void secondAbilityCreatesGoat() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        Permanent pasture = addPasture();
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        int idx = indexOf(pasture);
        harness.activateAbility(player1, idx, 1, null, null);
        harness.passBothPriorities();

        Permanent goat = findPermanent(player1, "Goat");
        assertThat(goat.getCard().getPower()).isEqualTo(0);
        assertThat(goat.getCard().getToughness()).isEqualTo(1);
        assertThat(goat.getCard().getSubtypes()).contains(CardSubtype.GOAT);
        assertThat(goat.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(goat.getCard().isToken()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(pasture.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Third ability: sacrifice 2 Goats adds 2 mana of chosen color and gains 2 life")
    void thirdAbilitySacrificesGoatsForManaAndLife() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Permanent pasture = addPasture();
        Permanent goat1 = addGoat(player1);
        Permanent goat2 = addGoat(player1);
        int lifeBefore = gd.getLife(player1.getId());

        int idx = indexOf(pasture);
        // X=2: exactly two Goats available -> both auto-sacrificed
        harness.activateAbility(player1, idx, 2, 2, null);

        // Mana ability -> prompts color choice, does not use the stack
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        assertThat(gd.stack).isEmpty();
        // Both Goats sacrificed as cost; life already gained during resolution
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(goat1, goat2);
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 2);

        harness.handleListChoice(player1, "RED");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(2);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(pasture.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Third ability chooses which Goats to sacrifice when more than X are available")
    void thirdAbilityInteractiveGoatChoice() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Permanent pasture = addPasture();
        Permanent goat1 = addGoat(player1);
        Permanent goat2 = addGoat(player1);
        Permanent goat3 = addGoat(player1);

        int idx = indexOf(pasture);
        // X=2 with 3 Goats -> player chooses which two
        harness.activateAbility(player1, idx, 2, 2, null);
        harness.handlePermanentChosen(player1, goat1.getId());
        harness.handlePermanentChosen(player1, goat2.getId());
        harness.handleListChoice(player1, "GREEN");

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(goat1, goat2);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(goat3);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
    }

    @Test
    @DisplayName("Third ability fails when fewer Goats than X are available")
    void thirdAbilityFailsWithoutEnoughGoats() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Permanent pasture = addPasture();
        addGoat(player1);

        int idx = indexOf(pasture);
        // X=2 but only one Goat
        assertThatThrownBy(() -> harness.activateAbility(player1, idx, 2, 2, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Zero Goats can be sacrificed without producing mana or gaining life")
    void thirdAbilityAllowsZeroGoats() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Permanent pasture = addPasture();
        int lifeBefore = gd.getLife(player1.getId());

        harness.activateAbility(player1, indexOf(pasture), 2, 0, null);

        assertThat(pasture.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertLife(player1, lifeBefore);
        for (ManaColor color : ManaColor.values()) {
            assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isZero();
        }
    }

    @Test
    @DisplayName("Goats controlled by an opponent cannot pay the sacrifice cost")
    void thirdAbilityCannotSacrificeOpponentsGoats() {
        Permanent pasture = addPasture();
        Permanent opposingGoat = addGoat(player2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(pasture), 2, 1, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opposingGoat);
        assertThat(pasture.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Creating a Goat requires four mana and uses the stack")
    void tokenAbilityRequiresFourManaAndUsesStack() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Permanent pasture = addPasture();
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(pasture), 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(pasture.isTapped()).isFalse();
        harness.assertNotOnBattlefield(player1, "Goat");

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, indexOf(pasture), 1, null, null);

        assertThat(gd.stack).hasSize(1);
        harness.assertNotOnBattlefield(player1, "Goat");
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Goat");
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"WHITE", "BLUE", "BLACK", "RED", "GREEN"})
    @DisplayName("All five colors are available, with the whole batch in one color")
    void thirdAbilityCanProduceEachColor(ManaColor color) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Permanent pasture = addPasture();
        addGoat(player1);
        addGoat(player1);
        int lifeBefore = gd.getLife(player1.getId());

        harness.activateAbility(player1, indexOf(pasture), 2, 2, null);
        harness.handleListChoice(player1, color.name());

        for (ManaColor poolColor : ManaColor.values()) {
            assertThat(gd.playerManaPools.get(player1.getId()).get(poolColor))
                    .isEqualTo(poolColor == color ? 2 : 0);
        }
        harness.assertLife(player1, lifeBefore + 2);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
    @Test
    @DisplayName("A noncreature kindred Goat can be sacrificed")
    @CardUsed({SpringjackPasture.class, ObsidianBattleAxe.class})
    void thirdAbilityCanSacrificeNoncreatureKindredGoat() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Permanent pasture = addPasture();
        ObsidianBattleAxe axe = new ObsidianBattleAxe();
        // Model a resolved Artificial Evolution changing Warrior to Goat on this kindred artifact.
        axe.setSubtypes(List.of(CardSubtype.GOAT, CardSubtype.EQUIPMENT));
        Permanent goat = harness.addToBattlefieldAndReturn(player1, axe);
        int lifeBefore = gd.getLife(player1.getId());

        harness.activateAbility(player1, indexOf(pasture), 2, 1, null);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(goat);
        harness.assertInGraveyard(player1, "Obsidian Battle-Axe");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        harness.assertLife(player1, lifeBefore + 1);
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addPasture() {
        return harness.addToBattlefieldAndReturn(player1, new SpringjackPasture());
    }

    private Permanent addGoat(Player player) {
        Permanent pasture = gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard() instanceof SpringjackPasture)
                .findFirst()
                .orElseGet(() -> harness.addToBattlefieldAndReturn(player, new SpringjackPasture()));
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player, ManaColor.COLORLESS, 4);
        harness.activateAbility(player, gd.playerBattlefields.get(player.getId()).indexOf(pasture), 1, null, null);
        harness.passBothPriorities();
        pasture.untap();
        return gd.playerBattlefields.get(player.getId()).getLast();
    }

    private int indexOf(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
