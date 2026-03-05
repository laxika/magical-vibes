package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.cards.g.GraftedExoskeleton;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.p.Pariah;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingExileReturn;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PermanentRemovalServiceTest extends BaseCardTest {

    private PermanentRemovalService prs;

    @org.junit.jupiter.api.BeforeEach
    void setUpPrs() {
        prs = harness.getPermanentRemovalService();
    }

    private Permanent addPermanent(com.github.laxika.magicalvibes.model.Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private static Card indestructibleCreature() {
        Card card = new Card();
        card.setName("Indestructible Golem");
        card.setType(CardType.CREATURE);
        card.setManaCost("{2}");
        card.setColor(null);
        card.setPower(2);
        card.setToughness(2);
        card.setKeywords(Set.of(Keyword.INDESTRUCTIBLE));
        return card;
    }

    // =========================================================================
    // removePermanentToGraveyard
    // =========================================================================

    @Nested
    @DisplayName("removePermanentToGraveyard")
    class RemovePermanentToGraveyard {

        @Test
        @DisplayName("Removes permanent from battlefield and puts card in graveyard")
        void removesFromBattlefieldAndAddsToGraveyard() {
            Permanent bears = addPermanent(player1, new GrizzlyBears());

            boolean result = prs.removePermanentToGraveyard(gd, bears);

            assertThat(result).isTrue();
            harness.assertNotOnBattlefield(player1, "Grizzly Bears");
            harness.assertInGraveyard(player1, "Grizzly Bears");
        }

        @Test
        @DisplayName("Returns false when permanent is not on any battlefield")
        void returnsFalseWhenNotOnBattlefield() {
            Permanent bears = new Permanent(new GrizzlyBears());

            boolean result = prs.removePermanentToGraveyard(gd, bears);

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Permanent with exileIfLeavesBattlefield is exiled instead of going to graveyard")
        void exileReplacementExileIfLeaves() {
            Permanent bears = addPermanent(player1, new GrizzlyBears());
            bears.setExileIfLeavesBattlefield(true);

            boolean result = prs.removePermanentToGraveyard(gd, bears);

            assertThat(result).isTrue();
            harness.assertNotOnBattlefield(player1, "Grizzly Bears");
            harness.assertNotInGraveyard(player1, "Grizzly Bears");
            assertThat(gd.playerExiledCards.get(player1.getId()))
                    .anyMatch(c -> c.getName().equals("Grizzly Bears"));
            assertThat(gd.gameLog)
                    .anyMatch(log -> log.contains("exiled instead of going to the graveyard"));
        }

        @Test
        @DisplayName("Permanent with exileInsteadOfDieThisTurn is exiled instead of going to graveyard")
        void exileReplacementExileInsteadOfDie() {
            Permanent bears = addPermanent(player1, new GrizzlyBears());
            bears.setExileInsteadOfDieThisTurn(true);

            boolean result = prs.removePermanentToGraveyard(gd, bears);

            assertThat(result).isTrue();
            harness.assertNotOnBattlefield(player1, "Grizzly Bears");
            harness.assertNotInGraveyard(player1, "Grizzly Bears");
            assertThat(gd.playerExiledCards.get(player1.getId()))
                    .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        }

        @Test
        @DisplayName("Stolen creature goes to original owner's graveyard")
        void stolenCreatureGoesToOriginalOwnersGraveyard() {
            Permanent stolen = addPermanent(player1, new GrizzlyBears());
            gd.stolenCreatures.put(stolen.getId(), player2.getId());

            prs.removePermanentToGraveyard(gd, stolen);

            // Card goes to player2's graveyard (original owner), not player1's (controller)
            harness.assertInGraveyard(player2, "Grizzly Bears");
            harness.assertNotInGraveyard(player1, "Grizzly Bears");
        }

        @Test
        @DisplayName("Cleans up stolenCreatures entry after removal")
        void cleansUpStolenCreaturesEntry() {
            Permanent stolen = addPermanent(player1, new GrizzlyBears());
            gd.stolenCreatures.put(stolen.getId(), player2.getId());

            prs.removePermanentToGraveyard(gd, stolen);

            assertThat(gd.stolenCreatures).doesNotContainKey(stolen.getId());
        }

        @Test
        @DisplayName("Creature death increments death count this turn")
        void creatureDeathIncreasesDeathCount() {
            Permanent bears = addPermanent(player1, new GrizzlyBears());
            int deathsBefore = gd.creatureDeathCountThisTurn.getOrDefault(player1.getId(), 0);

            prs.removePermanentToGraveyard(gd, bears);

            int deathsAfter = gd.creatureDeathCountThisTurn.getOrDefault(player1.getId(), 0);
            assertThat(deathsAfter).isEqualTo(deathsBefore + 1);
        }

        @Test
        @DisplayName("Non-creature permanent death does not increment creature death count")
        void nonCreatureDoesNotIncrementDeathCount() {
            Permanent artifact = addPermanent(player1, new Spellbook());
            int deathsBefore = gd.creatureDeathCountThisTurn.getOrDefault(player1.getId(), 0);

            prs.removePermanentToGraveyard(gd, artifact);

            int deathsAfter = gd.creatureDeathCountThisTurn.getOrDefault(player1.getId(), 0);
            assertThat(deathsAfter).isEqualTo(deathsBefore);
        }

        @Test
        @DisplayName("Exiled card returns to battlefield when source permanent leaves")
        void exileReturnOnLeave() {
            Permanent source = addPermanent(player1, new SerraAngel());

            Card exiledCard = new GrizzlyBears();
            gd.playerExiledCards.get(player2.getId()).add(exiledCard);
            gd.exileReturnOnPermanentLeave.put(source.getId(), new PendingExileReturn(exiledCard, player2.getId()));

            prs.removePermanentToGraveyard(gd, source);

            // Exiled card should return to the battlefield
            assertThat(gd.playerBattlefields.get(player2.getId()))
                    .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
            // And be removed from exile
            assertThat(gd.playerExiledCards.get(player2.getId()))
                    .noneMatch(c -> c.getName().equals("Grizzly Bears"));
            assertThat(gd.exileReturnOnPermanentLeave).doesNotContainKey(source.getId());
        }

        @Test
        @DisplayName("Equipment with SacrificeOnUnattachEffect sacrifices attached creature when removed")
        void sacrificeOnUnattachWhenEquipmentRemoved() {
            Permanent creature = addPermanent(player1, new GrizzlyBears());
            Permanent equipment = addPermanent(player1, new GraftedExoskeleton());
            equipment.setAttachedTo(creature.getId());

            prs.removePermanentToGraveyard(gd, equipment);

            // Equipment went to graveyard
            harness.assertInGraveyard(player1, "Grafted Exoskeleton");
            // Creature was sacrificed too
            harness.assertInGraveyard(player1, "Grizzly Bears");
            harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        }
    }

    // =========================================================================
    // removePermanentToHand
    // =========================================================================

    @Nested
    @DisplayName("removePermanentToHand")
    class RemovePermanentToHand {

        @Test
        @DisplayName("Removes permanent from battlefield and adds card to owner's hand")
        void removesToHand() {
            Permanent bears = addPermanent(player1, new GrizzlyBears());

            boolean result = prs.removePermanentToHand(gd, bears);

            assertThat(result).isTrue();
            harness.assertNotOnBattlefield(player1, "Grizzly Bears");
            harness.assertInHand(player1, "Grizzly Bears");
        }

        @Test
        @DisplayName("Returns false when permanent is not on any battlefield")
        void returnsFalseWhenNotOnBattlefield() {
            Permanent bears = new Permanent(new GrizzlyBears());

            boolean result = prs.removePermanentToHand(gd, bears);

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Permanent with exileIfLeavesBattlefield is exiled instead of returning to hand")
        void exileReplacementExileIfLeaves() {
            Permanent bears = addPermanent(player1, new GrizzlyBears());
            bears.setExileIfLeavesBattlefield(true);

            boolean result = prs.removePermanentToHand(gd, bears);

            assertThat(result).isTrue();
            harness.assertNotOnBattlefield(player1, "Grizzly Bears");
            harness.assertNotInHand(player1, "Grizzly Bears");
            assertThat(gd.playerExiledCards.get(player1.getId()))
                    .anyMatch(c -> c.getName().equals("Grizzly Bears"));
            assertThat(gd.gameLog)
                    .anyMatch(log -> log.contains("exiled instead of returning to hand"));
        }

        @Test
        @DisplayName("exileInsteadOfDieThisTurn does NOT redirect bounce to exile")
        void exileInsteadOfDieDoesNotAffectBounce() {
            Permanent bears = addPermanent(player1, new GrizzlyBears());
            bears.setExileInsteadOfDieThisTurn(true);

            prs.removePermanentToHand(gd, bears);

            // Should go to hand, not exile
            harness.assertInHand(player1, "Grizzly Bears");
            assertThat(gd.playerExiledCards.get(player1.getId()))
                    .noneMatch(c -> c.getName().equals("Grizzly Bears"));
        }

        @Test
        @DisplayName("Stolen creature returns to original owner's hand")
        void stolenCreatureReturnsToOriginalOwner() {
            Permanent stolen = addPermanent(player1, new GrizzlyBears());
            gd.stolenCreatures.put(stolen.getId(), player2.getId());

            prs.removePermanentToHand(gd, stolen);

            harness.assertInHand(player2, "Grizzly Bears");
            harness.assertNotInHand(player1, "Grizzly Bears");
            assertThat(gd.stolenCreatures).doesNotContainKey(stolen.getId());
        }

        @Test
        @DisplayName("Exiled card returns to battlefield when source permanent is bounced")
        void exileReturnOnLeave() {
            Permanent source = addPermanent(player1, new SerraAngel());

            Card exiledCard = new GrizzlyBears();
            gd.playerExiledCards.get(player2.getId()).add(exiledCard);
            gd.exileReturnOnPermanentLeave.put(source.getId(), new PendingExileReturn(exiledCard, player2.getId()));

            prs.removePermanentToHand(gd, source);

            assertThat(gd.playerBattlefields.get(player2.getId()))
                    .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
            assertThat(gd.playerExiledCards.get(player2.getId()))
                    .noneMatch(c -> c.getName().equals("Grizzly Bears"));
        }
    }

    // =========================================================================
    // removePermanentToExile
    // =========================================================================

    @Nested
    @DisplayName("removePermanentToExile")
    class RemovePermanentToExile {

        @Test
        @DisplayName("Removes permanent from battlefield and adds card to exile zone")
        void removesToExile() {
            Permanent bears = addPermanent(player1, new GrizzlyBears());

            boolean result = prs.removePermanentToExile(gd, bears);

            assertThat(result).isTrue();
            harness.assertNotOnBattlefield(player1, "Grizzly Bears");
            assertThat(gd.playerExiledCards.get(player1.getId()))
                    .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        }

        @Test
        @DisplayName("Returns false when permanent is not on any battlefield")
        void returnsFalseWhenNotOnBattlefield() {
            Permanent bears = new Permanent(new GrizzlyBears());

            boolean result = prs.removePermanentToExile(gd, bears);

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Stolen creature goes to original owner's exile zone")
        void stolenCreatureGoesToOriginalOwnersExile() {
            Permanent stolen = addPermanent(player1, new GrizzlyBears());
            gd.stolenCreatures.put(stolen.getId(), player2.getId());

            prs.removePermanentToExile(gd, stolen);

            assertThat(gd.playerExiledCards.get(player2.getId()))
                    .anyMatch(c -> c.getName().equals("Grizzly Bears"));
            assertThat(gd.playerExiledCards.get(player1.getId()))
                    .noneMatch(c -> c.getName().equals("Grizzly Bears"));
            assertThat(gd.stolenCreatures).doesNotContainKey(stolen.getId());
        }

        @Test
        @DisplayName("Equipment with SacrificeOnUnattachEffect sacrifices creature when exiled")
        void sacrificeOnUnattachWhenEquipmentExiled() {
            Permanent creature = addPermanent(player1, new GrizzlyBears());
            Permanent equipment = addPermanent(player1, new GraftedExoskeleton());
            equipment.setAttachedTo(creature.getId());

            prs.removePermanentToExile(gd, equipment);

            // Equipment went to exile
            assertThat(gd.playerExiledCards.get(player1.getId()))
                    .anyMatch(c -> c.getName().equals("Grafted Exoskeleton"));
            // Creature was sacrificed
            harness.assertInGraveyard(player1, "Grizzly Bears");
            harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        }

        @Test
        @DisplayName("Exiled card returns to battlefield when source permanent is exiled")
        void exileReturnOnLeave() {
            Permanent source = addPermanent(player1, new SerraAngel());

            Card exiledCard = new GrizzlyBears();
            gd.playerExiledCards.get(player2.getId()).add(exiledCard);
            gd.exileReturnOnPermanentLeave.put(source.getId(), new PendingExileReturn(exiledCard, player2.getId()));

            prs.removePermanentToExile(gd, source);

            assertThat(gd.playerBattlefields.get(player2.getId()))
                    .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
            assertThat(gd.playerExiledCards.get(player2.getId()))
                    .noneMatch(c -> c.getName().equals("Grizzly Bears"));
        }
    }

    // =========================================================================
    // tryDestroyPermanent
    // =========================================================================

    @Nested
    @DisplayName("tryDestroyPermanent")
    class TryDestroyPermanent {

        @Test
        @DisplayName("Destroys a normal permanent and sends it to graveyard")
        void destroysNormalPermanent() {
            Permanent bears = addPermanent(player1, new GrizzlyBears());

            boolean result = prs.tryDestroyPermanent(gd, bears);

            assertThat(result).isTrue();
            harness.assertNotOnBattlefield(player1, "Grizzly Bears");
            harness.assertInGraveyard(player1, "Grizzly Bears");
        }

        @Test
        @DisplayName("Indestructible permanent is not destroyed")
        void indestructibleSurvives() {
            Permanent golem = addPermanent(player1, indestructibleCreature());

            boolean result = prs.tryDestroyPermanent(gd, golem);

            assertThat(result).isFalse();
            harness.assertOnBattlefield(player1, "Indestructible Golem");
            harness.assertNotInGraveyard(player1, "Indestructible Golem");
        }

        @Test
        @DisplayName("Indestructible is logged when destruction is prevented")
        void indestructibleIsLogged() {
            Permanent golem = addPermanent(player1, indestructibleCreature());

            prs.tryDestroyPermanent(gd, golem);

            assertThat(gd.gameLog)
                    .anyMatch(log -> log.contains("Indestructible Golem") && log.contains("indestructible"));
        }

        @Test
        @DisplayName("Permanent with regeneration shield survives destruction")
        void regenerationPreventsDestruction() {
            Permanent bears = addPermanent(player1, new GrizzlyBears());
            bears.setRegenerationShield(1);

            boolean result = prs.tryDestroyPermanent(gd, bears);

            assertThat(result).isFalse();
            harness.assertOnBattlefield(player1, "Grizzly Bears");
        }

        @Test
        @DisplayName("cannotBeRegenerated flag bypasses regeneration")
        void cannotBeRegeneratedBypassesRegeneration() {
            Permanent bears = addPermanent(player1, new GrizzlyBears());
            bears.setRegenerationShield(1);

            boolean result = prs.tryDestroyPermanent(gd, bears, true);

            assertThat(result).isTrue();
            harness.assertNotOnBattlefield(player1, "Grizzly Bears");
            harness.assertInGraveyard(player1, "Grizzly Bears");
        }

        @Test
        @DisplayName("cannotBeRegenerated still respects indestructible")
        void cannotBeRegeneratedStillRespectsIndestructible() {
            Permanent golem = addPermanent(player1, indestructibleCreature());

            boolean result = prs.tryDestroyPermanent(gd, golem, true);

            assertThat(result).isFalse();
            harness.assertOnBattlefield(player1, "Indestructible Golem");
        }
    }

    // =========================================================================
    // removeCardFromGraveyardById
    // =========================================================================

    @Nested
    @DisplayName("removeCardFromGraveyardById")
    class RemoveCardFromGraveyardById {

        @Test
        @DisplayName("Removes card from graveyard by its ID")
        void removesCardFromGraveyard() {
            Card bears = new GrizzlyBears();
            gd.playerGraveyards.get(player1.getId()).add(bears);

            prs.removeCardFromGraveyardById(gd, bears.getId());

            harness.assertNotInGraveyard(player1, "Grizzly Bears");
        }

        @Test
        @DisplayName("Cleans up creature death tracking set")
        void cleansUpDeathTracking() {
            Card bears = new GrizzlyBears();
            gd.playerGraveyards.get(player1.getId()).add(bears);
            gd.creatureCardsPutIntoGraveyardFromBattlefieldThisTurn
                    .computeIfAbsent(player1.getId(), k -> new java.util.HashSet<>())
                    .add(bears.getId());

            prs.removeCardFromGraveyardById(gd, bears.getId());

            assertThat(gd.creatureCardsPutIntoGraveyardFromBattlefieldThisTurn.get(player1.getId()))
                    .doesNotContain(bears.getId());
        }

        @Test
        @DisplayName("No-op when card is not in any graveyard")
        void noOpWhenNotInGraveyard() {
            UUID fakeId = UUID.randomUUID();
            int sizeP1 = gd.playerGraveyards.get(player1.getId()).size();
            int sizeP2 = gd.playerGraveyards.get(player2.getId()).size();

            prs.removeCardFromGraveyardById(gd, fakeId);

            assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(sizeP1);
            assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(sizeP2);
        }
    }

    // =========================================================================
    // redirectPlayerDamageToEnchantedCreature
    // =========================================================================

    @Nested
    @DisplayName("redirectPlayerDamageToEnchantedCreature")
    class RedirectPlayerDamageToEnchantedCreature {

        @Test
        @DisplayName("Returns damage unchanged when no redirect aura is present")
        void noRedirectWhenNoAura() {
            int result = prs.redirectPlayerDamageToEnchantedCreature(gd, player1.getId(), 5, "Lightning Bolt");

            assertThat(result).isEqualTo(5);
        }

        @Test
        @DisplayName("Returns zero damage when redirect is <= 0")
        void returnsZeroOrNegativeDamageAsIs() {
            int result = prs.redirectPlayerDamageToEnchantedCreature(gd, player1.getId(), 0, "Source");

            assertThat(result).isEqualTo(0);
        }

        @Test
        @DisplayName("Redirects damage to enchanted creature and returns 0")
        void redirectsDamageToEnchantedCreature() {
            // Set up Pariah enchanting a creature on player1's battlefield
            Permanent creature = addPermanent(player1, new SerraAngel());
            Permanent pariah = new Permanent(new Pariah());
            pariah.setAttachedTo(creature.getId());
            gd.playerBattlefields.get(player1.getId()).add(pariah);

            int result = prs.redirectPlayerDamageToEnchantedCreature(gd, player1.getId(), 3, "Lightning Bolt");

            assertThat(result).isEqualTo(0);
            assertThat(gd.gameLog)
                    .anyMatch(log -> log.contains("Serra Angel") && log.contains("absorbs") && log.contains("redirected"));
        }

        @Test
        @DisplayName("Enchanted creature is destroyed when redirected damage meets toughness")
        void enchantedCreatureDestroyedByLethalDamage() {
            // Serra Angel has 4 toughness
            Permanent creature = addPermanent(player1, new SerraAngel());
            Permanent pariah = new Permanent(new Pariah());
            pariah.setAttachedTo(creature.getId());
            gd.playerBattlefields.get(player1.getId()).add(pariah);

            prs.redirectPlayerDamageToEnchantedCreature(gd, player1.getId(), 4, "Fireball");

            harness.assertNotOnBattlefield(player1, "Serra Angel");
            harness.assertInGraveyard(player1, "Serra Angel");
            assertThat(gd.gameLog)
                    .anyMatch(log -> log.contains("Serra Angel") && log.contains("destroyed"));
        }

        @Test
        @DisplayName("Indestructible enchanted creature survives lethal redirected damage")
        void indestructibleSurvivesLethalRedirect() {
            Permanent creature = addPermanent(player1, indestructibleCreature());
            Permanent pariah = new Permanent(new Pariah());
            pariah.setAttachedTo(creature.getId());
            gd.playerBattlefields.get(player1.getId()).add(pariah);

            int result = prs.redirectPlayerDamageToEnchantedCreature(gd, player1.getId(), 5, "Fireball");

            assertThat(result).isEqualTo(0);
            harness.assertOnBattlefield(player1, "Indestructible Golem");
            assertThat(gd.gameLog)
                    .anyMatch(log -> log.contains("indestructible") && log.contains("survives"));
        }
    }
}
