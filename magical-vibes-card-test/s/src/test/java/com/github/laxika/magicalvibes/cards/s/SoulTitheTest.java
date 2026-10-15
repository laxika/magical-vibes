package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AuraGraft;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.p.PithingNeedle;
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

@CardUsed({SoulTithe.class, GrizzlyBears.class, Forest.class, Naturalize.class, AuraGraft.class,
        Ornithopter.class, PithingNeedle.class})
class SoulTitheTest extends BaseCardTest {

    @Test
    @DisplayName("Can enchant a nonland permanent")
    void canEnchantNonlandPermanent() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new SoulTithe()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Soul Tithe")
                        && p.isAttached()
                        && p.getAttachedTo().equals(creature.getId()));
    }

    @Test
    @DisplayName("Cannot enchant a land")
    void cannotEnchantLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        addCreatureReady(player2, new GrizzlyBears()); // legal target so the Aura is playable

        harness.setHand(player1, List.of(new SoulTithe()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a nonland permanent");
    }

    @Test
    @DisplayName("Paying the enchanted permanent's mana value keeps it on the battlefield")
    void payingKeepsPermanent() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears()); // mana value 2
        attachSoulTithe(creature);

        advanceToUpkeep(player2);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isEqualTo(0);
    }

    @Test
    @DisplayName("Declining sacrifices the enchanted permanent")
    void decliningSacrificesPermanent() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        attachSoulTithe(creature);

        advanceToUpkeep(player2);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(creature);
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Accepting without enough mana still sacrifices the enchanted permanent")
    void cannotPaySacrificesPermanent() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        attachSoulTithe(creature);

        advanceToUpkeep(player2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true); // accepts but has no mana

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(creature);
    }

    @Test
    @DisplayName("Does not trigger during the Aura controller's upkeep")
    void doesNotFireDuringAuraControllerUpkeep() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        attachSoulTithe(creature);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);
    }

    @Test
    @DisplayName("Can enchant and sacrifice a noncreature artifact")
    void canEnchantAndSacrificeArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new PithingNeedle());
        harness.setHand(player1, List.of(new SoulTithe()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castEnchantment(player1, 0, artifact.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Soul Tithe").getAttachedTo()).isEqualTo(artifact.getId());
        advanceToUpkeep(player2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        harness.assertInGraveyard(player2, "Pithing Needle");
        harness.assertNotOnBattlefield(player2, "Pithing Needle");
        harness.assertInGraveyard(player1, "Soul Tithe");
    }

    @Test
    @DisplayName("A zero mana value permanent can be kept by paying zero")
    void canPayZeroWithoutMana() {
        Permanent creature = addCreatureReady(player2, new Ornithopter());
        attachSoulTithe(creature);

        advanceToUpkeep(player2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);
        harness.assertOnBattlefield(player1, "Soul Tithe");
    }

    @Test
    @DisplayName("Declining a zero mana payment still sacrifices the permanent")
    void decliningZeroPaymentSacrificesPermanent() {
        Permanent creature = addCreatureReady(player2, new Ornithopter());
        attachSoulTithe(creature);

        advanceToUpkeep(player2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        harness.assertNotOnBattlefield(player2, "Ornithopter");
        harness.assertInGraveyard(player2, "Ornithopter");
    }

    @Test
    @DisplayName("Destroying the Aura in response does not reduce the payment to zero")
    void paymentUsesLastKnownAttachmentAfterAuraIsDestroyed() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        attachSoulTithe(creature);
        Permanent aura = findPermanent(player1, "Soul Tithe");
        advanceToUpkeep(player2);

        harness.setHand(player1, List.of(new Naturalize()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castAndResolveInstant(player1, 0, aura.getId());
        harness.assertInGraveyard(player1, "Soul Tithe");
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(creature);
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("Moving the Aura before resolution makes the newly enchanted permanent be sacrificed")
    void sacrificeUsesCurrentAttachmentAtResolution() {
        Permanent original = addCreatureReady(player2, new GrizzlyBears());
        Permanent replacement = addCreatureReady(player2, new GrizzlyBears());
        attachSoulTithe(original);
        Permanent aura = findPermanent(player1, "Soul Tithe");
        advanceToUpkeep(player2);

        harness.setHand(player1, List.of(new AuraGraft()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player1, 0, aura.getId());
        harness.handlePermanentChosen(player1, replacement.getId());
        assertThat(aura.getAttachedTo()).isEqualTo(replacement.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(original).doesNotContain(replacement);
        harness.assertInGraveyard(player1, "Soul Tithe");
    }

    @Test
    @DisplayName("The upkeep player cannot sacrifice a permanent whose controller has changed")
    void decliningAfterControlChangeDoesNotSacrificePermanent() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        attachSoulTithe(creature);
        advanceToUpkeep(player2);

        // Set up a control change after the upkeep ability has triggered.
        gd.playerBattlefields.get(player2.getId()).remove(creature);
        gd.playerBattlefields.get(player1.getId()).add(creature);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    private void attachSoulTithe(Permanent permanent) {
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new SoulTithe());
        aura.setAttachedTo(permanent.getId());
    }
}
