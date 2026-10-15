package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AuraFinesse;
import com.github.laxika.magicalvibes.cards.c.CeremonialKnife;
import com.github.laxika.magicalvibes.cards.t.TravelingMinister;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SigardasImprisonment.class, CeremonialKnife.class, TravelingMinister.class, AuraFinesse.class})
class SigardasImprisonmentTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature cannot attack or block")
    void enchantedCreatureCannotAttackOrBlock() {
        Permanent enchanted = addCreatureReady(player1, new TravelingMinister());
        Permanent aura = attachAura(player2, enchanted);

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");

        Permanent attacker = addCreatureReady(player2, new TravelingMinister());
        attacker.setAttacking(true);
        prepareDeclareBlockers(player2);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player1,
                List.of(new BlockerAssignment(0, 1))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid blocker index");

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(aura);
    }

    @Test
    @DisplayName("Activated ability exiles enchanted creature and creates a Blood token")
    void activatedAbilityExilesCreatureAndCreatesBlood() {
        Permanent enchanted = addCreatureReady(player2, new TravelingMinister());
        Permanent aura = attachAura(player1, enchanted);

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(aura), null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(enchanted);
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(enchanted.getCard());
        assertThat(findPermanents(player1, "Blood")).hasSize(1);
        assertThat(findPermanent(player1, "Blood").getCard().getSubtypes()).contains(CardSubtype.BLOOD);
        harness.assertInGraveyard(player1, "Sigarda's Imprisonment");
    }

    @Test
    @DisplayName("Cannot cast Sigarda's Imprisonment on a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new CeremonialKnife());
        harness.setHand(player1, List.of(new SigardasImprisonment()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    void castingAttachesAuraToCreature() {
        Permanent creature = addCreatureReady(player2, new TravelingMinister());
        harness.setHand(player1, List.of(new SigardasImprisonment()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Sigarda's Imprisonment").getAttachedTo())
                .isEqualTo(creature.getId());
        assertThatThrownBy(() -> declareAttackers(player2, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    void eachActivationCreatesBloodEvenAfterFirstResolutionRemovesAuraAndCreature() {
        Permanent creature = addCreatureReady(player2, new TravelingMinister());
        Permanent aura = attachAura(player1, creature);
        harness.addMana(player1, ManaColor.WHITE, 10);

        harness.activateAbility(player1, 0, null, null);
        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(creature.getCard());
        assertThat(findPermanents(player1, "Blood")).hasSize(2);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(aura);
        harness.assertInGraveyard(player1, "Sigarda's Imprisonment");
    }

    @Test
    @CardUsed({SigardasImprisonment.class, TravelingMinister.class, AuraFinesse.class})
    void exilesCurrentlyEnchantedCreatureWhenAuraMovesInResponse() {
        Permanent original = addCreatureReady(player2, new TravelingMinister());
        Permanent destination = addCreatureReady(player2, new TravelingMinister());
        Permanent aura = attachAura(player1, original);
        harness.setHand(player1, List.of(new AuraFinesse()));
        harness.setLibrary(player1, List.of(new TravelingMinister()));
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.castInstant(player1, 0, List.of(aura.getId(), destination.getId()));
        harness.passBothPriorities();
        assertThat(aura.getAttachedTo()).isEqualTo(destination.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(original).doesNotContain(destination);
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(destination.getCard());
        assertThat(findPermanents(player1, "Blood")).hasSize(1);
        harness.assertInGraveyard(player1, "Sigarda's Imprisonment");
    }

    private Permanent attachAura(Player controller, Permanent creature) {
        Permanent aura = new Permanent(new SigardasImprisonment());
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(controller.getId()).add(aura);
        return aura;
    }
}
