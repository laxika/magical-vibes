package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Fog;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RiseFromTheGrave.class, RuneclawBear.class, Fog.class})
class RiseFromTheGraveTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Rise from the Grave puts it on the stack")
    void castingPutsItOnStack() {
        harness.setGraveyard(player1, List.of(new RuneclawBear()));
        harness.setHand(player1, List.of(new RiseFromTheGrave()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castSorcery(player1, 0, gd.playerGraveyards.get(player1.getId()).getFirst().getId());

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(entry.getCard()).isInstanceOf(RiseFromTheGrave.class);
        assertThat(entry.getTargetId()).isEqualTo(gd.playerGraveyards.get(player1.getId()).getFirst().getId());
    }

    @Test
    @DisplayName("Cannot cast without enough mana")
    void cannotCastWithoutEnoughMana() {
        harness.setGraveyard(player1, List.of(new RuneclawBear()));
        harness.setHand(player1, List.of(new RiseFromTheGrave()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, gd.playerGraveyards.get(player1.getId()).getFirst().getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Returns creature from own graveyard to battlefield")
    void returnsCreatureFromOwnGraveyard() {
        harness.setGraveyard(player1, List.of(new RuneclawBear()));
        harness.setHand(player1, List.of(new RiseFromTheGrave()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castSorcery(player1, 0, gd.playerGraveyards.get(player1.getId()).getFirst().getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Runeclaw Bear");
        harness.assertNotInGraveyard(player1, "Runeclaw Bear");
    }

    @Test
    @DisplayName("Returns creature from opponent's graveyard under your control")
    void returnsCreatureFromOpponentGraveyard() {
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of(new RuneclawBear()));
        harness.setHand(player1, List.of(new RiseFromTheGrave()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castSorcery(player1, 0, gd.playerGraveyards.get(player2.getId()).getFirst().getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Runeclaw Bear");
        harness.assertNotInGraveyard(player2, "Runeclaw Bear");
    }

    @Test
    @DisplayName("Returned creature permanently gains Zombie subtype")
    void returnedCreatureGainsZombieSubtype() {
        harness.setGraveyard(player1, List.of(new RuneclawBear()));
        harness.setHand(player1, List.of(new RiseFromTheGrave()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castSorcery(player1, 0, gd.playerGraveyards.get(player1.getId()).getFirst().getId());
        harness.passBothPriorities();

        Permanent bears = findPermanent(player1, "Runeclaw Bear");

        assertThat(gqs.hasEffectiveSubtype(gd, bears, CardSubtype.ZOMBIE)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, bears, CardSubtype.BEAR)).isTrue();
    }

    @Test
    @DisplayName("Returned creature permanently gains black color")
    void returnedCreatureGainsBlackColor() {
        harness.setGraveyard(player1, List.of(new RuneclawBear()));
        harness.setHand(player1, List.of(new RiseFromTheGrave()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castSorcery(player1, 0, gd.playerGraveyards.get(player1.getId()).getFirst().getId());
        harness.passBothPriorities();

        Permanent bears = findPermanent(player1, "Runeclaw Bear");

        assertThat(gqs.getEffectiveColors(gd, bears)).containsExactlyInAnyOrder(CardColor.BLACK, CardColor.GREEN);
    }

    @Test
    @DisplayName("Zombie subtype and black color survive turn reset")
    void grantsServiveTurnReset() {
        harness.setGraveyard(player1, List.of(new RuneclawBear()));
        harness.setHand(player1, List.of(new RiseFromTheGrave()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castSorcery(player1, 0, gd.playerGraveyards.get(player1.getId()).getFirst().getId());
        harness.passBothPriorities();

        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);

        Permanent bears = findPermanent(player1, "Runeclaw Bear");

        assertThat(gqs.hasEffectiveSubtype(gd, bears, CardSubtype.ZOMBIE)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, bears, CardSubtype.BEAR)).isTrue();
        assertThat(gqs.getEffectiveColors(gd, bears)).containsExactlyInAnyOrder(CardColor.BLACK, CardColor.GREEN);
    }

    @Test
    @DisplayName("Cannot target a noncreature card")
    void nonCreatureCardsNotValidChoices() {
        harness.setGraveyard(player1, List.of(new Fog()));
        harness.setGraveyard(player2, List.of());
        harness.setHand(player1, List.of(new RiseFromTheGrave()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0,
                gd.playerGraveyards.get(player1.getId()).getFirst().getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Stack is empty after resolution")
    void stackIsEmptyAfterResolution() {
        harness.setGraveyard(player1, List.of(new RuneclawBear()));
        harness.setHand(player1, List.of(new RiseFromTheGrave()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castSorcery(player1, 0, gd.playerGraveyards.get(player1.getId()).getFirst().getId());
        harness.passBothPriorities();

        assertThat(harness.getGameData().stack).isEmpty();
    }

    @Test
    @DisplayName("Rise from the Grave goes to graveyard after resolution")
    void goesToGraveyardAfterResolution() {
        harness.setGraveyard(player1, List.of(new RuneclawBear()));
        harness.setHand(player1, List.of(new RiseFromTheGrave()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castSorcery(player1, 0, gd.playerGraveyards.get(player1.getId()).getFirst().getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Rise from the Grave");
    }

    @Test
    @DisplayName("Cannot cast without choosing a creature card target")
    void cannotCastWithoutTarget() {
        harness.setGraveyard(player1, List.of(new RuneclawBear()));
        harness.setHand(player1, List.of(new RiseFromTheGrave()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot cast when neither graveyard contains a creature card")
    void cannotCastWithoutLegalTargets() {
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of());
        harness.setHand(player1, List.of(new RiseFromTheGrave()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A target that leaves the graveyard cannot be replaced by another creature")
    void removedTargetDoesNotAllowChoosingAnotherCreature() {
        RuneclawBear target = new RuneclawBear();
        RuneclawBear otherCreature = new RuneclawBear();
        harness.setGraveyard(player1, List.of(target, otherCreature));
        harness.setHand(player1, List.of(new RiseFromTheGrave()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castSorcery(player1, 0, target.getId());
        // Model the target being exiled in response while another legal creature remains.
        harness.setGraveyard(player1, List.of(otherCreature));
        harness.setExile(player1, List.of(target));
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertNotOnBattlefield(player1, "Runeclaw Bear");
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(otherCreature);
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Rise from the Grave");
    }
}
