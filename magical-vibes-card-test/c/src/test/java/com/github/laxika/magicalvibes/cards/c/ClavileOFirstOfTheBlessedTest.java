package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.Standardize;
import com.github.laxika.magicalvibes.cards.v.VampireOfTheDireMoon;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ClavileOFirstOfTheBlessed.class, VampireOfTheDireMoon.class, Shock.class, GrizzlyBears.class, Standardize.class})
class ClavileOFirstOfTheBlessedTest extends BaseCardTest {

    @Test
    @DisplayName("An attacking Vampire becomes a Demon and creates a Vampire Demon when it dies")
    void attackingVampireGetsDeathTrigger() {
        addCreatureReady(player1, new ClavileOFirstOfTheBlessed());
        Permanent vampire = addCreatureReady(player1, new VampireOfTheDireMoon());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        declareAttackers(List.of(1));

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(vampire.getId());
        harness.handlePermanentChosen(player1, vampire.getId());
        resolveAllTriggers();

        assertThat(vampire.getGrantedSubtypes()).contains(CardSubtype.DEMON);

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, vampire.getId());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));
        Permanent token = findPermanent(player1, "Vampire Demon");
        assertThat(token.getEffectivePower()).isEqualTo(4);
        assertThat(token.getEffectiveToughness()).isEqualTo(3);
        assertThat(token.getCard().getSubtypes())
                .containsExactlyInAnyOrder(CardSubtype.VAMPIRE, CardSubtype.DEMON);
    }

    @Test
    @DisplayName("A non-Vampire attacker cannot be chosen")
    void nonVampireAttackerIsNotTargetable() {
        addCreatureReady(player1, new ClavileOFirstOfTheBlessed());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(1));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(bears.getGrantedSubtypes()).doesNotContain(CardSubtype.DEMON);
    }

    @Test
    void deathTokenEntersTappedWithFlyingAndBothColors() {
        Permanent clavile = addCreatureReady(player1, new ClavileOFirstOfTheBlessed());
        harness.setLibrary(player1, List.of(new ClavileOFirstOfTheBlessed()));
        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, clavile.getId());
        resolveAllTriggers();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, clavile.getId());
        resolveAllTriggers();

        Permanent token = findPermanent(player1, "Vampire Demon");
        assertThat(token.isTapped()).isTrue();
        assertThat(token.hasKeyword(Keyword.FLYING)).isTrue();
        assertThat(token.getEffectiveColors()).containsExactlyInAnyOrder(CardColor.WHITE, CardColor.BLACK);
        assertThat(countPermanents(player1, "Vampire Demon")).isEqualTo(1);
        harness.assertInHand(player1, "Clavileño, First of the Blessed");
    }

    @Test
    void onlyAttackingNonDemonVampiresCanBeChosen() {
        Permanent clavile = addCreatureReady(player1, new ClavileOFirstOfTheBlessed());
        Permanent attacker = addCreatureReady(player1, new VampireOfTheDireMoon());
        addCreatureReady(player1, new VampireOfTheDireMoon());
        addCreatureReady(player2, new VampireOfTheDireMoon());

        declareAttackers(List.of(1));
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(attacker.getId());
        harness.handlePermanentChosen(player1, attacker.getId());
        resolveAllTriggers();

        attacker.untap();
        declareAttackers(List.of(0, 1));
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(clavile.getId());
        harness.handlePermanentChosen(player1, clavile.getId());
        resolveAllTriggers();
    }

    @Test
    void gainingDeathAbilityAgainAfterLosingDemonTypeCreatesTwoTokensAndDrawsTwice() {
        addCreatureReady(player1, new ClavileOFirstOfTheBlessed());
        Permanent vampire = addCreatureReady(player1, new VampireOfTheDireMoon());
        declareAttackers(List.of(1));
        harness.handlePermanentChosen(player1, vampire.getId());
        resolveAllTriggers();

        harness.castFromHand(player1, new Standardize(), "{U}{U}");
        harness.passBothPriorities();
        harness.handleListChoice(player1, CardSubtype.VAMPIRE.name());
        resolveAllTriggers();

        vampire.untap();
        declareAttackers(List.of(1));
        harness.handlePermanentChosen(player1, vampire.getId());
        resolveAllTriggers();

        harness.setLibrary(player1, List.of(new ClavileOFirstOfTheBlessed(), new ClavileOFirstOfTheBlessed()));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, vampire.getId());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(countPermanents(player1, "Vampire Demon")).isEqualTo(2);
    }
}
