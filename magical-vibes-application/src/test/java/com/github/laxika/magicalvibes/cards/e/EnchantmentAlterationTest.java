package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.FertileGround;
import com.github.laxika.magicalvibes.cards.g.GorillaWarrior;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.cards.s.SpreadingAlgae;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EnchantmentAlteration.class, Exploration.class, FertileGround.class, GorillaWarrior.class,
        Island.class, Pacifism.class, SpreadingAlgae.class, Swamp.class})
class EnchantmentAlterationTest extends BaseCardTest {

    @Test
    @DisplayName("Moves a creature Aura to another creature chosen during resolution")
    void movesCreatureAuraToAnotherCreature() {
        Permanent firstCreature = addCreature(player1);
        Permanent secondCreature = addCreature(player1);
        Permanent thirdCreature = addCreature(player2);
        Permanent aura = addAuraAttachedTo(player1, new Pacifism(), firstCreature);

        cast(aura);
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice = gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).contains(secondCreature.getId(), thirdCreature.getId())
                .doesNotContain(firstCreature.getId());

        harness.handlePermanentChosen(player1, secondCreature.getId());

        assertThat(aura.getAttachedTo()).isEqualTo(secondCreature.getId());
    }

    @Test
    @DisplayName("Moves a land Aura only to another land")
    void movesLandAuraOnlyToAnotherLand() {
        Permanent firstLand = addLand(player1);
        Permanent secondLand = addLand(player2);
        Permanent thirdLand = addLand(player2);
        Permanent creature = addCreature(player2);
        Permanent aura = addAuraAttachedTo(player1, new FertileGround(), firstLand);

        cast(aura);
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice = gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).contains(secondLand.getId(), thirdLand.getId())
                .doesNotContain(firstLand.getId(), creature.getId());

        harness.handlePermanentChosen(player1, secondLand.getId());

        assertThat(aura.getAttachedTo()).isEqualTo(secondLand.getId());
    }

    @Test
    @DisplayName("Does not move an Aura to a same-type permanent it cannot enchant")
    void doesNotMoveAuraToIllegalSameTypePermanent() {
        Permanent swamp = addSwamp(player1);
        addLand(player1);
        Permanent aura = addAuraAttachedTo(player1, new SpreadingAlgae(), swamp);

        cast(aura);
        harness.passBothPriorities();

        assertThat(aura.getAttachedTo()).isEqualTo(swamp.getId());
    }

    @Test
    @DisplayName("Cannot target an Aura attached to a noncreature, nonland permanent")
    void cannotTargetAuraAttachedToAnotherType() {
        Permanent unsupportedHost = harness.addToBattlefieldAndReturn(player1, new Exploration());
        Permanent aura = addAuraAttachedTo(player1, new Pacifism(), unsupportedHost);

        harness.setHand(player1, List.of(new EnchantmentAlteration()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, aura.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Aura attached to a creature or land");
        assertThat(aura.getAttachedTo()).isEqualTo(unsupportedHost.getId());
    }

    @Test
    @DisplayName("Moves a creature Aura automatically when only one other creature is available")
    void movesCreatureAuraWithoutChoiceWhenOnlyOneDestinationExists() {
        Permanent firstCreature = addCreature(player1);
        Permanent secondCreature = addCreature(player1);
        Permanent aura = addAuraAttachedTo(player1, new Pacifism(), firstCreature);

        cast(aura);
        harness.passBothPriorities();

        assertThat(aura.getAttachedTo()).isEqualTo(secondCreature.getId());
    }

    @Test
    @DisplayName("Leaves the Aura attached when no other permanent of its type is available")
    void leavesAuraAttachedWhenNoOtherPermanentOfSameTypeExists() {
        Permanent creature = addCreature(player1);
        addLand(player2);
        Permanent aura = addAuraAttachedTo(player1, new Pacifism(), creature);

        cast(aura);
        harness.passBothPriorities();

        assertThat(aura.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Cannot target a permanent that is not an Aura")
    void cannotTargetNonAura() {
        Permanent creature = addCreature(player1);
        Permanent aura = addAuraAttachedTo(player1, new Pacifism(), creature);

        harness.setHand(player1, List.of(new EnchantmentAlteration()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Aura attached to a creature or land");
        assertThat(aura.getAttachedTo()).isEqualTo(creature.getId());
    }

    private void cast(Permanent aura) {
        harness.setHand(player1, List.of(new EnchantmentAlteration()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstant(player1, 0, aura.getId());
    }

    private Permanent addCreature(Player player) {
        return harness.addToBattlefieldAndReturn(player, new GorillaWarrior());
    }

    private Permanent addLand(Player player) {
        return harness.addToBattlefieldAndReturn(player, new Island());
    }

    private Permanent addSwamp(Player player) {
        return harness.addToBattlefieldAndReturn(player, new Swamp());
    }

    private Permanent addAuraAttachedTo(Player player, com.github.laxika.magicalvibes.model.Card card,
                                        Permanent host) {
        Permanent aura = new Permanent(card);
        aura.setAttachedTo(host.getId());
        gd.playerBattlefields.get(player.getId()).add(aura);
        return aura;
    }

}
