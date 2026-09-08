package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WoodwraithStrangler.class, GrizzlyBears.class, Shock.class})
class WoodwraithStranglerTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles a creature card from the graveyard to gain a regeneration shield")
    void exilesCreatureCardForShield() {
        harness.addToBattlefield(player1, new WoodwraithStrangler());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleGraveyardCardChosen(player1, 0);
        harness.passBothPriorities();

        Permanent strangler = findPermanent(player1, "Woodwraith Strangler");
        assertThat(strangler.getRegenerationShield()).isEqualTo(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).extracting(card -> card.getName())
                .containsExactly("Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot activate without a creature card in the graveyard")
    void cannotActivateWithoutCreatureCard() {
        harness.addToBattlefield(player1, new WoodwraithStrangler());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The regeneration shield saves it from lethal damage and is spent")
    void shieldSurvivesLethalDamage() {
        harness.addToBattlefield(player1, new WoodwraithStrangler());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleGraveyardCardChosen(player1, 0);
        harness.passBothPriorities();

        UUID stranglerId = harness.getPermanentId(player1, "Woodwraith Strangler");
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, stranglerId);
        harness.passBothPriorities();

        Permanent strangler = findPermanent(player1, "Woodwraith Strangler");
        assertThat(strangler).isNotNull();
        assertThat(strangler.getRegenerationShield()).isZero();
    }
}
