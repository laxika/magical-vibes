package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AssembleFromParts.class, GrizzlyBears.class, LightningBolt.class})
class AssembleFromPartsTest extends BaseCardTest {

    @Test
    void perpetuallyGrantsAbilityAndCreatesZombieCopyThatShufflesSource() {
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        harness.setHand(player1, List.of(new AssembleFromParts()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castAndResolveInstant(player1, 0, bears.getId());

        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(bears);
        assertThat(gd.playerDecks.get(player1.getId())).contains(bears);
        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(token.getEffectivePower()).isEqualTo(4);
        assertThat(token.getEffectiveToughness()).isEqualTo(4);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.BLACK);
        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.ZOMBIE);
    }

    @Test
    void onlyTargetsCreatureCardsInYourGraveyard() {
        Card assemble = new AssembleFromParts();
        harness.setHand(player1, List.of(assemble));
        harness.addMana(player1, ManaColor.BLACK, 1);
        Card noncreature = new LightningBolt();
        harness.setGraveyard(player1, List.of(noncreature));

        assertThatThrownBy(() -> harness.castInstant(player1, 0, noncreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotTargetCreatureInOpponentsGraveyard() {
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(bears));
        harness.setHand(player1, List.of(new AssembleFromParts()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void tokenCopyRetainsSourcesManaCost() {
        Card bears = grantAbilityToBears();
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst().orElseThrow();
        assertThat(token.getCard().getManaCost()).isEqualTo(bears.getManaCost());
        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.BEAR, CardSubtype.ZOMBIE);
    }

    @Test
    void exilesSourceAsCostBeforeAbilityResolves() {
        Card bears = grantAbilityToBears();
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.activateGraveyardAbility(player1, 0);

        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(bears);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(bears);
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(bears);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }

    @Test
    void cannotActivateOutsideMainPhaseOrDuringOpponentsTurn() {
        Card bears = grantAbilityToBears();
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player2);
        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(bears);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(bears);
    }

    @Test
    void cannotActivateWithSpellOnStack() {
        Card bears = grantAbilityToBears();
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castInstant(player1, 0, player2.getId());

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(bears);
    }

    @Test
    void grantedAbilityPersistsAfterSourceReturnsFromLibraryToGraveyard() {
        Card bears = grantAbilityToBears();
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.playerDecks.get(player1.getId())).contains(bears);

        harness.setLibrary(player1, List.of());
        harness.setGraveyard(player1, List.of(bears));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).contains(bears);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken()).hasSize(2);
    }

    @Test
    void activationRequiresTwoBlackMana() {
        Card bears = grantAbilityToBears();
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(bears);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(bears);
    }

    private Card grantAbilityToBears() {
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        harness.setHand(player1, List.of(new AssembleFromParts()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castAndResolveInstant(player1, 0, bears.getId());
        return bears;
    }
}
