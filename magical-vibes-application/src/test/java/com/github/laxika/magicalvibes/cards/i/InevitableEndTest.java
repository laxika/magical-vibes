package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({InevitableEnd.class, GrizzlyBears.class, Swamp.class})
class InevitableEndTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Inevitable End attaches it to the target creature")
    void resolvingAttachesToTargetCreature() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());

        castInevitableEnd(creature);

        Permanent aura = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof InevitableEnd)
                .findFirst()
                .orElseThrow();
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

    private void castInevitableEnd(Permanent creature) {
        harness.setHand(player1, List.of(new InevitableEnd()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
    }
}
