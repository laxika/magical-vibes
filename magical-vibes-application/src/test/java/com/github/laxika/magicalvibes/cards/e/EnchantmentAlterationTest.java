package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EnchantmentAlterationTest extends BaseCardTest {

    @Test
    @DisplayName("Moves an Aura attached to a creature to another legal creature")
    void movesCreatureAuraToAnotherCreature() {
        Permanent oldCreature = addCreatureReady(player2, new GrizzlyBears());
        Permanent newCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent land = addLand(player1, new Island());
        Permanent aura = addAuraAttachedTo(player2, new Pacifism(), oldCreature);
        castSpell(aura);

        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice = gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).contains(newCreature.getId()).doesNotContain(oldCreature.getId(), land.getId());

        harness.handlePermanentChosen(player1, newCreature.getId());

        assertThat(aura.getAttachedTo()).isEqualTo(newCreature.getId());
    }

    @Test
    @DisplayName("Moves an Aura attached to a land to another legal land")
    void movesLandAuraToAnotherLand() {
        Permanent oldLand = addLand(player2, new Island());
        Permanent newLand = addLand(player1, new Island());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent aura = addAuraAttachedTo(player2, new EvilPresence(), oldLand);
        castSpell(aura);

        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice = gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).contains(newLand.getId()).doesNotContain(oldLand.getId(), creature.getId());

        harness.handlePermanentChosen(player1, newLand.getId());

        assertThat(aura.getAttachedTo()).isEqualTo(newLand.getId());
    }

    @Test
    @DisplayName("Does nothing when no other same-type permanent can receive the Aura")
    void staysAttachedWhenNoRecipientExists() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        Permanent aura = addAuraAttachedTo(player2, new Pacifism(), creature);
        castSpell(aura);

        harness.passBothPriorities();

        assertThat(aura.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
    }

    @Test
    @DisplayName("Cannot target an Aura not attached to a creature or land")
    void rejectsInvalidAuraTarget() {
        Permanent invalidAura = new Permanent(new Pacifism());
        gd.playerBattlefields.get(player2.getId()).add(invalidAura);
        Permanent validHost = addCreatureReady(player2, new GrizzlyBears());
        addAuraAttachedTo(player2, new Pacifism(), validHost);
        harness.setHand(player1, List.of(new EnchantmentAlteration()));
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, invalidAura.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Aura attached to a creature or land");
    }

    private void castSpell(Permanent aura) {
        harness.setHand(player1, List.of(new EnchantmentAlteration()));
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.BLUE, 1);
        harness.castInstant(player1, 0, aura.getId());
    }

    private Permanent addAuraAttachedTo(com.github.laxika.magicalvibes.model.Player owner,
                                        com.github.laxika.magicalvibes.model.Card auraCard,
                                        Permanent host) {
        Permanent aura = new Permanent(auraCard);
        aura.setAttachedTo(host.getId());
        gd.playerBattlefields.get(owner.getId()).add(aura);
        return aura;
    }

    private Permanent addLand(com.github.laxika.magicalvibes.model.Player player,
                              com.github.laxika.magicalvibes.model.Card landCard) {
        Permanent land = new Permanent(landCard);
        gd.playerBattlefields.get(player.getId()).add(land);
        return land;
    }
}
