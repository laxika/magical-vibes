package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.m.MotherBear;
import com.github.laxika.magicalvibes.cards.o.OnThinIce;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HallOfHeliodsGenerosity.class, OnThinIce.class, MotherBear.class})
class HallOfHeliodsGenerosityTest extends BaseCardTest {

    @Test
    @DisplayName("Produces one colorless mana")
    void producesColorlessMana() {
        Permanent hall = harness.addToBattlefieldAndReturn(player1, new HallOfHeliodsGenerosity());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(hall.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Puts a target enchantment card from the graveyard on top of the library")
    void putsTargetEnchantmentOnTopOfLibrary() {
        Permanent hall = harness.addToBattlefieldAndReturn(player1, new HallOfHeliodsGenerosity());
        Card enchantment = new OnThinIce();
        harness.setGraveyard(player1, List.of(enchantment));
        harness.setLibrary(player1, List.of(new MotherBear()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbilityWithGraveyardTargets(player1, 0, 1, List.of(enchantment.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId()).getFirst().getId()).isEqualTo(enchantment.getId());
        assertThat(hall.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Rejects a non-enchantment graveyard target")
    void rejectsNonEnchantmentTarget() {
        harness.addToBattlefield(player1, new HallOfHeliodsGenerosity());
        Card creature = new MotherBear();
        harness.setGraveyard(player1, List.of(creature));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, 0, 1, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target an enchantment in an opponent's graveyard")
    void rejectsOpponentsGraveyard() {
        harness.addToBattlefield(player1, new HallOfHeliodsGenerosity());
        Card enchantment = new OnThinIce();
        harness.setGraveyard(player2, List.of(enchantment));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, 0, 1, List.of(enchantment.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Requires exactly one enchantment target")
    void rejectsMissingTarget() {
        harness.addToBattlefield(player1, new HallOfHeliodsGenerosity());
        harness.setGraveyard(player1, List.of(new OnThinIce()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, 0, 1, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Does not return another enchantment when the target leaves the graveyard")
    void targetLeavingGraveyardDoesNotReturnAnotherCard() {
        Permanent hall = harness.addToBattlefieldAndReturn(player1, new HallOfHeliodsGenerosity());
        Card target = new OnThinIce();
        Card other = new OnThinIce();
        Card libraryCard = new MotherBear();
        harness.setGraveyard(player1, List.of(target, other));
        harness.setLibrary(player1, List.of(libraryCard));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbilityWithGraveyardTargets(player1, 0, 1, List.of(target.getId()));
        harness.setGraveyard(player1, List.of(other));
        harness.setExile(player1, List.of(target));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(other);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
        assertThat(hall.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
    }

    @Test
    @DisplayName("Can return an Aura to an empty library without an attachment")
    void returnsAuraToEmptyLibrary() {
        harness.addToBattlefield(player1, new HallOfHeliodsGenerosity());
        Card enchantment = new OnThinIce();
        harness.setGraveyard(player1, List.of(enchantment));
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbilityWithGraveyardTargets(player1, 0, 1, List.of(enchantment.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(enchantment);
        harness.assertNotOnBattlefield(player1, "On Thin Ice");
    }
}
