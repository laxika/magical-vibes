package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.BearTrap;
import com.github.laxika.magicalvibes.cards.d.DredgingClaw;
import com.github.laxika.magicalvibes.cards.l.LeylineOfTheVoid;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ComeBackWrong.class, CautiousSurvivor.class, BearTrap.class, DredgingClaw.class, LeylineOfTheVoid.class})
class ComeBackWrongTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys an opponent's creature and returns it under your control")
    void destroysAndReturnsCreatureUnderYourControl() {
        Permanent target = addSurvivor(player2);

        castComeBackWrong(target);

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        Permanent returned = findPermanent(player1, "Cautious Survivor");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(returned);
        assertThat(returned.getId()).isNotEqualTo(target.getId());

        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        harness.assertOnBattlefield(player1, "Cautious Survivor");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Cautious Survivor");
        harness.assertInGraveyard(player2, "Cautious Survivor");
    }

    @Test
    @DisplayName("Does not return an indestructible creature")
    void doesNotReturnIndestructibleCreature() {
        Permanent target = addSurvivor(player2);
        target.getGrantedKeywords().add(Keyword.INDESTRUCTIBLE);

        castComeBackWrong(target);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
    }

    @Test
    @DisplayName("Can target only a creature")
    void canTargetOnlyCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new BearTrap());
        harness.setHand(player1, List.of(new ComeBackWrong()));
        addMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Can destroy and return your own creature")
    void returnsYourOwnCreature() {
        Permanent target = addSurvivor(player1);

        castComeBackWrong(target);

        Permanent returned = findPermanent(player1, "Cautious Survivor");
        assertThat(returned.getId()).isNotEqualTo(target.getId());
        harness.assertNotInGraveyard(player1, "Cautious Survivor");
    }

    @Test
    @DisplayName("Regeneration prevents both destruction and return")
    void doesNotReturnRegeneratedCreature() {
        Permanent target = addSurvivor(player2);
        target.setRegenerationShield(1);

        castComeBackWrong(target);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(target.isTapped()).isTrue();
        harness.assertNotOnBattlefield(player1, "Cautious Survivor");
        harness.assertNotInGraveyard(player2, "Cautious Survivor");
    }

    @Test
    @DisplayName("A destroyed creature token is not returned")
    void doesNotReturnCreatureToken() {
        CautiousSurvivor token = new CautiousSurvivor();
        token.setToken(true);
        token.setOwnerId(player2.getId());
        Permanent target = harness.addToBattlefieldAndReturn(player2, token);

        castComeBackWrong(target);

        harness.assertNotOnBattlefield(player1, "Cautious Survivor");
        harness.assertNotOnBattlefield(player2, "Cautious Survivor");
        harness.assertNotInGraveyard(player2, "Cautious Survivor");
    }

    @Test
    @DisplayName("A destroyed face-down noncreature card stays in the graveyard")
    void doesNotReturnFaceDownNoncreatureCard() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BearTrap());
        target.setFaceDown(2, 2, Set.of(CardType.CREATURE));

        castComeBackWrong(target);

        harness.assertNotOnBattlefield(player1, "Bear Trap");
        harness.assertNotOnBattlefield(player2, "Bear Trap");
        harness.assertInGraveyard(player2, "Bear Trap");
    }

    @Test
    @DisplayName("A destroyed face-down creature card returns face up")
    void returnsFaceDownCreatureCardFaceUp() {
        Permanent target = addSurvivor(player2);
        target.setFaceDown(2, 2, Set.of(CardType.CREATURE));

        castComeBackWrong(target);

        Permanent returned = findPermanent(player1, "Cautious Survivor");
        assertThat(returned.isFaceDown()).isFalse();
        assertThat(returned.getId()).isNotEqualTo(target.getId());
    }

    @Test
    @DisplayName("The delayed sacrifice cannot sacrifice a permanent another player controls")
    void doesNotSacrificeAfterLosingControl() {
        Permanent target = addSurvivor(player2);
        castComeBackWrong(target);
        Permanent returned = findPermanent(player1, "Cautious Survivor");
        gd.playerBattlefields.get(player1.getId()).remove(returned);
        gd.playerBattlefields.get(player2.getId()).add(returned);

        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        if (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(returned);
        harness.assertNotInGraveyard(player2, "Cautious Survivor");
    }

    @Test
    @DisplayName("Skipping your end step does not sacrifice the creature on the opponent's end step")
    void waitsForYourNextEndStepAfterSkippedEndStep() {
        castComeBackWrong(addSurvivor(player2));

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntilWithNoAttackers(player2, TurnStep.END_STEP);

        harness.assertOnBattlefield(player1, "Cautious Survivor");
        assertThat(gd.stack).isEmpty();

        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        harness.assertOnBattlefield(player1, "Cautious Survivor");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Cautious Survivor");
        harness.assertInGraveyard(player2, "Cautious Survivor");
    }

    @Test
    @DisplayName("Returning an opponent's creature triggers equipment watching their graveyard")
    void usesTheOriginalGraveyardForEntryTriggers() {
        harness.addToBattlefield(player2, new DredgingClaw());

        castComeBackWrong(addSurvivor(player2));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        PendingInteraction.MayAbilityChoice choice =
                (PendingInteraction.MayAbilityChoice) gd.interaction.activeInteraction();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, false);
        harness.assertOnBattlefield(player1, "Cautious Survivor");
    }

    @Test
    @DisplayName("A creature exiled instead of dying is not returned")
    void doesNotReturnCreatureExiledByReplacement() {
        harness.addToBattlefield(player1, new LeylineOfTheVoid());
        Permanent target = addSurvivor(player2);

        castComeBackWrong(target);

        harness.assertNotOnBattlefield(player1, "Cautious Survivor");
        harness.assertNotOnBattlefield(player2, "Cautious Survivor");
        harness.assertNotInGraveyard(player2, "Cautious Survivor");
        assertThat(gd.findExiledCard(target.getCard().getId())).isNotNull();
    }

    private Permanent addSurvivor(Player player) {
        CautiousSurvivor card = new CautiousSurvivor();
        card.setOwnerId(player.getId());
        return harness.addToBattlefieldAndReturn(player, card);
    }

    private void castComeBackWrong(Permanent target) {
        harness.setHand(player1, List.of(new ComeBackWrong()));
        addMana();
        harness.castAndResolveSorcery(player1, 0, target.getId());
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
