package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SwordOfHearthAndHome.class, GrizzlyBears.class, Forest.class})
class SwordOfHearthAndHomeTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +2/+2 and protection from green and white")
    void equippedCreatureGetsBoostAndProtection() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent sword = addSwordReady();
        sword.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        assertThat(gqs.hasProtectionFrom(gd, creature, CardColor.GREEN)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, creature, CardColor.WHITE)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, creature, CardColor.RED)).isFalse();
    }

    @Test
    @DisplayName("Combat damage flickers an owned creature and puts a basic land onto the battlefield")
    void combatDamageFlickersCreatureAndSearchesBasicLand() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent sword = addSwordReady();
        sword.setAttachedTo(creature.getId());
        harness.setLibrary(player1, List.of(new Forest(), new GrizzlyBears()));

        declareAttackers(List.of(0));
        resolveCombat();

        PendingInteraction.PermanentChoice targetChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(targetChoice.validIds()).contains(creature.getId(), player1.getId())
                .doesNotContain(sword.getId());
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).extracting(Card::getName).containsExactly("Forest");
        harness.handleCardChosen(player1, 0);

        assertThat(findPermanents(player1, "Grizzly Bears")).hasSize(1)
                .doesNotContain(creature);
        Permanent forest = findPermanent(player1, "Forest");
        assertThat(forest.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The trigger can resolve without choosing a creature")
    void combatDamageCanSkipCreatureFlicker() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent sword = addSwordReady();
        sword.setAttachedTo(creature.getId());
        harness.setLibrary(player1, List.of(new Forest()));

        declareAttackers(List.of(0));
        resolveCombat();
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);
        assertThat(findPermanent(player1, "Forest").isTapped()).isFalse();
    }

    private Permanent addSwordReady() {
        Permanent sword = new Permanent(new SwordOfHearthAndHome());
        sword.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(sword);
        return sword;
    }
}
