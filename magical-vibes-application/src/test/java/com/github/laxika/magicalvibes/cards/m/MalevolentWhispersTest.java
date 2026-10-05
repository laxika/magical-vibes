package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.c.Catalog;
import com.github.laxika.magicalvibes.cards.q.QuilledWolf;
import com.github.laxika.magicalvibes.cards.v.VesselOfEphemera;
import com.github.laxika.magicalvibes.cards.w.WakeThrasher;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
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

@CardUsed({MalevolentWhispers.class, QuilledWolf.class, VesselOfEphemera.class,
        Catalog.class, WakeThrasher.class})
class MalevolentWhispersTest extends BaseCardTest {

    private void castOn(Permanent target) {
        harness.setHand(player1, List.of(new MalevolentWhispers()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castAndResolveSorcery(player1, 0, target.getId());
    }

    @Test
    @DisplayName("Resolving untaps, steals, pumps +2/+0 and grants haste")
    void resolvesFullPackage() {
        Permanent target = addCreatureReady(player2, new QuilledWolf());
        target.tap();

        castOn(target);

        assertThat(target.isTapped()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(p -> p.getId().equals(target.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId())).noneMatch(p -> p.getId().equals(target.getId()));
        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(gd.isStolenUntilEndOfTurn(target.getId())).isTrue();
        assertThat(harness.getGameQueryService().getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(harness.getGameQueryService().getEffectiveToughness(gd, target)).isEqualTo(2);
    }

    @Test
    @DisplayName("Control, haste and the +2/+0 boost all wear off at end of turn")
    void everythingExpiresAtCleanup() {
        Permanent target = addCreatureReady(player2, new QuilledWolf());

        castOn(target);

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gd.playerBattlefields.get(player2.getId())).anyMatch(p -> p.getId().equals(target.getId()));
        assertThat(target.hasKeyword(Keyword.HASTE)).isFalse();
        assertThat(gd.isStolenUntilEndOfTurn(target.getId())).isFalse();
        assertThat(harness.getGameQueryService().getEffectivePower(gd, target)).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetNonCreature() {
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new VesselOfEphemera());
        harness.setHand(player1, List.of(new MalevolentWhispers()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, enchantment.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Can untap and enhance the caster's own creature")
    void canTargetOwnCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new QuilledWolf());
        target.tap();

        castOn(target);

        assertThat(target.isTapped()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();
    }

    @Test
    @CardUsed({MalevolentWhispers.class, QuilledWolf.class, WakeThrasher.class})
    @DisplayName("Control changes before untapping, so only the new controller's Wake Thrasher triggers")
    void gainsControlBeforeUntapping() {
        Permanent casterThrasher = harness.addToBattlefieldAndReturn(player1, new WakeThrasher());
        Permanent opponentThrasher = harness.addToBattlefieldAndReturn(player2, new WakeThrasher());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new QuilledWolf());
        target.tap();

        castOn(target);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        assertThat(target.isTapped()).isFalse();
        assertThat(gqs.getEffectivePower(gd, casterThrasher)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, casterThrasher)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opponentThrasher)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, opponentThrasher)).isEqualTo(1);
    }

    @Test
    @DisplayName("Madness can cast the sorcery during an opponent's turn for three generic and one red mana")
    void madnessCastsDuringOpponentsTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new QuilledWolf());
        target.tap();
        MalevolentWhispers card = discardWithCatalog();

        assertThat(gd.findExiledCard(card.getId())).isNotNull();
        harness.assertNotInGraveyard(player1, "Malevolent Whispers");
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(card.getId())).isNull();
        harness.assertInGraveyard(player1, "Malevolent Whispers");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        assertThat(target.isTapped()).isFalse();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Declining madness puts the discarded card into its owner's graveyard")
    void decliningMadnessPutsCardInGraveyard() {
        MalevolentWhispers card = discardWithCatalog();

        assertThat(gd.findExiledCard(card.getId())).isNotNull();
        harness.assertNotInGraveyard(player1, "Malevolent Whispers");
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.findExiledCard(card.getId())).isNull();
        harness.assertInGraveyard(player1, "Malevolent Whispers");
        assertThat(gd.stack).isEmpty();
    }

    private MalevolentWhispers discardWithCatalog() {
        MalevolentWhispers card = new MalevolentWhispers();
        harness.forceActivePlayer(player2);
        harness.setHand(player1, List.of(new Catalog(), card));
        harness.setLibrary(player1, List.of(new Catalog(), new Catalog()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player1, 0);
        harness.handleCardChosen(player1, 0);
        return card;
    }
}
