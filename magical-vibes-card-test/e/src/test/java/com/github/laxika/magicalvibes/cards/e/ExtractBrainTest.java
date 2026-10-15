package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.cards.r.RuleOfLaw;
import com.github.laxika.magicalvibes.cards.t.ThaliaGuardianOfThraben;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ExtractBrain.class, Divination.class, Forest.class, GrizzlyBears.class, Naturalize.class,
        RuleOfLaw.class, ThaliaGuardianOfThraben.class})
class ExtractBrainTest extends BaseCardTest {

    @Test
    @DisplayName("The target chooses X cards and the caster may cast a revealed spell for free")
    void targetChoosesCardsAndCasterCastsRevealedSpell() {
        Card land = new Forest();
        Card spell = new Divination();
        Card creature = new GrizzlyBears();
        harness.setHand(player2, new ArrayList<>(List.of(land, spell, creature)));
        harness.setHand(player1, List.of(new ExtractBrain()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, 2, player2.getId());

        PendingInteraction.RevealCardsDiscardChoice reveal =
                gd.interaction.activeInteraction(PendingInteraction.RevealCardsDiscardChoice.class);
        assertThat(reveal).isNotNull();
        assertThat(reveal.revealStage()).isTrue();
        assertThat(reveal.decidingPlayerId()).isEqualTo(player2.getId());

        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 1);

        PendingInteraction.RevealCardsDiscardChoice castChoice =
                gd.interaction.activeInteraction(PendingInteraction.RevealCardsDiscardChoice.class);
        assertThat(castChoice).isNotNull();
        assertThat(castChoice.decidingPlayerId()).isEqualTo(player1.getId());
        assertThat(castChoice.validIndices()).containsExactly(1);

        harness.handleCardChosen(player1, 1);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.stack).singleElement().extracting(entry -> entry.getCard().getId())
                .isEqualTo(spell.getId());
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(land, creature);
    }

    @Test
    @DisplayName("Lands among the chosen cards are not offered as spells")
    void landsAreNotOffered() {
        Card land = new Forest();
        harness.setHand(player2, new ArrayList<>(List.of(land)));
        harness.setHand(player1, List.of(new ExtractBrain()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, 1, player2.getId());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(land);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Extract Brain cannot target its controller")
    void cannotTargetController() {
        harness.setHand(player1, List.of(new ExtractBrain()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 1, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Choosing zero cards leaves the opponent's hand untouched")
    void zeroXDoesNotOfferACast() {
        Card spell = new Divination();
        harness.setHand(player2, List.of(spell));
        harness.setHand(player1, List.of(new ExtractBrain()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(spell);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An empty opposing hand completes without a cast choice")
    void emptyHandDoesNotOfferACast() {
        harness.setHand(player2, List.of());
        harness.setHand(player1, List.of(new ExtractBrain()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, 3, player2.getId());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("When X exceeds hand size, the caster can decline and leave all cards in hand")
    void fewerCardsThanXAndDecliningCast() {
        Card spell = new Divination();
        Card land = new Forest();
        harness.setHand(player2, List.of(spell, land));
        harness.setHand(player1, List.of(new ExtractBrain()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, 3, player2.getId());

        PendingInteraction.RevealCardsDiscardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.RevealCardsDiscardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.revealStage()).isFalse();
        assertThat(choice.decidingPlayerId()).isEqualTo(player1.getId());
        assertThat(choice.validIndices()).containsExactly(0);
        harness.handleCardChosen(player1, 0);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(spell, land);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A chosen spell that has no legal targets stays in the opponent's hand")
    void uncastableSpellStaysInHand() {
        Card spell = new Naturalize();
        harness.setHand(player2, List.of(spell));
        harness.setHand(player1, List.of(new ExtractBrain()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, 1, player2.getId());

        if (gd.interaction.activeInteraction() instanceof PendingInteraction.RevealCardsDiscardChoice) {
            harness.handleCardChosen(player1, 0);
        }
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice) {
            harness.handleMayAbilityChosen(player1, true);
        }

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(spell);
        assertThat(gd.playerGraveyards.get(player2.getId())).doesNotContain(spell);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Rule of Law prevents casting a second spell through Extract Brain")
    void freeCastRespectsSpellLimit() {
        Card spell = new Divination();
        harness.addToBattlefield(player2, new RuleOfLaw());
        harness.setHand(player2, List.of(spell));
        harness.setHand(player1, List.of(new ExtractBrain()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, 1, player2.getId());

        if (gd.interaction.activeInteraction() instanceof PendingInteraction.RevealCardsDiscardChoice) {
            harness.handleCardChosen(player1, 0);
        }
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice) {
            harness.handleMayAbilityChosen(player1, true);
        }

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(spell);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Casting without paying mana cost still requires Thalia's spell tax")
    void freeCastRequiresManaForCostIncrease() {
        Card spell = new Divination();
        harness.addToBattlefield(player2, new ThaliaGuardianOfThraben());
        harness.setHand(player2, List.of(spell));
        harness.setHand(player1, List.of(new ExtractBrain()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, 1, player2.getId());

        if (gd.interaction.activeInteraction() instanceof PendingInteraction.RevealCardsDiscardChoice) {
            harness.handleCardChosen(player1, 0);
        }
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice) {
            harness.handleMayAbilityChosen(player1, true);
        }

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(spell);
        assertThat(gd.stack).isEmpty();
    }
}
