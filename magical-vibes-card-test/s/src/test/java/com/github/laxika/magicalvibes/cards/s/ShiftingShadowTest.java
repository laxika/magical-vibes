package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DarksteelMyr;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.cards.p.PaladinEnVec;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ShiftingShadow.class, GrizzlyBears.class, Forest.class, SimicGuildmage.class,
        DarksteelMyr.class, Naturalize.class, PaladinEnVec.class})
class ShiftingShadowTest extends BaseCardTest {

    private Permanent attachShadow(Player auraController, Permanent host) {
        Permanent aura = harness.addToBattlefieldAndReturn(auraController, new ShiftingShadow());
        aura.setAttachedTo(host.getId());
        return aura;
    }

    private Permanent addCreature(Player player) {
        return addCreatureReady(player, new GrizzlyBears());
    }

    @Test
    @DisplayName("Enchanted creature has haste")
    void enchantedCreatureHasHaste() {
        Permanent creature = addCreature(player1);
        attachShadow(player1, creature);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Enchanted controller's upkeep destroys the creature and reattaches to the revealed creature")
    void destroysAndRevealsForEnchantedController() {
        Permanent enchanted = addCreature(player2);
        Permanent aura = attachShadow(player1, enchanted);
        harness.setLibrary(player2, List.of(new Forest(), new GrizzlyBears()));

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(enchanted);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));
        Permanent revealed = findPermanent(player2, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(aura);
        assertThat(aura.getAttachedTo()).isEqualTo(revealed.getId());
        assertThat(gd.playerDecks.get(player2.getId()))
                .hasSize(1)
                .allMatch(card -> card.getName().equals("Forest"));
    }

    @Test
    @DisplayName("Aura controller's upkeep does not trigger the ability")
    void doesNotTriggerDuringAuraControllerUpkeep() {
        Permanent enchanted = addCreature(player2);
        Permanent aura = attachShadow(player1, enchanted);
        harness.setLibrary(player2, List.of(new GrizzlyBears()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(enchanted);
        assertThat(aura.getAttachedTo()).isEqualTo(enchanted.getId());
    }

    @Test
    @DisplayName("Without a creature in the library, the unattached Aura goes to the graveyard")
    void noCreatureFoundLeavesAuraToGraveyard() {
        Permanent enchanted = addCreature(player1);
        Permanent aura = attachShadow(player1, enchanted);
        harness.setLibrary(player1, List.of(new Forest()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(enchanted, aura);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"))
                .anyMatch(card -> card.getName().equals("Shifting Shadow"));
    }

    @Test
    @DisplayName("Can enchant only a creature")
    void cannotEnchantLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new ShiftingShadow()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void upkeepAbilityBelongsToEnchantedCreatureAndItsController() {
        Permanent host = addCreature(player2);
        attachShadow(player1, host);
        harness.setLibrary(player2, List.of(new GrizzlyBears()));

        advanceToUpkeep(player2);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getControllerId()).isEqualTo(player2.getId());
        assertThat(gd.stack.getFirst().getSourcePermanentId()).isEqualTo(host.getId());
    }

    @Test
    void movingAuraInResponseDoesNotChangeCreatureDestroyedByPendingTrigger() {
        addCreatureReady(player1, new SimicGuildmage());
        Permanent originalHost = addCreature(player2);
        Permanent newHost = addCreature(player2);
        Permanent aura = attachShadow(player1, originalHost);
        harness.setLibrary(player2, List.of(new GrizzlyBears()));

        advanceToUpkeep(player2);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 1, null, aura.getId());
        harness.passBothPriorities();
        assertThat(aura.getAttachedTo()).isEqualTo(newHost.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(newHost).doesNotContain(originalHost);
        Permanent revealed = findPermanents(player2, "Grizzly Bears").stream()
                .filter(permanent -> !permanent.getId().equals(newHost.getId()))
                .findFirst().orElseThrow();
        assertThat(aura.getAttachedTo()).isEqualTo(revealed.getId());
    }

    @Test
    void indestructibleHostSurvivesButAuraMovesToRevealedCreature() {
        Permanent host = addCreatureReady(player1, new DarksteelMyr());
        Permanent aura = attachShadow(player1, host);
        Forest revealedLand = new Forest();
        Forest unrevealedLand = new Forest();
        harness.setLibrary(player1, List.of(revealedLand, new GrizzlyBears(), unrevealedLand));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        Permanent revealed = findPermanent(player1, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(host, aura, revealed);
        assertThat(aura.getAttachedTo()).isEqualTo(revealed.getId());
        assertThat(gqs.hasKeyword(gd, host, Keyword.HASTE)).isFalse();
        assertThat(gqs.hasKeyword(gd, revealed, Keyword.HASTE)).isTrue();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(unrevealedLand, revealedLand);
    }

    @Test
    void auraRemovedInResponseDoesNotStopDestructionOrReveal() {
        Permanent host = addCreature(player1);
        Permanent aura = attachShadow(player1, host);
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new Naturalize()));

        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, aura.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(host, aura);
        harness.assertInGraveyard(player1, "Shifting Shadow");
        Permanent revealed = findPermanent(player1, "Grizzly Bears");
        assertThat(gqs.hasKeyword(gd, revealed, Keyword.HASTE)).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void protectionFromRedPreventsReattachmentButNotCreatureEntering() {
        Permanent host = addCreature(player1);
        Permanent aura = attachShadow(player1, host);
        harness.setLibrary(player1, List.of(new PaladinEnVec()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Paladin en-Vec");
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(host, aura);
        harness.assertInGraveyard(player1, "Shifting Shadow");
    }

    @Test
    void emptyLibraryStillDestroysHostAndPutsAuraInGraveyard() {
        Permanent host = addCreature(player1);
        Permanent aura = attachShadow(player1, host);
        harness.setLibrary(player1, List.of());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(host, aura);
        harness.assertInGraveyard(player1, "Shifting Shadow");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void castingAuraOntoCreatureGrantsHaste() {
        Permanent host = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ShiftingShadow()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0, host.getId());
        harness.passBothPriorities();

        Permanent aura = findPermanent(player1, "Shifting Shadow");
        assertThat(aura.getAttachedTo()).isEqualTo(host.getId());
        assertThat(gqs.hasKeyword(gd, host, Keyword.HASTE)).isTrue();
    }
}
