package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MishraArtificerProdigy.class, Spellbook.class, GrizzlyBears.class})
class MishraArtificerProdigyTest extends BaseCardTest {

    @Test
    @DisplayName("Casting an artifact offers the same-name battlefield search")
    void artifactCastOffersSameNameSearch() {
        harness.addToBattlefield(player1, new MishraArtificerProdigy());
        harness.setHand(player1, List.of(new Spellbook(), new Spellbook()));

        harness.castArtifact(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Spellbook");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Declining the search leaves the matching card in hand")
    void decliningSearchLeavesCardInHand() {
        harness.addToBattlefield(player1, new MishraArtificerProdigy());
        harness.setHand(player1, List.of(new Spellbook(), new Spellbook()));

        harness.castArtifact(player1, 0);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).extracting(card -> card.getName())
                .containsExactly("Spellbook");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getName().equals("Spellbook"));
    }

    @Test
    @DisplayName("Casting a nonartifact spell does not trigger the search")
    void nonartifactCastDoesNotTrigger() {
        harness.addToBattlefield(player1, new MishraArtificerProdigy());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }
}
