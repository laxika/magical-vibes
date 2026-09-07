package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.d.DarkRitual;
import com.github.laxika.magicalvibes.cards.l.LightningStrike;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BaralAndKariZev.class, DarkRitual.class, LightningStrike.class})
class BaralAndKariZevTest extends BaseCardTest {

    @Test
    @DisplayName("The first instant or sorcery offers a cheaper spell sharing its card type")
    void firstInstantOrSorceryOffersCheaperSharedTypeSpell() {
        setupBaral();
        LightningStrike lightningStrike = new LightningStrike();
        DarkRitual darkRitual = new DarkRitual();
        harness.setHand(player1, List.of(lightningStrike, darkRitual));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);
        resolveStack();

        assertThat(gd.playerGraveyards.get(player1.getId())).anyMatch(card -> card.getId().equals(darkRitual.getId()));
    }

    @Test
    @DisplayName("Declining every eligible spell creates First Mate Ragavan")
    void decliningEligibleSpellCreatesRagavan() {
        setupBaral();
        LightningStrike lightningStrike = new LightningStrike();
        harness.setHand(player1, List.of(lightningStrike, new DarkRitual()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        resolveStack();

        Permanent ragavan = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("First Mate Ragavan"))
                .findFirst()
                .orElseThrow();
        assertThat(ragavan.getEffectivePower()).isEqualTo(2);
        assertThat(ragavan.getEffectiveToughness()).isEqualTo(1);
        assertThat(ragavan.hasKeyword(Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("No qualifying spell creates First Mate Ragavan without a choice")
    void noQualifyingSpellCreatesRagavan() {
        setupBaral();
        harness.setHand(player1, List.of(new LightningStrike(), new LightningStrike()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(
                permanent -> permanent.getCard().getName().equals("First Mate Ragavan"));
    }

    @Test
    @DisplayName("Only the first instant or sorcery each turn triggers")
    void onlyFirstInstantOrSorceryTriggers() {
        setupBaral();
        harness.setHand(player1, List.of(new LightningStrike()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castInstant(player1, 0, player2.getId());
        resolveStack();

        harness.setHand(player1, List.of(new LightningStrike()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    private void setupBaral() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player1, new BaralAndKariZev());
    }

    private void resolveStack() {
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }
    }
}
