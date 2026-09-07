package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({NyleaKeenEyed.class, GrizzlyBears.class, Divination.class, Forest.class})
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
            harness.addToBattlefield(player1, new GrizzlyBears());
        }

        assertThat(gqs.isCreature(gd, nylea)).isTrue();
    }

    @Test
    @DisplayName("Creature spells you cast cost one generic mana less")
    void creatureSpellsCostOneLess() {
        harness.addToBattlefield(player1, new NyleaKeenEyed());
        Card spell = new GrizzlyBears();
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getId().equals(spell.getId()));
    }

    @Test
    @DisplayName("Noncreature spells do not receive Nylea's reduction")
    void noncreatureSpellsAreNotReduced() {
        harness.addToBattlefield(player1, new NyleaKeenEyed());
        harness.setHand(player1, List.of(new Divination()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A revealed creature card is put into hand")
    void revealedCreatureGoesToHand() {
        harness.addToBattlefield(player1, new NyleaKeenEyed());
        Card topCreature = new GrizzlyBears();
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

    private void activateAbility() {
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
    }

    private void setDeck(Card topCard) {
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).add(topCard);
    }
}
