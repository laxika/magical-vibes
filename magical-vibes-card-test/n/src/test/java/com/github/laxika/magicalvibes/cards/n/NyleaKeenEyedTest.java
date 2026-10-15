package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.MycosynthLattice;
import com.github.laxika.magicalvibes.cards.t.ThirstForMeaning;
import com.github.laxika.magicalvibes.model.Card;
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

@CardUsed({NyleaKeenEyed.class, NessianHornbeetle.class, ThirstForMeaning.class, Forest.class, MycosynthLattice.class})
class NyleaKeenEyedTest extends BaseCardTest {

    @Test
    @DisplayName("Nylea is not a creature below five devotion to green")
    void isNotCreatureBelowDevotionThreshold() {
        Permanent nylea = harness.addToBattlefieldAndReturn(player1, new NyleaKeenEyed());

        assertThat(gqs.isCreature(gd, nylea)).isFalse();
        assertThat(gqs.isEnchantment(gd, nylea)).isTrue();
    }

    @Test
    @DisplayName("Nylea becomes a creature at five devotion to green")
    void becomesCreatureAtDevotionThreshold() {
        Permanent nylea = harness.addToBattlefieldAndReturn(player1, new NyleaKeenEyed());
        for (int i = 0; i < 4; i++) {
            harness.addToBattlefield(player1, new NessianHornbeetle());
        }

        assertThat(gqs.isCreature(gd, nylea)).isTrue();
    }

    @Test
    @DisplayName("Creature spells you cast cost one generic mana less")
    void creatureSpellsCostOneLess() {
        harness.addToBattlefield(player1, new NyleaKeenEyed());
        Card spell = new NessianHornbeetle();
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getId().equals(spell.getId()));
    }

    @Test
    @DisplayName("Noncreature spells do not receive Nylea's reduction")
    void noncreatureSpellsAreNotReduced() {
        harness.addToBattlefield(player1, new NyleaKeenEyed());
        harness.setHand(player1, List.of(new ThirstForMeaning()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A revealed creature card is put into hand")
    void revealedCreatureGoesToHand() {
        harness.addToBattlefield(player1, new NyleaKeenEyed());
        Card topCreature = new NessianHornbeetle();
        setDeck(topCreature);
        int handBefore = gd.playerHands.get(player1.getId()).size();
        activateAbility();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).anyMatch(card -> card.getId().equals(topCreature.getId()));
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(gd.playerDecks.get(player1.getId())).noneMatch(card -> card.getId().equals(topCreature.getId()));
    }

    @Test
    @DisplayName("A revealed noncreature card may be put into the graveyard")
    void revealedNoncreatureMayGoToGraveyard() {
        harness.addToBattlefield(player1, new NyleaKeenEyed());
        Card topLand = new Forest();
        setDeck(topLand);
        activateAbility();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).anyMatch(card -> card.getId().equals(topLand.getId()));
        assertThat(gd.playerDecks.get(player1.getId())).noneMatch(card -> card.getId().equals(topLand.getId()));
    }

    @Test
    @DisplayName("Declining to graveyard a revealed noncreature leaves it on top")
    void decliningGraveyardLeavesRevealedCardOnTop() {
        harness.addToBattlefield(player1, new NyleaKeenEyed());
        Card topLand = new Forest();
        setDeck(topLand);
        activateAbility();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId()).getFirst().getId()).isEqualTo(topLand.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).noneMatch(card -> card.getId().equals(topLand.getId()));
    }

    @Test
    @DisplayName("Falling below five devotion makes Nylea stop being a creature")
    void stopsBeingCreatureWhenDevotionFalls() {
        Permanent nylea = harness.addToBattlefieldAndReturn(player1, new NyleaKeenEyed());
        for (int i = 0; i < 4; i++) {
            harness.addToBattlefield(player1, new NessianHornbeetle());
        }
        assertThat(gqs.isCreature(gd, nylea)).isTrue();

        gd.playerBattlefields.get(player1.getId()).removeLast();

        assertThat(gqs.isCreature(gd, nylea)).isFalse();
        assertThat(gqs.isEnchantment(gd, nylea)).isTrue();
    }

    @Test
    @DisplayName("Opposing green permanents and Forests do not provide devotion")
    void opponentsPermanentsAndLandsDoNotProvideDevotion() {
        Permanent nylea = harness.addToBattlefieldAndReturn(player1, new NyleaKeenEyed());
        for (int i = 0; i < 4; i++) {
            harness.addToBattlefield(player2, new NessianHornbeetle());
            harness.addToBattlefield(player1, new Forest());
        }

        assertThat(gqs.isCreature(gd, nylea)).isFalse();
    }

    @Test
    @DisplayName("An opposing Nylea does not reduce your creature spell costs")
    void opponentsCreatureSpellsAreNotReduced() {
        harness.addToBattlefield(player2, new NyleaKeenEyed());
        harness.setHand(player1, List.of(new NessianHornbeetle()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The cost reduction cannot pay a creature spell's colored mana")
    void reductionDoesNotRemoveColoredCost() {
        harness.addToBattlefield(player1, new NyleaKeenEyed());
        harness.setHand(player1, List.of(new NessianHornbeetle()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Revealing Nylea puts it in hand even without five devotion")
    void revealedGodIsCreatureCardOutsideBattlefield() {
        harness.addToBattlefield(player1, new NyleaKeenEyed());
        Card topGod = new NyleaKeenEyed();
        setDeck(topGod);

        activateAbility();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).contains(topGod);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("An empty library produces no reveal choice or draw")
    void emptyLibraryDoesNothing() {
        harness.addToBattlefield(player1, new NyleaKeenEyed());
        harness.setLibrary(player1, List.of());
        int handBefore = gd.playerHands.get(player1.getId()).size();

        activateAbility();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @CardUsed({NyleaKeenEyed.class, MycosynthLattice.class})
    @DisplayName("Losing creature status preserves an artifact type granted by an earlier Lattice")
    void devotionAbilityPreservesOtherCardTypes() {
        harness.enterBattlefieldAndReturn(player1, new MycosynthLattice());
        Permanent nylea = harness.enterBattlefieldAndReturn(player1, new NyleaKeenEyed());

        assertThat(gqs.isCreature(gd, nylea)).isFalse();
        assertThat(gqs.isEnchantment(gd, nylea)).isTrue();
        assertThat(gqs.isArtifact(gd, nylea)).isTrue();
    }

    @Test
    @DisplayName("Indestructible Nylea survives lethal damage at five devotion")
    void survivesLethalDamage() {
        Permanent nylea = harness.addToBattlefieldAndReturn(player1, new NyleaKeenEyed());
        for (int i = 0; i < 4; i++) {
            harness.addToBattlefield(player1, new NessianHornbeetle());
        }
        nylea.setMarkedDamage(6);

        harness.runStateBasedActions();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(nylea);
    }

    private void activateAbility() {
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
    }

    private void setDeck(Card topCard) {
        harness.setLibrary(player1, List.of(topCard));
    }
}
