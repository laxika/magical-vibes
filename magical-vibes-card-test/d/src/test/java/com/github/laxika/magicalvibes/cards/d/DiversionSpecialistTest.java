package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.l.LeylineOfResonance;
import com.github.laxika.magicalvibes.cards.h.HauntedScreen;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DiversionSpecialist.class, LeylineOfResonance.class, HauntedScreen.class, Mountain.class})
class DiversionSpecialistTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing another creature exiles the top card and lets it be played this turn")
    void sacrificesCreatureAndExilesTopCard() {
        Permanent specialist = addCreatureReady(player1, new DiversionSpecialist());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new DiversionSpecialist());
        Card top = putCardOnTop(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, battlefieldIndex(specialist), 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(creature.getCard());
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(top);
        assertThat(gd.exilePlayPermissions).containsEntry(top.getId(), player1.getId());
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn).contains(top.getId());
    }

    @Test
    @DisplayName("The activation can sacrifice an enchantment")
    void sacrificesEnchantment() {
        Permanent specialist = addCreatureReady(player1, new DiversionSpecialist());
        Permanent enchantment = harness.addToBattlefieldAndReturn(player1, new LeylineOfResonance());
        Card top = putCardOnTop(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, battlefieldIndex(specialist), 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(enchantment.getCard());
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(top);
    }

    @Test
    @DisplayName("The sacrifice choice only allows another creature or enchantment")
    void sacrificeChoiceFiltersPermanents() {
        Permanent specialist = addCreatureReady(player1, new DiversionSpecialist());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new DiversionSpecialist());
        Permanent enchantment = harness.addToBattlefieldAndReturn(player1, new LeylineOfResonance());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new HauntedScreen());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, battlefieldIndex(specialist), 0, null, null);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactlyInAnyOrder(creature.getId(), enchantment.getId());
        harness.handlePermanentChosen(player1, creature.getId());

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(creature.getCard());
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(artifact);
    }

    @Test
    @DisplayName("The activation cannot sacrifice an artifact")
    void cannotSacrificeArtifact() {
        Permanent specialist = addCreatureReady(player1, new DiversionSpecialist());
        harness.addToBattlefield(player1, new HauntedScreen());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(specialist), 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sacrifice");
    }

    @Test
    void cannotSacrificeItselfOrAnOpponentsCreature() {
        Permanent specialist = harness.addToBattlefieldAndReturn(player1, new DiversionSpecialist());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new DiversionSpecialist());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(specialist), 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sacrifice");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(specialist);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opponentCreature);
    }

    @Test
    void tappedSummoningSickSourcePaysSacrificeBeforeExilingOnResolution() {
        Permanent specialist = harness.addToBattlefieldAndReturn(player1, new DiversionSpecialist());
        specialist.setSummoningSick(true);
        specialist.tap();
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new LeylineOfResonance());
        Card top = putCardOnTop(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, battlefieldIndex(specialist), 0, null, null);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(sacrifice.getCard());
        assertThat(gd.playerDecks.get(player1.getId())).contains(top);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(top);
        harness.passBothPriorities();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(top);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(specialist);
    }

    @Test
    void exiledSpellRequiresItsNormalManaCost() {
        Permanent specialist = harness.addToBattlefieldAndReturn(player1, new DiversionSpecialist());
        harness.addToBattlefield(player1, new LeylineOfResonance());
        Card top = new DiversionSpecialist();
        harness.setLibrary(player1, List.of(top));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, battlefieldIndex(specialist), 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.castFromExile(player1, top.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(top);

        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castFromExile(player1, top.getId());
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(top);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anySatisfy(permanent -> assertThat(permanent.getCard()).isSameAs(top));
    }

    @Test
    void mayPlayAnExiledLandButDoesNotGrantAnExtraLandPlay() {
        Permanent specialist = harness.addToBattlefieldAndReturn(player1, new DiversionSpecialist());
        harness.addToBattlefield(player1, new LeylineOfResonance());
        Card top = putCardOnTop(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, battlefieldIndex(specialist), 0, null, null);
        harness.passBothPriorities();

        harness.castFromExile(player1, top.getId());

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anySatisfy(permanent -> assertThat(permanent.getCard()).isSameAs(top));
        harness.setHand(player1, List.of(new Mountain()));
        assertThatThrownBy(() -> harness.playLand(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void activatedAbilityWorksOnOpponentsTurnButCreatureCannotBeCastThen() {
        Permanent specialist = harness.addToBattlefieldAndReturn(player1, new DiversionSpecialist());
        harness.addToBattlefield(player1, new LeylineOfResonance());
        Card top = new DiversionSpecialist();
        harness.setLibrary(player1, List.of(top));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, battlefieldIndex(specialist), 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(top);
        assertThatThrownBy(() -> harness.castFromExile(player1, top.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(top);
    }

    @Test
    void unplayedCardStaysExiledAndPermissionExpiresAtEndOfTurn() {
        Permanent specialist = harness.addToBattlefieldAndReturn(player1, new DiversionSpecialist());
        harness.addToBattlefield(player1, new LeylineOfResonance());
        Card top = new Mountain();
        harness.setLibrary(player1, List.of(top, new Mountain(), new Mountain()));
        harness.setLibrary(player2, List.of(new Mountain(), new Mountain()));
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, battlefieldIndex(specialist), 0, null, null);
        harness.passBothPriorities();

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(top);
        assertThatThrownBy(() -> harness.castFromExile(player1, top.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(top);
    }

    @Test
    void emptyLibraryStillAllowsPayingTheCostWithoutDrawingACard() {
        Permanent specialist = harness.addToBattlefieldAndReturn(player1, new DiversionSpecialist());
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new LeylineOfResonance());
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, battlefieldIndex(specialist), 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(sacrifice.getCard());
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.status).isEqualTo(com.github.laxika.magicalvibes.model.GameStatus.RUNNING);
    }

    @Test
    void menaceRequiresAtLeastTwoBlockers() {
        addCreatureReady(player1, new DiversionSpecialist());
        Permanent first = addCreatureReady(player2, new DiversionSpecialist());
        Permanent second = addCreatureReady(player2, new DiversionSpecialist());
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("two or more creatures");
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS,
                () -> gs.declareBlockers(gd, player2,
                        List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0))));

        assertThat(first.isBlocking()).isTrue();
        assertThat(second.isBlocking()).isTrue();
    }

    @Test
    void abilityResolvesAndPermissionSurvivesAfterItsSourceIsSacrificed() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new DiversionSpecialist());
        harness.addToBattlefield(player1, new LeylineOfResonance());
        Card top = new Mountain();
        Card next = new HauntedScreen();
        harness.setLibrary(player1, List.of(top, next));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, battlefieldIndex(first), 0, null, null);
        Permanent second = harness.addToBattlefieldAndReturn(player1, new DiversionSpecialist());
        harness.activateAbility(player1, battlefieldIndex(second), 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(first.getCard());
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(top, next);
        harness.castFromExile(player1, top.getId());
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anySatisfy(permanent -> assertThat(permanent.getCard()).isSameAs(top));
    }

    @Test
    void activationRequiresOneManaEvenWithAnEligibleSacrifice() {
        Permanent specialist = harness.addToBattlefieldAndReturn(player1, new DiversionSpecialist());
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new LeylineOfResonance());

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(specialist), 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(specialist, sacrifice);
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }

    private Card putCardOnTop(Player player) {
        Card card = new Mountain();
        harness.setLibrary(player, List.of(card));
        return card;
    }
}
