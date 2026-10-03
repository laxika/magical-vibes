package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.cards.p.Persuasion;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BondOfRevival.class, GrizzlyBears.class, HolyDay.class, Persuasion.class})
class BondOfRevivalTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a target creature from your graveyard with haste until your next turn")
    void returnsCreatureWithTemporaryHaste() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        prepareCast();

        harness.castAndResolveSorcery(player1, 0, creature.getId());

        Permanent returned = findPermanent(player1, "Grizzly Bears");
        assertThat(gqs.hasKeyword(gd, returned, Keyword.HASTE)).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(creature.getId()));
    }

    @Test
    @DisplayName("Cannot target a noncreature card in your graveyard")
    void cannotTargetNoncreature() {
        Card instant = new HolyDay();
        harness.setGraveyard(player1, List.of(instant));
        prepareCast();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, instant.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Haste survives the opponent's turn and expires when your next turn begins")
    void hasteExpiresOnCastersNextTurn() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.setLibrary(player1, List.of(new HolyDay(), new HolyDay()));
        harness.setLibrary(player2, List.of(new HolyDay(), new HolyDay()));
        prepareCast();
        harness.castAndResolveSorcery(player1, 0, creature.getId());
        Permanent returned = findPermanent(player1, "Grizzly Bears");

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        assertThat(gqs.hasKeyword(gd, returned, Keyword.HASTE)).isTrue();

        harness.passUntil(player1, TurnStep.UPKEEP);
        assertThat(gqs.hasKeyword(gd, returned, Keyword.HASTE)).isFalse();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Changing control does not extend haste beyond the caster's next turn")
    void hasteExpiresEvenAfterOpponentGainsControl() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.setLibrary(player1, List.of(new HolyDay(), new HolyDay()));
        harness.setLibrary(player2, List.of(new HolyDay(), new HolyDay()));
        prepareCast();
        harness.castAndResolveSorcery(player1, 0, creature.getId());
        Permanent returned = findPermanent(player1, "Grizzly Bears");

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new Persuasion()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.castEnchantment(player2, 0, returned.getId());
        harness.passBothPriorities();
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gqs.hasKeyword(gd, returned, Keyword.HASTE)).isTrue();

        harness.passUntil(player1, TurnStep.UPKEEP);
        assertThat(gqs.hasKeyword(gd, returned, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Cannot target a creature in an opponent's graveyard")
    void cannotTargetOpponentsGraveyard() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(creature));
        prepareCast();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Does not return another creature when the target leaves the graveyard")
    void missingTargetDoesNotReturnAnotherCreature() {
        Card target = new GrizzlyBears();
        Card other = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(target, other));
        prepareCast();
        harness.castSorcery(player1, 0, target.getId());
        harness.setGraveyard(player1, List.of(other));
        harness.setExile(player1, List.of(target));

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(other);
        assertThat(gd.exiledCards).anyMatch(entry -> entry.card().getId().equals(target.getId()));
        assertThat(gd.stack).isEmpty();
    }

    private void prepareCast() {
        harness.setHand(player1, List.of(new BondOfRevival()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }
}
