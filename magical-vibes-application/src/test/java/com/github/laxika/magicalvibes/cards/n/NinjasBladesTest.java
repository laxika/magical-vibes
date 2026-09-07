package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NinjasBlades.class, Forest.class, GrizzlyBears.class})
class NinjasBladesTest extends BaseCardTest {

    @Test
    @DisplayName("Job select creates and equips a Hero, making it a Ninja")
    void jobSelectCreatesAndEquipsHero() {
        castBlades();

        Permanent blades = findPermanent(player1, "Ninja's Blades");
        Permanent hero = findPermanent(player1, "Hero");

        assertThat(blades.getAttachedTo()).isEqualTo(hero.getId());
        assertThat(gqs.getEffectivePower(gd, hero)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, hero)).isEqualTo(2);
        assertThat(gqs.effectiveCreatureSubtypes(gd, hero)).contains(CardSubtype.NINJA);
    }

    @Test
    @DisplayName("Combat damage draws, discards, and makes the damaged player lose the discarded card's mana value")
    void combatDamageRummagesAndLosesLife() {
        castBlades();
        Permanent hero = findPermanent(player1, "Hero");
        hero.setSummoningSick(false);
        harness.setHand(player1, new ArrayList<>(List.of(new GrizzlyBears())));
        harness.setLibrary(player1, List.of(new Forest()));

        int heroIndex = gd.playerBattlefields.get(player1.getId()).indexOf(hero);
        declareAttackers(List.of(heroIndex));
        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Forest");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    private void castBlades() {
        harness.setHand(player1, List.of(new NinjasBlades()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
