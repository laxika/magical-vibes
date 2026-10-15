package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.d.DalkovanPackbeasts;
import com.github.laxika.magicalvibes.cards.d.DragonstormGlobe;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GreatArashinCity.class, Forest.class, DalkovanPackbeasts.class, Plains.class, DragonstormGlobe.class})
class GreatArashinCityTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped when you control neither a Forest nor a Plains")
    void entersTappedWithoutForestOrPlains() {
        playCity();

        assertThat(findCity(player1).isTapped()).isTrue();
    }

    @Test
    @DisplayName("Enters untapped when you control a Forest or Plains")
    void entersUntappedWithForestOrPlains() {
        harness.addToBattlefield(player1, new Forest());
        playCity();

        assertThat(findCity(player1).isTapped()).isFalse();
    }

    @Test
    @DisplayName("Enters untapped when you control a Plains")
    void entersUntappedWithPlains() {
        harness.addToBattlefield(player1, new Plains());
        playCity();

        assertThat(findCity(player1).isTapped()).isFalse();
    }

    @Test
    @DisplayName("Tapping produces one black mana")
    void tappingProducesBlackMana() {
        addCityReady(player1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(findCity(player1).isTapped()).isTrue();
    }

    @Test
    @DisplayName("Token ability prompts for a creature card to exile")
    void tokenAbilityPromptsForCreatureCard() {
        addCityReady(player1);
        harness.setGraveyard(player1, List.of(new DalkovanPackbeasts()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.GraveyardExileCostChoice.class);
    }

    @Test
    @DisplayName("Token ability exiles a creature and creates a 1/1 white Spirit")
    void tokenAbilityCreatesSpirit() {
        addCityReady(player1);
        harness.setGraveyard(player1, List.of(new DalkovanPackbeasts()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleGraveyardCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player1, "Dalkovan Packbeasts");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Dalkovan Packbeasts"));

        Permanent spirit = findPermanent(player1, "Spirit");
        assertThat(spirit.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(spirit.getCard().getPower()).isEqualTo(1);
        assertThat(spirit.getCard().getToughness()).isEqualTo(1);
        assertThat(spirit.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(spirit.getCard().getSubtypes()).containsExactly(CardSubtype.SPIRIT);
        assertThat(gqs.hasKeyword(gd, spirit, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Token ability cannot activate without a creature card in the graveyard")
    void tokenAbilityRequiresCreatureCard() {
        addCityReady(player1);
        harness.setGraveyard(player1, List.of(new DragonstormGlobe()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    @Test
    @DisplayName("Opposing Forests and Plains do not let the city enter untapped")
    void opposingLandsDoNotSatisfyEntryCondition() {
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new Plains());

        playCity();

        assertThat(findCity(player1).isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapped Forest still lets the city enter untapped")
    void tappedForestSatisfiesEntryCondition() {
        harness.addToBattlefieldAndReturn(player1, new Forest()).tap();

        playCity();

        assertThat(findCity(player1).isTapped()).isFalse();
    }

    @Test
    @DisplayName("Token ability pays its costs before creating the Spirit")
    void tokenAbilityPaysCostsBeforeResolution() {
        addCityReady(player1);
        harness.setGraveyard(player1, List.of(new DragonstormGlobe(), new DalkovanPackbeasts()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleGraveyardCardChosen(player1, 1);

        assertThat(findCity(player1).isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
        harness.assertInGraveyard(player1, "Dragonstorm Globe");
        harness.assertNotInGraveyard(player1, "Dalkovan Packbeasts");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Dalkovan Packbeasts"));
        harness.assertNotOnBattlefield(player1, "Spirit");

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Spirit");
        harness.assertNotOnBattlefield(player2, "Spirit");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().getName().equals("Spirit"))
                .hasSize(1);
    }

    @Test
    @DisplayName("Opponent's creature graveyard cannot pay the token ability cost")
    void cannotExileOpponentsCreature() {
        addCityReady(player1);
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of(new DalkovanPackbeasts()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");

        harness.assertInGraveyard(player2, "Dalkovan Packbeasts");
        assertThat(findCity(player1).isTapped()).isFalse();
    }

    private void playCity() {
        harness.setHand(player1, List.of(new GreatArashinCity()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.playLand(player1, 0);
    }

    private Permanent addCityReady(Player player) {
        Permanent city = harness.addToBattlefieldAndReturn(player, new GreatArashinCity());
        city.setSummoningSick(false);
        return city;
    }

    private Permanent findCity(Player player) {
        return findPermanent(player, "Great Arashin City");
    }
}
