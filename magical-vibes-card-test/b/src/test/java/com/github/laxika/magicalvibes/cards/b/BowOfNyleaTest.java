package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.e.EnsoulArtifact;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.n.NessianCourser;
import com.github.laxika.magicalvibes.cards.p.PrescientChimera;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BowOfNylea.class, Forest.class, NessianCourser.class, PrescientChimera.class, EnsoulArtifact.class})
class BowOfNyleaTest extends BaseCardTest {

    private Permanent addBow() {
        Permanent bow = harness.addToBattlefieldAndReturn(player1, new BowOfNylea());
        harness.addMana(player1, ManaColor.GREEN, 2);
        return bow;
    }

    private int bowIndex(Permanent bow) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(bow);
    }

    @Test
    @DisplayName("Grants deathtouch to your attacking creatures only")
    void grantsDeathtouchToAttackingCreaturesYouControl() {
        addBow();
        Permanent attacker = addCreatureReady(player1, new NessianCourser());
        attacker.setAttacking(true);
        Permanent nonAttacker = addCreatureReady(player1, new NessianCourser());
        Permanent opponentAttacker = addCreatureReady(player2, new NessianCourser());
        opponentAttacker.setAttacking(true);

        assertThat(gqs.hasKeyword(gd, attacker, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.hasKeyword(gd, nonAttacker, Keyword.DEATHTOUCH)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponentAttacker, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    @DisplayName("An animated attacking Bow grants itself deathtouch")
    void animatedBowHasDeathtouchWhileAttacking() {
        Permanent bow = addBow();
        harness.setHand(player1, List.of(new EnsoulArtifact()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castEnchantment(player1, 0, bow.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bow, Keyword.DEATHTOUCH)).isFalse();
        bow.setAttacking(true);
        assertThat(gqs.hasKeyword(gd, bow, Keyword.DEATHTOUCH)).isTrue();
        bow.setAttacking(false);
        assertThat(gqs.hasKeyword(gd, bow, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    @DisplayName("Puts a +1/+1 counter on target creature")
    void putsCounterOnTargetCreature() {
        Permanent bow = addBow();
        Permanent bears = addCreatureReady(player2, new NessianCourser());

        harness.activateAbility(player1, bowIndex(bow), 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Deals 2 damage only to target creature with flying")
    void damagesTargetCreatureWithFlying() {
        Permanent bow = addBow();
        Permanent flyer = addCreatureReady(player2, new PrescientChimera());

        harness.activateAbility(player1, bowIndex(bow), 1, null, flyer.getId());
        harness.passBothPriorities();

        assertThat(flyer.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Rejects a nonflying creature for the damage mode")
    void rejectsNonflyingDamageTarget() {
        Permanent bow = addBow();
        Permanent bears = addCreatureReady(player2, new NessianCourser());

        assertThatThrownBy(() -> harness.activateAbility(player1, bowIndex(bow), 1, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Gains 3 life")
    void gainsLife() {
        Permanent bow = addBow();
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.activateAbility(player1, bowIndex(bow), 2, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 3);
    }

    @Test
    @DisplayName("Puts up to four targeted graveyard cards on the bottom in the chosen order")
    void putsGraveyardCardsOnBottomInChosenOrder() {
        Permanent bow = addBow();
        Card first = new Forest();
        Card second = new NessianCourser();
        harness.setGraveyard(player1, List.of(first, second));
        Card libraryCard = new PrescientChimera();
        harness.setLibrary(player1, List.of(libraryCard));

        harness.activateAbilityWithGraveyardTargets(player1, bowIndex(bow), 3,
                List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(1, 0)));

        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getId)
                .containsExactly(libraryCard.getId(), second.getId(), first.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Cannot target a card in an opponent's graveyard")
    void rejectsOpponentGraveyardTarget() {
        Permanent bow = addBow();
        Card card = new Forest();
        harness.setGraveyard(player2, List.of(card));

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, bowIndex(bow), 3, List.of(card.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The graveyard mode can be activated with zero targets")
    void allowsZeroGraveyardTargets() {
        Permanent bow = addBow();
        Card libraryCard = new Forest();
        harness.setGraveyard(player1, List.of());
        harness.setLibrary(player1, List.of(libraryCard));

        harness.activateAbilityWithGraveyardTargets(player1, bowIndex(bow), 3, List.of());
        harness.passBothPriorities();

        assertThat(bow.isTapped()).isTrue();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The graveyard mode rejects more than four targets")
    void rejectsFiveGraveyardTargets() {
        Permanent bow = addBow();
        List<Card> cards = List.of(new Forest(), new NessianCourser(), new PrescientChimera(),
                new Forest(), new NessianCourser());
        harness.setGraveyard(player1, cards);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(player1, bowIndex(bow), 3,
                cards.stream().map(Card::getId).toList()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The graveyard mode cannot target the same card twice")
    void rejectsDuplicateGraveyardTargets() {
        Permanent bow = addBow();
        Card card = new Forest();
        harness.setGraveyard(player1, List.of(card));

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(player1, bowIndex(bow), 3,
                List.of(card.getId(), card.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Activating a mode taps the Bow and prevents another activation")
    void tapCostPreventsAnotherMode() {
        Permanent bow = addBow();
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, bowIndex(bow), 2, null, null);

        assertThat(bow.isTapped()).isTrue();
        assertThatThrownBy(() -> harness.activateAbility(player1, bowIndex(bow), 2, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.passBothPriorities();
        harness.assertLife(player1, 23);
    }
}
