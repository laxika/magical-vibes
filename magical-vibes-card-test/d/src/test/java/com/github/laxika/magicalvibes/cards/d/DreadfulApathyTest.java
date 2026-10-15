package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AuraGraft;
import com.github.laxika.magicalvibes.cards.b.Boomerang;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({DreadfulApathy.class, AuraGraft.class, Boomerang.class, FountainOfYouth.class, GrizzlyBears.class})
class DreadfulApathyTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature cannot attack")
    void enchantedCreatureCannotAttack() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        attachAura(player2, creature);

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Enchanted creature cannot block")
    void enchantedCreatureCannotBlock() {
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        attachAura(player1, blocker);
        addCreatureReady(player1, new GrizzlyBears()).setAttacking(true);

        prepareDeclareBlockers(player1);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid blocker index");
    }

    @Test
    @DisplayName("Activated ability exiles the enchanted creature")
    void activatedAbilityExilesEnchantedCreature() {
        Permanent enchanted = addCreatureReady(player2, new GrizzlyBears());
        Permanent aura = attachAura(player1, enchanted);

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(aura), null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(enchanted);
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(enchanted.getCard());
        harness.assertInGraveyard(player1, "Dreadful Apathy");
    }

    @Test
    @DisplayName("Cannot enchant a noncreature permanent")
    void cannotEnchantNonCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new DreadfulApathy()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Casting the Aura attaches it to the chosen creature")
    void castingAttachesToChosenCreature() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new DreadfulApathy()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Dreadful Apathy").getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("The exile ability still resolves after the Aura is bounced")
    void exileResolvesAfterAuraLeavesBattlefield() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        Permanent aura = attachAura(player1, creature);
        harness.setHand(player1, List.of(new Boomerang()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(aura), null, null);
        harness.castAndResolveInstant(player1, 0, aura.getId());
        harness.assertInHand(player1, "Dreadful Apathy");
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(creature.getCard());
        harness.assertInHand(player1, "Dreadful Apathy");
        harness.assertNotInGraveyard(player1, "Dreadful Apathy");
    }

    @Test
    @DisplayName("The exile ability uses the creature enchanted at resolution after Aura Graft")
    void exileUsesCurrentAttachmentAfterAuraGraft() {
        Permanent original = addCreatureReady(player2, new GrizzlyBears());
        Permanent destination = addCreatureReady(player2, new GrizzlyBears());
        Permanent aura = attachAura(player1, original);
        harness.setHand(player1, List.of(new AuraGraft()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(aura), null, null);
        harness.castAndResolveInstant(player1, 0, aura.getId());
        harness.handlePermanentChosen(player1, destination.getId());
        assertThat(aura.getAttachedTo()).isEqualTo(destination.getId());
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(destination.getCard())
                .doesNotContain(original.getCard());
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(original).doesNotContain(destination);
        harness.assertInGraveyard(player1, "Dreadful Apathy");
    }

    private Permanent attachAura(Player controller, Permanent creature) {
        Permanent aura = harness.addToBattlefieldAndReturn(controller, new DreadfulApathy());
        aura.setAttachedTo(creature.getId());
        return aura;
    }
}
