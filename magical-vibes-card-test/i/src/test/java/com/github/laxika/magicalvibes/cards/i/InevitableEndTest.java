package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.NyxbornColossus;
import com.github.laxika.magicalvibes.cards.s.SternDismissal;
import com.github.laxika.magicalvibes.cards.s.Swamp;
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

@CardUsed({InevitableEnd.class, GrizzlyBears.class, Swamp.class, NyxbornColossus.class, SternDismissal.class})
class InevitableEndTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Inevitable End attaches it to the target creature")
    void resolvingAttachesToTargetCreature() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());

        castInevitableEnd(creature);

        Permanent aura = findPermanent(player1, "Inevitable End");
        assertThat(aura.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("At the enchanted creature controller's upkeep, that player sacrifices a creature")
    void enchantedCreatureControllerSacrificesAtUpkeep() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        castInevitableEnd(creature);

        advanceToUpkeep(player2);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(creature);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(creature.getCard());
    }

    @Test
    @DisplayName("The enchanted creature controller chooses which creature to sacrifice")
    void enchantedCreatureControllerChoosesSacrifice() {
        Permanent enchanted = addCreatureReady(player2, new GrizzlyBears());
        Permanent other = addCreatureReady(player2, new GrizzlyBears());
        castInevitableEnd(enchanted);

        advanceToUpkeep(player2);
        resolveAllTriggers();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());

        harness.handlePermanentChosen(player2, other.getId());

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(enchanted).doesNotContain(other);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(other.getCard());
    }

    @Test
    @DisplayName("Inevitable End cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        addCreatureReady(player1, new GrizzlyBears());
        Permanent swamp = harness.addToBattlefieldAndReturn(player1, new Swamp());
        harness.setHand(player1, List.of(new InevitableEnd()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, swamp.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("The Aura controller's upkeep does not trigger an opponent's enchanted creature")
    void doesNotTriggerOnAuraControllersUpkeep() {
        Permanent creature = addCreatureReady(player2, new NyxbornColossus());
        castInevitableEnd(creature);

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Enchanting your own creature makes you sacrifice it on your upkeep")
    void canEnchantOwnCreature() {
        Permanent creature = addCreatureReady(player1, new NyxbornColossus());
        castInevitableEnd(creature);

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(creature);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .contains(creature.getCard())
                .anyMatch(card -> card instanceof InevitableEnd);
    }

    @Test
    @DisplayName("Sacrificing another creature leaves the granted ability for the next upkeep")
    void triggersAgainAfterSacrificingAnotherCreature() {
        Permanent enchanted = addCreatureReady(player2, new NyxbornColossus());
        Permanent other = addCreatureReady(player2, new NyxbornColossus());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Swamp());
        castInevitableEnd(enchanted);

        advanceToUpkeep(player2);
        resolveAllTriggers();
        harness.handlePermanentChosen(player2, other.getId());
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(enchanted, land).doesNotContain(other);

        advanceToUpkeep(player2);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(land).doesNotContain(enchanted, other);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(enchanted.getCard(), other.getCard());
        harness.assertInGraveyard(player1, "Inevitable End");
    }

    @Test
    @DisplayName("Returning the Aura after the upkeep trigger does not stop the sacrifice")
    void triggerSurvivesAuraRemoval() {
        Permanent creature = addCreatureReady(player2, new NyxbornColossus());
        castInevitableEnd(creature);
        Permanent aura = findPermanent(player1, "Inevitable End");

        advanceToUpkeep(player2);
        assertThat(gd.stack).hasSize(1);
        harness.setHand(player2, List.of(new SternDismissal()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player2, 0, aura.getId());
        resolveAllTriggers();

        harness.assertInHand(player1, "Inevitable End");
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(creature);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(creature.getCard());
    }

    @Test
    @DisplayName("Returning the enchanted creature after its trigger still requires another sacrifice")
    void triggerSurvivesEnchantedCreatureRemoval() {
        Permanent enchanted = addCreatureReady(player2, new NyxbornColossus());
        Permanent other = addCreatureReady(player2, new NyxbornColossus());
        castInevitableEnd(enchanted);

        advanceToUpkeep(player2);
        assertThat(gd.stack).hasSize(1);
        harness.setHand(player1, List.of(new SternDismissal()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0, enchanted.getId());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player2.getId())).contains(enchanted.getCard());
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(other.getCard()).doesNotContain(enchanted.getCard());
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(enchanted, other);
        harness.assertInGraveyard(player1, "Inevitable End");
    }

    private void castInevitableEnd(Permanent creature) {
        harness.setHand(player1, List.of(new InevitableEnd()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
    }
}
