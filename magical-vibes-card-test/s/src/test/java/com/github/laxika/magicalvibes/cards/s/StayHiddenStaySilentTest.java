package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.b.BashfulBeastie;
import com.github.laxika.magicalvibes.cards.w.WitheringTorment;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StayHiddenStaySilent.class, BashfulBeastie.class, Forest.class, WitheringTorment.class})
class StayHiddenStaySilentTest extends BaseCardTest {

    @Test
    void enteringAuraTapsEnchantedCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new BashfulBeastie());
        harness.setHand(player1, List.of(new StayHiddenStaySilent()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    void enchantedCreatureDoesNotUntap() {
        Permanent creature = addCreatureReady(player2);
        creature.tap();
        attachAura(player1, creature);

        advanceToNextTurn(player1);

        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    void activatedAbilityShufflesCreatureThenControllerManifestsDread() {
        Permanent creature = addCreatureReady(player2);
        Permanent aura = attachAura(player1, creature);
        Card manifestedCard = new BashfulBeastie();
        Card graveyardCard = new Forest();
        harness.setLibrary(player1, List.of(manifestedCard, graveyardCard));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(aura), null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(creature);
        assertThat(gd.playerDecks.get(player2.getId())).contains(creature.getOriginalCard());

        PendingInteraction.LibraryRevealChoice choice = gd.interaction
                .activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.allCards()).containsExactly(manifestedCard, graveyardCard);

        harness.handleMultipleCardsChosen(player1, List.of(manifestedCard.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.isManifested()
                        && permanent.getCard().getId().equals(manifestedCard.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(graveyardCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void abilityStillShufflesAndManifestsAfterAuraIsDestroyed() {
        Permanent creature = addCreatureReady(player2);
        Permanent aura = attachAura(player1, creature);
        Card manifestedCard = new Forest();
        harness.setLibrary(player1, List.of(manifestedCard));
        harness.addMana(player1, ManaColor.BLUE, 6);
        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(aura), null, null);

        harness.setHand(player2, List.of(new WitheringTorment()));
        harness.addMana(player2, ManaColor.BLACK, 3);
        harness.castInstant(player2, 0, aura.getId());
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Stay Hidden, Stay Silent");
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(creature);
        assertThat(gd.playerDecks.get(player2.getId())).contains(creature.getOriginalCard());
        harness.handleMultipleCardsChosen(player1, List.of(manifestedCard.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.isManifested()
                        && permanent.getCard().getId().equals(manifestedCard.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(manifestedCard);
    }

    @Test
    void creatureDestroyedInResponseDoesNotPreventManifestDread() {
        Permanent creature = addCreatureReady(player2);
        Permanent aura = attachAura(player1, creature);
        Card manifestedCard = new Forest();
        harness.setLibrary(player1, List.of(manifestedCard));
        harness.setLibrary(player2, List.of());
        harness.addMana(player1, ManaColor.BLUE, 6);
        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(aura), null, null);

        harness.setHand(player2, List.of(new WitheringTorment()));
        harness.addMana(player2, ManaColor.BLACK, 3);
        harness.castInstant(player2, 0, creature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Bashful Beastie");
        harness.assertInGraveyard(player1, "Stay Hidden, Stay Silent");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class))
                .isNotNull();
        harness.handleMultipleCardsChosen(player1, List.of(manifestedCard.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.isManifested()
                        && permanent.getCard().getId().equals(manifestedCard.getId()));
    }

    @Test
    void abilityCannotBeActivatedDuringOpponentsTurn() {
        Permanent aura = attachAura(player1, addCreatureReady(player2));
        harness.addMana(player1, ManaColor.BLUE, 6);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(aura), null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void abilityCannotBeActivatedOutsideMainPhase() {
        Permanent aura = attachAura(player1, addCreatureReady(player2));
        harness.addMana(player1, ManaColor.BLUE, 6);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(aura), null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void abilityCannotBeActivatedWithAnotherAbilityOnStack() {
        Permanent aura = attachAura(player1, addCreatureReady(player2));
        harness.addMana(player1, ManaColor.BLUE, 12);
        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(aura), null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(aura), null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void emptyLibraryDoesNotPreventShufflingEnchantedCreature() {
        Permanent creature = addCreatureReady(player2);
        Permanent aura = attachAura(player1, creature);
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.BLUE, 6);
        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(aura), null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(creature);
        assertThat(gd.playerDecks.get(player2.getId())).contains(creature.getOriginalCard());
        harness.assertInGraveyard(player1, "Stay Hidden, Stay Silent");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }

    private Permanent addCreatureReady(Player player) {
        return harness.addToBattlefieldAndReturn(player, new BashfulBeastie());
    }

    private Permanent attachAura(Player controller, Permanent creature) {
        Permanent aura = harness.addToBattlefieldAndReturn(controller, new StayHiddenStaySilent());
        aura.setAttachedTo(creature.getId());
        return aura;
    }

    private void advanceToNextTurn(Player currentActivePlayer) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);
    }
}
