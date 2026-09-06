package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.c.CreepingMold;
import com.github.laxika.magicalvibes.cards.e.EbonyCharm;
import com.github.laxika.magicalvibes.cards.e.EmeraldCharm;
import com.github.laxika.magicalvibes.cards.e.EnchantmentAlteration;
import com.github.laxika.magicalvibes.cards.g.GrafdiggersCage;
import com.github.laxika.magicalvibes.cards.p.Python;
import com.github.laxika.magicalvibes.cards.w.WhiteKnight;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.turn.TurnCleanupService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Necromancy.class, Python.class, CreepingMold.class, GrafdiggersCage.class})
class NecromancyTest extends BaseCardTest {

    private void castAndChooseReanimate(Card creature) {
        castAndChooseReanimate(creature, TurnStep.PRECOMBAT_MAIN);
    }

    private void castAndChooseReanimate(Card creature, TurnStep step) {
        harness.forceActivePlayer(player1);
        harness.forceStep(step);
        harness.clearPriorityPassed();
        harness.castFromHand(player1, new Necromancy(), "{2}{B}");
        harness.passBothPriorities(); // resolve enchantment → ETB prompts graveyard target

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.passBothPriorities(); // resolve ETB
    }

    @Test
    @DisplayName("ETB reanimates a creature from your graveyard and attaches as an Aura")
    void reanimatesAndAttaches() {
        Python python = new Python();
        harness.setGraveyard(player1, List.of(python));

        castAndChooseReanimate(python);

        Permanent creature = findPermanent(player1, "Python");
        assertThat(creature).isNotNull();
        harness.assertNotInGraveyard(player1, "Python");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Necromancy")
                        && p.getCard().isAura()
                        && creature.getId().equals(p.getAttachedTo()));
    }

    @Test
    @DisplayName("Reanimates a creature from an opponent's graveyard under your control")
    void reanimatesFromOpponentGraveyard() {
        Python python = new Python();
        harness.setGraveyard(player2, List.of(python));

        castAndChooseReanimate(python);

        harness.assertOnBattlefield(player1, "Python");
        harness.assertNotOnBattlefield(player2, "Python");
        harness.assertNotInGraveyard(player2, "Python");
    }

    @Test
    @DisplayName("When Necromancy leaves, the reanimated creature is sacrificed")
    void sacrificesCreatureWhenLeaves() {
        Python python = new Python();
        harness.setGraveyard(player1, List.of(python));
        harness.setHand(player1, List.of(new Necromancy(), new CreepingMold()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(python.getId()));
        harness.passBothPriorities();

        Permanent aura = findPermanent(player1, "Necromancy");
        assertThat(aura).isNotNull();

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castSorcery(player1, 0, aura.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Python");
        harness.assertInGraveyard(player1, "Python");
        harness.assertInGraveyard(player1, "Necromancy");
    }

    @Test
    @DisplayName("Empty graveyards produce no ETB reanimation")
    void emptyGraveyardNoReanimation() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player1, new Necromancy(), "{2}{B}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        Permanent necromancy = findPermanent(player1, "Necromancy");
        assertThat(necromancy).isNotNull();
        assertThat(necromancy.getCard().isAura()).isFalse();
        assertThat(necromancy.isAttached()).isFalse();
    }

    @Test
    @DisplayName("Cast at sorcery speed, it survives cleanup")
    void castAtSorcerySpeedSurvivesCleanup() {
        Python python = new Python();
        harness.setGraveyard(player1, List.of(python));
        castAndChooseReanimate(python);

        GameTestEngineContext.get().getBean(TurnCleanupService.class).applyCleanupResets(gd);

        harness.assertOnBattlefield(player1, "Necromancy");
        harness.assertOnBattlefield(player1, "Python");
    }

    @Test
    @DisplayName("Cast at instant speed stamps sacrifice-at-next-cleanup")
    void castAtInstantSpeedStampsCleanupSacrifice() {
        Python python = new Python();
        harness.setGraveyard(player1, List.of(python));
        castAndChooseReanimate(python, TurnStep.BEGINNING_OF_COMBAT);

        Permanent necromancy = findPermanent(player1, "Necromancy");
        assertThat(necromancy).isNotNull();
        assertThat(necromancy.isSacrificeAtNextCleanup()).isTrue();
    }

    @Test
    @DisplayName("Cast at instant speed is sacrificed at the next cleanup")
    void castAtInstantSpeedIsSacrificedAtNextCleanup() {
        Python python = new Python();
        harness.setGraveyard(player1, List.of(python));
        castAndChooseReanimate(python, TurnStep.BEGINNING_OF_COMBAT);

        harness.inMutationScope(() ->
                GameTestEngineContext.get().getBean(TurnCleanupService.class).applyCleanupResets(gd));
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Necromancy");
        harness.assertNotOnBattlefield(player1, "Python");
        harness.assertInGraveyard(player1, "Necromancy");
        harness.assertInGraveyard(player1, "Python");
    }

    @Test
    @DisplayName("Only creature cards are offered for the ETB reanimation")
    void onlyCreatureCardsCanBeChosen() {
        CreepingMold noncreature = new CreepingMold();
        Python python = new Python();
        harness.setGraveyard(player1, List.of(noncreature, python));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player1, new Necromancy(), "{2}{B}");
        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(python.getId());

        harness.handleMultipleCardsChosen(player1, List.of(python.getId()));
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Python");
        harness.assertInGraveyard(player1, "Creeping Mold");
    }

    @Test
    @CardUsed(EmeraldCharm.class)
    @DisplayName("If Necromancy leaves before its ETB ability resolves, the ability does nothing")
    void doesNothingIfSourceLeavesBeforeResolution() {
        Python python = new Python();
        harness.setGraveyard(player1, List.of(python));
        castAndChooseReanimateTargetOnly(python);

        Permanent necromancy = findPermanent(player1, "Necromancy");
        assertThat(necromancy).isNotNull();
        harness.setHand(player1, List.of(new EmeraldCharm()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castInstant(player1, 0, 1, necromancy.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Necromancy");
        harness.assertInGraveyard(player1, "Necromancy");
        harness.assertInGraveyard(player1, "Python");
    }

    @Test
    @CardUsed(EbonyCharm.class)
    @DisplayName("If the targeted card leaves the graveyard, Necromancy's ability does not resolve")
    void remainsAnEnchantmentWhenTargetLeavesBeforeResolution() {
        Python python = new Python();
        harness.setGraveyard(player1, List.of(python));
        castAndChooseReanimateTargetOnly(python);

        harness.setHand(player1, List.of(new EbonyCharm()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castInstant(player1, 0, 1, null);
        harness.handleMultipleCardsChosen(player1, List.of(python.getId()));
        resolveAllTriggers();

        assertThat(gd.exiledCards.stream().map(entry -> entry.card().getId()))
                .contains(python.getId());
        harness.assertOnBattlefield(player1, "Necromancy");
        assertThat(findPermanent(player1, "Necromancy").isAttached()).isFalse();
    }

    @Test
    @CardUsed(EnchantmentAlteration.class)
    @DisplayName("Necromancy cannot be moved to a creature it could not enchant")
    void cannotReattachToCreatureNotPutOntoBattlefieldWithNecromancy() {
        Python reanimated = new Python();
        harness.setGraveyard(player1, List.of(reanimated));
        castAndChooseReanimate(reanimated);

        Permanent originalHost = findPermanent(player1, "Python");
        Python otherPython = new Python();
        Permanent otherHost = harness.addToBattlefieldAndReturn(player1, otherPython);
        Permanent necromancy = findPermanent(player1, "Necromancy");
        assertThat(necromancy).isNotNull();

        harness.setHand(player1, List.of(new EnchantmentAlteration()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstant(player1, 0, necromancy.getId());
        harness.passBothPriorities();

        assertThat(necromancy.getAttachedTo()).isEqualTo(originalHost.getId());
        assertThat(necromancy.getAttachedTo()).isNotEqualTo(otherHost.getId());
    }

    @Test
    @CardUsed(GrafdiggersCage.class)
    @DisplayName("A blocked opponent's creature remains in its owner's graveyard")
    void blockedOpponentReanimationStaysInOwnersGraveyard() {
        Python python = new Python();
        harness.setGraveyard(player2, List.of(python));
        harness.addToBattlefield(player1, new GrafdiggersCage());

        castAndChooseReanimate(python);

        harness.assertNotOnBattlefield(player1, "Python");
        harness.assertInGraveyard(player2, "Python");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(python.getId()));
    }

    @Test
    @CardUsed(WhiteKnight.class)
    @DisplayName("A creature with protection from black is sacrificed when Necromancy cannot attach")
    void protectedCreatureIsSacrificedWhenNecromancyCannotAttach() {
        WhiteKnight whiteKnight = new WhiteKnight();
        harness.setGraveyard(player1, List.of(whiteKnight));

        castAndChooseReanimate(whiteKnight);

        harness.assertNotOnBattlefield(player1, "White Knight");
        harness.assertInGraveyard(player1, "White Knight");
        harness.assertInGraveyard(player1, "Necromancy");
    }

    private void castAndChooseReanimateTargetOnly(Card creature) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player1, new Necromancy(), "{2}{B}");
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
    }
}
