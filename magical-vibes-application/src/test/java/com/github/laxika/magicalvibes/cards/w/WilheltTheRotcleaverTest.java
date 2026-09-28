package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.d.DiregrafGhoul;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
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
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WilheltTheRotcleaver.class, DiregrafGhoul.class, Shock.class})
class WilheltTheRotcleaverTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a decayed Zombie when another nontoken Zombie without decayed dies")
    void createsDecayedZombieWhenZombieDies() {
        harness.addToBattlefield(player1, new WilheltTheRotcleaver());
        Permanent ghoul = harness.addToBattlefieldAndReturn(player1, new DiregrafGhoul());

        destroyWithShock(ghoul.getId());
        harness.passBothPriorities();

        Permanent token = findPermanent(player1, "Zombie");
        assertThat(token.getCard().isToken()).isTrue();
        assertThat(token.getCard().getPower()).isEqualTo(2);
        assertThat(token.getCard().getToughness()).isEqualTo(2);
        assertThat(token.getCard().getKeywords()).contains(Keyword.DECAYED);
    }

    @Test
    @DisplayName("Does not create a token when the dying Zombie has decayed")
    void doesNotCreateTokenForDecayedZombie() {
        harness.addToBattlefield(player1, new WilheltTheRotcleaver());
        DiregrafGhoul card = new DiregrafGhoul();
        card.setKeywords(Set.of(Keyword.DECAYED));
        Permanent ghoul = harness.addToBattlefieldAndReturn(player1, card);

        destroyWithShock(ghoul.getId());

        assertThat(countPermanents(player1, "Zombie")).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("At your end step, sacrificing a Zombie draws a card")
    void sacrificesZombieToDrawAtEndStep() {
        harness.addToBattlefield(player1, new WilheltTheRotcleaver());
        Permanent ghoul = harness.addToBattlefieldAndReturn(player1, new DiregrafGhoul());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new DiregrafGhoul()));

        advanceToEndStep();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, ghoul.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(ghoul.getId()));
        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Diregraf Ghoul");
    }

    private void destroyWithShock(UUID targetId) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, targetId);
        harness.passBothPriorities();
    }

    private void advanceToEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
