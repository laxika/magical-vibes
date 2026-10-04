package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.ApocalypseDemon;
import com.github.laxika.magicalvibes.cards.a.AvenOfEnduringHope;
import com.github.laxika.magicalvibes.cards.h.HinterlandLogger;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.w.WoodlandChangeling;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GodPharaohsGift.class, GrizzlyBears.class, Plains.class,
        AvenOfEnduringHope.class, HinterlandLogger.class, WoodlandChangeling.class, ApocalypseDemon.class})
class GodPharaohsGiftTest extends BaseCardTest {

    private void advanceToCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(activePlayer, TurnStep.BEGINNING_OF_COMBAT);
    }

    private Permanent findToken(Player owner, String name) {
        return gd.playerBattlefields.get(owner.getId()).stream()
                .filter(p -> p.getCard().isToken() && p.getCard().getName().equals(name))
                .findFirst()
                .orElseThrow();
    }

    @Test
    @DisplayName("Triggers at beginning of combat on controller's turn and offers graveyard choice")
    void triggersOnControllersCombat() {
        harness.addToBattlefield(player1, new GodPharaohsGift());
        harness.setGraveyard(player1, new ArrayList<>(List.of(new GrizzlyBears())));

        advanceToCombat(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
    }

    @Test
    @DisplayName("Does not trigger at beginning of combat on opponent's turn")
    void doesNotTriggerOnOpponentsCombat() {
        harness.addToBattlefield(player1, new GodPharaohsGift());
        harness.setGraveyard(player1, new ArrayList<>(List.of(new GrizzlyBears())));

        advanceToCombat(player2);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().isToken());
    }

    @Test
    @DisplayName("Exiling a creature creates a 4/4 black Zombie token copy with haste until end of turn")
    void acceptingCreatesFourFourBlackZombieWithHaste() {
        harness.addToBattlefield(player1, new GodPharaohsGift());
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, new ArrayList<>(List.of(bears)));
        UUID bearsId = bears.getId();

        advanceToCombat(player1);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(bearsId));

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getId().equals(bearsId));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getId().equals(bearsId));

        Permanent token = findToken(player1, "Grizzly Bears");
        assertThat(token.getCard().getPower()).isEqualTo(4);
        assertThat(token.getCard().getToughness()).isEqualTo(4);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.BLACK);
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.ZOMBIE);
        assertThat(token.getGrantedKeywords()).contains(Keyword.HASTE);
        assertThat(token.getCard().getKeywords()).doesNotContain(Keyword.HASTE);
    }

    @Test
    @DisplayName("Declining the choice leaves the graveyard unchanged and creates no token")
    void decliningDoesNothing() {
        harness.addToBattlefield(player1, new GodPharaohsGift());
        harness.setGraveyard(player1, new ArrayList<>(List.of(new GrizzlyBears())));

        advanceToCombat(player1);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of());

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().isToken());
    }

    @Test
    @DisplayName("Non-creature cards in the graveyard are not offered")
    void onlyCreatureCardsAreOffered() {
        harness.addToBattlefield(player1, new GodPharaohsGift());
        GrizzlyBears bears = new GrizzlyBears();
        Plains plains = new Plains();
        harness.setGraveyard(player1, new ArrayList<>(List.of(bears, plains)));

        advanceToCombat(player1);
        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(bears.getId());
    }

    @Test
    @DisplayName("Copied enters abilities trigger and flying is retained")
    void copiedEntersAbilityTriggers() {
        harness.addToBattlefield(player1, new GodPharaohsGift());
        AvenOfEnduringHope aven = new AvenOfEnduringHope();
        harness.setGraveyard(player1, List.of(aven));
        harness.setLife(player1, 20);

        advanceToCombat(player1);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(aven.getId()));
        harness.passBothPriorities();

        harness.assertLife(player1, 23);
        Permanent token = findToken(player1, "Aven of Enduring Hope");
        assertThat(token.hasKeyword(Keyword.FLYING)).isTrue();
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(4);
    }

    @Test
    @DisplayName("Haste expires at cleanup while the token remains")
    void hasteExpiresButTokenRemains() {
        harness.addToBattlefield(player1, new GodPharaohsGift());
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));

        advanceToCombat(player1);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));
        Permanent token = findToken(player1, "Grizzly Bears");
        assertThat(token.hasKeyword(Keyword.HASTE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(token);
        assertThat(token.hasKeyword(Keyword.HASTE)).isFalse();
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(4);
    }

    @Test
    @DisplayName("A creature entering the graveyard after the trigger can be chosen")
    void graveyardChoiceIsMadeAtResolution() {
        harness.addToBattlefield(player1, new GodPharaohsGift());
        harness.setGraveyard(player1, List.of());
        advanceToCombat(player1);

        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));

        assertThat(findToken(player1, "Grizzly Bears")).isNotNull();
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("An empty own graveyard does not use an opponent's creature")
    void doesNotUseOpponentsGraveyard() {
        harness.addToBattlefield(player1, new GodPharaohsGift());
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of(new GrizzlyBears()));

        advanceToCombat(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(p -> p.getCard().isToken());
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @CardUsed({HinterlandLogger.class})
    @DisplayName("A token copying a double-faced creature can transform")
    void doubleFacedTokenCanTransform() {
        harness.addToBattlefield(player1, new GodPharaohsGift());
        HinterlandLogger logger = new HinterlandLogger();
        harness.setGraveyard(player1, List.of(logger));

        advanceToCombat(player1);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(logger.getId()));
        Permanent token = findToken(player1, "Hinterland Logger");

        gd.spellsCastLastTurn.clear();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.UPKEEP);
        harness.passBothPriorities();

        assertThat(token.isTransformed()).isTrue();
        assertThat(token.getCard().getName()).isEqualTo("Timber Shredder");
        assertThat(token.hasKeyword(Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(4);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.BLACK);
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.ZOMBIE);
    }


    @Test
    @CardUsed({WoodlandChangeling.class})
    @DisplayName("Replacing creature types with Zombie does not copy changeling")
    void tokenDoesNotRetainChangeling() {
        harness.addToBattlefield(player1, new GodPharaohsGift());
        WoodlandChangeling changeling = new WoodlandChangeling();
        harness.setGraveyard(player1, List.of(changeling));

        advanceToCombat(player1);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(changeling.getId()));

        Permanent token = findToken(player1, "Woodland Changeling");
        assertThat(gqs.hasKeyword(gd, token, Keyword.CHANGELING)).isFalse();
        assertThat(gqs.hasEffectiveSubtype(gd, token, CardSubtype.ZOMBIE)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, token, CardSubtype.ELF)).isFalse();
    }

    @Test
    @CardUsed({ApocalypseDemon.class})
    @DisplayName("The copy's power and toughness stay 4/4 instead of counting the graveyard")
    void powerToughnessDefiningAbilityIsNotCopied() {
        harness.addToBattlefield(player1, new GodPharaohsGift());
        ApocalypseDemon demon = new ApocalypseDemon();
        harness.setGraveyard(player1, List.of(demon, new Plains()));

        advanceToCombat(player1);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(demon.getId()));

        Permanent token = findToken(player1, "Apocalypse Demon");
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(4);
        assertThat(token.hasKeyword(Keyword.FLYING)).isTrue();
    }

}
