package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GodsEyeGateToTheReikai;
import com.github.laxika.magicalvibes.cards.k.KamiOfFalseHope;
import com.github.laxika.magicalvibes.cards.k.KamiOfTatteredShoji;
import com.github.laxika.magicalvibes.cards.k.KitsunePalliator;
import com.github.laxika.magicalvibes.cards.w.WaxmaneBaku;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MoonlitStrider.class, KamiOfFalseHope.class, KamiOfTatteredShoji.class,
        KitsunePalliator.class, GodsEyeGateToTheReikai.class, WaxmaneBaku.class})
class MoonlitStriderTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifice ability grants the chosen color protection to a creature you control")
    void sacAbilityGrantsChosenColorProtection() {
        harness.addToBattlefield(player1, new MoonlitStrider());
        harness.addToBattlefield(player1, new KitsunePalliator());

        harness.activateAbility(player1, 0, null, harness.getPermanentId(player1, "Kitsune Palliator"));
        resolveAllTriggers();
        harness.handleListChoice(player1, "RED");

        harness.assertInGraveyard(player1, "Moonlit Strider");
        Permanent palliator = findPermanent(player1, "Kitsune Palliator");
        assertThat(palliator.getProtectionFromColorsUntilEndOfTurn()).contains(CardColor.RED);
    }

    @Test
    @DisplayName("Protection wears off at end of turn")
    void protectionWearsOff() {
        harness.addToBattlefield(player1, new MoonlitStrider());
        harness.addToBattlefield(player1, new KitsunePalliator());

        harness.activateAbility(player1, 0, null, harness.getPermanentId(player1, "Kitsune Palliator"));
        resolveAllTriggers();
        harness.handleListChoice(player1, "BLACK");

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.UPKEEP);

        Permanent palliator = findPermanent(player1, "Kitsune Palliator");
        assertThat(palliator.getProtectionFromColorsUntilEndOfTurn()).isEmpty();
    }

    @Test
    @DisplayName("Cannot target a creature an opponent controls")
    void cannotTargetOpponentCreature() {
        harness.addToBattlefield(player1, new MoonlitStrider());
        harness.addToBattlefield(player2, new KitsunePalliator());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null,
                harness.getPermanentId(player2, "Kitsune Palliator")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");
    }

    @Test
    @DisplayName("Cannot target a noncreature you control")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player1, new MoonlitStrider());
        harness.addToBattlefield(player1, new GodsEyeGateToTheReikai());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null,
                harness.getPermanentId(player1, "Gods' Eye, Gate to the Reikai")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");
    }

    @Test
    @DisplayName("Soulshift 3 returns a targeted Spirit with mana value 3 or less when Moonlit Strider dies")
    void soulshiftReturnsCheapSpirit() {
        harness.addToBattlefield(player1, new MoonlitStrider());
        harness.addToBattlefield(player1, new KitsunePalliator());
        Card spirit = new KamiOfFalseHope();
        harness.setGraveyard(player1, List.of(spirit));

        harness.activateAbility(player1, 0, null, harness.getPermanentId(player1, "Kitsune Palliator"));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(spirit.getId()));
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(spirit.getId()));
    }

    @Test
    @DisplayName("Soulshift only offers its controller's Spirits with mana value 3 or less")
    void soulshiftRestrictsTargets() {
        harness.addToBattlefield(player1, new MoonlitStrider());
        harness.addToBattlefield(player1, new KitsunePalliator());
        Card eligible = new KamiOfFalseHope();
        Card tooExpensive = new KamiOfTatteredShoji();
        Card nonSpirit = new KitsunePalliator();
        Card opponentSpirit = new KamiOfFalseHope();
        harness.setGraveyard(player1, List.of(eligible, tooExpensive, nonSpirit));
        harness.setGraveyard(player2, List.of(opponentSpirit));

        harness.activateAbility(player1, 0, null, harness.getPermanentId(player1, "Kitsune Palliator"));

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(eligible.getId());

        harness.handleMultipleCardsChosen(player1, List.of(eligible.getId()));
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Kami of False Hope");
        harness.assertInGraveyard(player1, "Kami of Tattered Shoji");
        harness.assertInGraveyard(player1, "Kitsune Palliator");
        harness.assertInGraveyard(player2, "Kami of False Hope");
    }

    @Test
    @DisplayName("Soulshift requires a target and may be declined on resolution")
    void soulshiftMayBeDeclined() {
        harness.addToBattlefield(player1, new MoonlitStrider());
        harness.addToBattlefield(player1, new KitsunePalliator());
        Card spirit = new KamiOfFalseHope();
        harness.setGraveyard(player1, List.of(spirit));

        harness.activateAbility(player1, 0, null, harness.getPermanentId(player1, "Kitsune Palliator"));

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.minCount()).isEqualTo(1);
        harness.handleMultipleCardsChosen(player1, List.of(spirit.getId()));
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Kami of False Hope");
        harness.assertNotInHand(player1, "Kami of False Hope");
    }

    @Test
    @DisplayName("Soulshift offers no choice with no Spirit in your graveyard")
    void soulshiftNoLegalSpiritNoChoice() {
        harness.addToBattlefield(player1, new MoonlitStrider());
        harness.addToBattlefield(player1, new KitsunePalliator());
        harness.setGraveyard(player1, List.of(new KitsunePalliator(), new KamiOfTatteredShoji()));

        harness.activateAbility(player1, 0, null, harness.getPermanentId(player1, "Kitsune Palliator"));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.assertInGraveyard(player1, "Kitsune Palliator");
        harness.assertInGraveyard(player1, "Kami of Tattered Shoji");
    }

    @Test
    @DisplayName("Soulshift can return a Spirit with mana value exactly three")
    void soulshiftReturnsSpiritAtBoundary() {
        harness.addToBattlefield(player1, new MoonlitStrider());
        harness.addToBattlefield(player1, new KitsunePalliator());
        Card spirit = new WaxmaneBaku();
        harness.setGraveyard(player1, List.of(spirit));

        harness.activateAbility(player1, 0, null, harness.getPermanentId(player1, "Kitsune Palliator"));
        harness.handleMultipleCardsChosen(player1, List.of(spirit.getId()));
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Waxmane Baku");
        harness.assertNotInGraveyard(player1, "Waxmane Baku");

        resolveAllTriggers();
        harness.handleListChoice(player1, "WHITE");
        assertThat(findPermanent(player1, "Kitsune Palliator").getProtectionFromColorsUntilEndOfTurn())
                .contains(CardColor.WHITE);
    }

    @Test
    @DisplayName("Soulshift cannot return a target that leaves the graveyard before resolution")
    void soulshiftTargetLeavesGraveyard() {
        Card strider = new MoonlitStrider();
        harness.addToBattlefield(player1, strider);
        harness.addToBattlefield(player1, new KitsunePalliator());
        Card spirit = new KamiOfFalseHope();
        harness.setGraveyard(player1, List.of(spirit));

        harness.activateAbility(player1, 0, null, harness.getPermanentId(player1, "Kitsune Palliator"));
        harness.handleMultipleCardsChosen(player1, List.of(spirit.getId()));
        harness.setGraveyard(player1, List.of(strider));
        harness.passBothPriorities();

        harness.assertNotInHand(player1, "Kami of False Hope");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();

        resolveAllTriggers();
        harness.handleListChoice(player1, "BLUE");
        assertThat(findPermanent(player1, "Kitsune Palliator").getProtectionFromColorsUntilEndOfTurn())
                .contains(CardColor.BLUE);
    }

    @Test
    @DisplayName("Sacrificing the protection target in response makes the ability fail to resolve")
    void protectionTargetLeavesBattlefield() {
        harness.addToBattlefield(player1, new MoonlitStrider());
        harness.addToBattlefield(player1, new KamiOfFalseHope());

        harness.activateAbility(player1, 0, null, harness.getPermanentId(player1, "Kami of False Hope"));
        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Moonlit Strider");
        harness.assertInGraveyard(player1, "Kami of False Hope");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Moonlit Strider can target itself but is sacrificed before receiving protection")
    void canTargetItself() {
        harness.addToBattlefield(player1, new MoonlitStrider());

        harness.activateAbility(player1, 0, null, harness.getPermanentId(player1, "Moonlit Strider"));
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Moonlit Strider");
        harness.assertNotOnBattlefield(player1, "Moonlit Strider");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }
}
