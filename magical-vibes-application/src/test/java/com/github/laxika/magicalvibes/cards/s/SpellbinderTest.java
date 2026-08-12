package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Fog;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SpellbinderTest extends BaseCardTest {

    @Test
    @DisplayName("ETB can imprint an instant from hand")
    void etbImprintsInstantFromHand() {
        Fog fogCard = new Fog();
        harness.setHand(player1, List.of(new Spellbinder(), fogCard));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(fogCard);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(fogCard);

        Permanent spellbinder = findPermanent(player1, "Spellbinder");
        assertThat(gd.getImprintedCard(spellbinder.getCard())).isSameAs(fogCard);
    }

    @Test
    @DisplayName("Equipped creature dealing combat damage can cast a copy of the imprinted instant")
    void equippedCreatureCombatDamageCastsCopy() {
        Spellbinder spellbinderCard = new Spellbinder();
        Fog fogCard = new Fog();
        harness.addToBattlefield(player1, spellbinderCard);
        Permanent spellbinder = findPermanent(player1, "Spellbinder");
        gd.setImprintedCard(spellbinderCard, fogCard);
        gd.exiledCards.add(new ExiledCardEntry(fogCard, player1.getId(), spellbinder.getId()));

        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        spellbinder.setAttachedTo(creature.getId());
        creature.setAttacking(true);

        resolveCombat();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.stack).anyMatch(entry -> entry.isCopy());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(fogCard);
    }
}
