package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
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

@CardUsed({ReezugTheBonecobbler.class, GrizzlyBears.class, Shock.class})
class ReezugTheBonecobblerTest extends BaseCardTest {

    @Test
    @DisplayName("Perpetually changes a creature in the graveyard to an artifact and allows it to be cast")
    void perpetuallyChangesCreatureToArtifactAndAllowsCastThisTurn() {
        Permanent reezug = addReadyReezug();
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));

        activate(reezug, bears);

        Card modified = gd.playerGraveyards.get(player1.getId()).getFirst();
        assertThat(modified).isNotSameAs(bears);
        assertThat(modified.getId()).isEqualTo(bears.getId());
        assertThat(modified.hasType(CardType.ARTIFACT)).isTrue();
        assertThat(modified.hasType(CardType.CREATURE)).isFalse();

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castFromGraveyard(player1, modified.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(bears.getId())
                        && permanent.getCard().hasType(CardType.ARTIFACT)
                        && !permanent.getCard().hasType(CardType.CREATURE));
    }

    @Test
    @DisplayName("Only creature cards in the controller's graveyard are legal targets")
    void onlyOwnCreatureCardsAreTargetable() {
        Permanent reezug = addReadyReezug();
        Card nonCreature = new Shock();
        Card opponentCreature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(nonCreature));
        harness.setGraveyard(player2, List.of(opponentCreature));

        int index = gd.playerBattlefields.get(player1.getId()).indexOf(reezug);
        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, index, 0, List.of(nonCreature.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, index, 0, List.of(opponentCreature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Losing the creature card type removes its creature subtypes")
    void removesCreatureSubtypesInGraveyard() {
        Permanent reezug = addReadyReezug();
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));

        activate(reezug, bears);

        Card modified = gd.playerGraveyards.get(player1.getId()).getFirst();
        assertThat(gqs.cardHasSubtype(modified, CardSubtype.BEAR, gd, player1.getId())).isFalse();
        assertThat(gqs.getCardSubtypes(modified, gd, player1.getId())).doesNotContain(CardSubtype.BEAR);
    }

    @Test
    @DisplayName("The artifact change persists after the casting permission expires")
    void castingPermissionExpiresButArtifactChangePersists() {
        Permanent reezug = addReadyReezug();
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        activate(reezug, bears);

        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        harness.passUntilWithNoAttackers(player1, TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        Card modified = gd.playerGraveyards.get(player1.getId()).getFirst();
        assertThat(modified.hasType(CardType.ARTIFACT)).isTrue();
        assertThat(modified.hasType(CardType.CREATURE)).isFalse();
        assertThatThrownBy(() -> harness.castFromGraveyard(player1, modified.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Casting permission does not waive the card's mana cost")
    void castingStillRequiresMana() {
        Permanent reezug = addReadyReezug();
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        activate(reezug, bears);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(bears.getId()));
    }

    @Test
    @DisplayName("Casting permission does not allow an artifact to be cast at instant speed")
    void castingStillRequiresSorceryTiming() {
        Permanent reezug = addReadyReezug();
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        activate(reezug, bears);
        harness.forceStep(TurnStep.UPKEEP);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A summoning-sick Reezug cannot pay its tap cost")
    void summoningSicknessPreventsActivation() {
        Permanent reezug = harness.addToBattlefieldAndReturn(player1, new ReezugTheBonecobbler());
        reezug.setSummoningSick(true);
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));

        int index = gd.playerBattlefields.get(player1.getId()).indexOf(reezug);
        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, index, 0, List.of(bears.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("An ability whose target leaves the graveyard does not change another card")
    void targetLeavingGraveyardBeforeResolution() {
        Permanent reezug = addReadyReezug();
        Card target = new GrizzlyBears();
        Card other = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(target, other));
        int index = gd.playerBattlefields.get(player1.getId()).indexOf(reezug);
        harness.activateAbilityWithGraveyardTargets(player1, index, 0, List.of(target.getId()));
        harness.setGraveyard(player1, List.of(other));

        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()).getFirst().hasType(CardType.CREATURE)).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId()).getFirst().hasType(CardType.ARTIFACT)).isFalse();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        assertThatThrownBy(() -> harness.castFromGraveyard(player1, other.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyReezug() {
        Permanent reezug = harness.addToBattlefieldAndReturn(player1, new ReezugTheBonecobbler());
        reezug.setSummoningSick(false);
        return reezug;
    }

    private void activate(Permanent reezug, Card target) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        int index = gd.playerBattlefields.get(player1.getId()).indexOf(reezug);
        harness.activateAbilityWithGraveyardTargets(player1, index, 0, List.of(target.getId()));
        harness.passBothPriorities();
    }
}
