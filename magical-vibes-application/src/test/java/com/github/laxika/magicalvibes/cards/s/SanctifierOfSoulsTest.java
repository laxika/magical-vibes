package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BlessedAlliance;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
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

@CardUsed({SanctifierOfSouls.class, SteadfastCathar.class, BlessedAlliance.class})
class SanctifierOfSoulsTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +1/+1 until end of turn when another creature you control enters")
    void getsBoostWhenAllyCreatureEnters() {
        harness.addToBattlefield(player1, new SanctifierOfSouls());
        Permanent sanctifier = findPermanent(player1, "Sanctifier of Souls");

        castSteadfastCathar(player1);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, sanctifier)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, sanctifier)).isEqualTo(4);
    }

    @Test
    @DisplayName("Does not trigger when an opponent's creature enters")
    void noBoostWhenOpponentCreatureEnters() {
        harness.addToBattlefield(player1, new SanctifierOfSouls());
        Permanent sanctifier = findPermanent(player1, "Sanctifier of Souls");

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        castSteadfastCathar(player2);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, sanctifier)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, sanctifier)).isEqualTo(3);
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new SanctifierOfSouls());
        Permanent sanctifier = findPermanent(player1, "Sanctifier of Souls");

        castSteadfastCathar(player1);
        resolveAllTriggers();
        assertThat(gqs.getEffectivePower(gd, sanctifier)).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, sanctifier)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, sanctifier)).isEqualTo(3);
    }

    @Test
    @DisplayName("Exiles a creature card and creates an untapped 1/1 white Spirit with flying")
    void exilesCreatureAndCreatesSpiritToken() {
        harness.addToBattlefield(player1, new SanctifierOfSouls());
        harness.setGraveyard(player1, List.of(new SteadfastCathar()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.handleGraveyardCardChosen(player1, 0);
        harness.assertNotInGraveyard(player1, "Steadfast Cathar");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Steadfast Cathar"));

        harness.passBothPriorities();

        Permanent spirit = findPermanent(player1, "Spirit");
        assertThat(spirit.getCard().getPower()).isEqualTo(1);
        assertThat(spirit.getCard().getToughness()).isEqualTo(1);
        assertThat(spirit.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(spirit.getCard().getSubtypes()).contains(CardSubtype.SPIRIT);
        assertThat(spirit.getCard().getKeywords()).contains(Keyword.FLYING);
        assertThat(spirit.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot activate without a creature card in the graveyard")
    void cannotActivateWithoutCreatureInGraveyard() {
        harness.addToBattlefield(player1, new SanctifierOfSouls());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    @Test
    @DisplayName("Does not boost itself when it enters")
    void doesNotTriggerForItsOwnEntry() {
        harness.setHand(player1, List.of(new SanctifierOfSouls()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        Permanent sanctifier = findPermanent(player1, "Sanctifier of Souls");
        assertThat(gqs.getEffectivePower(gd, sanctifier)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, sanctifier)).isEqualTo(3);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The created Spirit triggers a separate boost on the stack")
    void spiritEntryBoostResolvesSeparately() {
        harness.addToBattlefield(player1, new SanctifierOfSouls());
        Permanent sanctifier = findPermanent(player1, "Sanctifier of Souls");
        harness.setGraveyard(player1, List.of(new SteadfastCathar()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.handleGraveyardCardChosen(player1, 0);

        assertThat(countPermanents(player1, "Spirit")).isZero();
        harness.passBothPriorities();
        assertThat(countPermanents(player1, "Spirit")).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, sanctifier)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, sanctifier)).isEqualTo(3);
        assertThat(gd.stack).hasSize(1);

        resolveAllTriggers();
        assertThat(gqs.getEffectivePower(gd, sanctifier)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, sanctifier)).isEqualTo(4);
        Permanent spirit = findPermanent(player1, "Spirit");
        assertThat(gqs.getEffectivePower(gd, spirit)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, spirit)).isEqualTo(1);
    }

    @Test
    @DisplayName("Can activate repeatedly while tapped and summoning sick on an opponent's turn")
    void repeatedActivationsCreateTokensAndStackBoosts() {
        harness.addToBattlefield(player1, new SanctifierOfSouls());
        Permanent sanctifier = findPermanent(player1, "Sanctifier of Souls");
        sanctifier.tap();
        sanctifier.setSummoningSick(true);
        harness.setGraveyard(player1, List.of(new SteadfastCathar(), new SteadfastCathar()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        for (int i = 0; i < 2; i++) {
            harness.activateAbility(player1, 0, null, null);
            harness.handleGraveyardCardChosen(player1, 0);
            resolveAllTriggers();
        }

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(2);
        assertThat(countPermanents(player1, "Spirit")).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, sanctifier)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, sanctifier)).isEqualTo(5);
        assertThat(sanctifier.isTapped()).isTrue();
    }

    @Test
    @DisplayName("A noncreature card cannot pay the graveyard exile cost")
    void cannotExileNoncreatureCard() {
        harness.addToBattlefield(player1, new SanctifierOfSouls());
        harness.setGraveyard(player1, List.of(new BlessedAlliance()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
        harness.assertInGraveyard(player1, "Blessed Alliance");
        assertThat(countPermanents(player1, "Spirit")).isZero();
    }

    @Test
    @DisplayName("An opponent's graveyard cannot pay the exile cost")
    void cannotExileCreatureFromOpponentsGraveyard() {
        harness.addToBattlefield(player1, new SanctifierOfSouls());
        harness.setGraveyard(player2, List.of(new SteadfastCathar()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
        harness.assertInGraveyard(player2, "Steadfast Cathar");
        assertThat(countPermanents(player1, "Spirit")).isZero();
    }

    private void castSteadfastCathar(Player player) {
        harness.setHand(player, List.of(new SteadfastCathar()));
        harness.addMana(player, ManaColor.WHITE, 1);
        harness.addMana(player, ManaColor.COLORLESS, 1);
        harness.castCreature(player, 0);
    }
}
