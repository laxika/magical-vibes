package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.c.Char;
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

@CardUsed({WoodwraithStrangler.class, Watchwolf.class, Char.class})
class WoodwraithStranglerTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles a creature card from the graveyard to gain a regeneration shield")
    void exilesCreatureCardForShield() {
        Permanent strangler = harness.addToBattlefieldAndReturn(player1, new WoodwraithStrangler());
        harness.setGraveyard(player1, List.of(new Watchwolf()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleGraveyardCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(strangler.getRegenerationShield()).isEqualTo(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).extracting(card -> card.getName())
                .containsExactly("Watchwolf");
    }

    @Test
    @DisplayName("Cannot activate without a creature card in the graveyard")
    void cannotActivateWithoutCreatureCard() {
        harness.addToBattlefield(player1, new WoodwraithStrangler());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate by exiling a noncreature card from the graveyard")
    void cannotActivateWithOnlyNoncreatureCard() {
        harness.addToBattlefield(player1, new WoodwraithStrangler());
        harness.setGraveyard(player1, List.of(new Char()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(card -> card.getName())
                .containsExactly("Char");
    }

    @Test
    @DisplayName("The regeneration shield saves it from lethal damage and is spent")
    void shieldSurvivesLethalDamage() {
        harness.addToBattlefield(player1, new WoodwraithStrangler());
        harness.setGraveyard(player1, List.of(new Watchwolf()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleGraveyardCardChosen(player1, 0);
        harness.passBothPriorities();

        UUID stranglerId = harness.getPermanentId(player1, "Woodwraith Strangler");
        harness.setHand(player1, List.of(new Char()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, stranglerId);
        harness.passBothPriorities();

        Permanent strangler = findPermanent(player1, "Woodwraith Strangler");
        assertThat(strangler).isNotNull();
        assertThat(strangler.getRegenerationShield()).isZero();
    }
    @Test
    @DisplayName("Exiling the creature is a cost paid before regeneration resolves")
    void exileCostIsPaidBeforeResolution() {
        Permanent strangler = harness.addToBattlefieldAndReturn(player1, new WoodwraithStrangler());
        harness.setGraveyard(player1, List.of(new Char(), new Watchwolf()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleGraveyardCardChosen(player1, 1);

        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(card -> card.getName())
                .containsExactly("Char");
        assertThat(gd.getPlayerExiledCards(player1.getId())).extracting(card -> card.getName())
                .containsExactly("Watchwolf");
        assertThat(strangler.getRegenerationShield()).isZero();

        harness.passBothPriorities();

        assertThat(strangler.getRegenerationShield()).isEqualTo(1);
        assertThat(strangler.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot pay the cost with a creature in the opponent's graveyard")
    void cannotExileOpponentsCreature() {
        harness.addToBattlefield(player1, new WoodwraithStrangler());
        harness.setGraveyard(player2, List.of(new Watchwolf()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player2.getId())).extracting(card -> card.getName())
                .containsExactly("Watchwolf");
    }

    @Test
    @DisplayName("Can activate while tapped and accumulate multiple shields")
    void canActivateRepeatedlyWhileTapped() {
        Permanent strangler = harness.addToBattlefieldAndReturn(player1, new WoodwraithStrangler());
        strangler.tap();
        harness.setGraveyard(player1, List.of(new Watchwolf(), new Watchwolf()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleGraveyardCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.handleGraveyardCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(strangler.getRegenerationShield()).isEqualTo(2);
        assertThat(strangler.isTapped()).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(2);
    }
}
