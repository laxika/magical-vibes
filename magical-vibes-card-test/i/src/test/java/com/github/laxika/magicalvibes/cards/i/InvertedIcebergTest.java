package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({InvertedIceberg.class, IcebergTitan.class})
class InvertedIcebergTest extends BaseCardTest {

    @Test
    @DisplayName("Enters by milling a card, then drawing a card")
    void entersByMillingAndDrawing() {
        Card milled = new IcebergTitan();
        Card drawn = new InvertedIceberg();
        harness.setHand(player1, List.of(new InvertedIceberg()));
        harness.setLibrary(player1, List.of(milled, drawn));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castArtifact(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(milled);
        assertThat(gd.playerHands.get(player1.getId())).contains(drawn);
    }

    @Test
    @DisplayName("Craft exiles another artifact and returns Iceberg Titan transformed")
    void craftsIntoIcebergTitan() {
        Permanent iceberg = harness.addToBattlefieldAndReturn(player1, new InvertedIceberg());
        Permanent material = harness.addToBattlefieldAndReturn(player1, new InvertedIceberg());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(iceberg);
        assertThat(gd.findExiledCard(material.getCard().getId())).isNotNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(permanent ->
                permanent.isTransformed() && permanent.getCard() instanceof IcebergTitan);
    }

    @Test
    @DisplayName("Iceberg Titan offers an artifact or creature to tap when it attacks")
    void attackTriggerTapsArtifactOrCreature() {
        InvertedIceberg front = new InvertedIceberg();
        Permanent titan = new Permanent(front);
        titan.setCard(front.getBackFaceCard());
        titan.setTransformed(true);
        titan.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(titan);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new InvertedIceberg());

        declareAttackers(player1, List.of(0));
        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactlyInAnyOrder(titan.getId(), target.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Craft can exile an artifact card from the graveyard as its material")
    void craftsWithGraveyardArtifact() {
        Permanent iceberg = harness.addToBattlefieldAndReturn(player1, new InvertedIceberg());
        Card material = new InvertedIceberg();
        harness.setGraveyard(player1, List.of(material));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(iceberg);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(material);
        assertThat(gd.findExiledCard(material.getId())).isNotNull();
        assertThat(gd.findExiledCard(iceberg.getOriginalCard().getId())).isNotNull();
        resolveAllTriggers();

        assertThat(gd.findExiledCard(iceberg.getOriginalCard().getId())).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(permanent ->
                permanent.isTransformed() && permanent.getCard() instanceof IcebergTitan
                        && permanent.isSummoningSick());
    }

    @Test
    @DisplayName("Craft cannot use its source as the only artifact material")
    void cannotCraftWithoutAnotherArtifact() {
        Permanent iceberg = harness.addToBattlefieldAndReturn(player1, new InvertedIceberg());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(iceberg);
        assertThat(gd.findExiledCard(iceberg.getOriginalCard().getId())).isNull();
    }

    @Test
    @DisplayName("Iceberg Titan can untap itself with its attack trigger")
    void attackTriggerUntapsItself() {
        Permanent titan = addCreatureReady(player1, new IcebergTitan());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, titan.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(titan.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Iceberg Titan's controller can decline to tap an opposing creature")
    void attackTriggerCanBeDeclined() {
        addCreatureReady(player1, new IcebergTitan());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new IcebergTitan());

        declareAttackers(player1, List.of(0));
        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(target.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Craft cannot be activated on the opponent's turn")
    void cannotCraftOnOpponentsTurn() {
        Permanent iceberg = harness.addToBattlefieldAndReturn(player1, new InvertedIceberg());
        Permanent material = harness.addToBattlefieldAndReturn(player1, new InvertedIceberg());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.forceActivePlayer(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(iceberg, material);
    }
}
