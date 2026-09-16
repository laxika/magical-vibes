package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({HallOfHeliodsGenerosity.class, HallOfGemstone.class, GrizzlyBears.class})
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
        Card enchantment = new HallOfGemstone();
        harness.setGraveyard(player1, List.of(enchantment));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
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
        harness.addToBattlefieldAndReturn(player1, new HallOfHeliodsGenerosity());
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, 0, 1, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }
}
